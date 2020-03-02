package io.mx51.spi.util;

import io.mx51.spi.model.KeyRollingResult;
import io.mx51.spi.model.Message;
import io.mx51.spi.model.Secrets;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public final class KeyRollingHelper {

    private KeyRollingHelper() {
    }

    public static KeyRollingResult performKeyRolling(Message krRequest, Secrets currentSecrets) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("status", "confirmed");
        final Message m = new Message(krRequest.getId(), Events.KEY_ROLL_RESPONSE, data, true);
        final MessageDigest digest = DigestUtils.getSha256Digest();
        final Secrets newSecrets = new Secrets(
                Crypto.byteArrayToHexString(digest.digest(Crypto.hexStringToByteArray(currentSecrets.getEncKey()))),
                Crypto.byteArrayToHexString(digest.digest(Crypto.hexStringToByteArray(currentSecrets.getHmacKey()))));
        return new KeyRollingResult(m, newSecrets);
    }

}
