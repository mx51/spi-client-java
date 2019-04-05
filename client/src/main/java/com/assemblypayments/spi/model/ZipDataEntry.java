package com.assemblypayments.spi.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

public class ZipDataEntry {
    @SerializedName("zip_data")
    private Map<String, Object> zipData;

    public ZipDataEntry(Map<String, Object> zipData) {
        this.zipData = zipData;
    }

    public String getStoreCode() {
        final Object storeCode = zipData.get("store_code");
        if (storeCode instanceof String) return (String) storeCode;
        return null;
    }

    public String getLocationId() {
        final Object locationId = zipData.get("location_id");
        if (locationId instanceof String) return (String) locationId;
        return null;
    }

    public String getReceiptNumber() {
        final Object receiptNumber = zipData.get("receipt_number");
        if (receiptNumber instanceof String) return (String) receiptNumber;
        return null;
    }

    public String getOriginalReceiptNumber() {
        final Object receiptNumber = zipData.get("original_receipt_number");
        if (receiptNumber instanceof String) return (String) receiptNumber;
        return null;
    }

    public static class ListType extends ArrayList<ZipDataEntry> {
    }

}
