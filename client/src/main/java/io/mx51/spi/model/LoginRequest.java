package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

@Deprecated
public class LoginRequest implements Message.Compatible {

    @Override
    public Message toMessage() {
        return new Message(RequestIdHelper.id("l"), Events.LOGIN_REQUEST, null, true);
    }

}
