package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class CashoutOnlyRequest implements Message.Compatible {

    private final String posRefId;
    private final int cashoutAmount;

    SpiConfig config = new SpiConfig();

    public CashoutOnlyRequest(int amountCents, String posRefId) {
        this.cashoutAmount = amountCents;
        this.posRefId = posRefId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public int getCashoutAmount() {
        return cashoutAmount;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", posRefId);
        data.put("cash_amount", cashoutAmount);
        config.addReceiptConfig(data);
        return new Message(RequestIdHelper.id("cshout"), Events.CASHOUT_ONLY_REQUEST, data, true);
    }

}
