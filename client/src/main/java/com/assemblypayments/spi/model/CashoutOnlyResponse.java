package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public class CashoutOnlyResponse extends AbstractChargeResponse {

    public CashoutOnlyResponse(@NotNull Message m) {
        super(m);
    }

    public int getCashoutAmount() {
        return m.getDataIntValue("cash_amount");
    }

    public int getBankNonCashAmount() {
        return m.getDataIntValue("bank_noncash_amount");
    }

    public int getBankCashAmount() {
        return m.getDataIntValue("bank_cash_amount");
    }

    public int getSurchargeAmount() {
        return m.getDataIntValue("surcharge_amount");
    }
}
