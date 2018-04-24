package com.assemblypayments.spi.model;

public abstract class AbstractTransactionResponse {

    protected final Message m;

    protected AbstractTransactionResponse(Message m) {
        this.m = m;
    }

    public String getRequestId() {
        return m.getId();
    }

    public Message.SuccessState getSuccessState() {
        return m.getSuccessState();
    }

    public boolean isSuccess() {
        return getSuccessState() == Message.SuccessState.SUCCESS;
    }

    /**
     * @deprecated Use {@link #isSuccess()} instead.
     */
    @Deprecated
    public boolean getSuccess() {
        return isSuccess();
    }

    public String getRRN() {
        return m.getDataStringValue("rrn");
    }

    public String getMerchantReceipt() {
        return m.getDataStringValue("merchant_receipt");
    }

    public String getTerminalId() {
        return m.getDataStringValue("terminal_id");
    }

    public String getResponseText() {
        return m.getDataStringValue("host_response_text");
    }

    public String getResponseValue(String attribute) {
        return m.getDataStringValue(attribute);
    }

}
