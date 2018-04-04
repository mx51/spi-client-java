package com.assemblypayments.spi.model;

public class SignatureRequired {

    private final String requestId;
    private final Message m;

    public SignatureRequired(Message m) {
        this.requestId = m.getId();
        this.m = m;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMerchantReceipt() {
        return m.getDataStringValue("merchant_receipt");
    }

}
