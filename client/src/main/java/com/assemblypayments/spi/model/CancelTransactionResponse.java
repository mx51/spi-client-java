package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

public class CancelTransactionResponse {
    private final Message m;
    public final String posRefId;
    private final Boolean success;

    public CancelTransactionResponse(@NotNull Message m) {
        this.m = m;
        this.success = (Boolean) m.getData().get("success");
        this.posRefId = m.getDataStringValue("pos_ref_id");
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorReason() {
        return m.getDataStringValue("error_reason");
    }

    public String getErrorDetail() {
        return m.getDataStringValue("error_detail");
    }

    public String getResponseValueWithAttribute(String attribute) {
        return m.getDataStringValue(attribute);
    }
}
