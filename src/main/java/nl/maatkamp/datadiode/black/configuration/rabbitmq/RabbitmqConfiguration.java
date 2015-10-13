package nl.maatkamp.datadiode.black.configuration.rabbitmq;

import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

/**
 * Created by marcel on 23-09-15.
 */
@Configuration
@EnableScheduling
public class RabbitmqConfiguration implements ChannelAwareMessageListener, BeanPostProcessor {

    final static String queueName = "spring-boot";
    private static final Logger log = LoggerFactory.getLogger(RabbitmqConfiguration.class);

    @Autowired
    AnnotationConfigApplicationContext context;

    String DATA_DIODE_QUEUENAME_SUFFIX = ".dd";

    List<String> standardExchanges =
            Arrays.asList(
                    "amq.direct",
                    "amq.fanout",
                    "amq.headers",
                    "amq.match",
                    "amq.rabbitmq.log",
                    "amq.rabbitmq.trace",
                    "amq.topic");


    @Autowired
    Environment environment;

    @Autowired
    UnicastSendingMessageHandler unicastSendingMessageHandler;

    @Bean
    DefaultClassMapper defaultClassMapper() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        return defaultClassMapper;
    }

    @Bean
    public ConnectionFactory connectionFactoryExternal() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(environment.getProperty("spring.datadiode.rabbitmq.external.host"));
        connectionFactory.setPort(environment.getProperty("spring.datadiode.rabbitmq.external.port", Integer.class));
        connectionFactory.setUsername(environment.getProperty("spring.datadiode.rabbitmq.external.username"));
        connectionFactory.setPassword(environment.getProperty("spring.datadiode.rabbitmq.external.password"));
        connectionFactory.createConnection();
        log.info("rabbitmq(" + connectionFactory.getHost() + ":" + connectionFactory.getPort() + ").channelCacheSize(" + connectionFactory.getChannelCacheSize() + ")");
        return connectionFactory;
    }

    @Bean
    public ConnectionFactory connectionFactoryInternal() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(environment.getProperty("spring.datadiode.rabbitmq.internal.host"));
        connectionFactory.setPort(environment.getProperty("spring.datadiode.rabbitmq.internal.port", Integer.class));
        connectionFactory.setUsername(environment.getProperty("spring.datadiode.rabbitmq.internal.username"));
        connectionFactory.setPassword(environment.getProperty("spring.datadiode.rabbitmq.internal.password"));
        connectionFactory.createConnection();
        log.info("rabbitmq(" + connectionFactory.getHost() + ":" + connectionFactory.getPort() + ").channelCacheSize(" + connectionFactory.getChannelCacheSize() + ")");
        return connectionFactory;
    }

    @Bean
    RabbitTemplate rabbitTemplateExternal() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactoryExternal());
        return rabbitTemplate;
    }

    @Bean
    RabbitTemplate rabbitTemplateInternal() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactoryInternal());
        return rabbitTemplate;
    }

    @Bean
    RabbitAdmin rabbitAdminExternal() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactoryExternal());
        return rabbitAdmin;
    }

    @Bean
    RabbitAdmin rabbitAdminInternal() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactoryInternal());
        return rabbitAdmin;
    }

    // https://github.com/spring-projects/spring-amqp/blob/master/spring-rabbit/src/test/java/org/springframework/amqp/rabbit/core/RabbitManagementTemplateTests.java
    // List<Exchange> list = this.template.getExchanges();
    // public static BrokerRunning brokerAndManagementRunning = BrokerRunning.isBrokerAndManagementRunning();
    // https://github.com/spring-projects/spring-integration/blob/master/spring-integration-ip/src/main/java/org/springframework/integration/ip/config/UdpOutboundChannelAdapterParser.java
    @Bean
    RabbitManagementTemplate rabbitManagementTemplate() {
        log.info("template: " + rabbitTemplateExternal());
        RabbitManagementTemplate rabbitManagementTemplate = new RabbitManagementTemplate(
                "http://"+environment.getProperty("spring.datadiode.rabbitmq.external.host")+":1"+environment.getProperty("spring.datadiode.rabbitmq.external.port", Integer.class)+"/api/",
                environment.getProperty("spring.datadiode.rabbitmq.external.username"),
                environment.getProperty("spring.datadiode.rabbitmq.external.password")
        );
        log.info("exchanges: " + rabbitManagementTemplate.getClient().getExchanges());
        return rabbitManagementTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    void checkForNewExchanges() {

        for (Exchange exchange : rabbitManagementTemplate().getExchanges()) {
            log.info("exchange(" + exchange.getName() + "/" + exchange.getType() + ").isDurable(" + exchange.isDurable() + ").isAutoDelete(" + exchange.isAutoDelete() + ").args(" + exchange.getArguments() + ")");
            String queueName = exchange.getName() + DATA_DIODE_QUEUENAME_SUFFIX;

            if (exchange.getName() != null && !standardExchanges.contains(exchange.getName())) {
                for (Binding binding : rabbitManagementTemplate().getBindings()) {
                    if (binding.getExchange().equals(exchange.getName())) {

                        if (binding.getDestination().equals(queueName)) {
                            // binding exists, so queue exists

                            log.info("binding exists: binding(" + binding.getExchange() + ").destination(" + binding.getDestination() + ").destinationType(" + binding.getDestinationType() + ").routingKey(" + binding.getRoutingKey() + ").isDestinationQueue(" + binding.isDestinationQueue() + ").args(" + binding.getArguments() + ")");
                        } else {
                            Queue bindQueue = null;

                            for (Queue queue : rabbitManagementTemplate().getQueues()) {
                                // queue exists, bind
                                if (queue.getName().equals(queueName)) {
                                    bindQueue = queue;
                                }
                            }

                            if (bindQueue == null) {
                                // queue does not exists
                                Queue queue = new Queue(queueName);
                                BindingBuilder.bind(queue).to(exchange).with("");
                                log.info("new queue and new binding(" + binding.getExchange() + ").destination(" + binding.getDestination() + ").destinationType(" + binding.getDestinationType() + ").routingKey(" + binding.getRoutingKey() + ").isDestinationQueue(" + binding.isDestinationQueue() + ").args(" + binding.getArguments() + ")");
                            } else {
                                BindingBuilder.bind(bindQueue).to(exchange).with("");
                                log.info("queue exists: new binding(" + binding.getExchange() + ").destination(" + binding.getDestination() + ").destinationType(" + binding.getDestinationType() + ").routingKey(" + binding.getRoutingKey() + ").isDestinationQueue(" + binding.isDestinationQueue() + ").args(" + binding.getArguments() + ")");
                            }

                            // listener to queue
                            SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
                            simpleMessageListenerContainer.setConnectionFactory(connectionFactoryExternal());
                            simpleMessageListenerContainer.setQueueNames(queueName);
                            simpleMessageListenerContainer.setMessageListener(this);

                        }
                    }
                }
            }
        }

        /**
         for(Queue queue : rabbitManagementTemplate().getQueues()) {
         log.info("queue("+queue.getName()+").isDurable("+queue.isDurable()+").isAutoDelete("+queue.isAutoDelete()+").isExclusive("+queue.isExclusive()+").args("+queue.getArguments()+")");
         }

         for(Binding binding : rabbitManagementTemplate().getBindings()) {
         log.info("binding("+binding.getExchange()+").destination("+binding.getDestination()+").destinationType("+binding.getDestinationType()+").routingKey("+binding.getRoutingKey()+").isDestinationQueue("+binding.isDestinationQueue()+").args("+binding.getArguments()+")");
         }
         */
    }

    /**
    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    Exchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
    return )BindingBuilder.bind(queue).to(exchange).with("";
    }
     */

    @Bean
    SimpleMessageListenerContainer simpleMessageListenerContainerExternal() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactoryExternal());
        container.setQueueNames(queueName);
        container.setMessageListener(this);
        return container;
    }



    public void onMessage(Message message) {
        String body = new String(message.getBody());
        log.info("body("+body.length()+"): (" + body + ")..");

        // create exchange
        // rabbitAdminInternal().declareExchange(exchange());

        // send other rmq
        // rabbitTemplateInternal().convertAndSend(exchange().getName(), "", body);

        // unicastSendingMessageHandler.handleMessageInternal(new GenericMessage<byte[]>(message.getBody()));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        log.info("postProcessBeforeInitialization: bean(" + beanName + ")");
        return bean;

    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        log.info(ReflectionToStringBuilder.toString(message));
    }
}
