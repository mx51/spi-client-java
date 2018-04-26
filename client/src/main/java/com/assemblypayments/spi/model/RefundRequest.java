package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class RefundRequest extends AbstractChargeRequest implements Message.Compatible {

    private final int refundAmount;

    public RefundRequest(int amountCents, String posRefId) {
        super(posRefId);
        this.refundAmount = amountCents;
    }

    public int getRefundAmount() {
        return refundAmount;
    }

    /**
     * @deprecated Use {@link #getPosRefId()} instead.
     */
    @Deprecated
    public String getId() {
        return getPosRefId();
    }

    /**
     * @deprecated Use {@link #getRefundAmount()} instead.
     */
    @Deprecated
    public int getAmountCents() {
        return getRefundAmount();
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("refund_amount", getRefundAmount());
        return toMessage(RequestIdHelper.id("refund"), Events.REFUND_REQUEST, data, true);
    }

}
