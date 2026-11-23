# sportmeet-group

Modul vlastní sportovní skupiny a členství v nich (role `OWNER`, `SECONDARY_OWNER`, `MEMBER`).
Veřejné endpointy jsou pod `/api/v1/groups` (zatím `POST` pro založení skupiny; vyžaduje přihlášení a CSRF token).

Identitu přihlášeného uživatele získává výhradně přes veřejný kontrakt modulu `user`
(`cz.osu.sportmeet.user.api.lookup.UserLookupFacade`); do interních balíčků modulu `user` nesahá.
