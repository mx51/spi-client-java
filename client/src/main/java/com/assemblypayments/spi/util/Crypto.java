package com.assemblypayments.spi.util;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public final class Crypto {

    private final static char[] HEX_ALPHABET = "0123456789ABCDEF".toCharArray();

    private Crypto() {
    }

    /**
     * Decrypt a block using a cipher mode of CBC and a padding mode of PKCS7.
     *
     * @param key        The key value
     * @param encMessage the message to decrypt
     * @return Returns the resulting plaintext data.
     */
    public static String aesDecrypt(byte[] key, String encMessage) throws GeneralSecurityException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        final byte[] inputBuffer = hexStringToByteArray(encMessage);

        final IvParameterSpec iv = new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

        final byte[] plainTextBytes = cipher.doFinal(inputBuffer);

        return new String(plainTextBytes, Charsets.UTF_8);
    }

    /**
     * Encrypt a block using a cipher mode of CBC and a padding mode of PKCS7.
     *
     * @param key     The key value
     * @param message The message to encrypt
     * @return Returns the resulting ciphertext data.
     */
    public static String aesEncrypt(byte[] key, String message) throws GeneralSecurityException {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        final byte[] inputBuffer = message.getBytes(Charsets.UTF_8);

        final IvParameterSpec iv = new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

        final byte[] cipherTextBytes = cipher.doFinal(inputBuffer);

        return byteArrayToHexString(cipherTextBytes);
    }

    /**
     * Calculates the HMACSHA256 signature of a message.
     *
     * @param key           The Hmac Key as Bytes
     * @param messageToSign The message to sign
     * @return The HMACSHA256 signature as a hex string
     */
    public static String hmacSignature(byte[] key, String messageToSign) {
        final byte[] msgBytes = messageToSign.getBytes(Charsets.UTF_8);
        final Mac mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key);
        return byteArrayToHexString(mac.doFinal(msgBytes));
    }

    public static String byteArrayToHexString(byte[] ba) {
        final char[] hexChars = new char[ba.length * 2];
        for (int j = 0; j < ba.length; j++) {
            final int v = ba[j] & 0xFF;
            hexChars[j * 2] = HEX_ALPHABET[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ALPHABET[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String hex) {
        final int len = hex.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

}
