package com.assemblypayments.spi.model;

public class PhoneForAuthRequired {

    private final String requestId;
    private final String posRefId;

    private final String phoneNumber;
    private final String merchantId;

    public PhoneForAuthRequired(Message m) {
        this.requestId = m.getId();
        this.posRefId = m.getDataStringValue("pos_ref_id");
        this.phoneNumber = m.getDataStringValue("auth_centre_phone_number");
        this.merchantId = m.getDataStringValue("merchant_id");
    }

    public PhoneForAuthRequired(String posRefId, String requestId, String phoneNumber, String merchantId) {
        this.requestId = requestId;
        this.posRefId = posRefId;
        this.phoneNumber = phoneNumber;
        this.merchantId = merchantId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMerchantId() {
        return merchantId;
    }

}
