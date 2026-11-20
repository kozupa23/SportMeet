package cz.osu.sportmeet.user.application;

import com.google.common.base.Preconditions;
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
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.UserConstraintViolations;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.entity.UserHistory;
import cz.osu.sportmeet.user.persistence.repository.UserHistoryRepository;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Coordinates changes to the current user. History is written by {@link UserHistoryRecorder}
 * from domain events published when the user is saved.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public class CurrentUserFacadeImpl implements CurrentUserFacade
{
    static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final UserValidator userValidator;

    /**
     * Creates the current-user use-case implementation with its required collaborators.
     *
     * @param userRepository        repository for users
     * @param userHistoryRepository repository for user change history
     * @param userValidator         validator and normalizer for user data
     */
    public CurrentUserFacadeImpl(UserRepository userRepository, UserHistoryRepository userHistoryRepository,
                                 UserValidator userValidator)
    {
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository must not be null");
        this.userHistoryRepository = Preconditions.checkNotNull(
                userHistoryRepository, "userHistoryRepository must not be null");
        this.userValidator = Preconditions.checkNotNull(userValidator, "userValidator must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetail getCurrentUser(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return toUserDetail(findActiveUser(login));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDetail updateCurrentUser(String login, UpdateUserInput input)
    {
        Preconditions.checkNotNull(login, "login must not be null");
        Preconditions.checkNotNull(input, "input must not be null");
        InvalidInputException.check(input.displayName() != null || input.email() != null,
                "At least one user attribute must be provided.");

        String displayName = input.displayName() == null ? null : userValidator.normalizeDisplayName(input.displayName());
        String email = input.email() == null ? null : userValidator.normalizeEmail(input.email());

        User user = findActiveUser(login);
        OffsetDateTime changedAt = OffsetDateTime.now(ZoneOffset.UTC);
        Long actorId = user.getId();
        boolean changed = false;

        boolean emailChanged = email != null && !email.equals(user.getEmail());
        if (email != null && emailChanged && userRepository.existsByEmail(email))
        {
            throw new DuplicateEmailException();
        }

        if (displayName != null)
        {
            if (user.changeDisplayName(displayName, changedAt, actorId, DataInfoObjectType.USER))
            {
                changed = true;
            }
        }

        if (email != null && emailChanged)
        {
            // TODO: Once e-mail delivery exists, require the current password and confirm
            //  ownership of the new address (verification link) before the change takes effect.
            if (user.changeEmail(email, changedAt, actorId, DataInfoObjectType.USER))
            {
                changed = true;
            }
        }

        if (changed)
        {
            saveAndDetectConflicts(user);
        }

        return toUserDetail(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<UserHistoryEntry> getHistory(String login, int page, int size)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        InvalidInputException.check(page >= 0, "Page must not be negative.");
        InvalidInputException.check(size >= 1 && size <= MAX_PAGE_SIZE, "Page size must be between 1 and 100.");

        User user = findActiveUser(login);
        PageRequest ordered = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("changedAt"), Sort.Order.desc("id")));
        Page<UserHistory> result = userHistoryRepository.findByUserId(user.getId(), ordered);

        return new PageResult<>(result.getContent().stream().map(CurrentUserFacadeImpl::historyEntry).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    /**
     * Saves the user, which publishes its domain events and so records history,
     * and flushes so that constraint and version conflicts surface inside this method.
     */
    private void saveAndDetectConflicts(User user)
    {
        try
        {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception)
        {
            throw UserConstraintViolations.translate(exception);
        } catch (OptimisticLockingFailureException exception)
        {
            throw new ConcurrentUserModificationException(exception);
        }
    }

    private User findActiveUser(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");
        User user = userRepository.findByLogin(login).orElseThrow(UserNotFoundException::new);
        if (!user.isValid() || user.getState() != UserState.ACTIVE)
        {
            throw new UserNotFoundException();
        }

        return user;
    }

    private static UserHistoryEntry historyEntry(UserHistory item)
    {
        Long id = Preconditions.checkNotNull(item.getId(), "History entry ID has not been generated yet");

        return new UserHistoryEntry(id, item.getAttributeName(), item.getOldValue(), item.getNewValue(),
                item.getChangedBy(), item.getChangedByType(), item.getChangedAt());
    }

    private static UserDetail toUserDetail(User user)
    {
        return new UserDetail(user.getId(), user.getLogin(), user.getDisplayName(),
                user.getEmail(), user.getState(), user.getDataInfo().getCreated(),
                user.getDataInfo().getModified().orElse(null));
    }
}
