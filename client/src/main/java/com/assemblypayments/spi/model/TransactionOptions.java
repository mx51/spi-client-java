package com.assemblypayments.spi.model;

import java.util.Map;

public class TransactionOptions {

    private String customerReceiptHeader;
    private String customerReceiptFooter;
    private String merchantReceiptHeader;
    private String merchantReceiptFooter;
    
    public void setCustomerReceiptHeader(String customerReceiptHeader) {
        this.customerReceiptHeader = customerReceiptHeader;
    }
    
    public void setCustomerReceiptFooter(String customerReceiptFooter) {
        this.customerReceiptFooter = customerReceiptFooter;
    }
    
    public void setMerchantReceiptHeader(String merchantReceiptHeader) {
        this.merchantReceiptHeader = merchantReceiptHeader;
    }
    
    public void setMerchantReceiptFooter(String merchantReceiptFooter) {
        this.merchantReceiptFooter = merchantReceiptFooter;
    }
    
    public void addOptions(Map<String, Object> messageData) {
		addOptionObject(customerReceiptHeader, "customer_receipt_header", messageData);
		addOptionObject(customerReceiptFooter, "customer_receipt_footer", messageData);
		addOptionObject(merchantReceiptHeader, "merchant_receipt_header", messageData);
		addOptionObject(merchantReceiptFooter, "merchant_receipt_footer", messageData);
	}	

	private void addOptionObject(Object value, String key, Map<String, Object> messageData){
		if (value != null) {
			messageData.put(key, value);
		}
		else {
			messageData.remove(key);
		}		
	}

}
