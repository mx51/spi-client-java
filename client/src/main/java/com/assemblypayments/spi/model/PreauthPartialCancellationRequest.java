package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthPartialCancellationRequest implements Message.Compatible {

    private final String preauthId;
    private final int partialCancellationAmount;
    private final String posRefId;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

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
        data.put("preauth_id", preauthId);
        data.put("preauth_cancel_amount", partialCancellationAmount);

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }

        return new Message(RequestIdHelper.id("prpc"), Events.PREAUTH_PARTIAL_CANCELLATION_REQUEST, data, true);
    }

}
