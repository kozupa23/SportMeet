# SportMeet – REST API v1

**Base URL (lokálně):** `http://localhost:8080`; konfigurace `SERVER_PORT` ji může změnit. Produkční komunikace má probíhat přes HTTPS.

## Přehled endpointů

| Metoda | Cesta | Modul | Přihlášení | CSRF | Úspěch |
|---|---|---|---|---|---|
| GET | `/api/v1/auth/csrf` | app | ne | ne | 200 JSON |
| POST | `/api/v1/auth/login` | app / Spring Security | ne | **ano** | 204 bez těla |
| POST | `/api/v1/auth/logout` | app / Spring Security | ano* | **ano** | 204 bez těla |
| GET | `/api/v1/auth/session` | app | ano | ne | 200 JSON |
| POST | `/api/v1/users` | user | ne | **výjimka** | 201 JSON + Location |
| GET | `/api/v1/users/me` | user | ano | ne | 200 JSON |
| PATCH | `/api/v1/users/me` | user | ano | **ano** | 200 JSON |
| GET | `/api/v1/users/me/history` | user | ano | ne | 200 JSON |
| POST | `/api/v1/groups` | group | ano | **ano** | 201 JSON + Location |

*Odhlášení je bezpečnostní operace nad aktuální session; jeho přesná odezva bez platného přihlášení závisí na zpracování Spring Security a není samostatně deklarována controllerem. Pro FE doporučujeme volat ho z přihlášeného stavu.*

## Společné konvence

- **Autentizace:** session cookie `SESSION` (Spring Session JDBC). Prohlížeč musí zachovat cookie mezi požadavky. Na frontendu typicky `fetch(url, { credentials: 'include' })`; při jiné origin konfiguraci je nutné správně nastavit CORS a cookie policy. Backend sám teď CORS explicitně nenastavuje.
- **CSRF:** načíst `GET /api/v1/auth/csrf`, převzít `headerName` a `token` a připojit je jako HTTP header při přihlášení, odhlášení, aktualizaci profilu a založení skupiny. Registrace `POST /api/v1/users` je aktuálně z CSRF vyňata. Token se může změnit po přihlášení/odhlášení; pak ho znovu načíst.
- **Content-Type:** běžné JSON requesty `application/json`. **Přihlášení je výjimka**: `application/x-www-form-urlencoded`, parametry `login` a `password`.
- **Časy:** JSON datumy jsou serializované `OffsetDateTime`, tedy ISO-8601 s offsetem, např. `2026-11-23T19:30:00Z`. `updatedAt` může být `null`.
- **Chybové odpovědi:** validační/konfliktní chyby z `sportmeet-user` a validační/autorizační chyby z `sportmeet-group` mají formát Spring `ProblemDetail` (typicky `type`, `title`, `status`, `detail`, případně `instance`). Bezpečnostní `401/403` a neúspěšné přihlášení mohou mít **prázdné tělo**. FE nesmí automaticky předpokládat JSON u všech chyb.
- **Nedokumentované endpointy:** například reset hesla, rate limiting, ověření e-mailu a obecné `GET /api/v1/users/{id}` v tomto ZIPu **nejsou implementované**. Stejně tak nejsou implementované výpis, detail, úprava a zrušení skupiny ani správa členů (`GET /api/v1/groups`, `GET /api/v1/groups/{id}` apod.). `Location` hlavičky registrace a založení skupiny sice na `/api/v1/users/{id}` a `/api/v1/groups/{id}` odkazují, ale neimplikují existenci GET endpointů.

## 1. Získání CSRF tokenu

**GET** `/api/v1/auth/csrf` · veřejný · bez request body.

**200 OK** (`Cache-Control: no-store`):

```json
{
  "headerName": "X-CSRF-TOKEN",
  "token": "example-token-value"
}
```

`headerName` je součást kontraktu; nepředpokládejte pevný název hlavičky v klientovi. Hodnota tokenu je proměnlivá. Běžný test: `GET` vrátí neprázdné obě položky; mutační volání bez hlavičky selže s `403`.

## 2. Přihlášení

**POST** `/api/v1/auth/login` · veřejný · `application/x-www-form-urlencoded` · vyžaduje CSRF.

| Form parametr | Typ | Povinný | Popis |
|---|---|---|---|
| `login` | string | ano | Uživatelské přihlašovací jméno (ne e-mail, pokud se liší). Na backendu se normalizuje. |
| `password` | string | ano | Heslo uživatele. |

```http
POST /api/v1/auth/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded
X-CSRF-TOKEN: <získaný-token>

login=runner&password=ExamplePassword123
```

**204 No Content:** žádné JSON tělo; autentizace je držena serverovou session/cookie.
**401 Unauthorized:** nesprávné údaje / účet nepovolený pro přihlášení (bez těla).
**403 Forbidden:** chybějící či neplatný CSRF token.

Testy: přihlášení správnými údaji, nesprávné heslo, nepřihlášená session před loginem, změna session, přístup k chráněným endpointům po loginu.

## 3. Aktuální session

