package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;

public class SettlementEnquiryRequest implements Message.Compatible {

    private final String id;

    public SettlementEnquiryRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public Message toMessage() {
        return new Message(id, Events.SETTLEMENT_ENQUIRY_REQUEST, null, true);
    }

}
