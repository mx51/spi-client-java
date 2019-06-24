package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class SettleRequest implements Message.Compatible {

    private final String id;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public SettleRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConfig(SpiConfig config) {
        this.config = config;
    }

    public void setOptions(TransactionOptions options) {
        this.options = options;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> baseData = new HashMap<String, Object>();

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(false);
        config.setEnabledPromptForCustomerCopyOnEftpos(false);
        config.addReceiptConfig(baseData);

        if (options != null) {
            options.addOptions(baseData);
        }

        return new Message(RequestIdHelper.id("stl"), Events.SETTLE_REQUEST, baseData, true);
    }

}
