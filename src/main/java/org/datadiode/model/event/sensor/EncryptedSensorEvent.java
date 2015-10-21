package org.datadiode.model.event.sensor;

/**
 * That will require you to:
 * <p>
 * generate a symmetric key
 * Encrypt the data with the symmetric key
 * Encrypt the symmetric key with rsa
 * send the encrypted key and the data
 * Decrypt the encrypted symmetric key with rsa
 * decrypt the data with the symmetric key
 * done :)
 * <p>
 * Created by marcelmaatkamp on 15/10/15.
 */
public class EncryptedSensorEvent {

    byte[] payload;
    byte[] encryptedSymmetricalKey;
    byte[] iv;

    public EncryptedSensorEvent(byte[] encryptedSymmetricalKey, byte[] payload) {
        this.payload = payload;
        this.encryptedSymmetricalKey = encryptedSymmetricalKey;
    }

    public byte[] getEncryptedSymmetricalKey() {
        return encryptedSymmetricalKey;
    }

    public void setEncryptedSymmetricalKey(byte[] encryptedSymmetricalKey) {
        this.encryptedSymmetricalKey = encryptedSymmetricalKey;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
