package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.PurchaseRequest;
import com.assemblypayments.spi.model.RefundRequest;

public final class PurchaseHelper {

    private PurchaseHelper() {
    }

    public static PurchaseRequest createPurchaseRequest(int purchaseAmount, String posRefId) {
        return createPurchaseRequest(purchaseAmount, posRefId, 0, 0, false);
    }

    public static PurchaseRequest createPurchaseRequest(int purchaseAmount, String posRefId, int tipAmount, int cashoutAmount, boolean promptForCashout) {
        final PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        request.setCashoutAmount(cashoutAmount);
        request.setTipAmount(tipAmount);
        request.setPromptForCashout(promptForCashout);
        return request;
    }

    public static RefundRequest createRefundRequest(int amountCents, String purchaseId) {
        return new RefundRequest(amountCents, purchaseId);
    }

}
