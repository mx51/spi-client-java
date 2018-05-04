package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum PaymentType {
    CARD("CARD"),
    CASH("CASH");

    private final String name;

    PaymentType(@NotNull String name) {
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

    public static PaymentType parse(String value) {
        if (value != null) {
            String key = value.toLowerCase();
            for (PaymentType t : values()) {
                if (t.getName().toLowerCase().equals(key)) return t;
            }
        }
        return null;
    }

}
