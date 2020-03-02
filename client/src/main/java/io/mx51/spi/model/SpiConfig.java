package io.mx51.spi.model;

import java.util.Map;

public class SpiConfig {

    private boolean promptForCustomerCopyOnEftpos;
    private boolean signatureFlowOnEftpos;
    private boolean printMerchantCopy;

    private boolean enabledPromptForCustomerCopyOnEftpos;
    private boolean enabledSignatureFlowOnEftpos;
    private boolean enabledPrintMerchantCopy;

    public SpiConfig() {
        setEnabledPromptForCustomerCopyOnEftpos(false);
        setEnabledSignatureFlowOnEftpos(false);
        setEnabledPrintMerchantCopy(false);
    }

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

    public boolean isPrintMerchantCopy() {
        return printMerchantCopy;
    }

    public void setPrintMerchantCopy(boolean printMerchantCopy) {
        this.printMerchantCopy = printMerchantCopy;
    }

    public boolean isEnabledPromptForCustomerCopyOnEftpos() {
        return enabledPromptForCustomerCopyOnEftpos;
    }

    public void setEnabledPromptForCustomerCopyOnEftpos(boolean enabledPromptForCustomerCopyOnEftpos) {
        this.enabledPromptForCustomerCopyOnEftpos = enabledPromptForCustomerCopyOnEftpos;
    }

    public boolean isEnabledSignatureFlowOnEftpos() {
        return enabledSignatureFlowOnEftpos;
    }

    public void setEnabledSignatureFlowOnEftpos(boolean enabledSignatureFlowOnEftpos) {
        this.enabledSignatureFlowOnEftpos = enabledSignatureFlowOnEftpos;
    }

    public boolean isEnabledPrintMerchantCopy() {
        return enabledPrintMerchantCopy;
    }

    public void setEnabledPrintMerchantCopy(boolean enabledPrintMerchantCopy) {
        this.enabledPrintMerchantCopy = enabledPrintMerchantCopy;
    }

    void addReceiptConfig(Map<String, Object> messageData) {
        if (isPromptForCustomerCopyOnEftpos() && isEnabledPromptForCustomerCopyOnEftpos()) {
            messageData.put("prompt_for_customer_copy", true);
        }
        if (isSignatureFlowOnEftpos() && isEnabledSignatureFlowOnEftpos()) {
            messageData.put("print_for_signature_required_transactions", true);
        }
        if (isPrintMerchantCopy() && isEnabledPrintMerchantCopy()) {
            messageData.put("print_merchant_copy", true);
        }

    }

    @Override
    public String toString() {
        return "PromptForCustomerCopyOnEftpos:" + promptForCustomerCopyOnEftpos + " SignatureFlowOnEftpos:" + signatureFlowOnEftpos + " PrintMerchantCopy:" + printMerchantCopy;
    }

}
