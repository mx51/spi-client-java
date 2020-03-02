package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthTopupRequest implements Message.Compatible {

    private final String preauthId;
    private final int topupAmount;
    private final String posRefId;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

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
        data.put("topup_amount", topupAmount);

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }

        return new Message(RequestIdHelper.id("prtu"), Events.PREAUTH_TOPUP_REQUEST, data, true);
    }

}
