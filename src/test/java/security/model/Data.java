package security.model;

import java.io.Serializable;

/**
 * Created by marcelmaatkamp on 20/10/15.
 */
public class Data implements Serializable {
    public String msg;

    public Data(String s) {
        this.msg = s;
    }
}
