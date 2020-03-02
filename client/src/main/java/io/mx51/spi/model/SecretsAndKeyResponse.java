package io.mx51.spi.model;

/**
 * Holder class for Secrets and KeyResponse, so that we can use them together in method signatures.
 */
public class SecretsAndKeyResponse {

    private final Secrets secrets;
    private final KeyResponse keyResponse;

    public SecretsAndKeyResponse(Secrets secrets, KeyResponse keyResponse) {
        this.secrets = secrets;
        this.keyResponse = keyResponse;
    }

    public Secrets getSecrets() {
        return secrets;
    }

    public KeyResponse getKeyResponse() {
        return keyResponse;
    }

}
