package io.mx51.spi.util;

import io.mx51.spi.model.Message;

public final class PingHelper {

    private PingHelper() {
    }

    public static Message generatePingRequest() {
        return new Message(RequestIdHelper.id("ping"), Events.PING, null, true);
    }

}
