package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

public class CancelTransactionRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("ctx"), Events.CANCEL_TRANSACTION_REQUEST, null, true);
    }

}
