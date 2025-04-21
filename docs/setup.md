# Konfiguracja Alpine Linux z usługami Java i SSH, oraz uruchomieniem aplikacji z load balancerem

Dokumentacja opisuje proces instalacji i konfiguracji systemu Alpine Linux w środowisku wirtualnym. W ramach konfiguracji uruchamiana jest aplikacja Java jako usługa działająca w tle, z dostępem przez SSH oraz zintegrowanym serwerem Nginx pełniącym rolę load balancera.

---

## 1. Pobranie i instalacja Alpine Linux

Pobierz obraz systemu z oficjalnej strony:  
[Alpine Linux](https://alpinelinux.org/downloads/)

Zalecane: obraz w wersji "Virtual".

### Dane dostępowe:

- **Login:** `root`
- **Hasło:** brak (pozostaw puste przy pierwszym logowaniu)

---

## 2. Podstawowa konfiguracja systemu

Po uruchomieniu systemu wykonaj interaktywną konfigurację:

```shell
setup-alpine -q
```

Wybierz układ klawiatury:

```shell
Select keyboard layout: pl
Select variant: pl
```

Następnie zainstaluj system na głównym dysku (`/dev/vda`) z wykorzystaniem trybu systemowego:

```shell
setup-disk -m sys /dev/vda
```

```shell
WARNING: Erase the above disk(s) and continue (y/n) [n]: y
```

Po zakończeniu procesu instalacji, wyłącz maszynę wirtualną:

```shell
poweroff
```

---

## 3. Konfiguracja po restarcie

Po ponownym uruchomieniu systemu ustaw hasło dla użytkownika `root`:

```shell
passwd root
```

Wprowadź nowe hasło dwukrotnie:

```shell
New password: root
Retype password: root
```

Zainstaluj edytor tekstu:

```shell
apk add nano
```

---

## 4. Konfiguracja repozytoriów pakietów

Edytuj plik zawierający listę źródeł pakietów:

```shell
nano /etc/apk/repositories
```

Zamień:

```shell
#/media/cdrom/apks
http://dl-cdn.alpinelinux.org/alpine/v3.21/main
#http://dl-cdn.alpinelinux.org/alpine/v3.21/community
```

na:

```shell
#/media/cdrom/apks
http://dl-cdn.alpinelinux.org/alpine/v3.21/main
http://dl-cdn.alpinelinux.org/alpine/v3.21/community
```

Zapisz zmiany i zamknij edytor (`Ctrl+S`, `Ctrl+X`).

Zaktualizuj indeks pakietów:

```shell
apk update
```

---

## 5. Instalacja środowiska Java

Zainstaluj OpenJDK w wersji 21:

```shell
apk add openjdk21
```

---

## 6. Konfiguracja serwera SSH

Zainstaluj i skonfiguruj serwer SSH:

```shell
apk add openssh-server
cp /etc/ssh/sshd_config /etc/ssh/ssh_config.backup
```

Wygeneruj klucze hosta:

```shell
ssh-keygen -A
```

**Opis parametru `-A`:** Generuje wszystkie domyślne typy kluczy hosta, jeżeli jeszcze nie istnieją. Klucze zostaną zapisane w domyślnych lokalizacjach bez hasła.

---

### Modyfikacja konfiguracji SSH

Edytuj plik konfiguracyjny klienta SSH:

```shell
nano /etc/ssh/ssh_config
```

Odkomentuj i zmodyfikuj linijkę:

```shell
#PermitRootLogging prohibit-password
```

na:

```shell
PermitRootLogging yes
```

Sprawdź poprawność konfiguracji:

```shell
sshd -t -f /etc/ssh/sshd_config
```

**Parametry:**

- `-t` – test trybu konfiguracji
- `-f` – wskazanie konkretnego pliku konfiguracyjnego

Uruchom ponownie usługę SSH:

```shell
service sshd restart
```

Dodaj usługę SSH do domyślnego poziomu uruchamiania:

```shell
rc-update add sshd default
rc-service sshd start
```

---

## 7. Połączenie z maszyną zdalnie

Z innej maszyny połącz się z Alpine Linux za pomocą SSH:

```shell
$ ssh root@192.168.122.26
The authenticity of host '192.168.122.26 (192.168.122.26)' can't be established.
ED25519 key fingerprint is SHA256:EMjnkHpSoKWgiz6S6fBAgR4aaKjGxC79sG/oSBUb0oA.
This key is not known by any other names.
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
Warning: Permanently added '192.168.122.26' (ED25519) to the list of known hosts.
root@192.168.122.26's password: 
Welcome to Alpine!

The Alpine Wiki contains a large amount of how-to guides and general
information about administrating Alpine systems.
See <https://wiki.alpinelinux.org/>.

You can setup the system with the command: setup-alpine

You may change this message by editing /etc/motd.

alpine:~#
```

---

## 8. Uruchomienie aplikacji Java

Prześlij aplikację na serwer:

```shell
scp ./stateless/build/libs/stateless.jar root@192.168.122.26:/stateless.jar
```

Przetestuj uruchomienie:

```shell
java -jar /stateless.jar
```

Dostęp testowy:

```
GET http://192.168.122.26:8080/random/boolean?probability=1.0
```

Aby uruchomić aplikację jako proces w tle:

```shell
nohup java -jar /stateless.jar > stateless.log 2>&1 &
```

Sprawdzenie, czy aplikacja działa:

```shell
ps aux | grep stateless.jar
```

---

## 9. Instalacja i konfiguracja serwera Nginx

Zainstaluj serwer WWW:

```shell
apk add nginx
```

Dodaj go do usług uruchamianych przy starcie systemu i uruchom:

```shell
rc-update add nginx default
rc-service nginx start
```

Zweryfikuj status usługi:

```shell
rc-service nginx status
```

Sprawdź działanie:

```shell
curl http://localhost
```

Z zewnątrz maszyny wirtualnej:
```shell
curl http://192.168.122.26
```

---

### Modyfikacja konfiguracji Nginx

Edytuj plik konfiguracyjny:

```shell
nano /etc/nginx/nginx.conf
```

Po wprowadzeniu zmian, zrestartuj konfigurację:

```shell
rc-service nginx reload
```
