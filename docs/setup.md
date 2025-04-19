
Image:

- alipine linux - virtual:
- https://alpinelinux.org/downloads/

Login: root
Password: {no password}

```shell
setup-alpine -q
```

```shell
Select keyboard layout: pl
Select variant: pl
```

```shell
setup-disk -m sys /dev/vda
```

```shell
poweroff
```

```shell
passwd root
```

```shell
New password: root
Retype password: root
```

```shell
apk add nano
```

```shell
nano /etc/apk/repositories
```

```shell
/media/cdrom/apks
http://dl-cdn.alpinelinux.org/alpine/v3.21/main
#http://dl-cdn.alpinelinux.org/alpine/v3.21/community
```
```shell
/media/cdrom/apks
http://dl-cdn.alpinelinux.org/alpine/v3.21/main
http://dl-cdn.alpinelinux.org/alpine/v3.21/community
```

```
ctrl+s
ctrl+x
```

```shell
apk update
```

# Java

```shell
apk add openjdk21
```
# Secure Shell


```shell
apk add openssh-server
cp /etc/ssh/sshd_config /etc/ssh/ssh_config.backup
```
```shell
ssh-keygen -A
```

- `-A` - Generate host keys of all default key types (rsa, ecdsa,
  and ed25519) if they do not already exist.  The host keys
  are generated with the default key file path, an empty
  passphrase, default bits for the key type, and default
  comment.  If -f has also been specified, its argument is
  used as a prefix to the default path for the resulting
  host key files.  This is used by /etc/rc to generate new
  host keys.

Uncomment defaults:
```shell
nano /etc/ssh/ssh_config
```

```shell
#PermitRootLogging prohibit-password
```

```shell
PermitRootLogging yes
```

```shell
sshd -t -f /etc/ssh/sshd_config
```

- `-t` - Test mode. Only check the validity of the configuration file and sanity of the keys. This is useful for updating sshd reliably as configuration options may change.
- `-f {config_file}` - Specifies the name of the configuration file. The default is /etc/ssh/sshd_config. sshd refuses to start if there is no configuration file.

```shell
service sshd restart
```

Register sshd as a service
```shell
rc-update add sshd default
```

```shell
rc-service sshd start
```

# Connect to the machine

```shell
$ ssh root@192.168.122.26
Welcome to Alpine!

The Alpine Wiki contains a large amount of how-to guides and general
information about administrating Alpine systems.
See <https://wiki.alpinelinux.org/>.

You can setup the system with the command: setup-alpine

You may change this message by editing /etc/motd.

alpine:~#
```

# Start service

```shell
scp ./stateless/build/libs/stateless.jar root@192.168.122.26:/stateless.jar
```

Test using:
```shell
java -jar /stateless.jar
```

```
GET http://192.168.122.26:8080/random/boolean?probability=1.0
```

To start daemon:
```shell
nohup java -jar /stateless.jar > stateless.log 2>&1 &
```

Verify:
```shell
ps aux | grep stateless.jar
```

# Load balancer

```shell
apk add nginx
```

```shell
rc-update add nginx default
rc-service nginx start
```

```shell
rc-service nginx status
curl http://localhost
http://<your-vm-ip>
```

edit config:
```shell
nano /etc/nginx/nginx.conf
```

Reload after modifications:
```shell
rc-service nginx reload
```
