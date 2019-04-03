package com.assemblypayments.spi.model;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class AbstractChargeResponse extends AbstractTransactionResponse {

    private final String schemeName;
    private final String posRefId;

    protected AbstractChargeResponse(@NotNull Message m) {
        super(m);
        this.schemeName = m.getDataStringValue("scheme_name");
        this.posRefId = m.getDataStringValue("pos_ref_id");
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getCustomerReceipt() {
        return m.getDataStringValue("customer_receipt");
    }

    public String getResponseCode() {
        return m.getDataStringValue("host_response_code");
    }

    public String getTerminalReferenceId() {
        return m.getDataStringValue("terminal_ref_id");
    }

    public String getCardEntry() {
        return m.getDataStringValue("card_entry");
    }

    public String getAccountType() {
        return m.getDataStringValue("account_type");
    }

    public String getAuthCode() {
        return m.getDataStringValue("auth_code");
    }

    public String getBankDate() {
        return m.getDataStringValue("bank_date");
    }

    public String getBankTime() {
        return m.getDataStringValue("bank_time");
    }

    public String getMaskedPan() {
        return m.getDataStringValue("masked_pan");
    }

    public boolean wasCustomerReceiptPrinted() {
        return m.getDataBooleanValue("customer_receipt_printed", false);
    }

    public Date getSettlementDate() {
        //"bank_settlement_date":"20042018"
        String dateStr = m.getDataStringValue("bank_settlement_date");
        try {
            return new SimpleDateFormat("ddMMyyyy", Locale.US).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

}
