package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Crypto;

public class Secrets {

    private final String encKey;
    private final String hmacKey;

    private final byte[] encKeyBytes;
    private final byte[] hmacKeyBytes;

    public Secrets(String encKey, String hmacKey) {
        this.encKey = encKey;
        this.hmacKey = hmacKey;

        this.encKeyBytes = Crypto.hexStringToByteArray(encKey);
        this.hmacKeyBytes = Crypto.hexStringToByteArray(hmacKey);
    }

    public String getEncKey() {
        return encKey;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public byte[] getEncKeyBytes() {
        return encKeyBytes;
    }

    public byte[] getHmacKeyBytes() {
        return hmacKeyBytes;
    }

}
