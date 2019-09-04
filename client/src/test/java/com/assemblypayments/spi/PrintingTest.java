package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

public class PrintingTest {
    @Test
    public void testPrintingRequest() {
        String key = "test";
        String payload = "test";

        PrintingRequest request = new PrintingRequest(key, payload);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "print");
        Assert.assertEquals(key, msg.getDataStringValue("key"));
        Assert.assertEquals(payload, msg.getDataStringValue("payload"));
    }

    @Test
    public void testPrintingResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"success\":true},\"datetime\":\"2019-06-14T18:51:00.948\",\"event\":\"print_response\",\"id\":\"C24.0\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PrintingResponse response = new PrintingResponse(msg);

        Assert.assertEquals(msg.getEventName(), "print_response");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(msg.getId(), "C24.0");
        Assert.assertEquals(response.getErrorReason(), "");
        Assert.assertEquals(response.getErrorDetail(), "");
        Assert.assertEquals(response.getResponseValueWithAttribute("error_detail"), "");
    }
}
