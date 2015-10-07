package nl.maatkamp.datadiode.black;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.config.EnableIntegration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@SpringBootApplication
@EnableIntegration
@EnableAutoConfiguration(exclude={RabbitAutoConfiguration.class})
public class DatadiodeBlackStarter {
    private static final Logger log = LoggerFactory.getLogger(DatadiodeBlackStarter.class);

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext configurableApplicationContext = new SpringApplicationBuilder(DatadiodeBlackStarter.class).web(false).run(args);
    }
}
