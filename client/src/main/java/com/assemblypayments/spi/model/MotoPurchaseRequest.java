package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class MotoPurchaseRequest extends AbstractChargeRequest {

    private final int purchaseAmount;

    public MotoPurchaseRequest(int purchaseAmount, String posRefId) {
        super(posRefId);
        this.purchaseAmount = purchaseAmount;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("purchase_amount", getPurchaseAmount());
        return toMessage(RequestIdHelper.id("moto"), Events.MOTO_PURCHASE_REQUEST, data, true);
    }

}
