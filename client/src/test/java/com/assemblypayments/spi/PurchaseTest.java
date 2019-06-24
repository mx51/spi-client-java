package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PurchaseTest {
    @Test
    public void testPurchaseRequest() {
        int purchaseAmount = 1000;
        String posRefId = "test";

        PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "purchase");
        Assert.assertEquals(purchaseAmount, msg.getDataIntValue("purchase_amount"));
        Assert.assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getPurchaseAmount(), purchaseAmount);
        Assert.assertNotNull(request.getId());
        Assert.assertEquals(request.amountSummary(), "Purchase: 10.00; Tip: 0.00; Cashout: 0.00; Surcharge: 0.00;");
    }

    @Test
    public void testPurchaseRequestWithFull() {
        int purchaseAmount = 1000;
        String posRefId = "test";
        int surchargeAmount = 100;
        int tipAmount = 200;
        boolean promptForCashout = true;
        int cashoutAmount = 200;

        PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        request.setTipAmount(tipAmount);
        request.setSurchargeAmount(surchargeAmount);
        request.setPromptForCashout(promptForCashout);
        request.setCashoutAmount(cashoutAmount);
        Message msg = request.toMessage();

        Assert.assertEquals(purchaseAmount, msg.getDataIntValue("purchase_amount"));
        Assert.assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(surchargeAmount, msg.getDataIntValue("surcharge_amount"));
        Assert.assertEquals(cashoutAmount, msg.getDataIntValue("cash_amount"));
        Assert.assertEquals(promptForCashout, msg.getDataBooleanValue("prompt_for_cashout", false));
    }

    @Test
    public void testPurchaseRequestWithConfig() {
        int purchaseAmount = 1000;
        String posRefId = "test";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPurchaseRequestWithOptions() {
        int purchaseAmount = 1000;
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPurchaseRequestWithOptions_None() {
        int purchaseAmount = 1000;
        String posRefId = "test";

        PurchaseRequest request = new PurchaseRequest(purchaseAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPurchaseResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"SAVINGS\",\"auth_code\":\"278045\",\"bank_cash_amount\":200,\"bank_date\":\"06062019\",\"bank_noncash_amount\":1200,\"bank_settlement_date\":\"06062019\",\"bank_time\":\"110750\",\"card_entry\":\"MAG_STRIPE\",\"cash_amount\":200,\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 06JUN19   11:07\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190606001102\\r\\nDebit(S)         SAV\\r\\nCARD............5581\\r\\nAUTH          278045\\r\\n\\r\\nPURCHASE    AUD10.00\\r\\nCASH         AUD2.00\\r\\nSURCHARGE    AUD2.00\\r\\nTOTAL       AUD14.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"expiry_date\":\"0822\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............5581\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 06JUN19   11:07\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190606001102\\r\\nDebit(S)         SAV\\r\\nCARD............5581\\r\\nAUTH          278045\\r\\n\\r\\nPURCHASE    AUD10.00\\r\\nCASH         AUD2.00\\r\\nSURCHARGE    AUD2.00\\r\\nTOTAL       AUD14.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prchs-06-06-2019-11-07-50\",\"purchase_amount\":1000,\"rrn\":\"190606001102\",\"scheme_name\":\"Debit\",\"stan\":\"001102\",\"success\":true,\"surcharge_amount\":200,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_06062019110812\",\"transaction_type\":\"PURCHASE\"},\"datetime\":\"2019-06-06T11:08:12.946\",\"event\":\"purchase_response\",\"id\":\"prchs5\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PurchaseResponse response = new PurchaseResponse(msg);

        Assert.assertEquals(msg.getEventName(), "purchase_response");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(response.getRequestId(), "prchs5");
        Assert.assertEquals(response.getPosRefId(), "prchs-06-06-2019-11-07-50");
        Assert.assertEquals(response.getSchemeName(), "Debit");
        Assert.assertEquals(response.getRRN(), "190606001102");
        Assert.assertEquals(response.getPurchaseAmount(), 1000);
        Assert.assertEquals(response.getCashoutAmount(), 200);
        Assert.assertEquals(response.getTipAmount(), 0);
        Assert.assertEquals(response.getSurchargeAmount(), 200);
        Assert.assertEquals(response.getBankNonCashAmount(), 1200);
        Assert.assertEquals(response.getBankCashAmount(), 200);
        Assert.assertNotNull(response.getCustomerReceipt());
        Assert.assertNotNull(response.getMerchantReceipt());
        Assert.assertEquals(response.getResponseText(), "APPROVED");
        Assert.assertEquals(response.getResponseCode(), "000");
        Assert.assertEquals(response.getTerminalReferenceId(), "12348842_06062019110812");
        Assert.assertEquals(response.getCardEntry(), "MAG_STRIPE");
        Assert.assertEquals(response.getAccountType(), "SAVINGS");
        Assert.assertEquals(response.getAuthCode(), "278045");
        Assert.assertEquals(response.getBankDate(), "06062019");
        Assert.assertEquals(response.getBankTime(), "110750");
        Assert.assertEquals(response.getMaskedPan(), "............5581");
        Assert.assertEquals(response.getTerminalId(), "100612348842");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
        Assert.assertEquals(response.getResponseValue("pos_ref_id"), response.getPosRefId());
    }

    @Test
    public void testCancelTransactionRequest() {
        CancelTransactionRequest request = new CancelTransactionRequest();
        Message msg = request.toMessage();

        Assert.assertNotNull(msg);
        Assert.assertEquals(msg.getEventName(), "cancel_transaction");
    }

    @Test
    public void testCancelTransactionResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\": {\"event\": \"cancel_response\", \"id\": \"0\", \"datetime\": \"2018-02-06T15:16:44.094\", \"data\": {\"pos_ref_id\": \"123456abc\", \"success\": false, \"error_reason\": \"txn_past_point_of_no_return\", \"error_detail\":\"Too late to cancel transaction\" }}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        CancelTransactionResponse response = new CancelTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "cancel_response");
        Assert.assertFalse(response.isSuccess());
        Assert.assertEquals(response.posRefId, "123456abc");
        Assert.assertEquals(response.getErrorReason(), "txn_past_point_of_no_return");
        Assert.assertNotNull(response.getErrorDetail());
        Assert.assertEquals(response.getResponseValueWithAttribute("pos_ref_id"), response.posRefId);
    }

    @Test
    public void testGetLastTransactionRequest() {
        GetLastTransactionRequest request = new GetLastTransactionRequest();
        Message msg = request.toMessage();

        Assert.assertNotNull(msg);
        Assert.assertEquals(msg.getEventName(), "get_last_transaction");
    }

    @Test
    public void testGetLastTransactionResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"139059\",\"bank_date\":\"14062019\",\"bank_noncash_amount\":1000,\"bank_settlement_date\":\"14062019\",\"bank_time\":\"153747\",\"card_entry\":\"EMV_CTLS\",\"currency\":\"AUD\",\"customer_receipt\":\"\",\"customer_receipt_printed\":false,\"emv_actioncode\":\"ARP\",\"emv_actioncode_values\":\"9BDDE227547B41F43030\",\"emv_pix\":\"1010\",\"emv_rid\":\"A000000003\",\"emv_tsi\":\"0000\",\"emv_tvr\":\"0000000000\",\"expiry_date\":\"1122\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 14JUN19   15:37\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190614001137\\r\\nVisa Credit     \\r\\nVisa(C)           CR\\r\\nCARD............3952\\r\\nAID   A0000000031010\\r\\nTVR       0000000000\\r\\nAUTH          139059\\r\\n\\r\\nPURCHASE    AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n*DUPLICATE  RECEIPT*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prchs-14-06-2019-15-37-49\",\"purchase_amount\":1000,\"rrn\":\"190614001137\",\"scheme_app_name\":\"Visa Credit\",\"scheme_name\":\"Visa\",\"stan\":\"001137\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_14062019153831\",\"transaction_type\":\"PURCHASE\"},\"datetime\":\"2019-06-14T15:38:31.620\",\"event\":\"last_transaction\",\"id\":\"glt10\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.SUCCESS);
        Assert.assertTrue(response.wasSuccessfulTx());
        Assert.assertEquals(response.getTxType(), "PURCHASE");
        Assert.assertEquals(response.getPosRefId(), "prchs-14-06-2019-15-37-49");
        Assert.assertEquals(response.getBankNonCashAmount(), 1000);
        Assert.assertEquals(response.getTransactionAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "14062019153747");
        Assert.assertEquals(response.getRRN(), "190614001137");
        Assert.assertEquals(response.getResponseText(), "APPROVED");
        Assert.assertEquals(response.getResponseCode(), "000");

        response.copyMerchantReceiptToCustomerReceipt();
        Assert.assertEquals(msg.getDataStringValue("customer_receipt"), msg.getDataStringValue("merchant_receipt"));
    }

    @Test
    public void testGetLastTransactionResponse_TimeOutOfSyncError() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"07062019\",\"bank_settlement_date\":\"06062019\",\"bank_time\":\"143821\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"see 'host_response_text' for details\",\"error_reason\":\"TIME_OUT_OF_SYNC\",\"host_response_code\":\"511\",\"host_response_text\":\"TRANS CANCELLED\",\"pos_ref_id\":\"prchs-07-06-2019-14-38-20\",\"rrn\":\"190606000000\",\"scheme_name\":\"TOTAL\",\"stan\":\"000000\",\"success\":false,\"terminal_ref_id\":\"12348842_07062019144136\"},\"datetime\":\"2019-06-07T14:41:36.857\",\"event\":\"last_transaction\",\"id\":\"glt18\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertEquals(msg.getErrorDetail(), "see 'host_response_text' for details");
        Assert.assertTrue(response.wasTimeOutOfSyncError());
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.FAILED);
        Assert.assertFalse(response.wasSuccessfulTx());
        Assert.assertEquals(response.getPosRefId(), "prchs-07-06-2019-14-38-20");
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "07062019143821");
        Assert.assertEquals(response.getRRN(), "190606000000");
        Assert.assertEquals(response.getResponseText(), "TRANS CANCELLED");
        Assert.assertEquals(response.getResponseCode(), "511");
    }

    @Test
    public void testGetLastTransactionResponse_OperationInProgressError() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"07062019\",\"bank_settlement_date\":\"06062019\",\"bank_time\":\"143821\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"see 'host_response_text' for details\",\"error_reason\":\"OPERATION_IN_PROGRESS\",\"host_response_code\":\"511\",\"host_response_text\":\"TRANS CANCELLED\",\"pos_ref_id\":\"prchs-07-06-2019-14-38-20\",\"rrn\":\"190606000000\",\"scheme_name\":\"TOTAL\",\"stan\":\"000000\",\"success\":false,\"terminal_ref_id\":\"12348842_07062019144136\"},\"datetime\":\"2019-06-07T14:41:36.857\",\"event\":\"last_transaction\",\"id\":\"glt18\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertTrue(response.wasOperationInProgressError());
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.FAILED);
        Assert.assertFalse(response.wasSuccessfulTx());
        Assert.assertEquals(response.getPosRefId(), "prchs-07-06-2019-14-38-20");
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "07062019143821");
        Assert.assertEquals(response.getRRN(), "190606000000");
        Assert.assertEquals(response.getResponseText(), "TRANS CANCELLED");
        Assert.assertEquals(response.getResponseCode(), "511");
    }

    @Test
    public void testGetLastTransactionResponse_WaitingForSignatureResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"07062019\",\"bank_settlement_date\":\"06062019\",\"bank_time\":\"143821\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"see 'host_response_text' for details\",\"error_reason\":\"OPERATION_IN_PROGRESS_AWAITING_SIGNATURE\",\"host_response_code\":\"511\",\"host_response_text\":\"TRANS CANCELLED\",\"pos_ref_id\":\"prchs-07-06-2019-14-38-20\",\"rrn\":\"190606000000\",\"scheme_name\":\"TOTAL\",\"stan\":\"000000\",\"success\":false,\"terminal_ref_id\":\"12348842_07062019144136\"},\"datetime\":\"2019-06-07T14:41:36.857\",\"event\":\"last_transaction\",\"id\":\"glt18\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertTrue(response.isWaitingForSignatureResponse());
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.FAILED);
        Assert.assertFalse(response.wasSuccessfulTx());
        Assert.assertEquals(response.getPosRefId(), "prchs-07-06-2019-14-38-20");
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "07062019143821");
        Assert.assertEquals(response.getRRN(), "190606000000");
        Assert.assertEquals(response.getResponseText(), "TRANS CANCELLED");
        Assert.assertEquals(response.getResponseCode(), "511");
    }

    @Test
    public void testGetLastTransactionResponse_WaitingForAuthCode() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"07062019\",\"bank_settlement_date\":\"06062019\",\"bank_time\":\"143821\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"see 'host_response_text' for details\",\"error_reason\":\"OPERATION_IN_PROGRESS_AWAITING_PHONE_AUTH_CODE\",\"host_response_code\":\"511\",\"host_response_text\":\"TRANS CANCELLED\",\"pos_ref_id\":\"prchs-07-06-2019-14-38-20\",\"rrn\":\"190606000000\",\"scheme_name\":\"TOTAL\",\"stan\":\"000000\",\"success\":false,\"terminal_ref_id\":\"12348842_07062019144136\"},\"datetime\":\"2019-06-07T14:41:36.857\",\"event\":\"last_transaction\",\"id\":\"glt18\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertTrue(response.isWaitingForAuthCode());
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.FAILED);
        Assert.assertFalse(response.wasSuccessfulTx());
        Assert.assertEquals(response.getPosRefId(), "prchs-07-06-2019-14-38-20");
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "07062019143821");
        Assert.assertEquals(response.getRRN(), "190606000000");
        Assert.assertEquals(response.getResponseText(), "TRANS CANCELLED");
        Assert.assertEquals(response.getResponseCode(), "511");
    }

    @Test
    public void testGetLastTransactionResponse_StillInProgress() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"07062019\",\"bank_settlement_date\":\"06062019\",\"bank_time\":\"143821\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"see 'host_response_text' for details\",\"error_reason\":\"OPERATION_IN_PROGRESS\",\"host_response_code\":\"511\",\"host_response_text\":\"TRANS CANCELLED\",\"pos_ref_id\":\"prchs-07-06-2019-14-38-20\",\"rrn\":\"190606000000\",\"scheme_name\":\"TOTAL\",\"stan\":\"000000\",\"success\":false,\"terminal_ref_id\":\"12348842_07062019144136\"},\"datetime\":\"2019-06-07T14:41:36.857\",\"event\":\"last_transaction\",\"id\":\"glt18\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        GetLastTransactionResponse response = new GetLastTransactionResponse(msg);

        Assert.assertEquals(msg.getEventName(), "last_transaction");
        Assert.assertTrue(response.isStillInProgress("prchs-07-06-2019-14-38-20"));
        Assert.assertTrue(response.wasRetrievedSuccessfully());
        Assert.assertEquals(response.getSuccessState(), Message.SuccessState.FAILED);
        Assert.assertFalse(response.wasSuccessfulTx());
        Assert.assertEquals(response.getPosRefId(), "prchs-07-06-2019-14-38-20");
        Assert.assertEquals(response.getBankNonCashAmount(), 0);
        Assert.assertEquals(response.getBankDateTimeString(), "07062019143821");
        Assert.assertEquals(response.getRRN(), "190606000000");
        Assert.assertEquals(response.getResponseText(), "TRANS CANCELLED");
        Assert.assertEquals(response.getResponseCode(), "511");
    }

    @Test
    public void testRefundRequest() {
        int refundAmount = 1000;
        String posRefId = "test";
        boolean suppressMerchantPassword = true;

        RefundRequest request = new RefundRequest(refundAmount, posRefId);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "refund");
        Assert.assertEquals(refundAmount, msg.getDataIntValue("refund_amount"));
        Assert.assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(suppressMerchantPassword, msg.getDataBooleanValue("suppress_merchant_password", false));
        Assert.assertNotNull(request.getId());
    }

    @Test
    public void testRefundRequestWithConfig() {
        int refundAmount = 1000;
        String posRefId = "test";
        boolean suppressMerchantPassword = true;

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        RefundRequest request = new RefundRequest(refundAmount, posRefId);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testRefundRequestWithOptions() {
        int refundAmount = 1000;
        String posRefId = "test";
        boolean suppressMerchantPassword = true;
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        RefundRequest request = new RefundRequest(refundAmount, posRefId);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testRefundRequestWithOptions_none() {
        int refundAmount = 1000;
        String posRefId = "test";
        boolean suppressMerchantPassword = true;

        RefundRequest request = new RefundRequest(refundAmount, posRefId);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testRefundResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"067849\",\"bank_date\":\"06062019\",\"bank_noncash_amount\":1000,\"bank_settlement_date\":\"06062019\",\"bank_time\":\"114905\",\"card_entry\":\"EMV_CTLS\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 06JUN19   11:49\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190606001105\\r\\nVisa(C)           CR\\r\\nCARD............5581\\r\\nAUTH          067849\\r\\n\\r\\nREFUND      AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"emv_actioncode\":\"ARQ\",\"emv_actioncode_values\":\"67031BCC5AD15818\",\"expiry_date\":\"0822\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............5581\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 06JUN19   11:49\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190606001105\\r\\nVisa(C)           CR\\r\\nCARD............5581\\r\\nAUTH          067849\\r\\n\\r\\nREFUND      AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"rfnd-06-06-2019-11-49-05\",\"refund_amount\":1000,\"rrn\":\"190606001105\",\"scheme_name\":\"Visa\",\"stan\":\"001105\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_06062019114915\",\"transaction_type\":\"REFUND\"},\"datetime\":\"2019-06-06T11:49:15.038\",\"event\":\"refund_response\",\"id\":\"refund150\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        RefundResponse response = new RefundResponse(msg);

        Assert.assertEquals(msg.getEventName(), "refund_response");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(response.getRequestId(), "refund150");
        Assert.assertEquals(response.getPosRefId(), "rfnd-06-06-2019-11-49-05");
        Assert.assertEquals(response.getSchemeName(), "Visa");
        Assert.assertEquals(response.getRRN(), "190606001105");
        Assert.assertEquals(response.getRefundAmount(), 1000);
        Assert.assertNotNull(response.getCustomerReceipt());
        Assert.assertNotNull(response.getMerchantReceipt());
        Assert.assertEquals(response.getResponseText(), "APPROVED");
        Assert.assertEquals(response.getResponseCode(), "000");
        Assert.assertEquals(response.getTerminalReferenceId(), "12348842_06062019114915");
        Assert.assertEquals(response.getCardEntry(), "EMV_CTLS");
        Assert.assertEquals(response.getAccountType(), "CREDIT");
        Assert.assertEquals(response.getAuthCode(), "067849");
        Assert.assertEquals(response.getBankDate(), "06062019");
        Assert.assertEquals(response.getBankTime(), "114905");
        Assert.assertEquals(response.getMaskedPan(), "............5581");
        Assert.assertEquals(response.getTerminalId(), "100612348842");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
        Assert.assertEquals(response.getResponseValue("pos_ref_id"), response.getPosRefId());
    }

    @Test
    public void testSignatureRequired() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"merchant_receipt\": \"\\nEFTPOS FROM WESTPAC\\nVAAS Product 2\\n275 Kent St\\nSydney 2000\\nAustralia\\n\\n\\nMID         02447506\\nTSP     100381990116\\nTIME 26APR17   11:29\\nRRN     170426000358\\nTRAN 000358   CREDIT\\nAmex               S\\nCARD............4477\\nAUTH          764167\\n\\nPURCHASE   AUD100.00\\nTIP          AUD5.00\\n\\nTOTAL      AUD105.00\\n\\n\\n (001) APPROVE WITH\\n     SIGNATURE\\n\\n\\n\\n\\n\\n\\nSIGN:_______________\\n\\n\\n\\n\\n\\n\\n\\n\",\"pos_ref_id\":\"prchs-06-06-2019-11-49-05\"},\"datetime\": \"2017-04-26T11:30:21.000\",\"event\": \"signature_required\",\"id\": \"24\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        SignatureRequired response = new SignatureRequired(msg);

        Assert.assertEquals(msg.getEventName(), "signature_required");
        Assert.assertEquals(response.getRequestId(), "24");
        Assert.assertEquals(response.getPosRefId(), "prchs-06-06-2019-11-49-05");
        Assert.assertNotNull(response.getMerchantReceipt());
    }

    @Test
    public void testSignatureRequired_MissingReceipt() {
        String posRefId = "test";
        String requestId = "12";
        String receiptToSign = "MISSING RECEIPT\n DECLINE AND TRY AGAIN.";
        SignatureRequired response = new SignatureRequired(posRefId, requestId, receiptToSign);

        Assert.assertEquals(response.getMerchantReceipt(), receiptToSign);
    }

    @Test
    public void testSignatureDecline() {
        String posRefId = "test";
        SignatureDecline request = new SignatureDecline(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "signature_decline");
        Assert.assertEquals(posRefId, request.getSignatureRequiredRequestId());
    }

    @Test
    public void testSignatureAccept() {
        String posRefId = "test";
        SignatureAccept request = new SignatureAccept(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "signature_accept");
        Assert.assertEquals(posRefId, request.getSignatureRequiredRequestId());
    }

    @Test
    public void testMotoPurchaseRequest() {
        String posRefId = "test";
        int purchaseAmount = 1000;
        int surchargeAmount = 200;
        boolean suppressMerchantPassword = true;

        MotoPurchaseRequest request = new MotoPurchaseRequest(purchaseAmount, posRefId);
        request.setSurchargeAmount(surchargeAmount);
        request.setSuppressMerchantPassword(suppressMerchantPassword);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "moto_purchase");
        Assert.assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(surchargeAmount, msg.getDataIntValue("surcharge_amount"));
        Assert.assertEquals(purchaseAmount, msg.getDataIntValue("purchase_amount"));
        Assert.assertEquals(suppressMerchantPassword, msg.getDataBooleanValue("suppress_merchant_password", false));
    }

    @Test
    public void testMotoPurchaseRequestWithConfig() {
        int purchaseAmount = 1000;
        String posRefId = "test";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        MotoPurchaseRequest request = new MotoPurchaseRequest(purchaseAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testMotoPurchaseRequestWithOptions() {
        int purchaseAmount = 1000;
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        MotoPurchaseRequest request = new MotoPurchaseRequest(purchaseAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testMotoPurchaseRequesteWithOptions_none() {
        int purchaseAmount = 1000;
        String posRefId = "test";

        MotoPurchaseRequest request = new MotoPurchaseRequest(purchaseAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testMotoPurchaseResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"pos_ref_id\":\"a-zA-Z0-9\",\"account_type\": \"CREDIT\",\"purchase_amount\": 1000,\"surcharge_amount\": 200,\"bank_noncash_amount\": 1200,\"bank_cash_amount\": 200,\"auth_code\": \"653230\",\"bank_date\": \"07092017\",\"bank_time\": \"152137\",\"bank_settlement_date\": \"21102017\",\"currency\": \"AUD\",\"emv_actioncode\": \"\",\"emv_actioncode_values\": \"\",\"emv_pix\": \"\",\"emv_rid\": \"\",\"emv_tsi\": \"\",\"emv_tvr\": \"\",\"expiry_date\": \"1117\",\"host_response_code\": \"000\",\"host_response_text\": \"APPROVED\",\"informative_text\": \"                \",\"masked_pan\": \"............0794\",\"merchant_acquirer\": \"EFTPOS FROM WESTPAC\",\"merchant_addr\": \"275 Kent St\",\"merchant_city\": \"Sydney\",\"merchant_country\": \"Australia\",\"merchant_id\": \"02447508\",\"merchant_name\": \"VAAS Product 4\",\"merchant_postcode\": \"2000\",\"online_indicator\": \"Y\",\"scheme_app_name\": \"\",\"scheme_name\": \"\",\"stan\": \"000212\",\"rrn\": \"1517890741\",\"success\": true,\"terminal_id\": \"100381990118\",\"transaction_type\": \"MOTO\",\"card_entry\": \"MANUAL_PHONE\",\"customer_receipt\":\"EFTPOS FROM WESTPAC\\r\\nVAAS Product 4\\r\\n275 Kent St\\r\\nSydney\\r\\nMID02447508\\r\\nTSP100381990118\\r\\nTIME 07SEP17   15:21\\r\\nRRN     1517890741\\r\\nTRAN 000212   CREDIT\\r\\nVisa Credit     \\r\\nVisa               M\\r\\nCARD............0794\\r\\nAUTH          653230\\r\\n\\r\\nMOTO   AUD10000\\r\\n\\r\\nTOTAL      AUD10000\\r\\n\\r\\n\\r\\n(000)APPROVED\\r\\n\\r\\n\\r\\n *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt\":\"EFTPOS FROM WESTPAC\\r\\nVAAS Product4\\r\\n275 Kent St\\r\\nSydney\\r\\nMID02447508\\r\\nTSP100381990118\\r\\nTIME 07SEP17   15:21\\r\\nRRN     1517890741\\r\\nTRAN 000212   CREDIT\\r\\nVisa Credit     \\r\\nVisa               M\\r\\nCARD............0794\\r\\nAUTH          653230\\r\\n\\r\\nPURCHASE   AUD10000\\r\\n\\r\\nTOTAL      AUD10000\\r\\n\\r\\n\\r\\n(000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\"},\"datetime\": \"2018-02-06T04:19:00.545\",\"event\": \"moto_purchase_response\",\"id\": \"4\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        MotoPurchaseResponse response = new MotoPurchaseResponse(msg);

        Assert.assertEquals(msg.getEventName(), "moto_purchase_response");
        Assert.assertTrue(response.getPurchaseResponse().isSuccess());
        Assert.assertEquals(response.getPurchaseResponse().getRequestId(), "4");
        Assert.assertEquals(response.getPurchaseResponse().getPosRefId(), "a-zA-Z0-9");
        Assert.assertEquals(response.getPurchaseResponse().getSchemeName(), "");
        Assert.assertEquals(response.getPurchaseResponse().getAuthCode(), "653230");
        Assert.assertEquals(response.getPurchaseResponse().getRRN(), "1517890741");
        Assert.assertEquals(response.getPurchaseResponse().getPurchaseAmount(), 1000);
        Assert.assertEquals(response.getPurchaseResponse().getSurchargeAmount(), 200);
        Assert.assertEquals(response.getPurchaseResponse().getBankNonCashAmount(), 1200);
        Assert.assertEquals(response.getPurchaseResponse().getBankCashAmount(), 200);
        Assert.assertNotNull(response.getPurchaseResponse().getCustomerReceipt());
        Assert.assertNotNull(response.getPurchaseResponse().getMerchantReceipt());
        Assert.assertEquals(response.getPurchaseResponse().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getPurchaseResponse().getResponseCode(), "000");
        Assert.assertEquals(response.getPurchaseResponse().getCardEntry(), "MANUAL_PHONE");
        Assert.assertEquals(response.getPurchaseResponse().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getPurchaseResponse().getBankDate(), "07092017");
        Assert.assertEquals(response.getPurchaseResponse().getBankTime(), "152137");
        Assert.assertEquals(response.getPurchaseResponse().getMaskedPan(), "............0794");
        Assert.assertEquals(response.getPurchaseResponse().getTerminalId(), "100381990118");
        Assert.assertFalse(response.getPurchaseResponse().wasCustomerReceiptPrinted());
        Assert.assertFalse(response.getPurchaseResponse().wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getPurchaseResponse().getResponseValue("pos_ref_id"), response.getPosRefId());
    }

    @Test
    public void testPhoneForAuthRequired() {
        String posRefId = "xyz";
        String merchantId = "12345678";
        String requestId = "20";
        String phoneNumnber = "1800999999";

        PhoneForAuthRequired request = new PhoneForAuthRequired(posRefId, requestId, phoneNumnber, merchantId);

        Assert.assertEquals(request.getPosRefId(), posRefId);
        Assert.assertEquals(request.getRequestId(), requestId);
        Assert.assertEquals(request.getPhoneNumber(), phoneNumnber);
        Assert.assertEquals(request.getMerchantId(), merchantId);
    }

    @Test
    public void testPhoneForAuthRequiredWithMessage() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"event\":\"authorisation_code_required\",\"id\":\"20\",\"datetime\":\"2017-11-01T06:09:33.918\",\"data\":{\"merchant_id\":\"12345678\",\"auth_centre_phone_number\":\"1800999999\",\"pos_ref_id\": \"xyz\"}}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PhoneForAuthRequired request = new PhoneForAuthRequired(msg);

        Assert.assertEquals(msg.getEventName(), "authorisation_code_required");
        Assert.assertEquals(request.getPosRefId(), "xyz");
        Assert.assertEquals(request.getRequestId(), "20");
        Assert.assertEquals(request.getPhoneNumber(), "1800999999");
        Assert.assertEquals(request.getMerchantId(), "12345678");
    }

    @Test
    public void testAuthCodeAdvice() {
        String posRefId = "xyz";
        String authcode = "1234ab";

        AuthCodeAdvice request = new AuthCodeAdvice(posRefId, authcode);
        Message msg = request.toMessage();

        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getAuthCode(), msg.getDataStringValue("auth_code"));
    }
}
