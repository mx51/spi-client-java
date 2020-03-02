package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

public class CancelTransactionRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("ctx"), Events.CANCEL_TRANSACTION_REQUEST, null, true);
    }

}
