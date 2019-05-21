package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum DeviceAddressResponseCode {
    SUCCESS("Success"),
    INVALID_SERIAL_NUMBER("Invalid Serial Number"),
    ADDRESS_NOT_CHANGED("Address Not Changed"),
    SERIAL_NUMBER_NOT_CHANGED("Serial Number Not Changed"),
    DEVICE_SERVICE_ERROR("Device Service Error");

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
