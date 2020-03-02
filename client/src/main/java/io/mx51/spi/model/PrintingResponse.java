package io.mx51.spi.model;

import org.jetbrains.annotations.NotNull;

public class PrintingResponse {
    private final Boolean success;
    private final Message m;

    public PrintingResponse(@NotNull Message m) {
        this.success = (Boolean) m.getData().get("success");
        this.m = m;
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
