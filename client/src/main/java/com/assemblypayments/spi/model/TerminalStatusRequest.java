package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TerminalStatusRequest {

    public TerminalStatusRequest() { }

    @NotNull
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();

        return new Message(RequestIdHelper.id("trmnl"), Events.TERMINAL_STATUS_REQUEST, data, true);
    }
}
