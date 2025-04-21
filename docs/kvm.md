# Instalacja i konfiguracja KVM

Poniższe kroki prowadzą przez proces instalacji i podstawowej konfiguracji KVM (Kernel-based Virtual Machine) wraz z narzędziem `virt-manager` umożliwiającym graficzne zarządzanie maszynami wirtualnymi.

### 1. Sprawdzenie wsparcia sprzętowego dla wirtualizacji

Aby uruchomić KVM, procesor musi obsługiwać technologię wirtualizacji. Można to sprawdzić za pomocą polecenia:

```sh
egrep -c '(vmx|svm)' /proc/cpuinfo
```

Jeśli wynik jest większy niż 0, system wspiera wirtualizację.

### 2. Instalacja wymaganych pakietów

Zainstaluj podstawowe komponenty KVM oraz narzędzia do zarządzania środowiskiem wirtualnym:

```sh
sudo apt update
sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils -y
```

- `qemu-kvm` – podstawowy komponent wirtualizacji
- `libvirt-daemon-system` i `libvirt-clients` – narzędzia do zarządzania maszynami
- `bridge-utils` – umożliwia konfigurację mostów sieciowych

### 3. Weryfikacja wsparcia sprzętowego KVM

Upewnij się, że system obsługuje akcelerację KVM:

```sh
sudo kvm-ok
```

Wynik powinien zawierać informację, że KVM jest dostępny i może być używany.

### 4. Dodanie użytkownika do grup systemowych

Aby móc korzystać z KVM bez uprawnień administratora, dodaj użytkownika do odpowiednich grup:

```sh
sudo adduser "$USER" libvirt
sudo adduser "$USER" kvm
```

Po wykonaniu tej czynności należy się przelogować, aby zmiany zostały uwzględnione.

### 5. Sprawdzenie statusu usługi `libvirtd`

Zweryfikuj, czy usługa `libvirtd` działa poprawnie:

```sh
sudo systemctl status libvirtd
```

### 6. Zarządzanie usługą `libvirtd`

W razie potrzeby możesz włączyć lub wyłączyć usługę `libvirtd`:

```sh
sudo systemctl enable --now libvirtd
sudo systemctl disable --now libvirtd
```

### 7. Instalacja interfejsu graficznego

Zainstaluj `virt-manager` – graficzny interfejs do zarządzania maszynami wirtualnymi:

```sh
sudo apt install virt-manager
```

### 8. Uruchomienie `virt-manager`

Po instalacji program można uruchomić poleceniem:

```sh
virt-manager
```

### 9. Instalacja bibliotek pomocniczych libvirt

```sh 
sudo apt-get install libvirt-dev
```