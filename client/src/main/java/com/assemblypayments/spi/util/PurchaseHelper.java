package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.PurchaseRequest;
import com.assemblypayments.spi.model.RefundRequest;

public final class PurchaseHelper {

    private PurchaseHelper() {
    }

    public static PurchaseRequest createPurchaseRequest(int amountCents, String purchaseId) {
        return new PurchaseRequest(amountCents, purchaseId);
    }

    public static RefundRequest createRefundRequest(int amountCents, String purchaseId) {
        return new RefundRequest(amountCents, purchaseId);
    }

}
