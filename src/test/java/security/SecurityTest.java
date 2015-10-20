package security;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.datadiode.black.DatadiodeBlackStarter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SerializationUtils;
import security.model.Data;
import security.model.EncryptedData;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
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
    Signature signatureServer;



    @Autowired
    Cipher cipherClient;

    @Autowired
    Cipher cipherSymmetricalKey;

    @Autowired
    SecretKey symmetricalKeyClient;

    @Autowired
    PublicKey publicKeyClient;

    @Autowired
    PrivateKey privateKeyClient;

    @Autowired
    Signature signatureClient;

    @Autowired
    IvParameterSpec ivParameterSpec;


    @Test
    public void testKeyWrapperWithStringAsData() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, SignatureException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

        // on the sensor
        // -------------

        String plain = "encrypt me!";

        // sign the contents
        signatureClient.initSign(privateKeyClient);
        signatureClient.update((plain).getBytes());
        byte[] signature = signatureClient.sign();

        // crypt text
        cipherSymmetricalKey.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient, ivParameterSpec);
        byte[] encrypted = cipherSymmetricalKey.doFinal(plain.getBytes());
        log.info("encrypted: " + Base64.toBase64String(encrypted));

        // crypt aes key
        cipherClient.init(Cipher.WRAP_MODE, publicKeyServer);
        byte[] encryptedSymmetricalKey = cipherClient.wrap(symmetricalKeyClient);


        // on the server
        // -------------

        // unwrap aes key
        cipherServer.init(Cipher.UNWRAP_MODE, privateKeyServer);
        SecretKey decryptedKey = (SecretKey) cipherServer.unwrap(encryptedSymmetricalKey, "AES", Cipher.SECRET_KEY);

        // decrypt text
        cipherSymmetricalKey.init(Cipher.DECRYPT_MODE, decryptedKey, ivParameterSpec);
        byte[] decypted = cipherSymmetricalKey.doFinal(encrypted);

        // verify signature
        signatureServer.initVerify(publicKeyClient);
        signatureServer.update(decypted);

        log.info("txt: " + new String(decypted));

        Assert.assertTrue("valid signature from client", signatureServer.verify(signature));
        Assert.assertEquals("decypted text same as original", plain, new String(decypted));
    }


    /**
     * Emulate a diode to test end-to-end encryption is configured for AES256
     *
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws SignatureException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    @Test
    public void testKeyWrapperWithPojoAsData() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, SignatureException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

        // -------------
        // on the client
        // -------------
        String text = "encrypt me!";

        byte[] plain = SerializationUtils.serialize(new Data(text));

        EncryptedData encryptedData = new EncryptedData();

        // calculate digest
        SHA256Digest sha256Digest = new SHA256Digest();
        byte[] digest = new byte[sha256Digest.getDigestSize()];
        sha256Digest.update(plain, 0, plain.length);
        sha256Digest.doFinal(digest, 0);

        // sign the digest
        signatureClient.initSign(privateKeyClient);
        signatureClient.update(digest);
        encryptedData.signature = signatureClient.sign();

        // crypt text
        cipherSymmetricalKey.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient, ivParameterSpec);
        encryptedData.encryptedData = cipherSymmetricalKey.doFinal(plain);

        // crypt aes key
        cipherClient.init(Cipher.WRAP_MODE, publicKeyServer);
        encryptedData.encryptedKey = cipherClient.wrap(symmetricalKeyClient);

        // ---------
        // transport via serialization
        // ---------
        EncryptedData serializedEncryptedData =
                (EncryptedData) SerializationUtils.deserialize(
                        SerializationUtils.serialize(encryptedData));

        // -------------
        // on the server
        // -------------

        // unwrap aes key
        cipherServer.init(Cipher.UNWRAP_MODE, privateKeyServer);
        SecretKey decryptedKey = (SecretKey) cipherServer.unwrap(
                serializedEncryptedData.encryptedKey, "AES", Cipher.SECRET_KEY);

        // decrypt text
        cipherSymmetricalKey.init(Cipher.DECRYPT_MODE, decryptedKey, ivParameterSpec);
        byte[] decyptedData = cipherSymmetricalKey.doFinal(serializedEncryptedData.encryptedData);

        // calculate digest
        SHA256Digest sha256DigestServer = new SHA256Digest();
        byte[] digestServer = new byte[sha256Digest.getDigestSize()];
        sha256DigestServer.update(decyptedData, 0, decyptedData.length);
        sha256DigestServer.doFinal(digestServer, 0);

        // verify signature from digest
        signatureServer.initVerify(publicKeyClient);
        signatureServer.update(digestServer);

        Assert.assertTrue("valid signature(" + Base64.toBase64String(serializedEncryptedData.signature) + ") from client", signatureServer.verify(serializedEncryptedData.signature));

        if (signatureServer.verify(serializedEncryptedData.signature)) {
            Data decryptedData = (Data) SerializationUtils.deserialize(decyptedData);
            Assert.assertEquals("decypted text same as original", decryptedData.msg, text);
        }

    }


    @Test
    public void testAESSingle() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(128);
        SecretKey secretKey = keygenerator.generateKey();

        cipherSymmetricalKey.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        //sensitive information
        byte[] text = "this is a secret".getBytes();

        byte[] textEncrypted = cipherSymmetricalKey.doFinal(text);

        cipherSymmetricalKey.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] textDecrypted = cipherSymmetricalKey.doFinal(textEncrypted);

        Assert.assertEquals("decypted text same as original", new String(text), new String(textDecrypted));
    }

}
