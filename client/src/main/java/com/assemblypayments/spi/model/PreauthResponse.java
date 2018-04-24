package com.assemblypayments.spi.model;

public class PreauthResponse {

    private final String posRefId;
    private final String preauthId;

    private final PurchaseResponse details;

    private final Message m;

    public PreauthResponse(Message m) {
        this.m = m;
        preauthId = m.getDataStringValue("preauth_id");
        details = new PurchaseResponse(m);
        posRefId = details.getPosRefId();
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getPreauthId() {
        return preauthId;
    }

    public PurchaseResponse getDetails() {
        return details;
    }

    public int getBalanceAmount() {
        final String txType = m.getDataStringValue("transaction_type");
        // PARTIAL CANCELLATION
        if ("PRE-AUTH".equals(txType)) {
            return m.getDataIntValue("preauth_amount");
        } else if ("TOPUP".equals(txType)) {
            return m.getDataIntValue("balance_amount");
        } else if ("CANCEL".equals(txType)) {
            return m.getDataIntValue("balance_amount");
        } else if ("PRE-AUTH EXT".equals(txType)) {
            return m.getDataIntValue("balance_amount");
        } else if ("PCOMP".equals(txType)) {
            return 0; // Balance is 0 after completion
        } else if ("PRE-AUTH CANCEL".equals(txType)) {
            return 0; // Balance is 0 after cancellation
        }
        return 0;
    }

    public int getPreviousBalanceAmount() {
        final String txType = m.getDataStringValue("transaction_type");
        // PARTIAL CANCELLATION
        if ("PRE-AUTH".equals(txType)) {
            return 0;
        } else if ("TOPUP".equals(txType)) {
            return m.getDataIntValue("existing_preauth_amount");
        } else if ("CANCEL".equals(txType)) {
            return m.getDataIntValue("existing_preauth_amount");
        } else if ("PRE-AUTH EXT".equals(txType)) {
            return m.getDataIntValue("existing_preauth_amount");
        } else if ("PCOMP".equals(txType)) {// THIS IS TECHNICALLY NOT CORRECT WHEN COMPLETION HAPPENS FOR A PARTIAL AMOUNT.
            // BUT UNFORTUNATELY, THIS RESPONSE DOES NOT CONTAIN "existing_preauth_amount".
            // SO "completion_amount" IS THE CLOSEST WE HAVE.
            return m.getDataIntValue("completion_amount");
        } else if ("PRE-AUTH CANCEL".equals(txType)) {
            return m.getDataIntValue("preauth_amount");
        }
        return 0;
    }

    public int getCompletionAmount() {
        final String txType = m.getDataStringValue("transaction_type");
        if ("PCOMP".equals(txType)) {
            return m.getDataIntValue("completion_amount");
        }
        return 0;
    }

}
