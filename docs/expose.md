# Wystawienie vm-ki z load-balancerem na zewnątrz

```shell
# Forward host port 2222 to VM port 22
sudo iptables -t nat -A PREROUTING -d 192.168.99.198 -p tcp --dport 2222 -j DNAT --to-destination 192.168.122.13:22
sudo iptables -t nat -A POSTROUTING -j MASQUERADE
```
