package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum BillRetrievalResult {
    SUCCESS("SUCCESS"),
    INVALID_TABLE_ID("INVALID_TABLE_ID"),
    INVALID_BILL_ID("INVALID_BILL_ID"),
    INVALID_OPERATOR_ID("INVALID_OPERATOR_ID");

    private final String name;

    BillRetrievalResult(@NotNull String name) {
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
