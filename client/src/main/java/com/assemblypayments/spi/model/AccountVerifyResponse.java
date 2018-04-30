package com.assemblypayments.spi.model;

public class AccountVerifyResponse {

    private final String posRefId;
    private final PurchaseResponse details;

    public AccountVerifyResponse(Message m) {
        details = new PurchaseResponse(m);
        posRefId = details.getPosRefId();
    }

    public String getPosRefId() {
        return posRefId;
    }

    public PurchaseResponse getDetails() {
        return details;
    }

}
