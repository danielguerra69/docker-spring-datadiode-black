package org.datadiode.model.event.sensor;

import org.datadiode.model.event.GeoLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
@Service
public class Sensor {
    private static final Logger log = LoggerFactory.getLogger(Sensor.class);

    String type;
    int id;

    GeoLocation geoLocation;

    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Sensor(String type, int id, GeoLocation geoLocation) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        this.type = type;
        this.id = id;
        this.geoLocation = geoLocation;

        SecureRandom random = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, random);

        this.keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec rsaPublicKeySpec = fact.getKeySpec(keyPair.getPublic(),
                RSAPublicKeySpec.class);
        RSAPrivateKeySpec rsaPrivateKeySpec = fact.getKeySpec(keyPair.getPrivate(),
                RSAPrivateKeySpec.class);

        saveToFile("public.key", rsaPublicKeySpec.getModulus(),
                rsaPublicKeySpec.getPublicExponent());
        saveToFile("private.key", rsaPrivateKeySpec.getModulus(),
                rsaPrivateKeySpec.getPrivateExponent());

    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void saveToFile(String keyname,
                           BigInteger mod, BigInteger exp) throws IOException {

        log.info("key(" + keyname + "): pub(" + mod + "),exp(" + exp + ")");

        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(keyname)));

        try {
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (Exception e) {
            throw new IOException("Unexpected error", e);
        } finally {
            oout.close();
        }

    }

    public SensorEvent generateEvent() {
        SensorEvent sensorEvent = new SensorEvent(this);
        return sensorEvent;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }
}
