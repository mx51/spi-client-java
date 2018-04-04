package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum TransactionType {
    PURCHASE("Purchase"),
    REFUND("Refund"),
    SETTLE("Settle"),
    GET_LAST_TRANSACTION("Get Last Transaction");

    private final String name;

    TransactionType(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
