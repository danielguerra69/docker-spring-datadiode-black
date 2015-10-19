package security;

import org.bouncycastle.util.encoders.Base64;
import org.datadiode.black.DatadiodeBlackStarter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by marcelmaatkamp on 19/10/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DatadiodeBlackStarter.class)
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class})
public class SecurityTest {
    private static final Logger log = LoggerFactory.getLogger(SecurityTest.class);

    @Autowired
    Cipher cipherServer;
    @Autowired
    PublicKey publicKeyServer;
    @Autowired
    PrivateKey privateKeyServer;

    @Autowired
    Cipher cipherClient;

    @Autowired
    Cipher cipherSymmetricalKeyClient;

    @Autowired
    SecretKey symmetricalKeyClient;

    @Autowired
    PublicKey publicKeyClient;

    @Test
    public void testKeyWrapper() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, SignatureException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException {

        String plain = "encrypt me!";

        // crypt aes key
        cipherServer.init(Cipher.WRAP_MODE, publicKeyServer);
        byte[] encryptedSymmetricalKey = cipherServer.wrap(symmetricalKeyClient);

        // crypt text
        cipherSymmetricalKeyClient.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient);
        byte[] encrypted = cipherSymmetricalKeyClient.doFinal(plain.getBytes());
        log.info("encrypted: " + Base64.toBase64String(encrypted));

        // unwrap aes key
        cipherServer.init(Cipher.UNWRAP_MODE, privateKeyServer);
        SecretKey decryptedKey = (SecretKey) cipherServer.unwrap(encryptedSymmetricalKey, "AES", Cipher.SECRET_KEY);

        // decrypt text
        cipherSymmetricalKeyClient.init(Cipher.DECRYPT_MODE, decryptedKey);
        byte[] decypted = cipherSymmetricalKeyClient.doFinal(encrypted);

        Signature sig = Signature.getInstance("RSA");
        sig.initVerify(publicKeyClient);
        sig.update(decypted);
        // sig.verify(signature);

        log.info("txt: " + new String(decypted));

    }


    @Test
    public void testAESSingle() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException {

        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(128);
        SecretKey secretKey = keygenerator.generateKey();

        cipherSymmetricalKeyClient.init(Cipher.ENCRYPT_MODE, secretKey);

        //sensitive information
        byte[] text = "this is a secret".getBytes();
        byte[] textEncrypted = cipherSymmetricalKeyClient.doFinal(text);
        log.info("Text Encryted : " + new String(textEncrypted, "UTF-8"));
        cipherSymmetricalKeyClient.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] textDecrypted = cipherSymmetricalKeyClient.doFinal(textEncrypted);
        log.info("Text Decryted : " + new String(textDecrypted));

    }

}
