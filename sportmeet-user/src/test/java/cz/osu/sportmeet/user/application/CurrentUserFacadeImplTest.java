package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.common.application.PageResult;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.application.dto.UpdateUserInput;
import cz.osu.sportmeet.user.application.dto.UserDetail;
import cz.osu.sportmeet.user.application.dto.UserHistoryEntry;
import cz.osu.sportmeet.user.application.exception.ConcurrentUserModificationException;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.UserNotFoundException;
import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.domain.event.UserAttributeChanged;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.entity.UserHistory;
import cz.osu.sportmeet.user.persistence.repository.UserHistoryRepository;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import cz.osu.sportmeet.user.support.TestUsers;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies the profile use cases without Spring, web or database.
 * History persistence itself is covered by {@link UserHistoryRecorderTest} and HTTP integration tests;
 * here it is enough to verify that the user is saved with the expected pending events.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class CurrentUserFacadeImplTest
{
    private static final long USER_ID = 7L;

    private final UserRepository users = mock(UserRepository.class);
    private final UserHistoryRepository history = mock(UserHistoryRepository.class);
    private final CurrentUserFacadeImpl facade = new CurrentUserFacadeImpl(users, history, new UserValidator());

    private User user;

    @BeforeEach
    void setUp()
    {
        user = TestUsers.persisted(USER_ID, "runner", "Runner", "runner@example.com");
        when(users.findByLogin("runner")).thenReturn(Optional.of(user));
    }

    @Test
    void returnsProfileOfActiveUser()
    {
        UserDetail profile = facade.getCurrentUser("runner");

        assertThat(profile).isEqualTo(new UserDetail(USER_ID, "runner", "Runner", "runner@example.com",
                UserState.ACTIVE, TestUsers.CREATED_AT, null));
    }

    @Test
    void unknownBlockedOrInvalidUserIsNotFound()
    {
        when(users.findByLogin("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> facade.getCurrentUser("ghost")).isInstanceOf(UserNotFoundException.class);

        TestUsers.withState(user, UserState.BLOCKED);
        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput("New", null)))
                .isInstanceOf(UserNotFoundException.class);

        TestUsers.withState(user, UserState.ACTIVE);
        TestUsers.invalid(user);
        assertThatThrownBy(() -> facade.getHistory("runner", 0, 20))
                .isInstanceOf(UserNotFoundException.class);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void changesBothAttributesAndSavesOnceWithTwoEvents()
    {
        UserDetail profile = facade.updateCurrentUser("runner",
                new UpdateUserInput("  Fast Runner ", " New@Example.COM "));

        assertThat(profile.displayName()).isEqualTo("Fast Runner");
        assertThat(profile.email()).isEqualTo("new@example.com");
        assertThat(profile.updatedAt()).isNotNull();
        assertThat(user.getDomainEvents())
                .extracting(UserAttributeChanged::attribute, UserAttributeChanged::changedBy, UserAttributeChanged::changedByType)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(UserHistoryAttribute.DISPLAY_NAME, USER_ID,
                                DataInfoObjectType.USER),
                        org.assertj.core.groups.Tuple.tuple(UserHistoryAttribute.EMAIL, USER_ID,
                                DataInfoObjectType.USER));

        verify(users).saveAndFlush(user);
    }

    @Test
    void unchangedValuesAreANoOp()
    {
        UserDetail profile = facade.updateCurrentUser("runner",
                new UpdateUserInput("Runner", "RUNNER@example.com"));

        assertThat(profile.updatedAt()).isNull();
        assertThat(user.getDomainEvents()).isEmpty();
        verify(users, never()).existsByEmail(anyString());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void requiresAtLeastOneAttribute()
    {
        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput(null, null)))
                .isInstanceOf(InvalidInputException.class);
        verifyNoInteractions(users);
    }

    @Test
    void invalidValuesAreRejectedBeforeDataAccess()
    {
        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput(null, "bad")))
                .isInstanceOf(InvalidInputException.class);
        verifyNoInteractions(users);
    }

    @Test
    void emailOfAnotherUserIsRejectedWithoutChange()
    {
        when(users.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> facade.updateCurrentUser("runner",
                new UpdateUserInput("Fast Runner", "taken@example.com")))
                .isInstanceOf(DuplicateEmailException.class);
        assertThat(user.getDisplayName()).isEqualTo("Runner");
        assertThat(user.getDomainEvents()).isEmpty();
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void concurrentEmailRaceDetectedOnFlushIsReportedAsDuplicate()
    {
        DataIntegrityViolationException failure = new DataIntegrityViolationException("constraint",
                new ConstraintViolationException("duplicate", new SQLException("duplicate", "23505"), "uq_t_user_email"));
        when(users.saveAndFlush(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput(null, "new@example.com")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void optimisticLockConflictIsReportedAsConcurrentModification()
    {
        when(users.saveAndFlush(any(User.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(User.class, USER_ID));

        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput("Fast Runner", null)))
                .isInstanceOf(ConcurrentUserModificationException.class);
    }

    @Test
    void unrelatedHistoryConstraintFailureIsNotReportedAsDuplicateEmail()
    {
        DataIntegrityViolationException failure = new DataIntegrityViolationException("constraint",
                new ConstraintViolationException("foreign key", new SQLException("foreign key", "23503"),
                        "FK_T_USER_HISTORY_ACTOR"));
        when(users.saveAndFlush(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> facade.updateCurrentUser("runner", new UpdateUserInput("Fast Runner", null)))
                .isSameAs(failure);
    }

    @Test
    void historyIsQueriedNewestFirstAndMapped()
    {
        OffsetDateTime at = OffsetDateTime.parse("2026-11-11T00:00:00Z");
        UserHistory entry = UserHistory.of(USER_ID, UserHistoryAttribute.EMAIL, "a@example.com", "b@example.com",
                USER_ID, DataInfoObjectType.USER, at);
        ReflectionTestUtils.setField(entry, "id", 99L);
        when(history.findByUserId(eq(USER_ID), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(entry), call.getArgument(1), 3));

        PageResult<UserHistoryEntry> page = facade.getHistory("runner", 1, 2);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(history).findByUserId(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue()).isEqualTo(PageRequest.of(1, 2,
                Sort.by(Sort.Order.desc("changedAt"), Sort.Order.desc("id"))));
        assertThat(page.content()).containsExactly(new UserHistoryEntry(99L, UserHistoryAttribute.EMAIL,
                "a@example.com", "b@example.com", USER_ID, DataInfoObjectType.USER, at));
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    void invalidPagingIsRejectedBeforeDataAccess()
    {
        assertThatThrownBy(() -> facade.getHistory("runner", -1, 20)).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> facade.getHistory("runner", 0, 0)).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> facade.getHistory("runner", 0, 101)).isInstanceOf(InvalidInputException.class);
        verifyNoInteractions(users, history);
    }
}
