package io.mx51.spi.model;

import io.mx51.spi.util.Events;

import java.util.HashMap;
import java.util.Map;

/**
 * Pairing Interaction 3: Outgoing.
 */
public class KeyResponse implements Message.Compatible {

    private final String requestId;
    private final String benc;
    private final String bhmac;

    public KeyResponse(String requestId, String benc, String bhmac) {
        this.requestId = requestId;
        this.benc = benc;
        this.bhmac = bhmac;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBenc() {
        return benc;
    }

    public String getBhmac() {
        return bhmac;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();

        final Map<String, Object> encData = new HashMap<String, Object>();
        encData.put("B", benc);
        data.put("enc", encData);

        final Map<String, Object> hmacData = new HashMap<String, Object>();
        hmacData.put("B", bhmac);
        data.put("hmac", hmacData);

        return new Message(requestId, Events.KEY_RESPONSE, data, false);
    }

}
