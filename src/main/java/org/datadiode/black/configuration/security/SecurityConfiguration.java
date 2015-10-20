package org.datadiode.black.configuration.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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

    public static final String ALGORITHM_SIGNATURE = "SHA1withRSA";

    // bouncycastle provider
    String SECURITY_PROVIDER = "BC";

    // RSA settings
    String ALGORITHM_RSA = "RSA";
    String ALGORITHM_RSA_CIPHER = "RSA/None/NoPadding";
    int ALGORITHM_RSA_KEYSIZE = 2048;

    // AES settings
    String ALGORITHM_AES = "AES";
    String ALGORITHM_AES_CIPHER = "AES/ECB/PKCS7Padding";
    int ALGORITHM_AES_KEYSIZE = 256;

    // pub/priv server

    @Bean
    KeyPairGenerator keyPairGeneratorServer() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGenerator.initialize(ALGORITHM_RSA_KEYSIZE);
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
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        return cipher;
    }

    @Bean
    Signature signatureServer() throws NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(ALGORITHM_SIGNATURE);
        return signature;
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
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        return cipher;
    }

    @Bean
    Signature signatureClient() throws NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(ALGORITHM_SIGNATURE);
        return signature;
    }



    // client.aes

    @Bean
    KeyGenerator keyGeneratorSymmetricalKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGenerator.init(ALGORITHM_AES_KEYSIZE);
        return keyGenerator;
    }

    @Bean
    SecretKey symmetricalKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        SecretKey key = keyGeneratorSymmetricalKey().generateKey();
        return key;
    }

    /**
     * https://www.owasp.org/index.php/Digital_Signature_Implementation_in_Java
     * <p>
     * When creating a symmetric cipher to encrypt the plaintext message, use "AES/CBC/PKCS5Padding" and
     * choose a random IV for each plaintext message rather than using simply "AES", which ends up using "AES/ECB/PKCS5Padding".
     * ECB mode is extremely weak for regular plaintext. (It is OK for encrypting random bits though, which is why it is OK to
     * use with RSA.) However, using CBC and PKCS5Padding could make you vulnerable to "padding oracle" attacks, so be careful.
     * You can use ESAPI 2.0's Encryptor to avoid it:
     * <p>
     * https://github.com/ESAPI/esapi-java
     *
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    @Bean
    Cipher cipherSymmetricalKey() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);
        return cipher;
    }


    // --- /sensor ---
}
