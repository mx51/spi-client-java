package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public enum TransactionType {
    PURCHASE("Purchase"),
    REFUND("Refund"),
    CASHOUT_ONLY("CashoutOnly"),
    MOTO("MOTO"),
    SETTLE("Settle"),
    GET_LAST_TRANSACTION("Get Last Transaction"),
    PREAUTH("Preauth"),
    ACCOUNT_VERIFY("AccountVerify");

    private final String name;

    TransactionType(@NotNull String name) {
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
