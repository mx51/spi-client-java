package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

public class CashoutTest {

    @Test
    public void testCashoutOnlyRequest() {
        String posRefId = "123";
        int amountCents = 1000;

        CashoutOnlyRequest request = new CashoutOnlyRequest(amountCents, posRefId);
        request.setSurchargeAmount(100);

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);
        request.setConfig(config);

        TransactionOptions options = new TransactionOptions();
        String receiptHeader = "Receipt Header";
        String receiptFooter = "Receipt Footer";
        options.setCustomerReceiptHeader(receiptHeader);
        options.setCustomerReceiptFooter(receiptFooter);
        options.setMerchantReceiptHeader(receiptHeader);
        options.setMerchantReceiptFooter(receiptFooter);
        request.setOptions(options);

        Message msg = request.toMessage();

        Assert.assertEquals(request.getPosRefId(), posRefId);
        Assert.assertEquals(request.getCashoutAmount(), amountCents);
        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
        Assert.assertEquals(receiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(receiptFooter, msg.getDataStringValue("customer_receipt_footer"));
        Assert.assertEquals(receiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(receiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
    }

    @Test
    public void testCashoutOnlyResponse() {
        String jsonStr = "{\"message\": {\"data\":{\"account_type\":\"SAVINGS\",\"auth_code\":\"265035\",\"bank_cash_amount\":1200,\"bank_date\":\"17062018\",\"bank_settlement_date\":\"18062018\",\"bank_time\":\"170950\",\"card_entry\":\"EMV_INSERT\",\"cash_amount\":1200,\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM WESTPAC\\\\r\\\\nMerchant4\\\\r\\\\n213 Miller Street\\\\r\\\\nSydney 2060\\\\r\\\\nAustralia\\\\r\\\\n\\\\r\\\\nTIME 17JUN18   17:09\\\\r\\\\nMID         22341845\\\\r\\\\nTSP     100312348845\\\\r\\\\nRRN     180617000151\\\\r\\\\nDebit(I)         SAV\\\\r\\\\nCARD............2797\\\\r\\\\nAUTH          265035\\\\r\\\\n\\\\r\\\\nCASH        AUD10.00\\\\r\\\\nSURCHARGE    AUD2.00\\\\r\\\\nTOTAL       AUD12.00\\\\r\\\\n\\\\r\\\\n   (000) APPROVED\\\\r\\\\n\\\\r\\\\n  *CUSTOMER COPY*\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\",\"customer_receipt_printed\":true,\"expiry_date\":\"0722\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............2797\",\"merchant_acquirer\":\"EFTPOS FROM WESTPAC\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341845\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM WESTPAC\\\\r\\\\nMerchant4\\\\r\\\\n213 Miller Street\\\\r\\\\nSydney 2060\\\\r\\\\nAustralia\\\\r\\\\n\\\\r\\\\nTIME 17JUN18   17:09\\\\r\\\\nMID         22341845\\\\r\\\\nTSP     100312348845\\\\r\\\\nRRN     180617000151\\\\r\\\\nDebit(I)         SAV\\\\r\\\\nCARD............2797\\\\r\\\\nAUTH          265035\\\\r\\\\n\\\\r\\\\nCASH        AUD10.00\\\\r\\\\nSURCHARGE    AUD2.00\\\\r\\\\nTOTAL       AUD12.00\\\\r\\\\n\\\\r\\\\n   (000) APPROVED\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\\\\r\\\\n\",\"merchant_receipt_printed\":true,\"online_indicator\":\"Y\",\"pos_ref_id\":\"launder-18-06-2018-03-09-17\",\"rrn\":\"180617000151\",\"scheme_name\":\"Debit\",\"stan\":\"000151\",\"success\":true,\"surcharge_amount\":200,\"terminal_id\":\"100312348845\",\"terminal_ref_id\":\"12348845_18062018031010\",\"transaction_type\":\"CASH\"},\"datetime\":\"2018-06-18T03:10:10.580\",\"event\":\"cash_response\",\"id\":\"cshout4\"}}\n" +
                "\n";

        Message msg = Message.fromJson(jsonStr, null);
        CashoutOnlyResponse response = new CashoutOnlyResponse(msg);

        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals(response.getRequestId(), "cshout4");
        Assert.assertEquals(response.getPosRefId(), "launder-18-06-2018-03-09-17");
        Assert.assertEquals(response.getSchemeName(), "Debit");
        Assert.assertEquals(response.getRRN(), "180617000151");
        Assert.assertEquals(response.getCashoutAmount(), 1200);
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankCashAmount(), 1200);
        Assert.assertEquals(response.getSurchargeAmount(), 200);
        Assert.assertNotNull(response.getCustomerReceipt());
        Assert.assertEquals(response.getResponseText(), "APPROVED");
        Assert.assertEquals(response.getResponseCode(), "000");
        Assert.assertEquals(response.getTerminalReferenceId(), "12348845_18062018031010");
        Assert.assertEquals(response.getAccountType(), "SAVINGS");
        Assert.assertEquals(response.getBankDate(), "17062018");
        Assert.assertNotNull(response.getMerchantReceipt());
        Assert.assertEquals(response.getBankTime(), "170950");
        Assert.assertEquals(response.getMaskedPan(), "............2797");
        Assert.assertEquals(response.getTerminalId(), "100312348845");
        Assert.assertEquals(response.getAuthCode(), "265035");
        Assert.assertTrue(response.wasCustomerReceiptPrinted());
        Assert.assertTrue(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getResponseValue("pos_ref_id"), response.getPosRefId());
    }
}
