package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

public class DropKeysRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("drpkys"), Events.DROP_KEYS_ADVICE, null, true);
    }

}
