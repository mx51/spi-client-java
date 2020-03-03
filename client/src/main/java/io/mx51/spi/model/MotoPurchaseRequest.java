package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class MotoPurchaseRequest extends AbstractChargeRequest {

    private final int purchaseAmount;
    private int surchargeAmount;
    private boolean suppressMerchantPassword;

    public MotoPurchaseRequest(int purchaseAmount, String posRefId) {
        super(posRefId);
        this.purchaseAmount = purchaseAmount;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public int getSurchargeAmount() {
        return surchargeAmount;
    }

    public void setSurchargeAmount(int surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }


    public boolean isSuppressMerchantPassword() {
        return suppressMerchantPassword;
    }

    public void setSuppressMerchantPassword(boolean suppressMerchantPassword) {
        this.suppressMerchantPassword = suppressMerchantPassword;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("purchase_amount", getPurchaseAmount());
        data.put("surcharge_amount", getSurchargeAmount());
        data.put("suppress_merchant_password", isSuppressMerchantPassword());
        return toMessage(RequestIdHelper.id("moto"), Events.MOTO_PURCHASE_REQUEST, data, true);
    }
}
