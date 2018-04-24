package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthTopupRequest implements Message.Compatible {

    private final String preauthId;
    private final int topupAmount;
    private final String posRefId;

    public PreauthTopupRequest(String preauthId, int topupAmountCents, String posRefId) {
        this.preauthId = preauthId;
        this.topupAmount = topupAmountCents;
        this.posRefId = posRefId;
    }

    public String getPreauthId() {
        return preauthId;
    }

    public int getTopupAmount() {
        return topupAmount;
    }

    public String getPosRefId() {
        return posRefId;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", posRefId);
        data.put("preauth_id", preauthId);
        data.put("topup_amount", topupAmount);

        return new Message(RequestIdHelper.id("prtu"), Events.PREAUTH_TOPUP_REQUEST, data, true);
    }

}
