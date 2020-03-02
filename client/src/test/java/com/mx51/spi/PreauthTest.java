package io.mx51.spi;

import io.mx51.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PreauthTest {
    @Test
    public void testAccountVerifyRequest() {
        final String posRefId = "test";

        AccountVerifyRequest request = new AccountVerifyRequest(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "account_verify");
        Assert.assertEquals(request.getPosRefId(), posRefId);
    }

    @Test
    public void testAccountVerifyResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        final String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"316810\",\"bank_date\":\"11062019\",\"bank_settlement_date\":\"11062019\",\"bank_time\":\"182739\",\"card_entry\":\"EMV_INSERT\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:27\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001109\\r\\nVisa Credit     \\r\\nVisa(I)           CR\\r\\nCARD............3952\\r\\nAID   A0000000031010\\r\\nTVR       0080048000\\r\\nAUTH          316810\\r\\n\\r\\nA/C VERIFIED AUD0.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"emv_actioncode\":\"TC\",\"emv_actioncode_values\":\"F1F17B37A5BEF2B1\",\"emv_pix\":\"1010\",\"emv_rid\":\"A000000003\",\"emv_tsi\":\"F800\",\"emv_tvr\":\"0080048000\",\"expiry_date\":\"1122\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:27\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001109\\r\\nVisa Credit     \\r\\nVisa(I)           CR\\r\\nCARD............3952\\r\\nAID   A0000000031010\\r\\nTVR       0080048000\\r\\nAUTH          316810\\r\\n\\r\\nA/C VERIFIED AUD0.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"actvfy-11-06-2019-18-27-39\",\"rrn\":\"190611001109\",\"scheme_app_name\":\"Visa Credit\",\"scheme_name\":\"Visa\",\"stan\":\"001109\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019182754\",\"transaction_type\":\"A/C VERIFIED\"},\"datetime\":\"2019-06-11T18:27:54.933\",\"event\":\"account_verify_response\",\"id\":\"prav15\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        AccountVerifyResponse response = new AccountVerifyResponse(msg);

        Assert.assertEquals(msg.getEventName(), "account_verify_response");
        Assert.assertEquals(response.getPosRefId(), "actvfy-11-06-2019-18-27-39");
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getRequestId(), "prav15");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001109");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "EMV_INSERT");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getAuthCode(), "316810");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "182739");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertFalse(response.getDetails().wasCustomerReceiptPrinted());
        Assert.assertFalse(response.getDetails().wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthOpenRequest() throws IllegalAccessException {
        int preauthAmount = 1000;
        String posRefId = "test";

        PreauthOpenRequest request = new PreauthOpenRequest(preauthAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "preauth");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(preauthAmount, msg.getDataIntValue("preauth_amount"));
    }

    @Test
    public void testPreauthOpenRequestWithConfig() throws IllegalAccessException {
        int preauthAmount = 1000;
        String posRefId = "test";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setEnabledPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthOpenRequest request = new PreauthOpenRequest(preauthAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthOpenRequestWithOptions() {
        int preauthAmount = 1000;
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthOpenRequest request = new PreauthOpenRequest(preauthAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthOpenRequestWithOptions_None() {
        int preauthAmount = 1000;
        String posRefId = "test";

        PreauthOpenRequest request = new PreauthOpenRequest(preauthAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreautOpenResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"318981\",\"bank_date\":\"11062019\",\"bank_noncash_amount\":1000,\"bank_settlement_date\":\"11062019\",\"bank_time\":\"182808\",\"card_entry\":\"EMV_INSERT\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:28\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001110\\r\\nVisa Credit     \\r\\nVisa(I)           CR\\r\\nCARD............3952\\r\\nAID   A0000000031010\\r\\nTVR       0080048000\\r\\nAUTH          318981\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"emv_actioncode\":\"TC\",\"emv_actioncode_values\":\"C0A8342DF36207F1\",\"emv_pix\":\"1010\",\"emv_rid\":\"A000000003\",\"emv_tsi\":\"F800\",\"emv_tvr\":\"0080048000\",\"expiry_date\":\"1122\",\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:28\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001110\\r\\nVisa Credit     \\r\\nVisa(I)           CR\\r\\nCARD............3952\\r\\nAID   A0000000031010\\r\\nTVR       0080048000\\r\\nAUTH          318981\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"propen-11-06-2019-18-28-08\",\"preauth_amount\":1000,\"preauth_id\":\"15765372\",\"rrn\":\"190611001110\",\"scheme_app_name\":\"Visa Credit\",\"scheme_name\":\"Visa\",\"stan\":\"001110\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019182827\",\"transaction_type\":\"PRE-AUTH\"},\"datetime\":\"2019-06-11T18:28:27.237\",\"event\":\"preauth_response\",\"id\":\"prac17\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "preauth_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "propen-11-06-2019-18-28-08");
        Assert.assertEquals(response.getCompletionAmount(), 0);
        Assert.assertEquals(response.getBalanceAmount(), 1000);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 0);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 0);
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getRequestId(), "prac17");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001110");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "EMV_INSERT");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getAuthCode(), "318981");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "182808");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019182827");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthTopupRequest() {
        int topupAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthTopupRequest request = new PreauthTopupRequest(preauthId, topupAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "preauth_topup");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getTopupAmount(), msg.getDataIntValue("topup_amount"));
    }

    @Test
    public void testPreauthTopupRequestWithConfig() {
        int topupAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthTopupRequest request = new PreauthTopupRequest(preauthId, topupAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthTopupRequestWithOptions() {
        int topupAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthTopupRequest request = new PreauthTopupRequest(preauthId, topupAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthTopupRequestWithOptions_None() {
        int topupAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthTopupRequest request = new PreauthTopupRequest(preauthId, topupAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthTopupResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"318981\",\"balance_amount\":1500,\"bank_date\":\"11062019\",\"bank_settlement_date\":\"11062019\",\"bank_time\":\"182852\",\"card_entry\":\"MANUAL\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:28\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001111\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nAUTH          318981\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\nTOP-UP       AUD5.00\\r\\nBALANCE     AUD15.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"existing_preauth_amount\":1000,\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:28\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001111\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nAUTH          318981\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\nTOP-UP       AUD5.00\\r\\nBALANCE     AUD15.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prtopup-15765372-11-06-2019-18-28-50\",\"preauth_id\":\"15765372\",\"rrn\":\"190611001111\",\"scheme_name\":\"Visa\",\"stan\":\"001111\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019182857\",\"topup_amount\":500,\"transaction_type\":\"TOPUP\"},\"datetime\":\"2019-06-11T18:28:57.154\",\"event\":\"preauth_topup_response\",\"id\":\"prtu21\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "preauth_topup_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "prtopup-15765372-11-06-2019-18-28-50");
        Assert.assertEquals(response.getCompletionAmount(), 0);
        Assert.assertEquals(response.getBalanceAmount(), 1500);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 1000);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 0);
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getRequestId(), "prtu21");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001111");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "MANUAL");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getAuthCode(), "318981");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "182852");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019182857");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthPartialCancellationRequest() {
        int partialCancellationAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthPartialCancellationRequest request = new PreauthPartialCancellationRequest(preauthId, partialCancellationAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "preauth_partial_cancellation");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getPartialCancellationAmount(), msg.getDataIntValue("preauth_cancel_amount"));
    }

    @Test
    public void testPreauthPartialCancellationRequestWithConfig() {
        int partialCancellationAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthPartialCancellationRequest request = new PreauthPartialCancellationRequest(preauthId, partialCancellationAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthPartialCancellationRequestWithOptions() {
        int partialCancellationAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthPartialCancellationRequest request = new PreauthPartialCancellationRequest(preauthId, partialCancellationAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }


    @Test
    public void testPreauthPartialCancellationRequestWithOptions_None() {
        int partialCancellationAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthPartialCancellationRequest request = new PreauthPartialCancellationRequest(preauthId, partialCancellationAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthPartialCancellationResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"balance_amount\":1000,\"bank_date\":\"11062019\",\"bank_settlement_date\":\"11062019\",\"bank_time\":\"182926\",\"card_entry\":\"MANUAL\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\n*--PARTIAL CANCEL--*\\r\\n\\r\\nTIME 11JUN19   18:29\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001112\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD15.00\\r\\nCANCEL       AUD5.00\\r\\nBALANCE     AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n*--PARTIAL CANCEL--*\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"existing_preauth_amount\":1500,\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\n*--PARTIAL CANCEL--*\\r\\n\\r\\nTIME 11JUN19   18:29\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001112\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD15.00\\r\\nCANCEL       AUD5.00\\r\\nBALANCE     AUD10.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n*--PARTIAL CANCEL--*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prtopd-15765372-11-06-2019-18-29-22\",\"preauth_cancel_amount\":500,\"preauth_id\":\"15765372\",\"rrn\":\"190611001112\",\"scheme_name\":\"Visa\",\"stan\":\"001112\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019182927\",\"transaction_type\":\"CANCEL\"},\"datetime\":\"2019-06-11T18:29:27.258\",\"event\":\"preauth_partial_cancellation_response\",\"id\":\"prpc24\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "preauth_partial_cancellation_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "prtopd-15765372-11-06-2019-18-29-22");
        Assert.assertEquals(response.getCompletionAmount(), 0);
        Assert.assertEquals(response.getBalanceAmount(), 1000);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 1500);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 0);
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getRequestId(), "prpc24");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001112");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "MANUAL");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "182926");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019182927");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthExtendRequest() {
        String posRefId = "test";
        String preauthId = "123456";

        PreauthExtendRequest request = new PreauthExtendRequest(preauthId, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "preauth_extend");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getPreauthId(), msg.getDataStringValue("preauth_id"));
    }

    @Test
    public void testPreauthExtendRequestWithConfig() {
        String posRefId = "test";
        String preauthId = "123456";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthExtendRequest request = new PreauthExtendRequest(preauthId, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthExtendRequestWithOptions() {
        String posRefId = "test";
        String preauthId = "123456";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthExtendRequest request = new PreauthExtendRequest(preauthId, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthExtendRequestWithOptions_None() {
        String posRefId = "test";
        String preauthId = "123456";

        PreauthExtendRequest request = new PreauthExtendRequest(preauthId, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthExtendResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"auth_code\":\"793647\",\"balance_amount\":1000,\"bank_date\":\"11062019\",\"bank_settlement_date\":\"11062019\",\"bank_time\":\"182942\",\"card_entry\":\"MANUAL\",\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:29\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001113\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nAUTH          793647\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\nTOP-UP       AUD5.00\\r\\nCANCEL       AUD5.00\\r\\nBALANCE     AUD10.00\\r\\nPRE-AUTH EXT AUD0.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"existing_preauth_amount\":1000,\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:29\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001113\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nAUTH          793647\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPRE-AUTH    AUD10.00\\r\\nTOP-UP       AUD5.00\\r\\nCANCEL       AUD5.00\\r\\nBALANCE     AUD10.00\\r\\nPRE-AUTH EXT AUD0.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prtopd-15765372-11-06-2019-18-29-39\",\"preauth_cancel_amount\":500,\"preauth_id\":\"15765372\",\"rrn\":\"190611001113\",\"scheme_name\":\"Visa\",\"stan\":\"001113\",\"success\":true,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019182946\",\"topup_amount\":500,\"transaction_type\":\"PRE-AUTH EXT\"},\"datetime\":\"2019-06-11T18:29:46.234\",\"event\":\"preauth_extend_response\",\"id\":\"prext26\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "preauth_extend_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "prtopd-15765372-11-06-2019-18-29-39");
        Assert.assertEquals(response.getCompletionAmount(), 0);
        Assert.assertEquals(response.getBalanceAmount(), 1000);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 1000);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 0);
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getAuthCode(), "793647");
        Assert.assertEquals(response.getDetails().getRequestId(), "prext26");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001113");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "MANUAL");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "182942");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019182946");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthCancelRequest() {
        String posRefId = "test";
        String preauthId = "123456";

        PreauthCancelRequest request = new PreauthCancelRequest(preauthId, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "preauth_cancellation");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getPreauthId(), msg.getDataStringValue("preauth_id"));
    }

    @Test
    public void testPreauthCancelRequestWithConfig() {
        String posRefId = "test";
        String preauthId = "123456";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthCancelRequest request = new PreauthCancelRequest(preauthId, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthCancelRequestWithOptions() {
        String posRefId = "test";
        String preauthId = "123456";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthCancelRequest request = new PreauthCancelRequest(preauthId, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthCancelRequestWithOptions_None() {
        String posRefId = "test";
        String preauthId = "123456";

        PreauthCancelRequest request = new PreauthCancelRequest(preauthId, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthCancelResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"NOT-SET\",\"bank_date\":\"11062019\",\"bank_settlement_date\":\"11062019\",\"bank_time\":\"183041\",\"card_entry\":\"NOT-SET\",\"error_detail\":\"Pre Auth ID 15765372 has already been completed\",\"error_reason\":\"PRE_AUTH_ID_ALREADY_COMPLETED\",\"host_response_text\":\"DECLINED\",\"pos_ref_id\":\"prtopd-15765372-11-06-2019-18-30-40\",\"preauth_amount\":0,\"preauth_id\":\"15765372\",\"scheme_name\":\"Visa\",\"stan\":\"001114\",\"success\":false,\"terminal_ref_id\":\"12348842_11062019183043\",\"transaction_type\":\"PRE-AUTH CANCEL\"},\"datetime\":\"2019-06-11T18:30:43.104\",\"event\":\"preauth_cancellation_response\",\"id\":\"prac31\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "preauth_cancellation_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "prtopd-15765372-11-06-2019-18-30-40");
        Assert.assertEquals(response.getCompletionAmount(), 0);
        Assert.assertEquals(response.getBalanceAmount(), 0);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 0);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 0);
        Assert.assertFalse(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getRequestId(), "prac31");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getResponseText(), "DECLINED");
        Assert.assertEquals(response.getDetails().getCardEntry(), "NOT-SET");
        Assert.assertEquals(response.getDetails().getAccountType(), "NOT-SET");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "183041");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019183043");
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }

    @Test
    public void testPreauthCompletionRequest() {
        int completionAmount = 1000;
        int surchargeAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthCompletionRequest request = new PreauthCompletionRequest(preauthId, completionAmount, posRefId);
        request.setSurchargeAmount(surchargeAmount);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "completion");
        Assert.assertEquals(request.getPosRefId(), msg.getDataStringValue("pos_ref_id"));
        Assert.assertEquals(request.getPreauthId(), msg.getDataStringValue("preauth_id"));
        Assert.assertEquals(request.getCompletionAmount(), msg.getDataIntValue("completion_amount"));
        Assert.assertEquals(request.getSurchargeAmount(), msg.getDataIntValue("surcharge_amount"));
    }

    @Test
    public void testPreauthCompletionRequestWithConfig() {
        int completionAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(false);
        config.setSignatureFlowOnEftpos(true);

        PreauthCompletionRequest request = new PreauthCompletionRequest(preauthId, completionAmount, posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertEquals(config.isPrintMerchantCopy(), msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertEquals(config.isPromptForCustomerCopyOnEftpos(), msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertEquals(config.isSignatureFlowOnEftpos(), msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testPreauthCompletionRequestWithOptions() {
        int completionAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        PreauthCompletionRequest request = new PreauthCompletionRequest(preauthId, completionAmount, posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthCompletionRequestWithOptions_None() {
        int completionAmount = 1000;
        String posRefId = "test";
        String preauthId = "123456";

        PreauthCompletionRequest request = new PreauthCompletionRequest(preauthId, completionAmount, posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testPreauthCompletionResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"account_type\":\"CREDIT\",\"bank_date\":\"11062019\",\"bank_noncash_amount\":900,\"bank_settlement_date\":\"11062019\",\"bank_time\":\"183025\",\"card_entry\":\"MANUAL\",\"completion_amount\":800,\"currency\":\"AUD\",\"customer_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:30\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001114\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPCOMP        AUD8.00\\r\\nSURCHARGE    AUD1.00\\r\\nTOTAL        AUD9.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n  *CUSTOMER COPY*\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"customer_receipt_printed\":false,\"host_response_code\":\"000\",\"host_response_text\":\"APPROVED\",\"informative_text\":\"                \",\"masked_pan\":\"............3952\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_addr\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_id\":\"22341842\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\nAustralia\\r\\n\\r\\nTIME 11JUN19   18:30\\r\\nMID         22341842\\r\\nTSP     100612348842\\r\\nRRN     190611001114\\r\\nVisa(M)           CR\\r\\nCARD............3952\\r\\nPRE-AUTH ID 15765372\\r\\n\\r\\nPCOMP        AUD8.00\\r\\nSURCHARGE    AUD1.00\\r\\nTOTAL        AUD9.00\\r\\n\\r\\n   (000) APPROVED\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"merchant_receipt_printed\":false,\"online_indicator\":\"Y\",\"pos_ref_id\":\"prcomp-15765372-11-06-2019-18-30-16\",\"preauth_cancel_amount\":500,\"preauth_id\":\"15765372\",\"rrn\":\"190611001114\",\"scheme_name\":\"Visa\",\"stan\":\"001114\",\"success\":true,\"surcharge_amount\":100,\"terminal_id\":\"100612348842\",\"terminal_ref_id\":\"12348842_11062019183026\",\"topup_amount\":500,\"transaction_type\":\"PCOMP\"},\"datetime\":\"2019-06-11T18:30:26.613\",\"event\":\"completion_response\",\"id\":\"prac29\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PreauthResponse response = new PreauthResponse(msg);

        Assert.assertEquals(msg.getEventName(), "completion_response");
        Assert.assertEquals(response.getPreauthId(), "15765372");
        Assert.assertEquals(response.getPosRefId(), "prcomp-15765372-11-06-2019-18-30-16");
        Assert.assertEquals(response.getCompletionAmount(), 800);
        Assert.assertEquals(response.getBalanceAmount(), 0);
        Assert.assertEquals(response.getPreviousBalanceAmount(), 800);
        Assert.assertEquals(response.getCompletionSurchargeAmount(), 100);
        Assert.assertTrue(response.getDetails().isSuccess());
        Assert.assertEquals(response.getDetails().getBankNonCashAmount(), 900);
        Assert.assertEquals(response.getDetails().getRequestId(), "prac29");
        Assert.assertEquals(response.getDetails().getSchemeName(), "Visa");
        Assert.assertEquals(response.getDetails().getRRN(), "190611001114");
        Assert.assertNotNull(response.getDetails().getCustomerReceipt());
        Assert.assertNotNull(response.getDetails().getMerchantReceipt());
        Assert.assertEquals(response.getDetails().getResponseText(), "APPROVED");
        Assert.assertEquals(response.getDetails().getResponseCode(), "000");
        Assert.assertEquals(response.getDetails().getCardEntry(), "MANUAL");
        Assert.assertEquals(response.getDetails().getAccountType(), "CREDIT");
        Assert.assertEquals(response.getDetails().getBankDate(), "11062019");
        Assert.assertEquals(response.getDetails().getBankTime(), "183025");
        Assert.assertEquals(response.getDetails().getMaskedPan(), "............3952");
        Assert.assertEquals(response.getDetails().getTerminalId(), "100612348842");
        Assert.assertEquals(response.getDetails().getTerminalReferenceId(), "12348842_11062019183026");
        Assert.assertFalse(response.wasCustomerReceiptPrinted());
        Assert.assertFalse(response.wasMerchantReceiptPrinted());
        Assert.assertEquals(response.getDetails().getSettlementDate(), new SimpleDateFormat("ddMMyyyy", Locale.US).parse(msg.getDataStringValue("bank_settlement_date")));
    }
}
