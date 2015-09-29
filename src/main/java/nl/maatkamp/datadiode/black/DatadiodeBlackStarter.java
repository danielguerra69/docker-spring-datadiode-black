package nl.maatkamp.datadiode.black;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableRabbit
public class DatadiodeBlackStarter {

    private static final Logger log = LoggerFactory.getLogger(DatadiodeBlackStarter.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(DatadiodeBlackStarter.class).web(false).run(args);
    }

}
