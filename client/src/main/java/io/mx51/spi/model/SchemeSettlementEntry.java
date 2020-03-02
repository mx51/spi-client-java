package io.mx51.spi.model;

import java.util.Map;

public class SchemeSettlementEntry {

    private final String schemeName;
    private final boolean settleByAcquirer;
    private final int totalCount;
    private final int totalValue;

    public SchemeSettlementEntry(String schemeName, boolean settleByAcquirer, int totalCount, int totalValue) {
        this.schemeName = schemeName;
        this.settleByAcquirer = settleByAcquirer;
        this.totalCount = totalCount;
        this.totalValue = totalValue;
    }

    public SchemeSettlementEntry(Map<String, Object> jo) {
        this.schemeName = (String) jo.get("scheme_name");
        this.settleByAcquirer = "yes".equals(((String) jo.get("settle_by_acquirer")).toLowerCase());

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

    public boolean isSettleByAcquirer() {
        return settleByAcquirer;
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
                ", SettleByAcquirer: " + settleByAcquirer +
                ", TotalCount: " + totalCount +
                ", TotalValue: " + totalValue;
    }

}
