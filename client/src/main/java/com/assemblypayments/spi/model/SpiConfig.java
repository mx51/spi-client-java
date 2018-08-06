package com.assemblypayments.spi.model;

import java.util.Map;

public class SpiConfig {

    private boolean promptForCustomerCopyOnEftpos;
    private boolean signatureFlowOnEftpos;
    private boolean printMerchantCopy;

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
    
    public void setPrintMerchantCopy(boolean printMerchantCopy) {
        this.printMerchantCopy = printMerchantCopy;
    }    

    void addReceiptConfig(Map<String, Object> messageData) {
        if (promptForCustomerCopyOnEftpos) {
            messageData.put("prompt_for_customer_copy", true);
        }
        if (signatureFlowOnEftpos) {
            messageData.put("print_for_signature_required_transactions", true);
        }
        if (printMerchantCopy) {
            messageData.put("print_merchant_copy", true);
        }

    }

    @Override
    public String toString() {
        return "PromptForCustomerCopyOnEftpos:" + promptForCustomerCopyOnEftpos + " SignatureFlowOnEftpos:" + signatureFlowOnEftpos + " PrintMerchantCopy:" + printMerchantCopy;
    }

}
