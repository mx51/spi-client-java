package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Pairing Interaction 1: Outgoing.
 */
public class PairRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("padding", true);
        return new Message(RequestIdHelper.id("pr"), Events.PAIR_REQUEST, data, false);
    }

}
