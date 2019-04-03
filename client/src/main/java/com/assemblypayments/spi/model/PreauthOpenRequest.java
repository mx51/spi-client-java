package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthOpenRequest implements Message.Compatible {

    private final String posRefId;
    private final int preauthAmount;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public PreauthOpenRequest(int amountCents, String posRefId) {
        this.posRefId = posRefId;
        this.preauthAmount = amountCents;
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

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", posRefId);
        data.put("preauth_amount", preauthAmount);

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }

        return new Message(RequestIdHelper.id("prac"), Events.PREAUTH_OPEN_REQUEST, data, true);
    }
}
