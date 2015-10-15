package org.datadiode.model;

import java.io.Serializable;

/**
 * Created by marcelmaatkamp on 13/10/15.
 */
public class MessageWithPayload implements Serializable {
    String exchange;
    ExchangeType exchangeType;
    byte[] payload;

    public MessageWithPayload(String exchange, ExchangeType exchangeType, byte[] payload) {
        this.exchange = exchange;
        this.payload = payload;
        this.exchangeType = exchangeType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public enum ExchangeType {
        TopicExchange,
        HeadersExchange,
        FanoutExchange
    }
}
