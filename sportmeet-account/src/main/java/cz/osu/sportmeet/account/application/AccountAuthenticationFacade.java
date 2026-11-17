package cz.osu.sportmeet.account.application;

import cz.osu.sportmeet.account.application.dto.AccountCredentials;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Account-module contract for authentication performed in the application module.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface AccountAuthenticationFacade
{
    /**
     * Finds credentials for a canonical login without exposing persistence entities.
     *
     * @param login normalized login
     * @return account credentials or an empty result for an unknown login
     */
    Optional<AccountCredentials> findByLogin(String login);
}
