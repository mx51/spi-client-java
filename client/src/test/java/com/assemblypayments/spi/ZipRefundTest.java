package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZipRefundTest {
    @Test
    public void testZipRefundRequestToMessage() {
        int amountCents = 1000;
        String posRefId = "test";

        ZipRefundRequest zipRefundRequest = new ZipRefundRequest(amountCents, posRefId);
        Message msg = zipRefundRequest.toMessage();

        assertEquals(amountCents, msg.getDataIntValue("refund_amount"));
        assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
    }

    @Test
    public void testZipRefundRequestToMessageWithOptions() {
        int amountCents = 1000;
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerHeader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        ZipRefundRequest zipRefundRequest = new ZipRefundRequest(amountCents, posRefId);
        zipRefundRequest.setOptions(options);
        Message msg = zipRefundRequest.toMessage();

        assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testZipRefundRequestToMessageWithOptions_none() {
        int amountCents = 1000;
        String posRefId = "test";

        ZipRefundRequest zipRefundRequest = new ZipRefundRequest(amountCents, posRefId);
        Message msg = zipRefundRequest.toMessage();

        assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testZipRefundRequestToMessageWithOrigina() {
        int amountCents = 1000;
        String posRefId = "test";
        String originalReceiptNumber = "123456";

        ZipRefundRequest zipRefundRequest = new ZipRefundRequest(amountCents, posRefId);
        zipRefundRequest.setOriginalReceiptNumber(originalReceiptNumber);
        Message msg = zipRefundRequest.toMessage();

        assertEquals(originalReceiptNumber, msg.getDataMapValue("zip_data").get("original_receipt_number"));
    }

    @Test
    public void testInitiateZipRefundRequest() throws Spi.CompatibilityException {
        int amountCents = 1000;
        String posRefId = "test";
        String originalReceiptNumber = "123456";
        TransactionOptions options = new TransactionOptions();

        Spi spi = new SpiTestUtils().clientWithTestSecrets();
        spi.setCurrentStatus(SpiStatus.PAIRED_CONNECTED);

        InitiateTxResult initiateTxResult = spi.initiateZipRefundTx(posRefId, amountCents, originalReceiptNumber, options);
        assertNotNull(initiateTxResult);
    }

    @Test
    public void testZipRefundResponse() throws Spi.CompatibilityException {
        String encKey = "81CF9E6A14CDAF244A30B298D4CECB505C730CE352C6AF6E1DE61B3232E24D3F";
        String hmacKey = "D35060723C9EECDB8AEA019581381CB08F64469FC61A5A04FE553EBDB5CD55B9";
        Secrets secrets = new Secrets(encKey, hmacKey);

        String jsonStr = "{\n" +
                "    \"message\":{\n" +
                "        \"event\": \"refund_zip_response\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"datetime\": \"2019-01-11T09:13:11.594\",\n" +
                "        \"data\":{\n" +
                "            \"bank_date\": \"11012019\",\n" +
                "            \"bank_time\": \"091304\",\n" +
                "            \"customer_receipt\": \"TBD\",\n" +
                "            \"customer_receipt_printed\": true,\n" +
                "            \"host_response_code\": \"000\",\n" +
                "            \"host_response_text\": \"TRANS APPROVED\",\n" +
                "            \"merchant_receipt\": \"TBD\",\n" +
                "            \"merchant_receipt_printed\": true,\n" +
                "            \"pos_ref_id\": \"POS_REF_ID_2\",\n" +
                "            \"refund_amount\": 1000,\n" +
                "            \"success\": true,\n" +
                "            \"zip_data\": {\n" +
                "                \"location_id\": \"1234\",\n" +
                "                \"original_receipt_number\": \"123456\",\n" +
                "                \"receipt_number\": \"9387643\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Message msg = Message.fromJson(jsonStr, secrets);
        ZipRefundResponse response = new ZipRefundResponse(msg);

        assertEquals(msg.getEventName(), "refund_zip_response");
        assertEquals(response.getBankDate(), "11012019");
        assertEquals(response.getBankTime(), "091304");
        assertNotNull(response.getCustomerReceipt());
        assertNotNull(response.getMerchantReceipt());
        assertEquals(response.getPosRefId(), "POS_REF_ID_2");
        assertEquals(response.getResponseCode(), "000");
        assertEquals(response.getResponseText(), "TRANS APPROVED");
        assertTrue(response.wasCustomerReceiptPrinted());
        assertTrue(response.wasMerchantReceiptPrinted());
        assertEquals(response.getRefundAmount(), 1000);

        ZipDataEntry zipDataEntry = new ZipDataEntry(response.getZipData());
        assertEquals(zipDataEntry.getLocationId(), "1234");
        assertEquals(zipDataEntry.getReceiptNumber(), "9387643");
        assertEquals(zipDataEntry.getOriginalReceiptNumber(), "123456");
    }
}
