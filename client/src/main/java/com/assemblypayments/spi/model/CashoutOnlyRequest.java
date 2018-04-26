package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class CashoutOnlyRequest extends AbstractChargeRequest implements Message.Compatible {

    private final int cashoutAmount;

    public CashoutOnlyRequest(int amountCents, String posRefId) {
        super(posRefId);
        this.cashoutAmount = amountCents;
    }

    public int getCashoutAmount() {
        return cashoutAmount;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("cash_amount", getCashoutAmount());
        return toMessage(RequestIdHelper.id("cshout"), Events.CASHOUT_ONLY_REQUEST, data, true);
    }

}
