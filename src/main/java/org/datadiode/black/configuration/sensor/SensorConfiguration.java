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
import java.security.*;
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

    public static SecretKey createKeyForAES(int bitLength, SecureRandom random)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator generator = KeyGenerator.getInstance("AES", "BC");
        generator.init(128, random);
        return generator.generateKey();
    }

    @Bean
    GeoLocation geoLocationAmsterdam() {
        return new GeoLocation(4.899431, 52.379189);
    }

    @Bean
    Sensor sensor() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        Sensor sensor = new Sensor("SomeSensorType", 1, geoLocationAmsterdam());
        return sensor;
    }

    @Bean
    SecureRandom secureRandom() {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom;
    }

    @Bean
    Key symmetricalKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256, secureRandom());
        Key key = keyGen.generateKey();
        return key;
    }

    @Bean
    Cipher cipherRSA() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, sensor().getPrivateKey());
        return cipher;
    }

    @Bean
    Cipher cipherAES() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        return cipher;
    }

    @Scheduled(fixedDelay = 200)
    void sendTestMessages() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SensorEvent sensorEvent = sensor().generateEvent();


// SENSOR.KEYS == SERVER.KEYS!!

        Key aesKey = createKeyForAES(256, secureRandom());
        SecretKey sessionKey = new SecretKeySpec(new byte[16], "AES");

        Cipher c = cipherRSA();
        c.init(Cipher.ENCRYPT_MODE, sensor().getPublicKey());
        byte[] result1 = c.doFinal(sessionKey.getEncoded());

        c.init(Cipher.WRAP_MODE, sensor().getPublicKey());
        byte[] result2 = c.wrap(sessionKey);

        c.init(Cipher.UNWRAP_MODE, sensor().getPrivateKey());
        SecretKey sessionKey1 = (SecretKey) c.unwrap(result1, "AES",
                Cipher.SECRET_KEY);

        c.init(Cipher.DECRYPT_MODE, sensor().getPrivateKey());
        SecretKey sessionKey2 = new SecretKeySpec(c.doFinal(result2), "AES");

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
