package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SettlementTest {

    @Test
    public void testParseDate_startTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_period_start_time", "05:01");
        data.put("settlement_period_start_date", "05Oct17");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long startTime = r.getPeriodStartTime();
        Calendar startTimeCalendar = Calendar.getInstance();
        startTimeCalendar.set(Calendar.YEAR, 2017);
        startTimeCalendar.set(Calendar.MONTH, 9);
        startTimeCalendar.set(Calendar.DAY_OF_MONTH, 5);
        startTimeCalendar.set(Calendar.HOUR_OF_DAY, 5);
        startTimeCalendar.set(Calendar.MINUTE, 1);
        startTimeCalendar.set(Calendar.SECOND, 0);
        startTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(startTimeCalendar.getTimeInMillis(), startTime);
    }

    @Test
    public void testParseDate_endTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_period_end_time", "06:02");
        data.put("settlement_period_end_date", "06Nov18");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long endTime = r.getPeriodEndTime();
        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(Calendar.YEAR, 2018);
        endTimeCalendar.set(Calendar.MONTH, 10);
        endTimeCalendar.set(Calendar.DAY_OF_MONTH, 6);
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endTimeCalendar.set(Calendar.MINUTE, 2);
        endTimeCalendar.set(Calendar.SECOND, 0);
        endTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(endTimeCalendar.getTimeInMillis(), endTime);
    }

    @Test
    public void testParseDate_trigTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_triggered_time", "07:03:45");
        data.put("settlement_triggered_date", "07Dec19");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long trigTime = r.getTriggeredTime();
        Calendar trigTimeCalendar = Calendar.getInstance();
        trigTimeCalendar.set(Calendar.YEAR, 2019);
        trigTimeCalendar.set(Calendar.MONTH, 11);
        trigTimeCalendar.set(Calendar.DAY_OF_MONTH, 7);
        trigTimeCalendar.set(Calendar.HOUR_OF_DAY, 7);
        trigTimeCalendar.set(Calendar.MINUTE, 3);
        trigTimeCalendar.set(Calendar.SECOND, 45);
        trigTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(trigTimeCalendar.getTimeInMillis(), trigTime);
    }

    @Test
    public void testSettleRequest() {
        String posRefId = "test";

        SettleRequest request = new SettleRequest(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "settle");
        Assert.assertEquals(request.getId(), posRefId);
    }

    @Test
    public void testSettleRequestWithConfig() {
        String posRefId = "test";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(true);
        config.setSignatureFlowOnEftpos(true);

        SettleRequest request = new SettleRequest(posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertTrue(msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertFalse(msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertFalse(msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testSettleRequestWithOptions() {
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        SettleRequest request = new SettleRequest(posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testSettleRequestWithOptions_None() {
        String posRefId = "test";

        SettleRequest request = new SettleRequest(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testSettlementResponse() throws ParseException {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"accumulacxted_purchase_count\":\"1\",\"accumulated_purchase_value\":\"1000\",\"accumulated_settle_by_acquirer_count\":\"1\",\"accumulated_settle_by_acquirer_value\":\"1000\",\"accumulated_total_count\":\"1\",\"accumulated_total_value\":\"1000\",\"bank_date\":\"14062019\",\"bank_time\":\"160940\",\"host_response_code\":\"941\",\"host_response_text\":\"CUTOVER COMPLETE\",\"merchant_acquirer\":\"EFTPOS FROM BANK SA\",\"merchant_address\":\"213 Miller Street\",\"merchant_city\":\"Sydney\",\"merchant_country\":\"Australia\",\"merchant_name\":\"Merchant4\",\"merchant_postcode\":\"2060\",\"merchant_receipt\":\"EFTPOS FROM BANK SA\\r\\nMerchant4\\r\\n213 Miller Street\\r\\nSydney 2060\\r\\n\\r\\nAustralia\\r\\n\\r\\n\\r\\n SETTLEMENT CUTOVER\\r\\nTSP     100612348842\\r\\nTIME   14JUN19 16:09\\r\\nTRAN   001137-001137\\r\\nFROM   13JUN19 20:00\\r\\nTO     14JUN19 16:09\\r\\n\\r\\nDebit\\r\\nTOT     0      $0.00\\r\\n\\r\\nMasterCard\\r\\nTOT     0      $0.00\\r\\n\\r\\nVisa\\r\\nPUR     1     $10.00\\r\\nTOT     1     $10.00\\r\\n\\r\\nBANKED  1     $10.00\\r\\n\\r\\nAmex\\r\\nTOT     0      $0.00\\r\\n\\r\\nDiners\\r\\nTOT     0      $0.00\\r\\n\\r\\nJCB\\r\\nTOT     0      $0.00\\r\\n\\r\\nUnionPay\\r\\nTOT     0      $0.00\\r\\n\\r\\nTOTAL\\r\\nPUR     1     $10.00\\r\\nTOT     1     $10.00\\r\\n\\r\\n (941) CUTOVER COMP\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\",\"schemes\":[{\"scheme_name\":\"Debit\",\"settle_by_acquirer\":\"Yes\",\"total_count\":\"0\",\"total_value\":\"0\"},{\"scheme_name\":\"MasterCard\",\"settle_by_acquirer\":\"Yes\",\"total_count\":\"0\",\"total_value\":\"0\"},{\"scheme_name\":\"Visa\",\"settle_by_acquirer\":\"Yes\",\"total_count\":\"1\",\"total_purchase_count\":\"1\",\"total_purchase_value\":\"1000\",\"total_value\":\"1000\"},{\"scheme_name\":\"Amex\",\"settle_by_acquirer\":\"No\",\"total_count\":\"0\",\"total_value\":\"0\"},{\"scheme_name\":\"Diners\",\"settle_by_acquirer\":\"No\",\"total_count\":\"0\",\"total_value\":\"0\"},{\"scheme_name\":\"JCB\",\"settle_by_acquirer\":\"No\",\"total_count\":\"0\",\"total_value\":\"0\"},{\"scheme_name\":\"UnionPay\",\"settle_by_acquirer\":\"No\",\"total_count\":\"0\",\"total_value\":\"0\"}],\"settlement_period_end_date\":\"14Jun19\",\"settlement_period_end_time\":\"16:09\",\"settlement_period_start_date\":\"13Jun19\",\"settlement_period_start_time\":\"20:00\",\"settlement_triggered_date\":\"14Jun19\",\"settlement_triggered_time\":\"16:09:40\",\"stan\":\"000000\",\"success\":true,\"terminal_id\":\"100612348842\",\"transaction_range\":\"001137-001137\"},\"datetime\":\"2019-06-14T16:09:46.395\",\"event\":\"settle_response\",\"id\":\"settle116\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        Settlement response = new Settlement(msg);

        Assert.assertEquals(msg.getEventName(), "settle_response");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(response.getRequestId(), "settle116");
        Assert.assertEquals(response.getSettleByAcquirerCount(), 1);
        Assert.assertEquals(response.getSettleByAcquirerValue(), 1000);
        Assert.assertEquals(response.getTotalCount(), 1);
        Assert.assertEquals(response.getTotalValue(), 1000);
        Assert.assertEquals(response.getPeriodStartTime(), new SimpleDateFormat("HH:mmddMMMyy", Locale.US).parse(msg.getDataStringValue("settlement_period_start_time") + msg.getDataStringValue("settlement_period_start_date")).getTime());
        Assert.assertEquals(response.getPeriodEndTime(), new SimpleDateFormat("HH:mmddMMMyy", Locale.US).parse(msg.getDataStringValue("settlement_period_end_time") + msg.getDataStringValue("settlement_period_end_date")).getTime());
        Assert.assertEquals(response.getTriggeredTime(), new SimpleDateFormat("HH:mm:ssddMMMyy", Locale.US).parse(msg.getDataStringValue("settlement_triggered_time") + msg.getDataStringValue("settlement_triggered_date")).getTime());
        Assert.assertEquals(response.getResponseText(), "CUTOVER COMPLETE");
        Assert.assertNotNull(response.getReceipt());
        Assert.assertEquals(response.getTransactionRange(), "001137-001137");
        Assert.assertEquals(response.getTerminalId(), "100612348842");
        Assert.assertFalse(response.wasMerchantReceiptPrinted());

        List<SchemeSettlementEntry> listScheme = new ArrayList<>();
        for (SchemeSettlementEntry item : response.getSchemeSettlementEntries()) {
            listScheme.add(item);
        }

        Assert.assertEquals(listScheme.size(), msg.getDataListValue("schemes").size());
    }

    @Test
    public void testSettlementEnquiryRequest() {
        String posRefId = "test";

        SettlementEnquiryRequest request = new SettlementEnquiryRequest(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "settlement_enquiry");
        Assert.assertEquals(request.getId(), posRefId);
    }

    @Test
    public void testSettlementEnquiryRequestWithConfig() {
        String posRefId = "test";

        SpiConfig config = new SpiConfig();
        config.setPrintMerchantCopy(true);
        config.setPromptForCustomerCopyOnEftpos(true);
        config.setSignatureFlowOnEftpos(false);

        SettlementEnquiryRequest request = new SettlementEnquiryRequest(posRefId);
        request.setConfig(config);

        Message msg = request.toMessage();

        Assert.assertTrue(msg.getDataBooleanValue("print_merchant_copy", false));
        Assert.assertFalse(msg.getDataBooleanValue("prompt_for_customer_copy", false));
        Assert.assertFalse(msg.getDataBooleanValue("print_for_signature_required_transactions", false));
    }

    @Test
    public void testSettlementEnquiryRequestWithOptions() {
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerheader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        SettlementEnquiryRequest request = new SettlementEnquiryRequest(posRefId);
        request.setOptions(options);
        Message msg = request.toMessage();

        Assert.assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testSettlementEnquiryRequestWithOptions_None() {
        String posRefId = "test";

        SettlementEnquiryRequest request = new SettlementEnquiryRequest(posRefId);
        Message msg = request.toMessage();

        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        Assert.assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testSchemeSettlementEntry() {
        String schemeName = "VISA";
        boolean settleByAcquirer = true;
        int totalCount = 1;
        int totalValue = 1;

        SchemeSettlementEntry request = new SchemeSettlementEntry(schemeName, settleByAcquirer, totalCount, totalValue);

        Assert.assertEquals(request.toString(), "SchemeName: VISA, SettleByAcquirer: true, TotalCount: 1, TotalValue: 1");
    }
}
