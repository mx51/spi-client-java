package com.assemblypayments.spi.model;

public class RefundResponse {

    private final boolean success;
    private final String requestId;
    private final String schemeName;

    private final Message m;

    public RefundResponse(Message m) {
        this.requestId = m.getId();
        this.m = m;
        this.schemeName = this.m.getDataStringValue("scheme_name");
        this.success = m.getSuccessState() == Message.SuccessState.SUCCESS;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getRRN() {
        return m.getDataStringValue("rrn");
    }

    public String getCustomerReceipt() {
        return m.getDataStringValue("customer_receipt");
    }

    public String getMerchantReceipt() {
        return m.getDataStringValue("merchant_receipt");
    }

    public String getResponseText() {
        return m.getDataStringValue("host_response_text");
    }

    public String getResponseValue(String attribute) {
        return m.getDataStringValue(attribute);
    }

}
