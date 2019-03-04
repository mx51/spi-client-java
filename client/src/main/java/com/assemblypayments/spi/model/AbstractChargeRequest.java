package com.assemblypayments.spi.model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractChargeRequest implements Message.Compatible {

    private final String posRefId;

    private SpiConfig config = new SpiConfig();

    private TransactionOptions options = new TransactionOptions();

    protected AbstractChargeRequest(String posRefId) {
        this.posRefId = posRefId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public void setConfig(SpiConfig config) {
        this.config = config;
    }

    public void setOptions(TransactionOptions options) {
        this.options = options;
    }

    protected Message toMessage(String id, String eventName, Map<String, Object> data, boolean needsEncryption) {
        final Map<String, Object> baseData = new HashMap<String, Object>();
        baseData.put("pos_ref_id", getPosRefId());
        baseData.putAll(data);

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(baseData);

        if (options != null) {
            options.addOptions(baseData);
        }

        return new Message(id, eventName, baseData, needsEncryption);
    }

}
