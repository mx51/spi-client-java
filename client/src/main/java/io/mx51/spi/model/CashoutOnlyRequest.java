package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class CashoutOnlyRequest extends AbstractChargeRequest implements Message.Compatible {

    private final int cashoutAmount;

    private int surchargeAmount;

    public CashoutOnlyRequest(int amountCents, String posRefId) {
        super(posRefId);
        this.cashoutAmount = amountCents;
    }

    public int getCashoutAmount() {
        return cashoutAmount;
    }

    public void setSurchargeAmount(int surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }

    public int getSurchargeAmount() {
        return surchargeAmount;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("cash_amount", getCashoutAmount());
        data.put("surcharge_amount", getSurchargeAmount());
        return toMessage(RequestIdHelper.id("cshout"), Events.CASHOUT_ONLY_REQUEST, data, true);
    }

}
