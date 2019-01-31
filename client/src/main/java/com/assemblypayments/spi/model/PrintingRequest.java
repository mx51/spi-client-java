package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PrintingRequest {
    private final String key;
    private final String payload;

    public PrintingRequest(String key, String payload) {
        this.key = key;
        this.payload = payload;
    }

    @NotNull
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", key);
        data.put("payload", payload);

        return new Message(RequestIdHelper.id("print"), Events.PRINTING_REQUEST, data, true);
    }
}
