package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public class TerminalStatusResponse {
    private final Message m;

    public TerminalStatusResponse(@NotNull Message m) {
        this.m = m;
    }

    public String getStatus() {
        return m.getDataStringValue("status");
    }

    public String getBatteryLevel() {
        return m.getDataStringValue("battery_level");
    }

    public boolean isCharging() {
        return m.getDataBooleanValue("charging", false);
    }
}
