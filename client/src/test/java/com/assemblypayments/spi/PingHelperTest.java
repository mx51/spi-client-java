package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.*;
import org.junit.Assert;
import org.junit.Test;

public class PingHelperTest {
    @Test
    public void testGeneratePingRequest() {
        Message msg = PingHelper.generatePingRequest();

        Assert.assertEquals(msg.getEventName(), "ping");
    }

    @Test
    public void tesGeneratePongResponse() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"datetime\":\"2019-06-14T18:47:55.411\",\"event\":\"pong\",\"id\":\"ping563\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        Message message = PongHelper.generatePongResponse(msg);
        Assert.assertEquals(msg.getEventName(), "pong");
    }
}
