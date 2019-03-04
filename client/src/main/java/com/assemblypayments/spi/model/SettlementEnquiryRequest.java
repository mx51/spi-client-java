package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;

import java.util.HashMap;
import java.util.Map;

public class SettlementEnquiryRequest implements Message.Compatible {

    private final String id;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public SettlementEnquiryRequest(String id) {
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
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(baseData);

        if (options != null) {
            options.addOptions(baseData);
        }

        return new Message(id, Events.SETTLEMENT_ENQUIRY_REQUEST, baseData, true);
    }

}
