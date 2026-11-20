package cz.osu.sportmeet.user.persistence.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.common.persistence.MainDatabaseEntity;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.domain.event.UserAttributeChanged;
import cz.osu.sportmeet.user.persistence.schema.UserSchema;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registered SportMeet user with embedded audit metadata.
 * Password hashes are never exposed by the REST API or generated toString methods.
 * <p>
 * Every change of a tracked attribute registers a {@link UserAttributeChanged} domain event.
 * Spring Data publishes pending events when the entity is passed to a repository {@code save*}
 * method, therefore callers changing an user must always save it through {@code UserRepository}.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = UserSchema.TABLE_NAME)
public class User implements MainDatabaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = UserSchema.ID, nullable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    private @Nullable Long id;

    @Column(name = UserSchema.LOGIN, nullable = false, length = 80)
    private String login;

    @Column(name = UserSchema.DISPLAY_NAME, nullable = false)
    private String displayName;

    @Column(name = UserSchema.EMAIL, nullable = false, length = 254)
    private String email;

    @Column(name = UserSchema.PASSWORD_HASH, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = UserSchema.STATE, nullable = false, length = 32)
    private UserState state;

    @Column(name = UserSchema.VALID, nullable = false)
    private boolean valid;

    @Embedded
    private DataInfo dataInfo;

    @Version
    @Column(name = UserSchema.VERSION, nullable = false)
    private @Nullable Long version;

    @Transient
    @Getter(AccessLevel.NONE)
    private final List<UserAttributeChanged> domainEvents = new ArrayList<>();

    /**
     * Creates an active, technically valid user.
     * Input identities must have been normalized and the password already encoded.
     *
     * @param login        normalized user login
     * @param displayName  visible name selected by the user
     * @param email        normalized e-mail address
     * @param passwordHash encoded password
     * @param createdAt    creation time
     * @return newly initialized user
     */
    public static User create(String login, String displayName, String email,
                              String passwordHash, OffsetDateTime createdAt)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(login), "login must not be blank");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(displayName), "displayName must not be blank");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(email), "email must not be blank");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(passwordHash), "passwordHash must not be blank");
        Preconditions.checkNotNull(createdAt, "createdAt must not be null");

        User user = new User();
        user.login = login;
        user.displayName = displayName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.state = UserState.ACTIVE;
        user.valid = true;
        user.dataInfo = DataInfo.forSystemCreation(createdAt);

        return user;
    }

    /**
     * Returns the database-generated primary key.
     *
     * @return persisted user identifier
     * @throws IllegalStateException before persistence assigns an ID
     */
    @Override
    public Long getId()
    {
        Preconditions.checkState(id != null, "User ID has not been generated yet");

        return id;
    }

    /**
     * Updates the visible name and modification metadata when it actually changes
     * and registers a {@link UserAttributeChanged} event.
     *
     * @param name          validated visible name
     * @param changedAt     UTC timestamp
     * @param changedBy     identifier of the acting user; null for SYSTEM
     * @param changedByType category of the actor
     * @return true if the visible name changed
     */
    public boolean changeDisplayName(String name, OffsetDateTime changedAt,
                                     @Nullable Long changedBy, DataInfoObjectType changedByType)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name) && name.length() <= 255,
                "Display name must have between 1 and 255 characters.");

        if (displayName.equals(name))
        {
            return false;
        }

        String previous = displayName;
        dataInfo.markModified(changedAt, changedBy, changedByType);
        displayName = name;
        registerChange(UserHistoryAttribute.DISPLAY_NAME, previous, name, changedAt, changedBy, changedByType);

        return true;
    }

    /**
     * Updates the e-mail address and modification metadata when it actually changes
     * and registers a {@link UserAttributeChanged} event.
     *
     * @param newEmail      normalized, validated e-mail address
     * @param changedAt     UTC timestamp
     * @param changedBy     identifier of the acting user; null for SYSTEM
     * @param changedByType category of the actor
     * @return true if the e-mail changed
     */
    public boolean changeEmail(String newEmail, OffsetDateTime changedAt,
                               @Nullable Long changedBy, DataInfoObjectType changedByType)
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newEmail) && newEmail.length() <= 254,
                "Email is invalid.");

        if (email.equals(newEmail))
        {
            return false;
        }

        String previous = email;
        dataInfo.markModified(changedAt, changedBy, changedByType);
        email = newEmail;
        registerChange(UserHistoryAttribute.EMAIL, previous, newEmail, changedAt, changedBy, changedByType);

        return true;
    }

    /**
     * Returns events registered since the last publication.
     * Invoked by Spring Data after {@code save*}; also useful for verifying domain behaviour.
     *
     * @return immutable snapshot of pending events
     */
    @DomainEvents
    public List<UserAttributeChanged> getDomainEvents()
    {
        return List.copyOf(domainEvents);
    }

    /**
     * Discards events after Spring Data has published them.
     */
    @AfterDomainEventPublication
    void clearDomainEvents()
    {
        domainEvents.clear();
    }

    private void registerChange(UserHistoryAttribute attribute, @Nullable String oldValue, @Nullable String newValue,
                                OffsetDateTime changedAt, @Nullable Long changedBy,
                                DataInfoObjectType changedByType)
    {
        domainEvents.add(new UserAttributeChanged(
                getId(), attribute, oldValue, newValue, changedBy, changedByType, changedAt));
    }
}
