package com.assemblypayments.spi.model;

public class Settlement extends AbstractTransactionResponse {

    public Settlement(Message m) {
        super(m);
    }

    public String getResponseText() {
        return super.getResponseText();
    }

    /**
     * @deprecated Use {@link #getMerchantReceipt()} instead.
     */
    @Deprecated
    public String getReceipt() {
        return getMerchantReceipt();
    }

}
