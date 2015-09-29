package nl.maatkamp.datadiode.black.configuration.udpproducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.util.SocketUtils;
import reactor.core.Environment;
import reactor.io.encoding.StandardCodecs;
import reactor.net.netty.udp.NettyDatagramServer;
import reactor.net.udp.DatagramServer;
import reactor.net.udp.spec.DatagramServerSpec;
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

    @Autowired
    UdpProducerConfigurationProperties udpProducerConfigurationProperties;
    @Bean
    UnicastSendingMessageHandler unicastSendingMessageHandler() {

        UnicastSendingMessageHandler unicastSendingMessageHandler =
                new UnicastSendingMessageHandler(
                        udpProducerConfigurationProperties.getHost(),
                        udpProducerConfigurationProperties.getPort());

        return unicastSendingMessageHandler;
    }

    @ConfigurationProperties(prefix = "application.datadiode.black")
    public static class UdpProducerConfigurationProperties {
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

        String host;
        int port;

    }


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



}
