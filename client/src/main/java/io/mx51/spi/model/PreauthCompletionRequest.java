package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthCompletionRequest implements Message.Compatible {

    private final String preauthId;
    private final int completionAmount;
    private final String posRefId;
    private int surchargeAmount;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public PreauthCompletionRequest(String preauthId, int completionAmountCents, String posRefId) {
        this.preauthId = preauthId;
        this.completionAmount = completionAmountCents;
        this.posRefId = posRefId;
    }

    public String getPreauthId() {
        return preauthId;
    }

    public int getCompletionAmount() {
        return completionAmount;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public void setSurchargeAmount(int surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }

    public int getSurchargeAmount() {
        return surchargeAmount;
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
        data.put("pos_ref_id", getPosRefId());
        data.put("preauth_id", getPreauthId());
        data.put("completion_amount", getCompletionAmount());
        data.put("surcharge_amount", getSurchargeAmount());

        config.setEnabledPrintMerchantCopy(true);
        config.setEnabledSignatureFlowOnEftpos(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(true);
        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }

        return new Message(RequestIdHelper.id("prac"), Events.PREAUTH_COMPLETE_REQUEST, data, true);
    }

}
