package com.assemblypayments.spi.model;

import java.util.Map;

public class SchemeSettlementEntry {

    private final String schemeName;
    private final boolean settleByAquirer;
    private final int totalCount;
    private final int totalValue;

    public SchemeSettlementEntry(String schemeName, boolean settleByAquirer, int totalCount, int totalValue) {
        this.schemeName = schemeName;
        this.settleByAquirer = settleByAquirer;
        this.totalCount = totalCount;
        this.totalValue = totalValue;
    }

    public SchemeSettlementEntry(Map<String, Object> jo) {
        this.schemeName = (String) jo.get("scheme_name");
        this.settleByAquirer = "yes".equals(((String) jo.get("settle_by_acquirer")).toLowerCase());

        int totalValue = 0;
        try {
            totalValue = Integer.parseInt((String) jo.get("total_value"));
        } catch (NumberFormatException ignored) {
        }
        this.totalValue = totalValue;

        int totalCount = 0;
        try {
            totalCount = Integer.parseInt((String) jo.get("total_count"));
        } catch (NumberFormatException ignored) {
        }
        this.totalCount = totalCount;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public boolean isSettleByAquirer() {
        return settleByAquirer;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalValue() {
        return totalValue;
    }

    @Override
    public String toString() {
        return "SchemeName: " + schemeName +
                ", SettleByAquirer: " + settleByAquirer +
                ", TotalCount: " + totalCount +
                ", TotalValue: " + totalValue;
    }

}
