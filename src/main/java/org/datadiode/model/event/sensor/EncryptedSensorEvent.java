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

    public EncryptedSensorEvent(byte[] encryptedSymmetricalKey, byte[] payload) {
        this.payload = payload;
        this.encryptedSymmetricalKey = encryptedSymmetricalKey;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
