package com.assemblypayments.spi.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class GetLastTransactionResponse {

    private final Message m;

    public GetLastTransactionResponse(@NotNull Message m) {
        Validate.notNull(m, "Cannot construct response with a null message!");
        this.m = m;
    }

    public boolean wasRetrievedSuccessfully() {
        // We can't rely on checking "success" flag here,
        // as retrieval may be successful, but the retrieved transaction was a fail.
        // So we check if we got back an RRN.
        return !StringUtils.isEmpty(getResponseCode());
    }

    public boolean wasTimeOutOfSyncError() {
        return m.getError().startsWith("TIME_OUT_OF_SYNC");
    }

    public boolean wasOperationInProgressError() {
        return m.getError().startsWith("OPERATION_IN_PROGRESS");
    }

    public boolean isWaitingForSignatureResponse() {
        return m.getError().startsWith("OPERATION_IN_PROGRESS_AWAITING_SIGNATURE");
    }

    public boolean isWaitingForAuthCode() {
        return m.getError().startsWith("OPERATION_IN_PROGRESS_AWAITING_PHONE_AUTH_CODE");
    }

    public boolean isStillInProgress(String posRefId) {
        return wasOperationInProgressError() && getPosRefId().equals(posRefId);
    }

    public Message.SuccessState getSuccessState() {
        return m.getSuccessState();
    }

    public boolean wasSuccessfulTx() {
        return m.getSuccessState() == Message.SuccessState.SUCCESS;
    }

    public String getTxType() {
        return getResponseValue("transaction_type");
    }

    public String getPosRefId() {
        return m.getDataStringValue("pos_ref_id");
    }

    /**
     * @deprecated Should not need to look at this in a GLT Response.
     */
    @Deprecated
    public int getTransactionAmount() {
        return m.getDataIntValue("amount_transaction_type");
    }

    /**
     * @deprecated Should not need to look at this in a GLT Response.
     */
    @Deprecated
    public String getBankDateTimeString() {
        return m.getDataStringValue("bank_date") + m.getDataStringValue("bank_time");
    }

    /**
     * @deprecated Should not need to look at this in a GLT Response.
     */
    @Deprecated
    public String getRRN() {
        return m.getDataStringValue("rrn");
    }

    public String getResponseText() {
        return m.getDataStringValue("host_response_text");
    }

    public String getResponseCode() {
        return m.getDataStringValue("host_response_code");
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