**GET** `/api/v1/auth/session` · vyžaduje přihlášení · bez request body.

**200 OK:**

```json
{ "login": "runner" }
```

**401 Unauthorized:** není platná autentizovaná session. Pro FE použitelné jako kontrola přihlášení po načtení stránky.

## 4. Odhlášení

**POST** `/api/v1/auth/logout` · vyžaduje CSRF · bez request body.

**204 No Content:** Spring Security zneplatní HTTP session, vyčistí autentizaci a smaže cookie `SESSION`. **403** při chybějícím CSRF. Následující `GET /api/v1/auth/session` má vrátit **401**.

## 5. Registrace uživatele

**POST** `/api/v1/users` · veřejný · JSON · bez CSRF (současná výjimka).

| Pole | Typ | Povinné | API validace / aplikační pravidlo |
|---|---|---|---|
| `login` | string | ano | neprázdné; po `strip()` a převodu na malá písmena délka **2–80**, unikátní |
| `displayName` | string | ano | neprázdné; po `strip()` délka **1–255** |
| `email` | string | ano | neprázdné, po normalizaci max **254**, kontrola e-mailového vzoru, unikátní |
| `password` | string | ano | minimálně **8 znaků**, maximálně **72 UTF-8 bajtů** (BCrypt omezení) |

```json
{
  "login": "Runner",
  "displayName": "Sportovec",
  "email": "Runner@example.com",
  "password": "ExamplePassword123"
}
```

**201 Created**, `Location: /api/v1/users/42`:

```json
{
  "id": 42,
  "login": "runner",
  "displayName": "Sportovec",
  "email": "runner@example.com",
  "state": "ACTIVE"
}
```

`state` je aktuálně přímo `ACTIVE` (žádné potvrzení e-mailu po registraci). `password` ani `passwordHash` se nikdy nevrací. **400 Bad Request** pro neplatný JSON / pole / doménovou validaci; **409 Conflict** pro duplicitní login nebo e-mail. Tyto chyby používají `ProblemDetail`.

## 6. Vlastní profil

**GET** `/api/v1/users/me` · vyžaduje přihlášení · bez request body.

**200 OK:**

```json
{
  "id": 42,
  "login": "runner",
  "displayName": "Sportovec",
  "email": "runner@example.com",
  "state": "ACTIVE",
  "createdAt": "2026-11-23T19:30:00Z",
  "updatedAt": null
}
```

Možné `state`: `ACTIVE`, `DISABLED`, `BLOCKED`; běžné zobrazení přes fasádu vyžaduje aktivní platný účet. **401 Unauthorized** bez přihlášení; **404 Not Found** pokud aktuální platný/aktivní uživatel není nalezen. ID uživatele se neposílá v URL ani requestu: získá se z autentizovaného principala.

## 7. Úprava vlastního profilu

**PATCH** `/api/v1/users/me` · vyžaduje přihlášení a CSRF · JSON.

| Pole | Typ | Povinné | Pravidla |
|---|---|---|---|
| `displayName` | string | ne | je-li uvedeno nenulové, po `strip()` 1–255 znaků |
| `email` | string | ne | je-li uveden nenulový, validní e-mail max 254 znaků, unikátní; normalizace na malá písmena |

```json
{ "displayName": "Nové jméno" }
```

Lze poslat i obě pole zároveň. Vynechaná pole zůstávají beze změny. Tělo s oběma poli chybějícími nebo `null` skončí chybou `400`. Neznámá pole (např. `state`, `login`) jsou explicitně odmítána `400`.

**200 OK:** stejné atributy jako `GET /api/v1/users/me`, již s novými hodnotami a zpravidla novým `updatedAt`.

**400** neplatné údaje / neznámé pole; **401** nepřihlášený; **403** bez CSRF; **404** chybějící či neaktivní účet; **409** duplicitní e-mail nebo souběžná modifikace (`@Version`). Změna stejnou hodnotou je no-op bez dalšího záznamu historie.

**Pozor:** aktuální implementace změnu e-mailu provádí okamžitě **bez ověření nové adresy**. Je to popis současného stavu, nikoli bezpečnostní doporučení.

## 8. Historie změn vlastního účtu

**GET** `/api/v1/users/me/history?page=0&size=20` · vyžaduje přihlášení.

| Query parametr | Typ | Výchozí | Omezení |
|---|---|---|---|
| `page` | integer | `0` | ≥ 0; stránkování od nuly |
| `size` | integer | `20` | 1–100 |

**200 OK:**

