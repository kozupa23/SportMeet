# sportmeet-app

## Účel modulu

`sportmeet-app` je spustitelná Spring Boot aplikace a vstupní bod backendu SportMeet. Sestavuje ostatní backendové moduly do jednoho procesu, obsahuje konfiguraci autentizace a HTTP sessions a spravuje databázové migrace.

Jde o **composition root** modulárního monolitu – business pravidla uživatelských účtů zůstávají ve `sportmeet-user`.

## Struktura

Základní package: `cz.osu.sportmeet`.

```text
src/
├── main/
│   ├── java/cz/osu/sportmeet/
│   │   ├── SportMeetApplication.java  # Spring Boot entry point
│   │   ├── api/                       # Autentizační REST endpointy a DTO
│   │   └── config/                    # Spring Security a UserDetailsService
│   └── resources/
│       ├── application.properties     # Konfigurace aplikace
│       └── db/migration/              # Flyway SQL migrace
└── test/
    ├── java/cz/osu/sportmeet/        # HTTP, persistence a integrační testy
    │   ├── architecture/             # ArchUnit testy hranic
    │   └── support/                  # Testovací pomocné třídy
    └── resources/
        └── application-test.properties
```

## Odpovědnosti

| Oblast | Aktuální řešení |
|---|---|
| Spuštění | `SportMeetApplication` |
| Autentizace | Spring Security `formLogin` a `SportMeetUserDetailsService` |
| Sessions | Spring Session JDBC nad relační databází |
| HTTP API | `AuthController` a Spring Security autentizační filtry |
| Persistence infrastruktura | Datasource, Flyway migrace |
| Ověřování architektury | ArchUnit testy |

### Autentizační API

- `GET /api/v1/auth/csrf` – získání CSRF tokenu.
- `GET /api/v1/auth/session` – informace o aktuální autentizované session.
- `POST /api/v1/auth/login` – přihlášení zpracovávané **Spring Security filtrem**, nikoliv metodou controlleru.
- Odhlášení zpracovává rovněž Spring Security podle konfigurace v `SecurityConfiguration`.

Při úpravách bezpečnosti zachovávat stávající session-based přístup a ověřovat chování CSRF a cookies.

## Databázové migrace

Migrace se nacházejí v `src/main/resources/db/migration`:

| Migrace | Účel |
|---|---|
| `V1__initialize_database.sql` | Inicializace databáze |
| `V2__create_user.sql` | Uživatelské účty |
| `V3__create_spring_session.sql` | Tabulky Spring Session JDBC |
| `V4__create_user_history.sql` | Historie změn uživatele |

Aplikované Flyway migrace neupravovat. Při změně schématu přidat další verzovanou migraci.

## Závislosti a pravidla

- Modul závisí na `sportmeet-user` a přes něj využívá také `sportmeet-common`.
- Jako jediný backendový modul obsahuje hlavní Spring Boot entry point.
- Konfigurace Spring Security a Spring Session patří sem; doménovou správu uživatele řeší `sportmeet-user`.
- Při integraci nových modulů zde přidávat pouze potřebné zapojení a infrastrukturu, nikoliv přenášet jejich business logiku do `sportmeet-app`.

## Spuštění a testování

Pro vývoj je nutné JDK 25 a PostgreSQL. Konfigurace připojení a obecné instrukce jsou v [hlavním README](../README.md).

Spuštění z kořene repozitáře po sestavení modulů:

```bash
./mvnw clean install
./mvnw -pl sportmeet-app spring-boot:run
```

Testy všech backendových modulů:

```bash
./mvnw clean verify
```

HTTP, persistence a architektonické testy modulu jsou v `src/test`. Testovací konfigurace používá H2; pro ověřování chování specifického pro PostgreSQL je vhodné doplnit samostatné PostgreSQL integrační testy.

## Úplný REST API kontrakt

Autentizační endpointy uvedené výše jsou součástí společného kontraktu backendu. Podrobný popis všech aktuálních endpointů včetně request/response atributů, autentizace, CSRF a příkladů najdete v [`docs/REST_API.md`](../docs/REST_API.md); strojově čitelná OpenAPI specifikace je v [`docs/openapi.yaml`](../docs/openapi.yaml). Při změně přihlašovacího filtru, response DTO nebo bezpečnostních pravidel aktualizujte oba dokumenty a odpovídající integrační testy.
