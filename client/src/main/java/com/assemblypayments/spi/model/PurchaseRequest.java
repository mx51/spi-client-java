package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;

import java.util.HashMap;
import java.util.Map;

public class PurchaseRequest implements Message.Compatible {

    private final int amountCents;
    private final String id;

    public PurchaseRequest(int amountCents, String id) {
        this.amountCents = amountCents;
        this.id = id;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public String getId() {
        return id;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("amount_purchase", amountCents);
        return new Message(id, Events.PURCHASE_REQUEST, data, true);
    }

}
