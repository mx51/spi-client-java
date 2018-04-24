package com.assemblypayments.spi.model;

public class RefundResponse extends AbstractChargeResponse {

    public RefundResponse(Message m) {
        super(m);
    }

    public int getRefundAmount() {
        return m.getDataIntValue("refund_amount");
    }

}
