package org.datadiode.black.configuration.rabbitmq;

import org.datadiode.black.configuration.rabbitmq.listener.SensorEventListener;
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
public class RabbitmqConfiguration implements BeanPostProcessor {

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
    int index = 1000;
    int count = 1;

    @Bean
    DefaultClassMapper defaultClassMapper() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        return defaultClassMapper;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
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
    RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        log.info("converter: " + rabbitTemplate.getMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        return rabbitAdmin;
    }

    // https://github.com/spring-projects/spring-amqp/blob/master/spring-rabbit/src/test/java/org/springframework/amqp/rabbit/core/RabbitManagementTemplateTests.java
    // List<Exchange> list = this.template.getExchanges();
    // public static BrokerRunning brokerAndManagementRunning = BrokerRunning.isBrokerAndManagementRunning();
    // https://github.com/spring-projects/spring-integration/blob/master/spring-integration-ip/src/main/java/org/springframework/integration/ip/config/UdpOutboundChannelAdapterParser.java
    @Bean
    RabbitManagementTemplate rabbitManagementTemplate() {
        log.info("template: " + rabbitTemplate());
        RabbitManagementTemplate rabbitManagementTemplate = new RabbitManagementTemplate(
                "http://" + environment.getProperty("spring.datadiode.rabbitmq.external.host") + ":1" + environment.getProperty("spring.datadiode.rabbitmq.external.port", Integer.class) + "/api/",
                environment.getProperty("spring.datadiode.rabbitmq.external.username"),
                environment.getProperty("spring.datadiode.rabbitmq.external.password")
        );
        log.info("exchanges: " + rabbitManagementTemplate.getClient().getExchanges());
        return rabbitManagementTemplate;
    }

    @Bean
    Exchange testExchange() {
        Exchange exchange = new FanoutExchange("testExchange");
        return exchange;
    }

    // @Scheduled(fixedDelay = 200)
    void sendTestMessages() {
        String msg = "";
        for(int i = 0; i<index; i++) {
            msg = msg + "X";
        }
        rabbitTemplate().convertAndSend(testExchange().getName(), null, msg);

        log.info("msg(" + count + ")");
        // index = index + 100;
        count = count + 1;
    }

    @Scheduled(fixedDelay = 5000)
    void checkForNewExchanges() {

        for (Exchange exchange : rabbitManagementTemplate().getExchanges()) {
            if (exchange.getName() != null && exchange.getName() != "" && !standardExchanges.contains(exchange.getName())) {
                // log.info("exchange(" + exchange.getName() + "/" + exchange.getType() + ").isDurable(" + exchange.isDurable() + ").isAutoDelete(" + exchange.isAutoDelete() + ").args(" + exchange.getArguments() + ")");

                String queueName = exchange.getName() + DATA_DIODE_QUEUENAME_SUFFIX;
                Queue bindQueue = null;
                boolean bindingExists = false;

                for (Binding binding : rabbitManagementTemplate().getBindings()) {
                    if (binding.getExchange().equals(exchange.getName())) {

                        if (binding.getDestination().equals(queueName)) {
                            // binding exists, so queue exists
                            bindingExists = true;
                            // log.info("binding exists: binding(" + binding.getExchange() + ").destination(" + binding.getDestination() + ").destinationType(" + binding.getDestinationType() + ").routingKey(" + binding.getRoutingKey() + ").isDestinationQueue(" + binding.isDestinationQueue() + ").args(" + binding.getArguments() + ")");
                        } else {

                            for (Queue queue : rabbitManagementTemplate().getQueues()) {
                                // queue exists, bind
                                if (queue.getName().equals(queueName)) {
                                    log.info("found " + queueName);
                                    bindQueue = queue;
                                }
                            }
                        }
                    }
                }


                if (!bindingExists) {
                    if (bindQueue == null) {
                        log.info("create " + queueName);
                        bindQueue = new Queue(queueName);
                        rabbitAdmin().declareQueue(bindQueue);
                    }
                    log.info("exchange(" + exchange.getName() + ") -> queue(" + bindQueue + ")");
                    BindingBuilder.bind(bindQueue).to(exchange).with("");
                    rabbitAdmin().declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchange.getName(), "", null));
                }

                // queue exists, binding exists, listen!

                SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
                simpleMessageListenerContainer.setConnectionFactory(connectionFactory());

                MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(sensorEventListener());

                simpleMessageListenerContainer.setQueueNames(queueName);
                simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
                simpleMessageListenerContainer.start();
            }
        }
    }

    @Bean
    SensorEventListener sensorEventListener() {
        SensorEventListener sensorEventListener = new SensorEventListener();
        return sensorEventListener;
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


}
