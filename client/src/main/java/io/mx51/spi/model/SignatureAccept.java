package io.mx51.spi.model;

import io.mx51.spi.util.Events;

public class SignatureAccept implements Message.Compatible {

    private final String signatureRequiredRequestId;

    public SignatureAccept(String signatureRequiredRequestId) {
        this.signatureRequiredRequestId = signatureRequiredRequestId;
    }

    public String getSignatureRequiredRequestId() {
        return signatureRequiredRequestId;
    }

    @Override
    public Message toMessage() {
        return new Message(signatureRequiredRequestId, Events.SIGNATURE_ACCEPTED, null, true);
    }

}
