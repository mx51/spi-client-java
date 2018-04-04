package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.Message;

public final class PingHelper {

    private PingHelper() {
    }

    public static Message generatePingRequest() {
        return new Message(RequestIdHelper.id("ping"), Events.PING, null, true);
    }

}
