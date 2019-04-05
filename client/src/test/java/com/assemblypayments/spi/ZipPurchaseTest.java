package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZipPurchaseTest {

    @Test
    public void testZipPurchaseRequestToMessage() {
        int amountCents = 1000;
        String posRefId = "test";

        ZipPurchaseRequest zipPurchaseRequest = new ZipPurchaseRequest(amountCents, posRefId);
        Message msg = zipPurchaseRequest.toMessage();

        assertEquals(amountCents, msg.getDataIntValue("purchase_amount"));
        assertEquals(posRefId, msg.getDataStringValue("pos_ref_id"));
    }

    @Test
    public void testZipPurchaseRequestToMessageWithOptions() {
        int amountCents = 1000;
        String posRefId = "test";
        String merchantReceiptHeader = "";
        String merchantReceiptFooter = "merchantfooter";
        String customerReceiptHeader = "customerHeader";
        String customerReceiptFooter = "";

        TransactionOptions options = new TransactionOptions();
        options.setMerchantReceiptFooter(merchantReceiptFooter);
        options.setCustomerReceiptHeader(customerReceiptHeader);

        ZipPurchaseRequest zipPurchaseRequest = new ZipPurchaseRequest(amountCents, posRefId);
        zipPurchaseRequest.setOptions(options);
        Message msg = zipPurchaseRequest.toMessage();

        assertEquals(merchantReceiptHeader, msg.getDataStringValue("merchant_receipt_header"));
        assertEquals(merchantReceiptFooter, msg.getDataStringValue("merchant_receipt_footer"));
        assertEquals(customerReceiptHeader, msg.getDataStringValue("customer_receipt_header"));
        assertEquals(customerReceiptFooter, msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testZipPurchaseRequestToMessageWithOptions_none() {
        int amountCents = 1000;
        String posRefId = "test";

        ZipPurchaseRequest zipPurchaseRequest = new ZipPurchaseRequest(amountCents, posRefId);
        Message msg = zipPurchaseRequest.toMessage();

        assertEquals("", msg.getDataStringValue("merchant_receipt_header"));
        assertEquals("", msg.getDataStringValue("merchant_receipt_footer"));
        assertEquals("", msg.getDataStringValue("customer_receipt_header"));
        assertEquals("", msg.getDataStringValue("customer_receipt_footer"));
    }

    @Test
    public void testZipPurchaseRequestToMessageWithStoreCodeAndDescription() {
        int amountCents = 1000;
        String posRefId = "test";
        String storeCode = "sc";
        String description = "desc";

        ZipPurchaseRequest zipPurchaseRequest = new ZipPurchaseRequest(amountCents, posRefId);
        zipPurchaseRequest.setStoreCode(storeCode);
        zipPurchaseRequest.setDescription(description);
        Message msg = zipPurchaseRequest.toMessage();

        assertEquals(storeCode, msg.getDataMapValue("zip_data").get("store_code"));
        assertEquals(description, msg.getDataMapValue("basket").get("description"));
    }

    @Test
    public void testInitiateZipPurchaseRequest() throws Spi.CompatibilityException {
        int amountCents = 1000;
        String posRefId = "test";
        String storeCode = "sc";
        String description = "desc";
        TransactionOptions options = new TransactionOptions();

        Spi spi = new SpiTestUtils().clientWithTestSecrets();
        spi.setCurrentStatus(SpiStatus.PAIRED_CONNECTED);

        InitiateTxResult initiateTxResult = spi.initiateZipPurchaseTx(posRefId, amountCents, description, storeCode, options);
        assertNotNull(initiateTxResult);
    }

    @Test
    public void testZipPurchaseResponse() throws Spi.CompatibilityException {
        String encKey = "81CF9E6A14CDAF244A30B298D4CECB505C730CE352C6AF6E1DE61B3232E24D3F";
        String hmacKey = "D35060723C9EECDB8AEA019581381CB08F64469FC61A5A04FE553EBDB5CD55B9";
        Secrets secrets = new Secrets(encKey, hmacKey);

        String jsonStr = "{\n" +
                "    \"message\":{\n" +
                "        \"event\": \"purchase_zip_response\",\n" +
                "        \"id\": \"0\",\n" +
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
                "            \"pos_ref_id\": \"POS_REF_ID_1\",\n" +
                "            \"purchase_amount\": 1000,\n" +
                "            \"success\": true,\n" +
                "            \"zip_data\": {\n" +
                "                \"location_id\": \"1234\",\n" +
                "                \"receipt_number\": \"123456\",\n" +
                "                \"store_code\": \"123456\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Message msg = Message.fromJson(jsonStr, secrets);
        ZipPurchaseResponse response = new ZipPurchaseResponse(msg);

        assertEquals(msg.getEventName(), "purchase_zip_response");
        assertEquals(response.getBankDate(), "11012019");
        assertEquals(response.getBankTime(), "091304");
        assertNotNull(response.getCustomerReceipt());
        assertNotNull(response.getMerchantReceipt());
        assertEquals(response.getPosRefId(), "POS_REF_ID_1");
        assertEquals(response.getResponseCode(), "000");
        assertEquals(response.getResponseText(), "TRANS APPROVED");
        assertTrue(response.wasCustomerReceiptPrinted());
        assertTrue(response.wasMerchantReceiptPrinted());
        assertEquals(response.getPurchaseAmount(), 1000);

        ZipDataEntry zipDataEntry = new ZipDataEntry(response.getZipData());
        assertEquals(zipDataEntry.getLocationId(), "1234");
        assertEquals(zipDataEntry.getReceiptNumber(), "123456");
        assertEquals(zipDataEntry.getStoreCode(), "123456");
    }
}
