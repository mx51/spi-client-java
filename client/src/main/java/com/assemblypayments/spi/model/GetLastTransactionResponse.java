package com.assemblypayments.spi.model;

public class GetLastTransactionResponse {

    private final Message m;

    public GetLastTransactionResponse(Message m) {
        this.m = m;
    }

    public boolean wasRetrievedSuccessfully() {
        // We can't rely on checking "success" flag here,
        // as retrieval may be successful, but the retrieved transaction was a fail.
        // So we check if we got back an RRN.
        String rrn = getRRN();
        return rrn != null && !rrn.isEmpty();
    }

    public boolean wasOperationInProgressError() {
        return "OPERATION_IN_PROGRESS".equals(m.getError());
    }

    public Message.SuccessState getSuccessState() {
        return m.getSuccessState();
    }

    public String getTxType() {
        return getResponseValue("transaction_type");
    }

    public int getTransactionAmount() {
        return m.getDataIntValue("amount_transaction_type");
    }

    public String getBankDateTimeString() {
        return m.getDataStringValue("bank_date") + m.getDataStringValue("bank_time");
    }

    public String getRRN() {
        return m.getDataStringValue("rrn");
    }

    public String getResponseValue(String attribute) {
        return m.getDataStringValue(attribute);
    }

    /**
     * There is a bug, VSV-920, whereby the customer_receipt is missing from a glt response.
     * The current recommendation is to use the merchant receipt in place of it if required.
     * This method modifies the underlying incoming message data by copying
     * the merchant receipt into the customer receipt only if there
     * is a merchant_receipt and there is not a customer_receipt.
     */
    public void copyMerchantReceiptToCustomerReceipt() {
        final String cr = m.getDataStringValue("customer_receipt");
        final String mr = m.getDataStringValue("merchant_receipt");
        if (mr.length() > 0 && cr.length() == 0) {
            m.getData().put("customer_receipt", mr);
        }
    }

}
