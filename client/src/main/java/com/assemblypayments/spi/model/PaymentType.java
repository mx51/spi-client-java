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
        for (PaymentType t : values()) {
            if (t.getName().equals(value)) return t;
        }
        return null;
    }

}
