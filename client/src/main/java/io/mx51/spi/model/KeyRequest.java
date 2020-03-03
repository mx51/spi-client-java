package io.mx51.spi.model;

import java.util.Map;

/**
 * Pairing Interaction 2: Incoming
 */
public class KeyRequest {

    private final String requestId;
    private final String aenc;
    private final String ahmac;

    @SuppressWarnings("unchecked")
    public KeyRequest(Message m) {
        this.requestId = m.getId();
        this.aenc = (String) ((Map<String, Object>) m.getData().get("enc")).get("A");
        this.ahmac = (String) ((Map<String, Object>) m.getData().get("hmac")).get("A");
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAenc() {
        return aenc;
    }

    public String getAhmac() {
        return ahmac;
    }

}
