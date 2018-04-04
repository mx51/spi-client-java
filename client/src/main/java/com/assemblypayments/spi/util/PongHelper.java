package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.Message;

public final class PongHelper {

    private PongHelper() {
    }

    public static Message generatePongResponse(Message ping) {
        return new Message(ping.getId(), Events.PONG, null, true);
    }

}
