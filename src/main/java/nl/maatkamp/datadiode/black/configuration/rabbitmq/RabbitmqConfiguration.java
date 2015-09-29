package nl.maatkamp.datadiode.black.configuration.rabbitmq;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.support.GenericMessage;



import javax.annotation.PostConstruct;

/**
 * Created by marcel on 23-09-15.
 */
@Configuration
public class RabbitmqConfiguration implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqConfiguration.class);

    final static String queueName = "spring-boot";


    @Bean
    public DefaultClassMapper defaultClassMapper() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        return defaultClassMapper;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(environment.getProperty("spring.rabbitmq.host"));

        connectionFactory.setHost(environment.getProperty("spring.rabbitmq.host"));
        connectionFactory.setPort(environment.getProperty("spring.rabbitmq.port", Integer.class));
        connectionFactory.setUsername(environment.getProperty("spring.rabbitmq.username"));
        connectionFactory.setPassword(environment.getProperty("spring.rabbitmq.password"));

        log.info("rabbitmq(" + connectionFactory.getHost() + ":" + connectionFactory.getPort() + ").channelCacheSize(" + connectionFactory.getChannelCacheSize() + ")");

        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory());
        return amqpAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        return rabbitTemplate;
    }


    @Autowired
    AnnotationConfigApplicationContext context;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Environment environment;

    @Autowired
    UnicastSendingMessageHandler unicastSendingMessageHandler;

    // https://github.com/spring-projects/spring-amqp/blob/master/spring-rabbit/src/test/java/org/springframework/amqp/rabbit/core/RabbitManagementTemplateTests.java
    // List<Exchange> list = this.template.getExchanges();
    // public static BrokerRunning brokerAndManagementRunning = BrokerRunning.isBrokerAndManagementRunning();
    // https://github.com/spring-projects/spring-integration/blob/master/spring-integration-ip/src/main/java/org/springframework/integration/ip/config/UdpOutboundChannelAdapterParser.java
    @Bean
    RabbitManagementTemplate rabbitManagementTemplate() {
        log.info("template: " + rabbitTemplate);


        RabbitManagementTemplate rabbitManagementTemplate = new RabbitManagementTemplate(
                "http://"+environment.getProperty("spring.rabbitmq.host")+":1"+environment.getProperty("spring.rabbitmq.port", Integer.class)+"/api/",
                environment.getProperty("spring.rabbitmq.username"),
                environment.getProperty("spring.rabbitmq.password")
        );
        return rabbitManagementTemplate;
    }

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(this);
        return container;
    }

    public void onMessage(Message message) {
        String body = new String(message.getBody());
        log.info("body("+body.length()+"): (" + body + ")..");
        unicastSendingMessageHandler.handleMessageInternal(new GenericMessage<byte[]>(message.getBody()));
    }
}
