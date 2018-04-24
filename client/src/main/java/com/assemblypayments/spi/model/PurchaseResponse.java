package com.assemblypayments.spi.model;

public class PurchaseResponse extends AbstractChargeResponse {

    public PurchaseResponse(Message m) {
        super(m);
    }

    public int getPurchaseAmount() {
        return m.getDataIntValue("purchase_amount");
    }

    public int getTipAmount() {
        return m.getDataIntValue("tip_amount");
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

}
