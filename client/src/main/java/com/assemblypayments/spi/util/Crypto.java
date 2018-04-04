package com.assemblypayments.spi.util;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class Crypto {

    private final static char[] HEX_ALPHABET = "0123456789ABCDEF".toCharArray();

    private Crypto() {
    }

    /**
     * Decrypt a block using a cipher mode of CBC and a padding mode of PKCS7.
     * <p>
     * Note: To catch all exception types, use {@link GeneralSecurityException}.
     *
     * @param key        The key value
     * @param encMessage the message to decrypt
     * @return Returns the resulting plaintext data.
     * @throws NoSuchPaddingException             Padding scheme 'PKCS5PADDING' not available.
     * @throws NoSuchAlgorithmException           Algorithm 'AES/CBC' not implemented.
     * @throws InvalidAlgorithmParameterException The given algorithm 16-byte initialization vector is inappropriate
     *                                            for this cipher, or it implies a cryptographic strength that would
     *                                            exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                The given key is inappropriate for initializing this cipher, or
     *                                            its key size exceeds the maximum allowable key size (as determined
     *                                            from the configured jurisdiction policy files).
     * @throws BadPaddingException                This cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes.
     * @throws IllegalBlockSizeException          This cipher is a block cipher, no padding has been requested (only
     *                                            in encryption mode), and the total input length of the data
     *                                            processed by this cipher is not a multiple of block size; or if this
     *                                            encryption algorithm is unable to process the input data provided.
     */
    public static String aesDecrypt(byte[] key, String encMessage) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        if (key == null) throw new IllegalArgumentException("Key must not be null");

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
     * <p>
     * Note: To catch all exception types, use {@link GeneralSecurityException}.
     *
     * @param key     The key value
     * @param message The message to encrypt
     * @return Returns the resulting ciphertext data.
     * @throws NoSuchPaddingException             Padding scheme 'PKCS5PADDING' not available.
     * @throws NoSuchAlgorithmException           Algorithm 'AES/CBC' not implemented.
     * @throws InvalidAlgorithmParameterException The given algorithm 16-byte initialization vector is inappropriate
     *                                            for this cipher, or it implies a cryptographic strength that would
     *                                            exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                The given key is inappropriate for initializing this cipher, or
     *                                            its key size exceeds the maximum allowable key size (as determined
     *                                            from the configured jurisdiction policy files).
     * @throws BadPaddingException                This cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes.
     * @throws IllegalBlockSizeException          This cipher is a block cipher, no padding has been requested (only
     *                                            in encryption mode), and the total input length of the data
     *                                            processed by this cipher is not a multiple of block size; or if this
     *                                            encryption algorithm is unable to process the input data provided.
     */
    public static String aesEncrypt(byte[] key, String message) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        if (key == null) throw new IllegalArgumentException("Key cannot be null");

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

    /**
     * Proactively checks for any known JDK compatibility issues throws them. Should be
     * used while instantiating to prevent more issues arising during execution.
     * <p>
     * Note: To catch all exception types, use {@link GeneralSecurityException}.
     *
     * @throws NoSuchPaddingException             Padding scheme 'PKCS5PADDING' not available.
     * @throws NoSuchAlgorithmException           Algorithm 'AES/CBC' not implemented.
     * @throws InvalidAlgorithmParameterException The given algorithm 16-byte initialization vector is inappropriate
     *                                            for this cipher, or it implies a cryptographic strength that would
     *                                            exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                The given key is inappropriate for initializing this cipher, or
     *                                            its key size exceeds the maximum allowable key size (as determined
     *                                            from the configured jurisdiction policy files).
     * @throws BadPaddingException                This cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes.
     * @throws IllegalBlockSizeException          This cipher is a block cipher, no padding has been requested (only
     *                                            in encryption mode), and the total input length of the data
     *                                            processed by this cipher is not a multiple of block size; or if this
     *                                            encryption algorithm is unable to process the input data provided.
     * @throws CompatibilityValidationException   Input and output of the encryption are mismatched and no other types
     *                                            of exception have been fired.
     */
    public static void checkCompatibility() throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
            CompatibilityValidationException {

        final byte[] key = new byte[32];
        final String message = "MESSAGE";

        final String cipherText = aesEncrypt(key, message);
        final String plainText = aesDecrypt(key, cipherText);

        if (!message.equals(plainText)) {
            throw new CompatibilityValidationException("Decrypted text mismatch");
        }
    }

    /**
     * Compatibility check failure in a way not already covered by other {@link GeneralSecurityException} types.
     */
    public static class CompatibilityValidationException extends GeneralSecurityException {

        public CompatibilityValidationException(String message) {
            super(message);
        }

    }

}
