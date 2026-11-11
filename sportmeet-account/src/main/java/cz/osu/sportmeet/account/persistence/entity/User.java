package cz.osu.sportmeet.account.persistence.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import cz.osu.sportmeet.account.domain.UserState;
import cz.osu.sportmeet.account.persistence.schema.UserSchema;
import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.MainDatabaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Registered SportMeet account with embedded audit metadata.
 * Password hashes are never exposed by the REST API or generated toString methods.
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

    /**
     * -- GETTER --
     * Returns a password hash solely for internal authentication services.
     * Never include it in API DTOs, logs, audit events or history.
     *
     */
    @Getter
    @Column(name = UserSchema.PASSWORD_HASH, nullable = false
    )
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

    /**
     * Creates an active, technically valid account.
     * Input identities must have been normalized and the password already encoded.
     *
     * @param login        normalized account login
     * @param displayName  visible name selected by the user
     * @param email        normalized e-mail address
     * @param passwordHash encoded password
     * @param createdAt    creation time
     * @return newly initialized account
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
     * @return persisted account identifier
     * @throws IllegalStateException before persistence assigns an ID
     */
    @Override
    public Long getId()
    {
        Preconditions.checkState(id != null, "User ID has not been generated yet");

        return id;
    }

}
