package com.assemblypayments.spi.model;

public class Settlement {

    private final boolean success;
    private final String requestId;

    private final Message m;

    public Settlement(Message m) {
        this.requestId = m.getId();
        this.m = m;
        this.success = m.getSuccessState() == Message.SuccessState.SUCCESS;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResponseText() {
        return m.getDataStringValue("host_response_text");
    }

    public String getReceipt() {
        return m.getDataStringValue("merchant_receipt");
    }

}
