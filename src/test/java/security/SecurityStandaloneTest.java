package security;

import configuration.SecurityTestConfiguration;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
public class SecurityStandaloneTest {
    private static final Logger log = LoggerFactory.getLogger(SecurityStandaloneTest.class);

    @Test
    public void testKeyWrapperWithStringAsData() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, SignatureException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

        String ALGORITHM_SIGNATURE = "SHA1withRSA";

        // bouncycastle provider
        String SECURITY_PROVIDER = "BC";

        // RSA settings
        String ALGORITHM_RSA = "RSA";
        String ALGORITHM_RSA_CIPHER = "RSA/None/NoPadding";
        int ALGORITHM_RSA_KEYSIZE = 2048;

        // AES settings
        String ALGORITHM_AES = "AES";
        String ALGORITHM_AES_CIPHER = "AES/CBC/PKCS7Padding";
        int ALGORITHM_AES_KEYSIZE = 256;

        // on the sensor
        // -------------

        String plain = "encrypt me!";

        // SERVER
        // pub/priv keys

        KeyPairGenerator keyPairGeneratorServer = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGeneratorServer.initialize(ALGORITHM_RSA_KEYSIZE);
        KeyPair keyPairServer = keyPairGeneratorServer.generateKeyPair();
        PrivateKey privateKeyServer = keyPairServer.getPrivate();
        PublicKey publicKeyServer = keyPairServer.getPublic();
        Cipher cipherServer = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        Signature signatureServer = Signature.getInstance(ALGORITHM_SIGNATURE);

