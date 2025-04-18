
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

Skalowalność i wysoka dostępność

Funkcje:

- automatyzacja skalowania usługi (dodawanie/usuwanie VM),
- równoważenie obciążenia (np. haproxy)
- zwiększanie niezawodności.

Sprawdzić jaki wpływ na obsługę ma awaria jednej/większej liczby maszyn. Serwis bezstanowy.

Automatyzacja zarządzania maszynami wirtualnymi:

- Zestaw skryptów/program ułatwiający zarządzanie VM/klastrem
- Opracować odpowiedni scenariusz
- Użycie narzędzi!

## Scenariusz

<!-- – Scenariusz – doprecyzowanie zadania: jaki problem rozwiązujecie -->

- wiele serwisów bezstanowych, które zwracają losową wartość (bool, int, float, double)
- całych ruch przechodzi przez load-balancer (nginx), postawiony na oddzielnej maszynie wirtualnej, jeden dla każdego serwera w klastrze

```{.mermaid scale=1}
%%{ init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#d0e6f6",
    "primaryBorderColor": "#aacbe3",
    "primaryTextColor": "#000000",
    "secondaryColor": "#fce5cd",
    "tertiaryColor": "#d5e8d4",
    "lineColor": "#bfbfbf",
    "fontFamily": "Inter, Segoe UI, sans-serif",
    "fontSize": "14px",
    "edgeLabelBackground": "#ffffff"
  }
}}%%
flowchart LR
    c1(Client 1) --> LB(Load Balancer)
    c2(Client 2) --> LB(Load Balancer)
    c3(Client 3)  --> LB(Load Balancer)
    LB(Load Balancer) --> s1(Server 1)
    LB(Load Balancer) --> s2(Server 2)
    LB(Load Balancer) --> s3(Server 3)
```

- na każdym laptopie w naszym klastrze uruchomiony jest daemon manager, który zarządza maszynami wirtualnymi
- każdy manager posiada endpointy do utworzenia i usuwania maszyny wirtualnej z serwisem bezstanowym, do aktualizacji serwisów z innych menadżerów
- menadżerzy mają skonfigurowane swoje adresy ip, propagują między sobą informacje o uruchomionych maszynach

```{.mermaid scale=3}
%%{ init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#d0e6f6",
    "primaryBorderColor": "#aacbe3",
    "primaryTextColor": "#000000",
    "secondaryColor": "#fce5cd",
    "tertiaryColor": "#d5e8d4",
    "lineColor": "#bfbfbf",
    "fontFamily": "Inter, Segoe UI, sans-serif",
    "fontSize": "14px",
    "edgeLabelBackground": "#ffffff"
  }
}}%%
flowchart LR
    subgraph lan[LAN]
        admin[Admin]

        subgraph host1[Host 1]
            manager1[Server Manager]
            subgraph virt1[KVM/QUEMU]
                lb1[Load Balancer]
                sl1[Stateless 1]
                sl2[Stateless 2]
            end
        end

        subgraph host2[Host 2]
            manager2[Server Manager]
            subgraph virt2[KVM/QUEMU]
                lb2[Load Balancer]
                sl3[Stateless 1]
                sl4[Stateless 2]
            end
        end

        manager1 <--> manager2

        admin --request new vm--> manager1
        admin --request new vm--> manager2

        manager1 --create/delete vm--> virt1
        manager2 --create/delete vm--> virt2

        manager1 --heart beat--> lb1
        manager1 --heart beat--> sl1
        manager1 --heart beat--> sl2

        manager2 --heart beat--> lb2
        manager2 --heart beat--> sl3
        manager2 --heart beat--> sl4

        lb1 --> sl1
        lb1 --> sl2
        lb1 --> sl3
        lb1 --> sl4

        lb2 --> sl1
        lb2 --> sl2
        lb2 --> sl3
        lb2 --> sl4
    end
    c1[Client1]
    c1 --request number--> lb1
    c1 --request number--> lb2
```

## Ryzyko

<!-- – Ryzyko: jakie przewidujecie problemy, jakie metody poradzenia sobie z nimi -->

- Maszyna z serwisem bezstanowym umiera:

Menadżer przez cały czas działa serwisu utrzymuje połączenie heartbeat, serwis co jakiś czas wysyła wiadomość zwrotną o treści: `data: {"status": "OK"}` sygnalizującą poprawne działanie serwisu. Jeśli menadżer nie wykryje przez określony czas połączenia, kilkukrotnie próbuje nawiązanie połączenia, jeśli się to nie uda to usuwamy taką maszynę i menadżer stawia nową maszynę w jej miejsce.

Adres ip nowej maszyny jest taki sam jak adres ip starej maszyny. W przypadku błędnej odpowiedzi serwera, odpytuje kolejną maszynę (proxy_next_upstream error).

- Maszyna z load balancerem umiera

Heartbeat w ramach load balancera działa podobnie jak w serwisie bezstanowym

Mamy jeden publiczny adres ip, który jest na starcie przypisany do jednego menadżera, jeśli menadżer ma problem ze swoim load balancerem, to mianuje drugiego menadżera głównym i przypisuje do niego publiczny adres ip, a nasz load balancer wyłączamy i próbujemy postawić na nowo z innym adresem ip.

## Sprzet
<!-- sprzęt: np. 2 laptopy -->

Dwa laptopy z systemem Ubuntu w tej samej sieci wifi.

## Narzędzia

<!-- narzędzia, oprogramowanie: jaki wirtualizator, jakie dodatkowe pakiety, ew. jak -->

- Wirtualizator: KVM
- Serwisy: Spring w kotlinie
- Zarządzanie konfiguracją maszyn: Ansible
- Skrypty testowe: Bash

## Plan testów

<!-- plan testów uwzględniający ograniczenia sprzętu -->

Skrypt który odpytuje load balancer, działa z pewnym opóźnieniem, cały czas dopóki nie zostanie zatrzymany.
W oddzielnej konsoli wyłączymy maszynę bezstanową/ load balancer, podobnie na drugim laptopie i monitorujemy jak zachowuje się serwis, czy wszystkie odpowiedzi zwracane są poprawne.