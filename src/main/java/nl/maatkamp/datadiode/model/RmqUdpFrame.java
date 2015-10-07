package nl.maatkamp.datadiode.model;

import java.io.Serializable;

/**
 * Created by marcel on 07-10-15.
 */
public class RmqUdpFrame implements Serializable {

    int channel;
    int type;

    public RmqUdpFrame(int channel, int type, byte[] payload) {
        this.channel = channel;
        this.type = type;
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    byte[] payload;
}
