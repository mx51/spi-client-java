package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.PurchaseRequest;
import com.assemblypayments.spi.model.RefundRequest;
import com.assemblypayments.spi.model.TransactionOptions;

public final class PurchaseHelper {

    private PurchaseHelper() {
    }

    public static PurchaseRequest createPurchaseRequest(int purchaseAmount, String posRefId) {
        return createPurchaseRequest(purchaseAmount, posRefId, 0, 0, false);
    }

    public static PurchaseRequest createPurchaseRequest(int purchaseAmount, String posRefId, int tipAmount, int cashoutAmount, boolean promptForCashout) {
        return createPurchaseRequest(purchaseAmount, posRefId, tipAmount, cashoutAmount, promptForCashout, 0);
    }

    public static PurchaseRequest createPurchaseRequest(int purchaseAmount, String posRefId, int tipAmount, int cashoutAmount, boolean promptForCashout, int surchargeAmount) {
        final PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        request.setCashoutAmount(cashoutAmount);
        request.setTipAmount(tipAmount);
        request.setPromptForCashout(promptForCashout);
        request.setSurchargeAmount(surchargeAmount);
        return request;
    }

    public static RefundRequest createRefundRequest(int amountCents, String purchaseId) {
        return createRefundRequest(amountCents, purchaseId, false);
    }

    public static RefundRequest createRefundRequest(int amountCents, String purchaseId, boolean suppressMerchantPassword) {
        final RefundRequest request = new RefundRequest(amountCents, purchaseId);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        return request;
    }

}
