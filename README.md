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

