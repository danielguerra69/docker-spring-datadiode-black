package org.datadiode.black.configuration.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import org.datadiode.model.MessageWithPayload;
import org.datadiode.model.event.sensor.SensorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.SerializationUtils;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
public class SensorEventListener implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(SensorEventListener.class);

    @Autowired
    UnicastSendingMessageHandler unicastSendingMessageHandler;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();

        String exchange = messageProperties.getReceivedExchange();
        byte[] body = message.getBody();

        SensorEvent sensorEvent = (SensorEvent) rabbitTemplate.getMessageConverter().fromMessage(message);

        byte[] payload = SerializationUtils.serialize(new MessageWithPayload(exchange, MessageWithPayload.ExchangeType.HeadersExchange, body));

        unicastSendingMessageHandler.handleMessageInternal(new GenericMessage<byte[]>(payload));
        log.info("msg.payload(" + payload.length + ").body(" + body.length + ")");
    }
}
