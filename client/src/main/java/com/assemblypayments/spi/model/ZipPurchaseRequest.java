package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ZipPurchaseRequest implements Message.Compatible {
    private static final Gson GSON = new Gson();
    private final String posRefId;
    private final int purchaseAmount;
    private String storeCode = "";
    private String description = "";
    private SpiConfig config = new SpiConfig();
    private TransactionOptions options = new TransactionOptions();

    public ZipPurchaseRequest(int amountCents, String posRefId) {
        this.posRefId = posRefId;
        this.purchaseAmount = amountCents;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String amountSummary() {
        return String.format("Purchase: %.2f;", getPurchaseAmount() / 100.0);
    }

    public void setConfig(SpiConfig config) {
        this.config = config;
    }

    public void setOptions(TransactionOptions options) {
        this.options = options;
    }

//    public List<PaymentHistoryEntry> getBillPaymentHistory() {
//        final byte[] bdArray = Base64.decodeBase64(billData);
//        final String bdStr = new String(bdArray, Charsets.UTF_8);
//        return GSON.fromJson(bdStr, PaymentHistoryEntry.ListType.class);
//    }

    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", getPosRefId());
        data.put("purchase_amount", getPurchaseAmount());
        final Map<String, Object> zipData = new HashMap<String, Object>();
        zipData.put("store_code", getStoreCode());
        data.put("zip_data", zipData);
        final Map<String, Object> basketData = new HashMap<String, Object>();
        basketData.put("description", getDescription());
        data.put("basket", basketData);

        config.addReceiptConfig(data);

        if (options != null) {
            options.addOptions(data);
        }
        return new Message(RequestIdHelper.id("zprchs"), Events.ZIP_PURCHASE_REQUEST, data, true);
    }
}
