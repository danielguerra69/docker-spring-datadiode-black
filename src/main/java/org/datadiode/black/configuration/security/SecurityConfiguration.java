package org.datadiode.black.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by marcelmaatkamp on 19/10/15.
 */
@Configuration
public class SecurityConfiguration {

    String ALGORITHM_RSA = "RSA";
    int ALGORITHM_RSA_KEYSIZE = 1024;

    String ALGORITHM_AES = "AES";
    int ALGORITHM_AES_KEYSIZE = 128;

    // pub/priv server

    @Bean
    KeyPairGenerator keyPairGeneratorServer() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGenerator.initialize(1024);
        return keyPairGenerator;
    }

    @Bean
    KeyPair keyPairServer() throws NoSuchAlgorithmException {
        return keyPairGeneratorServer().generateKeyPair();
    }

    @Bean
    PrivateKey privateKeyServer() throws NoSuchAlgorithmException {
        return keyPairServer().getPrivate();
    }

    @Bean
    PublicKey publicKeyServer() throws NoSuchAlgorithmException {
        return keyPairServer().getPublic();
    }

    @Bean
    Cipher cipherServer() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        // cipher.init(Cipher.ENCRYPT_MODE, privateKeyServer());
        return cipher;
    }

    // pub.priv client

    @Bean
    KeyPairGenerator keyPairGeneratorClient() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGenerator.initialize(ALGORITHM_RSA_KEYSIZE);
        return keyPairGenerator;
    }

    @Bean
    KeyPair keyPairClient() throws NoSuchAlgorithmException {
        return keyPairGeneratorClient().generateKeyPair();
    }

    @Bean
    PrivateKey privateKeyClient() throws NoSuchAlgorithmException {
        return keyPairClient().getPrivate();
    }

    @Bean
    PublicKey publicKeyClient() throws NoSuchAlgorithmException {
        return keyPairClient().getPublic();
    }

    @Bean
    Cipher cipherClient() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, privateKeyClient());
        return cipher;
    }

    // client.aes

    @Bean
    KeyGenerator keyGeneratorSymmetricalKeyClient() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES, "BC");
        keyGenerator.init(ALGORITHM_AES_KEYSIZE);
        return keyGenerator;
    }

    @Bean
    SecretKey symmetricalKeyClient() throws NoSuchProviderException, NoSuchAlgorithmException {
        SecretKey key = keyGeneratorSymmetricalKeyClient().generateKey();
        return key;
    }

    @Bean
    Cipher cipherSymmetricalKeyClient() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        return cipher;
    }


    @Bean
    Cipher cipherSymmetricalKeyClientA() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, InvalidKeyException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient());
        return cipher;
    }
    // --- /sensor ---
}
