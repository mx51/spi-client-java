package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ZipPurchaseResponse {
    private Message m;

    public ZipPurchaseResponse(@NotNull Message m) {
        this.m = m;
    }

    public String getPosRefId() {
        return m.getDataStringValue("pos_ref_id");
    }

    public int getPurchaseAmount() {
        return m.getDataIntValue("purchase_amount");
    }

    public String getCustomerReceipt() {
        return m.getDataStringValue("customer_receipt");
    }

    public String getMerchantReceipt() {
        return m.getDataStringValue("merchant_receipt");
    }

    public String getBankDate() {
        return m.getDataStringValue("bank_date");
    }

    public String getBankTime() {
        return m.getDataStringValue("bank_time");
    }

    public String getResponseCode() {
        return m.getDataStringValue("host_response_code");
    }

    public String getResponseText() {
        return m.getDataStringValue("host_response_text");
    }

    public boolean wasCustomerReceiptPrinted() {
        return m.getDataBooleanValue("customer_receipt_printed", false);
    }

    public boolean wasMerchantReceiptPrinted() {
        return m.getDataBooleanValue("merchant_receipt_printed", false);
    }

    public Message.SuccessState getSuccessState() {
        return m.getSuccessState();
    }

    public boolean isSuccess() {
        return getSuccessState() == Message.SuccessState.SUCCESS;
    }

    public Map<String, Object> getZipData() {
        return m.getDataMapValue("zip_data");
    }
}
