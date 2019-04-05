package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class ZipRefundRequest implements Message.Compatible {

    private final String posRefId;
    private final int refundAmount;
    private String originalReceiptNumber;
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public ZipRefundRequest(int amountCents, String posRefId) {
        this.posRefId = posRefId;
        this.refundAmount = amountCents;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public int getRefundAmount() {
        return refundAmount;
    }

    public String getOriginalReceiptNumber() {
        return originalReceiptNumber;
    }

    public void setOriginalReceiptNumber(String originalReceiptNumber) {
        this.originalReceiptNumber = originalReceiptNumber;
    }

    public void setConfig(SpiConfig config) {
        this.config = config;
    }

    public void setOptions(TransactionOptions options) {
        this.options = options;
    }

    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", getPosRefId());
        data.put("refund_amount", getRefundAmount());
        final Map<String, Object> zipData = new HashMap<String, Object>();
        zipData.put("original_receipt_number", getOriginalReceiptNumber());
        data.put("zip_data", zipData);

        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }
        return new Message(RequestIdHelper.id("refund"), Events.ZIP_REFUND_REQUEST, data, true);
    }
}
