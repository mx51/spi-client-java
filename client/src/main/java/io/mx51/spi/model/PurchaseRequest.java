package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PurchaseRequest extends AbstractChargeRequest implements Message.Compatible {

    private final int purchaseAmount;
    private int tipAmount;
    private int cashoutAmount;
    private boolean promptForCashout;
    private int surchargeAmount;

    public PurchaseRequest(int amountCents, String posRefId) {
        super(posRefId);
        this.purchaseAmount = amountCents;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public int getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(int tipAmount) {
        this.tipAmount = tipAmount;
    }

    public int getCashoutAmount() {
        return cashoutAmount;
    }

    public void setCashoutAmount(int cashoutAmount) {
        this.cashoutAmount = cashoutAmount;
    }

    public boolean isPromptForCashout() {
        return promptForCashout;
    }

    public void setPromptForCashout(boolean promptForCashout) {
        this.promptForCashout = promptForCashout;
    }

    public int getSurchargeAmount() {
        return surchargeAmount;
    }

    public void setSurchargeAmount(int surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }


    /**
     * @deprecated Use {@link #getPosRefId()} instead.
     */
    @Deprecated
    public String getId() {
        return getPosRefId();
    }

    /**
     * @deprecated Use {@link #getPurchaseAmount()} instead.
     */
    @Deprecated
    public int getAmountCents() {
        return purchaseAmount;
    }

    public String amountSummary() {
        return String.format("Purchase: %.2f; Tip: %.2f; Cashout: %.2f; Surcharge: %.2f;",
                getPurchaseAmount() / 100.0,
                getTipAmount() / 100.0,
                getCashoutAmount() / 100.0,
                getSurchargeAmount() / 100.0);
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("purchase_amount", getPurchaseAmount());
        data.put("tip_amount", getTipAmount());
        data.put("cash_amount", getCashoutAmount());
        data.put("prompt_for_cashout", isPromptForCashout());
        data.put("surcharge_amount", getSurchargeAmount());
        return toMessage(RequestIdHelper.id("prchs"), Events.PURCHASE_REQUEST, data, true);
    }

}
