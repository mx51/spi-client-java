package io.mx51.spi.model;

import io.mx51.spi.util.Events;

public class SignatureDecline implements Message.Compatible {

    private final String signatureRequiredRequestId;

    public SignatureDecline(String signatureRequiredRequestId) {
        this.signatureRequiredRequestId = signatureRequiredRequestId;
    }

    public String getSignatureRequiredRequestId() {
        return signatureRequiredRequestId;
    }

    @Override
    public Message toMessage() {
        return new Message(signatureRequiredRequestId, Events.SIGNATURE_DECLINED, null, true);
    }

}
