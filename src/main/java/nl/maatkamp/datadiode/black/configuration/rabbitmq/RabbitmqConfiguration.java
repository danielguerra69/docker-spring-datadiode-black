package nl.maatkamp.datadiode.black.configuration.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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

/**
 * Created by marcel on 23-09-15.
 */
@Configuration
public class RabbitmqConfiguration implements MessageListener, BeanPostProcessor {
    final static String queueName = "spring-boot";
    private static final Logger log = LoggerFactory.getLogger(RabbitmqConfiguration.class);
    @Autowired
    AnnotationConfigApplicationContext context;




    // org.springframework.boot.autoconfigure.amqp.RabbitAnnotationDrivenConfiguration
    // org.springframework.amqp.rabbit.config.internalRabbitListenerEndpointRegistry
    // org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
    // org.springframework.boot.autoconfigure.amqp.RabbitProperties
    // org.springframework.amqp.rabbit.annotation.RabbitBootstrapConfiguration

    // RabbitListenerAnnotationBeanPostProcessor

    // RabbitListenerEndpointRegistry
    // * <p>Contrary to {@link MessageListenerContainer}s created manually, listener
    // * containers managed by registry are not beans in the application context and
    // * are not candidates for autowiring. Use {@link #getListenerContainers()} if
    //         * you need to access this registry's listener containers for management purposes.
    //         * If you need to access to a specific message listener container, use
    // * {@link #getListenerContainer(String)} with the id of the endpoint.


    // org.springframework.amqp.rabbit.connection.AbstractConnectionFactory.createBareConnection() {
    //   connection = new SimpleConnection(this.rabbitConnectionFactory.newConnection(this.executorService, this.addresses), this.closeTimeout) ->

    // com.rabbitmq.client.ConnectionFactory
    //      private int requestedFrameMax = DEFAULT_FRAME_MAX;
    //   public void setRequestedFrameMax(int requestedFrameMax) {this.requestedFrameMax = requestedFrameMax;}
    //   newConnection(ExecutorService executor, Address[] addrs) {
    //   ..
    //   ConnectionParams params = params(executor);
    //   AMQConnection conn = new AMQConnection(params, handler);
    // }

    // com.rabbitmq.client.impl.AMQConnection():
    //          this.requestedFrameMax = params.getRequestedFrameMax();

    // int channelMax = negotiateChannelMax(this.requestedChannelMax,connTune.getChannelMax());
    //  _channelManager = instantiateChannelManager(channelMax, threadFactory);
    //  int frameMax = negotiatedMaxValue(this.requestedFrameMax,connTune.getFrameMax());
    //  this._frameMax = frameMax;
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
        return BindingBuilder.bind(queue).to(exchange).with("");
    }

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
        rabbitAdminInternal().declareExchange(exchange());

        // send other rmq
        rabbitTemplateInternal().convertAndSend(exchange().getName(), "", body);

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
}
