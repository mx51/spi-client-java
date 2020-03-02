package io.mx51.spi.model;

import org.jetbrains.annotations.NotNull;

public class BillPaymentFlowEndedResponse {
    private final String billId;
    private final int billOutstandingAmount;
    private final int billTotalAmount;
    private final String tableId;
    private final String operatorId;
    private final int cardTotalCount;
    private final int cardTotalAmount;
    private final int cashTotalCount;
    private final int cashTotalAmount;

    public BillPaymentFlowEndedResponse(@NotNull Message m) {
        this.billId = m.getDataStringValue("bill_id");
        this.billOutstandingAmount = m.getDataIntValue("bill_outstanding_amount");
        this.billTotalAmount = m.getDataIntValue("bill_total_amount");
        tableId = m.getDataStringValue("table_id");
        operatorId = m.getDataStringValue("operator_id");
        cardTotalCount = m.getDataIntValue("card_total_count");
        cardTotalAmount = m.getDataIntValue("card_total_amount");
        cashTotalCount = m.getDataIntValue("cash_total_count");
        cashTotalAmount = m.getDataIntValue("cash_total_amount");
    }

    public String getBillId() {
        return billId;
    }

    public int getBillOutstandingAmount() {
        return billOutstandingAmount;
    }

    public int getBillTotalAmount() {
        return billTotalAmount;
    }

    public String getTableId() {
        return tableId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public int getCardTotalCount() {
        return cardTotalCount;
    }

    public int getCardTotalAmount() {
        return cardTotalAmount;
    }

    public int getCashTotalCount() {
        return cashTotalCount;
    }

    public int getCashTotalAmount() {
        return cashTotalAmount;
    }
}
