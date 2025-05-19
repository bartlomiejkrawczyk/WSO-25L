---
title: "Wirtualne Sieci Obliczeniowe"
author: Bartłomiej Krawczyk, Mateusz Brzozowski
geometry: margin=2cm
header-includes:
    - \usepackage{float}
    - \floatplacement{figure}{H}
    - \renewcommand{\figurename}{Rysunek}
    - \usepackage{hyperref}
---

## Automatyzacja: Skalowalność i wysoka dostępność - serwisy bezstanowe

# Serwis bezstanowy

1. Weryfikacja czy maszyna się uruchomiła, jeżeli się nie udało powtarzamy.

```shell
$ virsh qemu-agent-command default {"execute":"guest-ping"}
{"return":{}}
```

2. Weryfikacja przydzielonego adresu IP.

```shell
$ virsh domifaddr default --source agent
 Name       MAC address          Protocol     Address
-------------------------------------------------------------------------------
 lo         00:00:00:00:00:00    ipv4         127.0.0.1/8
 -          -                    ipv6         ::1/128
 eth0       52:54:00:40:ec:c3    ipv4         192.168.122.231/24
 -          -                    ipv6         fe80::5054:ff:fe40:ecc3/64
```

3. Ustalenie adresu IP z puli dostępnych adresów danego menadżera.

```yaml
- name: Configure network
  hosts: "{{ current_ip }}"
  become: yes
  vars:
    interface: eth0
    ansible_become: false
    ansible_user: root
    ansible_ssh_pass: root

  tasks:
    - name: Configure static ip
      template:
        src: config/interfaces.j2
        dest: /etc/network/interfaces
      notify: Restart networking

  handlers:
    - name: Restart networking
      command: /etc/init.d/networking restart
      async: 1
      poll: 0
```

```sh
auto lo
iface lo inet loopback

auto eth0
iface eth0 inet static
    address {{ new_ip }}
    netmask 255.255.255.0
    gateway 192.168.122.1
```

```shell
$ ansible-playbook -i 192.168.122.231, ./playbooks/network.yaml -e current_ip=192.168.122.231 -e new_ip=192.168.122.13

PLAY [Configure network] *******************************************************

TASK [Gathering Facts] *********************************************************
ok: [192.168.122.231]

TASK [Configure static ip] *****************************************************
changed: [192.168.122.231]

RUNNING HANDLER [Restart networking] *******************************************
changed: [192.168.122.231]

PLAY RECAP *********************************************************************
192.168.122.231            : ok=3    changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

4. Weryfikacja czy wirtualna maszyna jest dostępna pod ustalonym adresem IP.

```shell
$ ping -c 1 192.168.122.13
PING 192.168.122.13 (192.168.122.13) 56(84) bytes of data.
64 bytes from 192.168.122.13: icmp_seq=1 ttl=64 time=1021 ms

--- 192.168.122.13 ping statistics ---
1 packets transmitted, 1 received, 0% packet loss, time 0ms
rtt min/avg/max/mdev = 1020.793/1020.793/1020.793/0.000 ms
```

5. Uruchomienie daemona z serwisem bezstanowym na porcie 8080.

```yaml
- name: Configure stateless service
  hosts: "{{ ip }}"
  become: yes
  vars:
    interface: eth0
    ansible_become: false
    ansible_user: root
    ansible_ssh_pass: root

  tasks:
    - name: Start stateless daemon
      ansible.builtin.shell: |
        nohup java -jar /stateless.jar --server.port={{ port }} > stateless.log 2>&1 &
      async: 1
      poll: 0
```

```shell
$ ansible-playbook -i 192.168.122.13, ./playbooks/stateless.yaml -e ip=192.168.122.13 -e port=8080

PLAY [Configure stateless service] *********************************************

TASK [Gathering Facts] *********************************************************
ok: [192.168.122.13]

TASK [Start stateless daemon] **************************************************
changed: [192.168.122.13]

PLAY RECAP *********************************************************************
192.168.122.13             : ok=2    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

# Load balancer

Punktu 1-4. tak samo jak w serwisie bezstanowym.

5. Uruchomienie daemon z heart beatem na porcie 8080.

```yaml
- name: Configure heartbeat service
  hosts: "{{ ip }}"
  become: yes
  vars:
    interface: eth0
    ansible_become: false
    ansible_user: root
    ansible_ssh_pass: root

  tasks:
    - name: Start heartbeat daemon
      ansible.builtin.shell: |
        nohup java -jar /heartbeat.jar --server.port={{ port }} > heartbeat.log 2>&1 &
      async: 1
      poll: 0
```


```shell
$ ansible-playbook -i 192.168.122.12, ./playbooks/heart_beat.yaml -e ip=192.168.122.12 -e port=8080

PLAY [Configure heartbeat service] *********************************************

TASK [Gathering Facts] *********************************************************
ok: [192.168.122.12]

TASK [Start heartbeat daemon] **************************************************
changed: [192.168.122.12]

PLAY RECAP *********************************************************************
192.168.122.12             : ok=2    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

6. Uruchomienie daemona z load balancerem na porcie 80.

```yaml
- name: Configure load balancer service
  hosts: "{{ ip }}"
  become: yes
  vars:
    interface: eth0
    ansible_become: false
    ansible_user: root
    ansible_ssh_pass: root

  tasks:
    - name: Copy nginx.conf to the server
      copy:
        src: config/nginx.conf
        dest: /etc/nginx/nginx.conf
        backup: yes

    - name: Reload Nginx
      service:
        name: nginx
        state: reloaded
```

```shell
$ ansible-playbook -i 192.168.122.12, ./playbooks/load_balancer.yaml -e ip=192.168.122.12

PLAY [Configure load balancer service] *****************************************

TASK [Gathering Facts] *********************************************************
ok: [192.168.122.12]

TASK [Copy nginx.conf to the server] *******************************************
changed: [192.168.122.12]

TASK [Reload Nginx] ************************************************************
changed: [192.168.122.12]

PLAY RECAP *********************************************************************
192.168.122.12             : ok=3    changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

![Swagger z menadżera](./docs/img/manager.png)

![Swagger z serwisu bezstanowego](./docs/img/stateless.png)

![Swagger z load balancera](./docs/img/load_balancer.png)