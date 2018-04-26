package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PurchaseResponse extends AbstractChargeResponse {

    public PurchaseResponse(Message m) {
        super(m);
    }

    public int getPurchaseAmount() {
        return m.getDataIntValue("purchase_amount");
    }

    public int getTipAmount() {
        return m.getDataIntValue("tip_amount");
    }

    public int getCashoutAmount() {
        return m.getDataIntValue("cash_amount");
    }

    public int getBankNonCashAmount() {
        return m.getDataIntValue("bank_noncash_amount");
    }

    public int getBankCashAmount() {
        return m.getDataIntValue("bank_cash_amount");
    }

    @NotNull
    public Map<String, Object> toPaymentSummary() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("account_type", getAccountType());
        data.put("auth_code", getAuthCode());
        data.put("bank_date", getBankDate());
        data.put("bank_time", getBankTime());
        data.put("host_response_code", getResponseCode());
        data.put("host_response_text", getResponseText());
        data.put("masked_pan", getMaskedPan());
        data.put("purchase_amount", getPurchaseAmount());
        data.put("rrn", getRRN());
        data.put("scheme_name", getSchemeName());
        data.put("terminal_id", getTerminalId());
        data.put("terminal_ref_id", getTerminalReferenceId());
        data.put("tip_amount", getTipAmount());
        return data;
    }

}
