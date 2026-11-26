# sportmeet-group

Modul vlastní sportovní skupiny a členství v nich (role `OWNER`, `SECONDARY_OWNER`, `MEMBER`).
Veřejné endpointy jsou pod `/api/v1/groups` a vyžadují přihlášení:

- `POST /api/v1/groups` – založení skupiny (navíc CSRF token),
- `GET /api/v1/groups/{id}` – detail skupiny; aktuální členové vidí i seznam členů, nečlen jen základní údaje
  veřejné skupiny. Soukromá, zneplatněná i neexistující skupina vrací stejné 404.

Identitu přihlášeného uživatele získává výhradně přes veřejný kontrakt modulu `user`
(`cz.osu.sportmeet.user.api.lookup.UserLookupFacade`); do interních balíčků modulu `user` nesahá.
