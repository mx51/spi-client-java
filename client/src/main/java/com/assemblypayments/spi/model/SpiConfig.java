package com.assemblypayments.spi.model;

import java.util.Map;

public class SpiConfig {

    private boolean promptForCustomerCopyOnEftpos;
    private boolean signatureFlowOnEftpos;

    public boolean isPromptForCustomerCopyOnEftpos() {
        return promptForCustomerCopyOnEftpos;
    }

    public void setPromptForCustomerCopyOnEftpos(boolean promptForCustomerCopyOnEftpos) {
        this.promptForCustomerCopyOnEftpos = promptForCustomerCopyOnEftpos;
    }

    public boolean isSignatureFlowOnEftpos() {
        return signatureFlowOnEftpos;
    }

    public void setSignatureFlowOnEftpos(boolean signatureFlowOnEftpos) {
        this.signatureFlowOnEftpos = signatureFlowOnEftpos;
    }

    void addReceiptConfig(Map<String, Object> messageData) {
        if (promptForCustomerCopyOnEftpos) {
            messageData.put("prompt_for_customer_copy", true);
        }
        if (signatureFlowOnEftpos) {
            messageData.put("print_for_signature_required_transactions", true);
        }

    }

    @Override
    public String toString() {
        return "PromptForCustomerCopyOnEftpos:" + promptForCustomerCopyOnEftpos + " SignatureFlowOnEftpos:" + signatureFlowOnEftpos;
    }

}
