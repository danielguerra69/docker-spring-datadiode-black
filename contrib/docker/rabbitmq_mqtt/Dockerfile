FROM rabbitmq:management

RUN rabbitmq-plugins enable --offline \
  rabbitmq_federation \
  rabbitmq_federation_management \
  rabbitmq_shovel \
  rabbitmq_shovel_management \
  rabbitmq_mqtt

RUN apt-get update && apt-get dist-upgrade -f
# RUN apt-get install -fy build-essential zip wget git python xsltproc erlang-dev erlang-src erlang-base erlang-base-hipe
RUN apt-get install -fy build-essential zip wget git python xsltproc erlang-dev 

RUN mkdir /rabbitmq
WORKDIR /rabbitmq

RUN git clone https://github.com/rabbitmq/rabbitmq-public-umbrella.git
WORKDIR /rabbitmq/rabbitmq-public-umbrella

RUN git clone https://github.com/rabbitmq/rabbitmq-server.git
RUN git clone https://github.com/rabbitmq/rabbitmq-erlang-client.git
RUN git clone https://github.com/rabbitmq/rabbitmq-codegen.git
RUN git clone https://github.com/rabbitmq/rabbitmq-auth-mechanism-ssl
RUN git clone https://github.com/tonyg/udp-exchange.git

WORKDIR /rabbitmq//rabbitmq-public-umbrella/udp-exchange

RUN make

RUN cp -r build/app/rabbit_udp_exchange-0.0.0 /usr/lib/rabbitmq/lib/rabbitmq_server-*/plugins
RUN rabbitmq-plugins enable --offline rabbit_udp_exchange

EXPOSE 5672 15672 25672

