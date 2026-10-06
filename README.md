# SportMeet

Backend aplikace SportMeet postavená na Spring Bootu.

## Požadavky

- JDK 25
- Git
- IntelliJ IDEA
- DB TODO

Samostatnou instalaci Mavenu nepotřebuješ. Projekt obsahuje Maven Wrapper, který používá Maven 3.9.16.

## První spuštění v IntelliJ IDEA

1. Naklonuj repozitář.
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

## Co patří do Gitu

Do repozitáře patří zdrojové kódy, `pom.xml`, Maven Wrapper, `.run/`, `.editorconfig`, `.gitattributes`, `.gitignore` a README.

Naopak se necommitují `.idea/`, `*.iml`, `target/`, lokální `.env` soubory ani soubory se secrets.
