package nl.maatkamp.datadiode.black;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit

class DatadiodeBlackStarter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatadiodeBlackStarter.class);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DatadiodeBlackStarter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Waiting five seconds...");

    }
}
