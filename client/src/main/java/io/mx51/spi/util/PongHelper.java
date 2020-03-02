package io.mx51.spi.util;

import io.mx51.spi.model.Message;

public final class PongHelper {

    private PongHelper() {
    }

    public static Message generatePongResponse(Message ping) {
        return new Message(ping.getId(), Events.PONG, null, true);
    }

}
