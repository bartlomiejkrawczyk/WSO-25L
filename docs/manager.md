## Lokalna konfiguracja i uruchomienie menadżera

Aplikacja obsługuje dwa profile środowiskowe:

* `bk` – Bartłomiej Krawczyk,
* `mb` – Mateusz Brzozowski.

### 1. Instalacja środowiska SDK

Użyj polecenia poniżej, aby zainstalować wymagane wersje JDK i narzędzi na podstawie pliku `.sdkmanrc`:

```shell
sdk env install
```

### 2. Budowanie jara

```shell
./gradlew manager:bootJar
```

### 3. Uruchomienie aplikacji z określonym profilem

Aby uruchomić aplikację z wybranym profilem, użyj jednej z poniższych komend:

#### Profil `bk`:

```shell
SPRING_PROFILES_ACTIVE=bk java -jar ./manager/build/libs/manager.jar
```

#### Profil `mb`:

```shell
SPRING_PROFILES_ACTIVE=mb java -jar ./manager/build/libs/manager.jar
```