```json
{
  "content": [
    {
      "id": 88,
      "attributeName": "DISPLAY_NAME",
      "oldValue": "Sportovec",
      "newValue": "Nové jméno",
      "changedBy": 42,
      "changedByType": "USER",
      "changedAt": "2026-11-23T19:40:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

`attributeName`: `DISPLAY_NAME`, `EMAIL`, `PASSWORD`, `STATE`. `changedByType` je enum technického auditního aktéra (např. `USER`); `changedBy` může být `null` pro systémovou změnu. `oldValue` a `newValue` mohou být `null`, u citlivých hodnot se nezaznamenávají. Historie je řazená sestupně podle `changedAt` a `id`. **400** pro neplatné stránkování; **401** bez session; **404** chybějící/neaktivní účet. Prázdná historie vrací `content: []`.

## 9. Založení sportovní skupiny

**POST** `/api/v1/groups` · vyžaduje přihlášení a CSRF · JSON.

Přihlášený uživatel se stává jediným aktivním vlastníkem (`OWNER`) nové skupiny. Skupina i členství vlastníka se ukládají v jedné transakci. Vlastník se nikdy neposílá v těle: získá se z autentizovaného principala.

| Pole | Typ | Povinné | API validace / aplikační pravidlo |
|---|---|---|---|
| `name` | string | ano | neprázdné; po `strip()` délka **1–100** znaků |
| `description` | string | ne | po `strip()` max **2000** znaků; `null` nebo text z bílých znaků se uloží jako `null` |
| `visibility` | string | ne | `PUBLIC` nebo `PRIVATE`; při vynechání nebo `null` se použije `PRIVATE` |

```json
{
  "name": "  Florbal Ostrava – Poruba ",
  "description": "Pravidelné tréninky každé úterý",
  "visibility": "PUBLIC"
}
```

**201 Created**, `Location: /api/v1/groups/1`:

```json
{
  "id": 1,
  "name": "Florbal Ostrava – Poruba",
  "description": "Pravidelné tréninky každé úterý",
  "visibility": "PUBLIC"
}
```

`name` a `description` se vrací normalizované (ořezané). Odpověď neobsahuje interní příznak platnosti, auditní údaje ani členy skupiny. Názvy skupin nejsou unikátní. Neznámá pole v těle se ignorují. `Location` odkazuje na `GET /api/v1/groups/{id}`, který zatím neexistuje.

Význam `visibility`: `PUBLIC` – skupinu lze najít a kdokoli o členství může požádat; `PRIVATE` – skupina je viditelná jen členům, do skupiny se vstupuje pozváním.

| Stav | Příčina | Tělo |
|---|---|---|
| **400** | chybějící/prázdný `name`, neplatný JSON, neznámá hodnota `visibility` | `ProblemDetail`, `title` = `Invalid group data`, `detail` = `Group data failed validation.` |
| **400** | název delší než 100 znaků nebo popis delší než 2000 znaků | `ProblemDetail`, `title` = `Invalid group data`, `detail` s porušeným pravidlem |
| **401** | uživatel není přihlášen | bez těla |
| **403** | chybějící nebo neplatný CSRF token | bez `ProblemDetail`; tělo generuje Spring Security / Spring Boot |
| **403** | uživatel je přihlášen, ale jeho účet už není `ACTIVE` (např. byl zablokován po přihlášení) | `ProblemDetail`, `title` = `User account not active` |

```json
{
  "type": "about:blank",
  "title": "Invalid group data",
  "status": 400,
  "detail": "Group name must have between 1 and 100 characters.",
  "instance": "/api/v1/groups"
}
```

Chybové odpovědi nikdy nevracejí vložené hodnoty ani stack trace. Pořadí kontrol: nejdříve se ověří přihlášení a CSRF, poté Bean Validation (`name`), potom aktivní stav účtu a nakonec obchodní validace délek; uživatel s neaktivním účtem tedy dostane `403`, aniž by se dozvěděl cokoli o platnosti zbylých údajů.

Role členů (`OWNER`, `SECONDARY_OWNER`, `MEMBER`) a stavy členství (`ACTIVE`, `ENDED`) jsou součástí datového modelu, ale zatím se přes REST API nevystavují.

## Typický scénář pro frontend a API testy

1. `POST /api/v1/users` → `201` a nový účet `ACTIVE`.
2. `GET /api/v1/auth/csrf` → `200`; uložit `headerName` a `token` a zachovat cookie.
3. `POST /api/v1/auth/login` jako **form-urlencoded** s CSRF → `204`; zachovat session cookie.
4. `GET /api/v1/auth/session` → `200`, aktuální `login`.
5. `GET /api/v1/users/me` → `200`, aktuální uživatel bez hesla.
6. `PATCH /api/v1/users/me` jako JSON s CSRF → `200`, upravený profil.
7. `GET /api/v1/users/me/history?page=0&size=20` → `200`, historie obsahuje změnu.
8. `POST /api/v1/groups` jako JSON s CSRF → `201`, vlastní skupina s `id` a `Location`.
9. `POST /api/v1/auth/logout` s CSRF → `204`; opakovaný `GET /api/v1/auth/session` → `401`.

## Údržba dokumentace

Dokumentaci API (`REST_API.md` a `openapi.yaml`) udržovat verzovanou spolu s backendovým kódem. V PR měnícím REST API musí být aktualizována obě místa, testy a případně README modulu. V budoucnu lze zavést `springdoc-openapi` a generovat část specifikace automaticky; aktuálně není součástí projektového kódu. V případě rozporu do jeho odstranění rozhoduje skutečná implementace a integrační testy, nikoli tento dokument.
