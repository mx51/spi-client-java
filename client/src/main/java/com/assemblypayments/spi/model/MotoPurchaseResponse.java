package com.assemblypayments.spi.model;

public class MotoPurchaseResponse {

    private final String posRefId;
    private final PurchaseResponse purchaseResponse;

    public MotoPurchaseResponse(Message m) {
        purchaseResponse = new PurchaseResponse(m);
        posRefId = purchaseResponse.getPosRefId();
    }

    public String getPosRefId() {
        return posRefId;
    }

    public PurchaseResponse getPurchaseResponse() {
        return purchaseResponse;
    }

}
