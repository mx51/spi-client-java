package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthPartialCancellationRequest implements Message.Compatible {

    private final String preauthId;
    private final int partialCancellationAmount;
    private final String posRefId;

    public PreauthPartialCancellationRequest(String preauthId, int partialCancellationAmountCents, String posRefId) {
        this.preauthId = preauthId;
        this.partialCancellationAmount = partialCancellationAmountCents;
        this.posRefId = posRefId;
    }

    public String getPreauthId() {
        return preauthId;
    }

    public int getPartialCancellationAmount() {
        return partialCancellationAmount;
    }

    public String getPosRefId() {
        return posRefId;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", posRefId);
        data.put("preauth_id", preauthId);
        data.put("preauth_cancel_amount", partialCancellationAmount);

        return new Message(RequestIdHelper.id("prpc"), Events.PREAUTH_PARTIAL_CANCELLATION_REQUEST, data, true);
    }

}
