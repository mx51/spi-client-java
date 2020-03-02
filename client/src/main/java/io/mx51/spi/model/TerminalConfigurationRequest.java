package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TerminalConfigurationRequest {

    @NotNull
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();

        return new Message(RequestIdHelper.id("trmnlcnfg"), Events.TERMINAL_CONFIGURATION_REQUEST, data, true);
    }

}
