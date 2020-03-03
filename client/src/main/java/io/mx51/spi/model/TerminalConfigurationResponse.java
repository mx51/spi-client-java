package io.mx51.spi.model;

import org.jetbrains.annotations.NotNull;

public class TerminalConfigurationResponse {
    private final Message m;

    public TerminalConfigurationResponse(@NotNull Message m) {
        this.m = m;
    }

    public String getCommsSelected() {
        return m.getDataStringValue("comms_selected");
    }

    public String getMerchantId() {
        return m.getDataStringValue("merchant_id");
    }

    public String getPAVersion() {
        return m.getDataStringValue("pa_version");
    }

    public String getPaymentInterfaceVersion() {
        return m.getDataStringValue("payment_interface_version");
    }

    public String getPluginVersion() {
        return m.getDataStringValue("plugin_version");
    }

    public String getSerialNumber() {
        return m.getDataStringValue("serial_number");
    }

    public String getTerminalId() {
        return m.getDataStringValue("terminal_id");
    }

    public String getTerminalModel() {
        return m.getDataStringValue("terminal_model");
    }
}
