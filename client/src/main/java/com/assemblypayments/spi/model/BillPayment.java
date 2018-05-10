package com.assemblypayments.spi.model;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class BillPayment {

    private final String billId;
    private final String tableId;
    private final String operatorId;

    private final PaymentType paymentType;

    private final int purchaseAmount;
    private final int tipAmount;

    private final PurchaseResponse purchaseResponse;

    public BillPayment(@NotNull Message m) {
        Validate.notNull(m, "Cannot construct bill payment with a null message!");

        billId = m.getDataStringValue("bill_id");
        tableId = m.getDataStringValue("table_id");
        operatorId = m.getDataStringValue("operator_id");

        paymentType = PaymentType.parse(m.getDataStringValue("payment_type"));

        // this is when we ply the sub object "payment_details" into a purchase response for convenience.
        final Message purchaseMsg = new Message(m.getId(), "payment_details", m.getDataMapValue("payment_details"), false);

        purchaseResponse = new PurchaseResponse(purchaseMsg);

        purchaseAmount = purchaseResponse.getPurchaseAmount();
        tipAmount = purchaseResponse.getTipAmount();
    }

    public String getBillId() {
        return billId;
    }

    public String getTableId() {
        return tableId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public int getTipAmount() {
        return tipAmount;
    }

    public PurchaseResponse getPurchaseResponse() {
        return purchaseResponse;
    }

}
