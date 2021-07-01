package io.mx51.spi;

import io.mx51.spi.model.*;
import io.mx51.spi.util.Events;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MessagesTest {

    @Test
    public void testIncomingMessageUnencrypted() {
        // Here's an incoming msg from the server
        String msgJsonStr = "{\"message\": {\"event\": \"event_x\",\"id\": \"62\",\"data\": {\"param1\": \"value1\"}}}";

        // Let's parse it, I don't have secrets yet. I don't expect it to be encrypted
        Message m = Message.fromJson(msgJsonStr, null);

        // And test that it's what we expected
        Assert.assertNotNull(m);
        Assert.assertEquals("event_x", m.getEventName());
        Assert.assertEquals("value1", m.getData().get("param1"));
    }

    @Test
    public void testIncomingMessageEncrypted() {
        // Here's an incoming encrypted msg
        String msgJsonStr = "{\"enc\": \"819A6FF34A7656DBE5274AC44A28A48DD6D723FCEF12570E4488410B83A1504084D79BA9DF05C3CE58B330C6626EA5E9EB6BAAB3BFE95345A8E9834F183A1AB2F6158E8CDC217B4970E6331B4BE0FCAA\",\"hmac\": \"21FB2315E2FB5A22857F21E48D3EEC0969AD24C0E8A99C56A37B66B9E503E1EF\"}";

        // Here are our secrets
        Secrets secrets = new Secrets("11A1162B984FEF626ECC27C659A8B0EEAD5248CA867A6A87BEA72F8A8706109D", "40510175845988F13F6162ED8526F0B09F73384467FA855E1E79B44A56562A58");

        // Let's parse it
        Message m = Message.fromJson(msgJsonStr, secrets);

        // And test that it's what we expected
        Assert.assertNotNull(m);
        Assert.assertEquals("pong", m.getEventName());
        Assert.assertEquals("2017-11-16T21:51:50.499", m.getDateTimeStamp());
    }

    @Test
    public void testIncomingMessageEncrypted_badSig() {
        // Here's an incoming encrypted msg
        String msgJsonStr = "{\"enc\": \"819A6FF34A7656DBE5274AC44A28A48DD6D723FCEF12570E4488410B83A1504084D79BA9DF05C3CE58B330C6626EA5E9EB6BAAB3BFE95345A8E9834F183A1AB2F6158E8CDC217B4970E6331B4BE0FCAA\",\"hmac\": \"21FB2315E2FB5A22857F21E48D3EEC0969AD24C0E8A99C56A37B66B9E503E1EA\"}";

        // Here are our secrets
        Secrets secrets = new Secrets("11A1162B984FEF626ECC27C659A8B0EEAD5248CA867A6A87BEA72F8A8706109D", "40510175845988F13F6162ED8526F0B09F73384467FA855E1E79B44A56562A58");

        // Let's parse it
        Message m = Message.fromJson(msgJsonStr, secrets);
        Assert.assertNotNull(m);
        Assert.assertEquals(Events.INVALID_HMAC_SIGNATURE, m.getEventName());
    }

    @Test
    public void testOutgoingMessageUnencrypted() {
        // Create a message
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("param1", "value1");
        Message m = new Message("77", "event_y", data, false);

        MessageStamp stamp = new MessageStamp("BAR1", null);
        stamp.resetConnection(); // manually reset since no connection is established;

        // Serialize it to Json
        String mJson = m.toJson(stamp);

        // Let's assert Serialize Result by parsing it back.
        Message revertedM = Message.fromJson(mJson, null);
        Assert.assertNotNull(revertedM);
        Assert.assertEquals("event_y", revertedM.getEventName());
        Assert.assertEquals("value1", revertedM.getData().get("param1"));
    }

    @Test
    public void testOutgoingMessageEncrypted() {
        // Create a message
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("param1", "value1");
        Message m = new Message("2", "ping", data, true);

        // Here are our secrets
        Secrets secrets = new Secrets("11A1162B984FEF626ECC27C659A8B0EEAD5248CA867A6A87BEA72F8A8706109D", "40510175845988F13F6162ED8526F0B09F73384467FA855E1E79B44A56562A58");

        MessageStamp stamp = new MessageStamp("BAR1", null);
        stamp.resetConnection(); // manually reset since no connection is established;

        stamp.setSecrets(secrets);
        // Serialize it to Json
        String mJson = m.toJson(stamp);

        // Let's assert Serialize Result by parsing it back.
        Message revertedM = Message.fromJson(mJson, secrets);
        Assert.assertNotNull(revertedM);
        Assert.assertEquals("ping", revertedM.getEventName());
        Assert.assertEquals("value1", revertedM.getData().get("param1"));
    }

}
