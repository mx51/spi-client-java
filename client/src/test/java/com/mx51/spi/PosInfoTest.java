package io.mx51.spi;

import io.mx51.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

public class PosInfoTest {
    @Test
    public void testSetPosInfoRequest() {
        String version = "2.6.0";
        String vendorId = "25";
        String libraryLanguage = ".Net";
        String libraryVersion = "2.6.0";

        SetPosInfoRequest request = new SetPosInfoRequest(version, vendorId, libraryLanguage, libraryVersion, null);
        Message msg = request.toMessage();

        Assert.assertEquals(msg.getEventName(), "set_pos_info");
        Assert.assertEquals(version, msg.getDataStringValue("pos_version"));
        Assert.assertEquals(vendorId, msg.getDataStringValue("pos_vendor_id"));
        Assert.assertEquals(libraryLanguage, msg.getDataStringValue("library_language"));
        Assert.assertEquals(libraryVersion, msg.getDataStringValue("library_version"));
    }

    @Test
    public void tesSetPosInfoResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        final String jsonStr = "{\"message\":{\"data\":{\"success\":true},\"datetime\":\"2019-06-07T10:53:31.517\",\"event\":\"set_pos_info_response\",\"id\":\"prav3\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        SetPosInfoResponse response = new SetPosInfoResponse(msg);

        Assert.assertEquals(msg.getEventName(), "set_pos_info_response");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(response.getErrorReason(), "");
        Assert.assertEquals(response.getErrorDetail(), "");
        Assert.assertEquals(response.getResponseValueWithAttribute("error_detail"), "");
    }
}
