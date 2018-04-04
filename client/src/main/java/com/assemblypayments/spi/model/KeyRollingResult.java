package com.assemblypayments.spi.model;

public class KeyRollingResult {

    private final Message keyRollingConfirmation;
    private final Secrets newSecrets;

    public KeyRollingResult(Message keyRollingConfirmation, Secrets newSecrets) {
        this.keyRollingConfirmation = keyRollingConfirmation;
        this.newSecrets = newSecrets;
    }

    public Message getKeyRollingConfirmation() {
        return keyRollingConfirmation;
    }

    public Secrets getNewSecrets() {
        return newSecrets;
    }

}
