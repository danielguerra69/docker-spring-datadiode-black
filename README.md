# AMQP over data diode (outside)

Reference Implementation of AMQP with RabbitMQ over a data diode where messages are as big as udp packets to avoid the problem of out-of-order arriving packets.

# Start 

Start rabbitmq with mqtt enabled to prove IoT 
```
cd contrib/rabbitmq
docker-compose up
```

Start oudside 
```
gradle run
```


http://www.cyberciti.biz/faq/rhel-centos-debian-ubuntu-jumbo-frames-configuration/


To fake a data diode with jumbo frames enabled:
```
sudo ethtool -K eth0 sg off
sudo ifconfig eth0 mtu 9000
sudo ifconfig eth0 mtu 16110
ip link show eth0

black
socat UDP4-RECVFROM:1234,fork udp-datagram:172.16.99.255:1235,broadcast

red
socat UDP4-RECVFROM:1235,fork UDP4-SENDTO:172.16.128.4:1234
```
