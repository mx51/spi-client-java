package com.assemblypayments.spi.model;

public class SignatureRequired {

    private final String requestId;
    private final String posRefId;
    private final String receiptToSign;

    public SignatureRequired(Message m) {
        this.requestId = m.getId();
        this.posRefId = m.getDataStringValue("pos_ref_id");
        this.receiptToSign = m.getDataStringValue("merchant_receipt");
    }

    public SignatureRequired(String posRefId, String requestId, String receiptToSign)
    {
        this.requestId = requestId;
        this.posRefId = posRefId;
        this.receiptToSign = receiptToSign;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getMerchantReceipt() {
        return receiptToSign;
    }

}
