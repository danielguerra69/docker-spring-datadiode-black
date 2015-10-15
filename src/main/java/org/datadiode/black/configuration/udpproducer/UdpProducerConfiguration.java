package org.datadiode.black.configuration.udpproducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import reactor.spring.context.config.EnableReactor;

/**
 * Created by marcel on 25-09-15.
 */
@Configuration
@EnableConfigurationProperties(UdpProducerConfiguration.UdpProducerConfigurationProperties.class)
@EnableReactor
public class UdpProducerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(UdpProducerConfiguration.class);

    // https://github.com/spring-projects/spring-integration/blob/master/spring-integration-ip/src/test/java/org/springframework/integration/ip/config/ParserUnitTests.java
    // https://github.com/JacobASeverson/udp-reactor/blob/master/src/main/java/com/objectpartners/udp/UdpReactorApp.java

    @Autowired
    UdpProducerConfigurationProperties udpProducerConfigurationProperties;

    @Bean
    UnicastSendingMessageHandler unicastSendingMessageHandler() {
        UnicastSendingMessageHandler unicastSendingMessageHandler =
                new UnicastSendingMessageHandler(
                        udpProducerConfigurationProperties.getHost(),
                        udpProducerConfigurationProperties.getPort());
        // SocketFrameHandler.socketFrameHandler().setUnicastSendingMessageHandler(unicastSendingMessageHandler);
        return unicastSendingMessageHandler;
    }

    // https://anonsvn.springframework.org/svn/spring-integration/branches/filetoentries/spring-integration-ip/src/test/java/org/springframework/integration/ip/udp/UdpChannelAdapterTests.java
/**
    @Bean
    UnicastReceivingChannelAdapter unicastReceivingChannelAdapter() throws Exception {
        QueueChannel channel = new QueueChannel(2);
        UnicastReceivingChannelAdapter adapter = new UnicastReceivingChannelAdapter(udpProducerConfigurationProperties.getPort()+1);
        adapter.setOutputChannel(channel);
        adapter.start();

        int l = 5000;

        byte[] msg = new byte[l];
        for (int i = 0; i < l; i++) {
            msg[i] = (byte) (i % 2 == 0 ? 'A' : 'B');
        }


        Message<byte[]> message = MessageBuilder.withPayload(msg).build();
        unicastSendingMessageHandler().handleMessageInternal(new GenericMessage<byte[]>(message.getPayload()));


        Message<byte[]> receivedMessage = (Message<byte[]>) channel.receive(l);
        log.info("received(" + receivedMessage.getPayload().length + "): " + new String(receivedMessage.getPayload()));
        adapter.stop();

        return adapter;
    }
 */

    @ConfigurationProperties(prefix = "application.datadiode.black")
    public static class UdpProducerConfigurationProperties {
        String host;
        int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

/**
    @Bean
    public DatagramServer<byte[], byte[]> datagramServer(Environment env) throws InterruptedException {
        final DatagramServer<byte[], byte[]> server = new DatagramServerSpec<byte[], byte[]>(NettyDatagramServer.class)
                .env(env)
                .listen(udpProducerConfigurationProperties.port)
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consumeInput(bytes -> log.info("received(" + bytes.length + "): " + new String(bytes)))
                .get();

        server.start().await();
        return server;
    }
 */


}
