package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

public class GetLastTransactionRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("glt"), Events.GET_LAST_TRANSACTION_REQUEST, null, true);
    }

}
