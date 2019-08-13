package com.assemblypayments.spi;

import com.assemblypayments.spi.model.CancelTransactionResponse;
import com.assemblypayments.spi.model.Message;
import com.assemblypayments.spi.model.Secrets;
import org.junit.Assert;
import org.junit.Test;

public class PurchaseTest {
    @Test
    public void testCancelTransactionResponseOnValidResponseReturnObjects() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\": {\"event\": \"cancel_response\", \"id\": \"0\", \"datetime\": \"2018-02-06T15:16:44.094\", \"data\": {\"pos_ref_id\": \"123456abc\", \"success\": false, \"error_reason\": \"TXN_PAST_POINT_OF_NO_RETURN\", \"error_detail\":\"Too late to cancel transaction\" }}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        CancelTransactionResponse response = new CancelTransactionResponse(msg);

        Assert.assertEquals("cancel_response", msg.getEventName());
        Assert.assertFalse(response.isSuccess());
        Assert.assertEquals("123456abc", response.posRefId);
        Assert.assertEquals("TXN_PAST_POINT_OF_NO_RETURN", response.getErrorReason());
        Assert.assertNotNull(response.getErrorDetail());
        Assert.assertEquals(response.getResponseValueWithAttribute("pos_ref_id"), response.posRefId);
    }
}

