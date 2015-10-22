package org.datadiode.black.configuration.sensor;

import org.datadiode.model.event.GeoLocation;
import org.datadiode.model.event.sensor.Sensor;
import org.datadiode.model.event.sensor.SensorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
@Configuration
public class SensorConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SensorConfiguration.class);


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Exchange testExchange;
    @Autowired
    Cipher cipherServer;
    @Autowired
    SecretKey symmetricalKey;

    @Bean
    GeoLocation geoLocationAmsterdam() {
        return new GeoLocation(4.899431, 52.379189);
    }

    @Bean
    Sensor sensor() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        Sensor sensor = new Sensor("SomeSensorType", 1, geoLocationAmsterdam());
        return sensor;
    }

    @Scheduled(fixedDelay = 200)
    void sendTestMessages() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SensorEvent sensorEvent = sensor().generateEvent();


// SENSOR.KEYS == SERVER.KEYS!!

        cipherServer.init(Cipher.ENCRYPT_MODE, sensor().getPublicKey());
        byte[] result1 = cipherServer.doFinal(symmetricalKey.getEncoded());

        cipherServer.init(Cipher.WRAP_MODE, sensor().getPublicKey());
        byte[] result2 = cipherServer.wrap(symmetricalKey);

        // crypt contents and base64 the result as per http://stackoverflow.com/questions/3451670/java-aes-and-using-my-own-key

        cipherServer.init(Cipher.UNWRAP_MODE, sensor().getPrivateKey());
        SecretKey sessionKey1 = (SecretKey) cipherServer.unwrap(result1, "AES",
                Cipher.SECRET_KEY);

        cipherServer.init(Cipher.DECRYPT_MODE, sensor().getPrivateKey());
        SecretKey sessionKey2 = new SecretKeySpec(cipherServer.doFinal(result2), "AES");

        log.info("eq: " + Arrays.equals(sessionKey1.getEncoded(),
                sessionKey2.getEncoded()));

/**
 EncryptedSensorEvent encryptedSensorEvent = new EncryptedSensorEvent(
 cipherRSA().doFinal(symmetricalKey().getEncoded()),
 cipher.doFinal(SerializationUtils.serialize(sensorEvent)));
 */
        rabbitTemplate.convertAndSend(testExchange.getName(), null, "");
    }
}
