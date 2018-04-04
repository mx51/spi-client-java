package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

public class GetLastTransactionRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("glt"), Events.GET_LAST_TRANSACTION_REQUEST, null, true);
    }

}
