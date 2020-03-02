package io.mx51.spi.model;

import org.jetbrains.annotations.NotNull;

public class TerminalBattery {
    public final String batteryLevel;

    public TerminalBattery(@NotNull Message m) {
        this.batteryLevel = m.getData().get("battery_level").toString();
    }
}
