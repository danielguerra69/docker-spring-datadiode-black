package nl.maatkamp.datadiode.model;

import java.io.Serializable;

/**
 * Created by marcel on 07-10-15.
 */
public class RmqUdpFrame implements Serializable {

    int index;
    int channel;
    int type;
    byte[] payload;

    public RmqUdpFrame(int index, int channel, int type, byte[] payload) {
        this.index = index;
        this.channel = channel;
        this.type = type;
        this.payload = payload;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
}