        // symmetrical
        KeyGenerator keyGeneratorSymmetricalKeyServer = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGeneratorSymmetricalKeyServer.init(ALGORITHM_AES_KEYSIZE);
        SecretKey symmetricalKeyServer = keyGeneratorSymmetricalKeyServer.generateKey();
        Cipher cipherSymmetricalKeyServer = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);
        SecureRandom secureRandomServer = new SecureRandom();
        IvParameterSpec ivParameterSpecServer = new IvParameterSpec(secureRandomServer.generateSeed(16));


        // CLIENT
        // pub/priv keys

        KeyPairGenerator keyPairGeneratorClient = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGeneratorClient.initialize(ALGORITHM_RSA_KEYSIZE);
        KeyPair keyPairClient = keyPairGeneratorClient.generateKeyPair();
        PrivateKey privateKeyClient = keyPairClient.getPrivate();
        PublicKey publicKeyClient = keyPairClient.getPublic();
        Cipher cipherClient = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        Signature signatureClient = Signature.getInstance(ALGORITHM_SIGNATURE);

        // symmetrical
        KeyGenerator keyGeneratorSymmetricalKeyClient = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGeneratorSymmetricalKeyClient.init(ALGORITHM_AES_KEYSIZE);
        SecretKey symmetricalKeyClient = keyGeneratorSymmetricalKeyClient.generateKey();
        Cipher cipherSymmetricalKeyClient = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);
        SecureRandom secureRandomClient = new SecureRandom();
        IvParameterSpec ivParameterSpecClient = new IvParameterSpec(secureRandomClient.generateSeed(16));


        // lets start crypting!

        // sign the contents
        signatureClient.initSign(privateKeyClient);
        signatureClient.update((plain).getBytes());
        byte[] signature = signatureClient.sign();

        // crypt text
        cipherSymmetricalKeyClient.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient, ivParameterSpecServer);
        byte[] encrypted = cipherSymmetricalKeyClient.doFinal(plain.getBytes());
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
        cipherSymmetricalKeyServer.init(Cipher.DECRYPT_MODE, decryptedKey, ivParameterSpecClient);
        byte[] decypted = cipherSymmetricalKeyServer.doFinal(encrypted);

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
    public void testRSAWithAESAndCBCWithIVs() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, SignatureException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());

        String ALGORITHM_SIGNATURE = "SHA1withRSA";

        // bouncycastle provider
        String SECURITY_PROVIDER = "BC";

        // RSA settings
        String ALGORITHM_RSA = "RSA";
        String ALGORITHM_RSA_CIPHER = "RSA/None/NoPadding";
        int ALGORITHM_RSA_KEYSIZE = 2048;

        // AES settings
        String ALGORITHM_AES = "AES";
        String ALGORITHM_AES_CIPHER = "AES/CBC/PKCS7Padding";
        int ALGORITHM_AES_KEYSIZE = 256;

        // SERVER
        // pub/priv keys

        KeyPairGenerator keyPairGeneratorServer = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGeneratorServer.initialize(ALGORITHM_RSA_KEYSIZE);
        KeyPair keyPairServer = keyPairGeneratorServer.generateKeyPair();
        PrivateKey privateKeyServer = keyPairServer.getPrivate();
        PublicKey publicKeyServer = keyPairServer.getPublic();
        Cipher cipherServer = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        Signature signatureServer = Signature.getInstance(ALGORITHM_SIGNATURE);

        // symmetrical
        KeyGenerator keyGeneratorSymmetricalKeyServer = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGeneratorSymmetricalKeyServer.init(ALGORITHM_AES_KEYSIZE);
        SecretKey symmetricalKeyServer = keyGeneratorSymmetricalKeyServer.generateKey();
        Cipher cipherSymmetricalKeyServer = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);
        SecureRandom secureRandomServer = new SecureRandom();
        IvParameterSpec ivParameterSpecServer = new IvParameterSpec(secureRandomServer.generateSeed(16));


        // CLIENT
        // pub/priv keys

        KeyPairGenerator keyPairGeneratorClient = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGeneratorClient.initialize(ALGORITHM_RSA_KEYSIZE);
        KeyPair keyPairClient = keyPairGeneratorClient.generateKeyPair();
        PrivateKey privateKeyClient = keyPairClient.getPrivate();
        PublicKey publicKeyClient = keyPairClient.getPublic();
        Cipher cipherClient = Cipher.getInstance(ALGORITHM_RSA_CIPHER, SECURITY_PROVIDER);
        Signature signatureClient = Signature.getInstance(ALGORITHM_SIGNATURE);

        // symmetrical
        KeyGenerator keyGeneratorSymmetricalKeyClient = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGeneratorSymmetricalKeyClient.init(ALGORITHM_AES_KEYSIZE);
        SecretKey symmetricalKeyClient = keyGeneratorSymmetricalKeyClient.generateKey();
        Cipher cipherSymmetricalKeyClient = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);


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
        cipherSymmetricalKeyServer.init(Cipher.ENCRYPT_MODE, symmetricalKeyClient, ivParameterSpecServer);
        encryptedData.encryptedData = cipherSymmetricalKeyServer.doFinal(plain);
        encryptedData.iv = ivParameterSpecServer.getIV();

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
        IvParameterSpec ivParameterSpecClient = new IvParameterSpec(ivParameterSpecServer.getIV());

        cipherSymmetricalKeyClient.init(Cipher.DECRYPT_MODE, decryptedKey, ivParameterSpecClient);
        byte[] decyptedData = cipherSymmetricalKeyClient.doFinal(serializedEncryptedData.encryptedData);

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
    public void testAESWithCBC() throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());

        String SECURITY_PROVIDER = "BC";
        String ALGORITHM_AES_CIPHER = "AES/CBC/PKCS7Padding";
        int ALGORITHM_AES_KEYSIZE = 256;

        // SERVER
        Cipher cipherSymmetricalKeyServer = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);
        SecureRandom secureRandomServer = new SecureRandom();
        IvParameterSpec ivParameterSpecServer = new IvParameterSpec(secureRandomServer.generateSeed(16));

        // CLIENT
        Cipher cipherSymmetricalKeyClient = Cipher.getInstance(ALGORITHM_AES_CIPHER, SECURITY_PROVIDER);

        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(ALGORITHM_AES_KEYSIZE);
        SecretKey secretKey = keygenerator.generateKey();

        byte[] text = "this is a secret".getBytes();

        // crypt
        cipherSymmetricalKeyServer.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpecServer);
        byte[] textEncrypted = cipherSymmetricalKeyServer.doFinal(text);
        byte[] iv = ivParameterSpecServer.getIV();

        // decrypt
        IvParameterSpec ivParameterSpecClient = new IvParameterSpec(iv);
        cipherSymmetricalKeyClient.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpecClient);
        byte[] textDecrypted = cipherSymmetricalKeyClient.doFinal(textEncrypted);

        // validate
        Assert.assertEquals("decypted text same as original", new String(text), new String(textDecrypted));
    }
}
