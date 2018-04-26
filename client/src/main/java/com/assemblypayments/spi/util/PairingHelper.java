package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * This static class helps you with the pairing process as documented here:
 * http://www.simplepaymentapi.com/#/api/pairing-process
 */
public final class PairingHelper {

    private PairingHelper() {
    }

    /**
     * Generates a pairing request.
     *
     * @return New {@link PairRequest}.
     */
    public static PairRequest newPairRequest() {
        return new PairRequest();
    }

    /**
     * Calculates/generates {@link Secrets} and {@link KeyResponse} given an incoming {@link KeyRequest}.
     *
     * @return {@link Secrets} and {@link KeyResponse} to send back.
     */
    public static SecretsAndKeyResponse generateSecretsAndKeyResponse(KeyRequest keyRequest) {
        final PublicKeyAndSecret encPubAndSec = calculateMyPublicKeyAndSecret(keyRequest.getAenc());
        final String benc = encPubAndSec.getMyPublicKey();
        final String senc = encPubAndSec.getSharedSecretKey();

        final PublicKeyAndSecret hmacPubAndSec = calculateMyPublicKeyAndSecret(keyRequest.getAhmac());
        final String bhmac = hmacPubAndSec.getMyPublicKey();
        final String shmac = hmacPubAndSec.getSharedSecretKey();

        final Secrets secrets = new Secrets(senc, shmac);
        final KeyResponse keyResponse = new KeyResponse(keyRequest.getRequestId(), benc, bhmac);

        return new SecretsAndKeyResponse(secrets, keyResponse);
    }

    /**
     * Turns an incoming "A" value from the PIN pad into the outgoing "B" value
     * and the secret value using DiffieHellman helper.
     *
     * @param theirPublicKey The incoming A value.
     * @return Your B value and the secret.
     */
    private static PublicKeyAndSecret calculateMyPublicKeyAndSecret(String theirPublicKey) {
        // SPI uses the 2048-bit MODP Group as the shared constants for the DH algorithm
        // https://tools.ietf.org/html/rfc3526#section-3
        final BigInteger modp2048P = new BigInteger("32317006071311007300338913926423828248817941241140239112842009751400741706634354222619689417363569347117901737909704191754605873209195028853758986185622153212175412514901774520270235796078236248884246189477587641105928646099411723245426622522193230540919037680524235519125679715870117001058055877651038861847280257976054903569732561526167081339361799541336476559160368317896729073178384589680639671900977202194168647225871031411336429319536193471636533209717077448227988588565369208645296636077250268955505928362751121174096972998068410554359584866583291642136218231078990999448652468262416972035911852507045361090559");
        final BigInteger modp2048G = BigInteger.valueOf(2);

        final BigInteger theirPublicBI = spiAHexStringToBigInteger(theirPublicKey);
        final BigInteger myPrivateBI = DiffieHellman.randomPrivateKey(modp2048P);
        final BigInteger myPublicBI = DiffieHellman.publicKey(modp2048P, modp2048G, myPrivateBI);
        final BigInteger secretBI = DiffieHellman.secret(modp2048P, theirPublicBI, myPrivateBI);

        final String myPublic = myPublicBI.toString(16).toUpperCase();
        final String secret = dhSecretToSPISecret(secretBI);

        return new PublicKeyAndSecret(myPublic, secret);
    }

    /**
     * Converts an incoming A value into a {@link BigInteger}.
     * There are some "gotchas" here which is why this piece of work is abstracted so it can be tested separately.
     *
     * @param hexStringA String A (as hex).
     * @return A value as a {@link BigInteger}.
     */
    public static BigInteger spiAHexStringToBigInteger(String hexStringA) {
        // We add "00" to bust signed little-endian that BigInteger expects.
        // Because we received an assumed unsigned hex-number string.
        return new BigInteger("00" + hexStringA.toLowerCase(), 16);
    }

    /**
     * Converts the DH secret {@link BigInteger} into the hex-string to be used as the secret.
     * There are some "gotchas" here which is why this piece of work is abstracted so it can be tested separately.
     * See: http://www.simplepaymentapi.com/#/api/pairing-process
     *
     * @param secretBI Secret as {@link BigInteger}.
     * @return Secret as hex string.
     */
    public static String dhSecretToSPISecret(BigInteger secretBI) {
        String mySecretHex = secretBI.toString(16).toUpperCase();
        if (mySecretHex.length() == 513) { // Sometimes we end up with an extra odd leading "0" which is strange, but we need to remove it.
            mySecretHex = mySecretHex.substring(1);
        }

        if (mySecretHex.length() < 512) { // in case we ended up wth a small secret, we need to pad it up. because padding=true.
            mySecretHex = StringUtils.leftPad(mySecretHex, 512, '0');
        }

        final MessageDigest digest = DigestUtils.getSha256Digest();
        return Crypto.byteArrayToHexString(digest.digest(Crypto.hexStringToByteArray(mySecretHex)));
    }

    /**
     * Internal holder class for public and secret, so that we can use them together in method signatures.
     */
    private static class PublicKeyAndSecret {

        private final String myPublicKey;
        private final String sharedSecretKey;

        public PublicKeyAndSecret(String myPublicKey, String sharedSecretKey) {
            this.myPublicKey = myPublicKey;
            this.sharedSecretKey = sharedSecretKey;
        }

        public String getMyPublicKey() {
            return myPublicKey;
        }

        public String getSharedSecretKey() {
            return sharedSecretKey;
        }

    }

}
