package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class RefundRequest extends AbstractChargeRequest implements Message.Compatible {

    private final int refundAmount;

    private boolean suppressMerchantPassword;

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

    public void setSuppressMerchantPassword(boolean suppressMerchantPassword) {
        this.suppressMerchantPassword = suppressMerchantPassword;
    }

    public boolean isSuppressMerchantPassword() {
        return suppressMerchantPassword;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("refund_amount", getRefundAmount());
        data.put("suppress_merchant_password", isSuppressMerchantPassword());
        return toMessage(RequestIdHelper.id("refund"), Events.REFUND_REQUEST, data, true);
    }

}
