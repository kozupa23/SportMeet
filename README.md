# SportMeet

Backend aplikace SportMeet postavená na Spring Bootu.

## Požadavky

- JDK 25
- Git
- IntelliJ IDEA
- DB TODO

Samostatnou instalaci Mavenu nepotřebuješ. Projekt obsahuje Maven Wrapper, který používá Maven 3.9.16.

## Stažení projektu

Tyhle kroky uděláš jen jednou. Potřebuješ nainstalovaný [Git](https://git-scm.com/downloads).

1. Otevři terminál: na Windows Git Bash nebo PowerShell, na Linuxu/macOS Terminál. Příkazem `cd` přejdi do složky, kam chceš projekt uložit.
2. Pokud jsi Git na tomhle počítači ještě nepoužíval, nastav své jméno a e-mail. Git je zapisuje ke každému commitu a bez nich commit neudělá. Použij stejný e-mail jako na GitHubu, aby se commity propojily s tvým účtem:

   ```bash
   git config --global user.name "Jan Novák"
   git config --global user.email "jan.novak@example.com"
   ```

   Stačí to jednou, platí to pro všechny projekty na tomhle počítači.

3. Stáhni repozitář. Ve složce z prvního kroku se vytvoří složka `SportMeet`:

   ```bash
   git clone https://github.com/kozupa23/SportMeet.git
   ```

4. Přejdi do složky projektu:

   ```bash
   cd SportMeet
   ```

5. Zapni kontrolu zpráv commitů (víc v sekci [Commit zprávy](#commit-zprávy)):

   ```bash
   git config core.hooksPath .githooks
   ```

   Příkaz nic nevypíše. Jestli je kontrola zapnutá, ověříš příkazem `git config core.hooksPath`, který vypíše `.githooks`.

## První spuštění v IntelliJ IDEA

1. Stáhni projekt podle sekce [Stažení projektu](#stažení-projektu).
2. V IntelliJ IDEA zvol **Open** a otevři kořenovou složku projektu (tu, ve které je `pom.xml`).
3. Pokud IDEA nenajde JDK 25, nastav nebo stáhni ho přes **File -> Project Structure -> Project SDK**.
4. V **Settings -> Build, Execution, Deployment -> Build Tools -> Maven** nastav Maven home path na **Use Maven wrapper**.
5. Nech IntelliJ dokončit Maven import a stažení dependencies.
6. Spusť sdílenou konfiguraci **SportMeet**, případně přímo `SportMeetApplication`.

Projekt nyní nevyžaduje databázi ani Docker.

## Spuštění bez IntelliJ IDEA

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

## Testy

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

## Maven

Projekt používá Maven Wrapper (`mvnw`, `mvnw.cmd`). Verze Mavenu je definovaná v:

```text
.mvn/wrapper/maven-wrapper.properties
```

Díky tomu všichni vývojáři i CI používají stejnou verzi Mavenu a není nutné udržovat vlastní Maven instalaci.

## Commit zprávy

Zprávy commitů používají formát [Conventional Commits](https://www.conventionalcommits.org/):

```text
typ(scope): popis
```

Scope je volitelný. Vykřičník před dvojtečkou (`feat!: ...`) značí breaking change.

| Typ | Použití |
|-----|---------|
| `feat` | nová funkcionalita |
| `fix` | oprava chyby |
| `docs` | dokumentace |
| `style` | formátování kódu bez změny chování |
| `refactor` | úprava kódu bez změny chování |
| `perf` | zlepšení výkonu |
| `test` | testy |
| `build` | build, Maven, závislosti |
| `ci` | konfigurace CI |
| `chore` | ostatní údržba |
| `revert` | vrácení dřívější změny |

Příklady:

```text
feat: registrace uživatele
fix(auth): oprava expirace tokenu
```

Formát kontroluje hook `.githooks/commit-msg`. Zprávy, které generuje Git (`Merge ...`, `Revert "..."`, `fixup!`, `squash!`), projdou bez kontroly.

Hook se zapíná při [stažení projektu](#stažení-projektu). Pokud máš projekt naklonovaný dřív, spusť v jeho kořenové složce:

```bash
git config core.hooksPath .githooks
```

## Co patří do Gitu

Do repozitáře patří zdrojové kódy, `pom.xml`, Maven Wrapper, `.githooks/`, `.run/`, `.editorconfig`, `.gitattributes`, `.gitignore` a README.

Naopak se necommitují `.idea/`, `*.iml`, `target/`, lokální `.env` soubory ani soubory se secrets.
