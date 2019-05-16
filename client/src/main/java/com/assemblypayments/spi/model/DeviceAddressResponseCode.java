package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum DeviceAddressResponseCode {
    SUCCESS("SUCCESS"),
    INVALID_SERIAL_NUMBER("INVALID_SERIAL_NUMBER"),
    ADDRESS_NOT_CHANGED("ADDRESS_NOT_CHANGED"),
    SERIAL_NUMBER_NOT_CHANGED("SERIAL_NUMBER_NOT_CHANGED"),
    DEVICE_SERVICE_ERROR("DEVICE_SERVICE_ERROR");

    private final String name;

    DeviceAddressResponseCode(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
