## Lokalna konfiguracja i uruchomienie menadżera

Aplikacja obsługuje dwa profile środowiskowe:

* `bk` – Bartłomiej Krawczyk,
* `mb` – Mateusz Brzozowski.

### 1. Instalacja środowiska SDK

Użyj polecenia poniżej, aby zainstalować wymagane wersje JDK i narzędzi na podstawie pliku `.sdkmanrc`:

```shell
sdk env install
```

### 2. Uruchomienie aplikacji z określonym profilem

Aby uruchomić aplikację z wybranym profilem, użyj jednej z poniższych komend:

#### Profil `bk`:

```shell
SPRING_PROFILES_ACTIVE=bk ./gradlew :manager:bootRun
```

#### Profil `mb`:

```shell
SPRING_PROFILES_ACTIVE=mb ./gradlew :manager:bootRun
```
