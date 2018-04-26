package com.assemblypayments.spi.model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractChargeRequest {

    private final String posRefId;

    final SpiConfig config = new SpiConfig();

    protected AbstractChargeRequest(String posRefId) {
        this.posRefId = posRefId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    protected Message toMessage(String id, String eventName, Map<String, Object> data, boolean needsEncryption) {
        final Map<String, Object> baseData = new HashMap<String, Object>();
        baseData.put("pos_ref_id", getPosRefId());
        baseData.putAll(data);
        config.addReceiptConfig(baseData);
        return new Message(id, eventName, baseData, needsEncryption);
    }

}
