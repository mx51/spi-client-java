package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class ReversalRequest {

    private String posRefId;

    public String getPosRefId() {
        return posRefId;
    }

    public ReversalRequest(String posRefId) {
        this.posRefId = posRefId;
    }

    public Message ToMessage()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("pos_ref_id", this.posRefId);

        return new Message(RequestIdHelper.id("rev"), Events.REVERSAL_REQUEST, data, true);
    }
}
