package com.assemblypayments.spi.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

public class PaymentHistoryEntry {

    @SerializedName("payment_type")
    private String paymentType;
    @SerializedName("payment_summary")
    private Map<String, Object> paymentSummary;

    public String getPaymentType() {
        return paymentType;
    }

    public Map<String, Object> getPaymentSummary() {
        return paymentSummary;
    }

    public String getTerminalRefId() {
        final Object terminalRefId = paymentSummary.get("terminal_ref_id");
        if (terminalRefId instanceof String) return (String) terminalRefId;
        return null;
    }

    public static class List extends ArrayList<PaymentHistoryEntry> {
    }

}
