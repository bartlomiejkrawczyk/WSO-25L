

sudo ip link add name wso type bridge

sudo ip link set dev wso up

sudo ip link add link wso name wso.100 type vlan id 100

sudo ip link set dev wso.100 up

# Better

sudo ip link add link enx000ec6b01bc1 name vlan200 type vlan id 200

sudo ip link set dev eth0.200 up

sudo brctl addif wso eth0.200

# Assign network

sudo ip addr add 192.168.10.1/24 dev wso




# 2 try

Netplan config for BK:
```yaml
network:
  version: 2
  ethernets:
    eno2: {}
  bridges:
    br0:
      interfaces: [eno2]
      dhcp4: no
      addresses: [192.168.10.1/24]
```

Netplan config for MB:
```yaml
network:
  version: 2
  ethernets:
    enx000ec6b01bc1: {}
  bridges:
    br0:
      interfaces: [enx000ec6b01bc1]
      dhcp4: no
      addresses: [192.168.10.2/24]
```

```shell
sudo netplan apply
```

```shell
brctl show
```


Check if physical NIC is added as a port to the bridge:
```shell
bridge link show
```

If not attach it:
```shell
sudo ip link set eno2 down
sudo ip link set eno2 master br0
sudo ip link set eno2 up
```

Then:
```shell
sudo ip addr flush dev eno2
```
