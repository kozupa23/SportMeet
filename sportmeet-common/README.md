# sportmeet-common

## Účel modulu

`sportmeet-common` obsahuje malé společné typy a abstrahované technické prvky, které používají další backendové moduly. Neobsahuje samostatnou business funkcionalitu SportMeet ani spustitelnou aplikaci.

Do tohoto modulu patří pouze kód se skutečně sdíleným významem. Konkrétní doménová pravidla (např. uživatelé, členství nebo akce) mají zůstat ve svých modulech.

## Struktura

Základní package: `cz.osu.sportmeet.common`.

```text
src/
├── main/java/cz/osu/sportmeet/common/
│   ├── api/                 # Obecné typy pro REST odpovědi
│   ├── application/         # Sdílené aplikační výsledky a výjimky
│   └── persistence/         # Základní persistence typy a metadata
└── test/java/.../common/
    └── persistence/         # Testy sdílených persistence typů
```

### Hlavní součásti

| Oblast | Typy | Účel |
|---|---|---|
| API | `PageResponse` | Společná reprezentace stránkované odpovědi |
| Application | `PageResult`, `InvalidInputException` | Aplikační stránkování a obecná chyba vstupu |
| Persistence | `DatabaseEntity`, `MainDatabaseEntity` | Základní kontrakty pro databázové entity |
| Persistence | `DataInfo`, `DataInfoObjectType`, `DataInfoSchema`, `DatabaseConstants` | Sdílená metadata a konstanty persistence |

## Závislosti a pravidla

- Modul je běžná Maven knihovna, nikoliv Spring Boot aplikace.
- `sportmeet-user` závisí na `sportmeet-common`.
- `sportmeet-common` nesmí záviset na konkrétních doménových modulech, například `sportmeet-user`.
- Nepřidávat sem typy jen proto, že se používají ve dvou třídách. Nejprve ověřit, že jde o stabilní sdílený kontrakt.
- Modul nevlastní žádné samostatné REST endpointy ani databázové migrace.

## Testování

V modulu je aktuálně test `DataInfoTest`. Testy spouštějte z kořenové složky projektu pomocí Maven Wrapperu:

```bash
./mvnw -pl sportmeet-common test
```

Na Windows použijte `mvnw.cmd` místo `./mvnw`.

Podrobnosti k sestavení celého projektu jsou v [hlavním README](../README.md).
