package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SpiPayAtTableTest {

    @Test
    public void testBillStatusResponseToMessage() {
        BillStatusResponse a = new BillStatusResponse();
        a.setBillId("1");
        a.setTableId("2");
        a.setOutstandingAmount(10000);
        a.setTotalAmount(20000);
        a.setBillData("Ww0KICAgICAgICAgICAgICAgIHsNCiAgICAgICAgICAgICAgICAgICAgInBheW1lbnRfdHlwZSI6ImNhc2giLCAgICAgICAgICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgICJwYXltZW50X3N1bW1hcnkiOnsgICAgICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJiYW5rX2RhdGUiOiIxMjAzMjAxOCIsICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgICAgICAiYmFua190aW1lIjoiMDc1NDAzIiwgICAgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInB1cmNoYXNlX2Ftb3VudCI6MTIzNCwgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInRlcm1pbmFsX2lkIjoiUDIwMTUwNzEiLCAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgICAgICAidGVybWluYWxfcmVmX2lkIjoic29tZSBzdHJpbmciLCAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInRpcF9hbW91bnQiOjAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgIHsNCiAgICAgICAgICAgICAgICAgICAgInBheW1lbnRfdHlwZSI6ImNhcmQiLCAgICAgICAgICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgICJwYXltZW50X3N1bW1hcnkiOnsgICAgICAgICAgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgImFjY291bnRfdHlwZSI6IkNIRVFVRSIsICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJhdXRoX2NvZGUiOiIwOTQyMjQiLCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJiYW5rX2RhdGUiOiIxMjAzMjAxOCIsICAgICAgICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJiYW5rX3RpbWUiOiIwNzU0NDciLCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiaG9zdF9yZXNwb25zZV9jb2RlIjoiMDAwIiwgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgImhvc3RfcmVzcG9uc2VfdGV4dCI6IkFQUFJPVkVEIiwgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgIm1hc2tlZF9wYW4iOiIuLi4uLi4uLi4uLi40MzUxIiwgICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgICAgICAicHVyY2hhc2VfYW1vdW50IjoxMjM0LCAgICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJycm4iOiIxODAzMTIwMDAzNzkiLCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICANCiAgICAgICAgICAgICAgICAgICAgICAgICJzY2hlbWVfbmFtZSI6IkFtZXgiLCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInRlcm1pbmFsX2lkIjoiMTAwNFAyMDE1MDcxIiwgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInRlcm1pbmFsX3JlZl9pZCI6InNvbWUgc3RyaW5nIiwgICAgICAgICAgICAgICAgICAgIA0KICAgICAgICAgICAgICAgICAgICAgICAgInRpcF9hbW91bnQiOjEyMzQgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgDQogICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICBd");
        a.setResult(BillRetrievalResult.SUCCESS);

        Message m = a.toMessage("d");

        Assert.assertEquals(m.getEventName(), "bill_details");
        Assert.assertEquals(a.getBillId(), m.getDataStringValue("bill_id"));
        Assert.assertEquals(a.getTableId(), m.getDataStringValue("table_id"));
        Assert.assertEquals(a.getOutstandingAmount(), m.getDataIntValue("bill_outstanding_amount"));
        Assert.assertEquals(a.getTotalAmount(), m.getDataIntValue("bill_total_amount"));
        Assert.assertEquals(a.getBillPaymentHistory().get(0).getTerminalRefId(), "some string");
    }

    @Test
    public void testGetOpenTablesResponse() {
        List<OpenTablesEntry> openTablesEntries = new ArrayList<>();
        OpenTablesEntry openTablesEntry = new OpenTablesEntry("1", "1", 2000);
        openTablesEntries.add(openTablesEntry);

        openTablesEntry = new OpenTablesEntry("2", "2", 2500);
        openTablesEntries.add(openTablesEntry);

        GetOpenTablesResponse getOpenTablesResponse = new GetOpenTablesResponse();
        getOpenTablesResponse.setOpenTablesEntries(openTablesEntries);
        Message m = getOpenTablesResponse.toMessage("1234");

        List<Object> getOpenTablesList = m.getDataListValue("tables");
        Assert.assertEquals(getOpenTablesList.size(), openTablesEntries.size());
        Assert.assertEquals(openTablesEntries.size(), 2);
    }

    @Test
    public void testBillPaymentFlowEndedResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"bill_id\":\"1554246591041.23\",\"bill_outstanding_amount\":1000,\"bill_total_amount\":1000,\"card_total_amount\":0,\"card_total_count\":0,\"cash_total_amount\":0,\"cash_total_count\":0,\"operator_id\":\"1\",\"table_id\":\"1\"},\"datetime\":\"2019-04-03T10:11:21.328\",\"event\":\"bill_payment_flow_ended\",\"id\":\"C12.4\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        BillPaymentFlowEndedResponse response = new BillPaymentFlowEndedResponse(msg);

        Assert.assertEquals(msg.getEventName(), "bill_payment_flow_ended");
        Assert.assertEquals(response.getBillId(), "1554246591041.23");
        Assert.assertEquals(response.getBillOutstandingAmount(), 1000);
        Assert.assertEquals(response.getBillTotalAmount(), 1000);
        Assert.assertEquals(response.getTableId(), "1");
        Assert.assertEquals(response.getOperatorId(), "1");
        Assert.assertEquals(response.getCardTotalCount(), 0);
        Assert.assertEquals(response.getCardTotalAmount(), 0);
        Assert.assertEquals(response.getCashTotalCount(), 0);
        Assert.assertEquals(response.getCashTotalAmount(), 0);
    }

    @Test
    public void testSpiPayAtTable() throws Spi.CompatibilityException, IllegalAccessException {
        Spi spi = new Spi("", "", "", null);
        SpiPayAtTable spiPay = new SpiPayAtTable(spi);
        Assert.assertNotNull(spiPay.getConfig());

        Spi spi2 = (Spi) SpiClientTestUtils.getInstanceField(spiPay, "spi");
        Assert.assertEquals(spi.getCurrentStatus(), spi2.getCurrentStatus());

        spiPay = new SpiPayAtTable(null);
        Spi spi3 = (Spi) SpiClientTestUtils.getInstanceField(spiPay, "spi");
        Assert.assertNull(spi3);
    }
}
