package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

public class SettleRequest implements Message.Compatible {

    private final String id;

    public SettleRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("stl"), Events.SETTLE_REQUEST, null, true);
    }

}
