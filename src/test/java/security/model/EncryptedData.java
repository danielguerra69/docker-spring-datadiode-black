package security.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by marcelmaatkamp on 20/10/15.
 */
public class EncryptedData implements Serializable {

    public byte[] signature;
    public byte[] encryptedKey;
    public byte[] encryptedData;

    public String toString() {
        return (new ReflectionToStringBuilder(this) {
            protected Object getValue(Field f) throws IllegalAccessException {
                if ("signature".equals(f.getName())) {
                    return Base64.toBase64String(signature);
                } else if ("encryptedKey".equals(f.getName())) {
                    return Base64.toBase64String(encryptedKey);
                } else if ("encryptedData".equals(f.getName())) {
                    return Base64.toBase64String(encryptedData);
                } else {
                    return super.getValue(f);
                }
            }
        }).toString();
    }
}
