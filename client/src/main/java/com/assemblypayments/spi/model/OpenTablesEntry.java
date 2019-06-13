package com.assemblypayments.spi.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OpenTablesEntry {
    @SerializedName("table_id")
    private String tableId;
    @SerializedName("label")
    private String label;
    @SerializedName("bill_outstanding_amount")
    private int billOutstandingAmount;

    public OpenTablesEntry(String tableId, String label, int billOutstandingAmount) {
        this.tableId = tableId;
        this.label = label;
        this.billOutstandingAmount = billOutstandingAmount;
    }

    public String getTableId() {
        return tableId;
    }

    public String getLabel() {
        return label;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public int getBillOutstandingAmount() {
        return billOutstandingAmount;
    }

    public static class ListType extends ArrayList<OpenTablesEntry> {
    }
}
