package io.mx51.spi.model;

import io.mx51.spi.util.Crypto;
import io.mx51.spi.util.Events;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Message represents the contents of a message.
 * See http://www.simplepaymentapi.com/#/api/message-encryption
 */
public class Message {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private static final Gson GSON = new Gson();
    // Denotes whether an outgoing message needs to be encrypted in toJson()
    private transient final boolean needsEncryption;
    @SerializedName("id")
    private String id;
    @SerializedName("event")
    private String eventName;
    @SerializedName("data")
    private Map<String, Object> data;
    @SerializedName("datetime")
    private String dateTimeStamp;
    @SerializedName("pos_counter")
    private Integer posCounter;
    @SerializedName("conn_id")
    private String connId;
    @SerializedName("pos_id")
    private String posId;
    private transient String incomingHmac;
    private transient String decryptedJson;

    public Message(String id, String eventName, Map<String, Object> data, boolean needsEncryption) {
        this.id = id;
        this.eventName = eventName;
        this.data = data;
        this.needsEncryption = needsEncryption;
    }

    public Message() {
        this.needsEncryption = false;
    }

    public static Message fromJson(String msgJson, Secrets secrets) {
        final MessageEnvelope env = GSON.fromJson(msgJson, MessageEnvelope.class);
        if (env.getMessage() != null) {
            final Message message = env.getMessage();
            message.setDecryptedJson(msgJson);
            return message;
        }

        if (secrets == null) {
            // This may happen if we somehow received an encrypted message from eftpos but we're not configured with secrets.
            // For example, if we cancel the pairing process a little late in the game and we get an encrypted key_check message after we've dropped the keys.
            return new Message("UNKNOWN", "NOSECRETS", null, false);
        }

        final String sig = Crypto.hmacSignature(secrets.getHmacKeyBytes(), env.getEnc());
        if (!sig.equals(env.getHmac())) {
            return new Message("_", Events.INVALID_HMAC_SIGNATURE, null, false);
        }

        final String decryptedJson;
        try {
            decryptedJson = Crypto.aesDecrypt(secrets.getEncKeyBytes(), env.getEnc());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        try {
            final MessageEnvelope decryptedEnv = GSON.fromJson(decryptedJson, MessageEnvelope.class);
            final Message message = decryptedEnv.getMessage();
            message.setIncomingHmac(env.getHmac());
            message.setDecryptedJson(decryptedJson);
            return decryptedEnv.getMessage();
        } catch (JsonSyntaxException e) {
            final Map<String, Object> data = new HashMap<String, Object>();
            data.put("msg", decryptedJson);
            return new Message("UNKNOWN", "UNPARSEABLE", data, false);
        }
    }

    public String getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    /**
     * POS ID is set here only for outgoing unencrypted messages (not in the envelope's top level,
     * which would just have the "message" field).
     */
    public String getPosId() {
        return posId;
    }

    /**
     * Sometimes the logic around the incoming message might need access to the signature, for
     * example in the key_check.
     */
    public String getIncomingHmac() {
        return incomingHmac;
    }

    private void setIncomingHmac(String incomingHmac) {
        this.incomingHmac = incomingHmac;
    }

    /**
     * Set on an incoming message just so you can have a look at what it looked like in its JSON form.
     */
    public String getDecryptedJson() {
        return decryptedJson;
    }

    private void setDecryptedJson(String decryptedJson) {
        this.decryptedJson = decryptedJson;
    }

    @NotNull
    public SuccessState getSuccessState() {
        if (data == null) return SuccessState.UNKNOWN;
        final Object success = data.get("success");
        if (success instanceof Boolean) return (Boolean) success ? SuccessState.SUCCESS : SuccessState.FAILED;
        return SuccessState.UNKNOWN;
    }

    public String getError() {
        final Object e = data.get("error_reason");
        if (e instanceof String) return (String) e;
        return null;
    }

    public String getErrorDetail() {
        return getDataStringValue("error_detail");
    }

    @NotNull
    public String getDataStringValue(String attribute) {
        final Object v = data.get(attribute);
        if (v instanceof String) return (String) v;
        return "";
    }

    public int getDataIntValue(String attribute) {
        final Object v = data.get(attribute);
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double) return ((Double) v).intValue();
        if (v instanceof String) return Integer.parseInt((String) v);
        return 0;
    }

    public boolean getDataBooleanValue(String attribute, boolean defaultIfNotFound) {
        final Object v = data.get(attribute);
        if (v instanceof Boolean) return ((Boolean) v);
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        return defaultIfNotFound;
    }

    @NotNull
    public Map<String, Object> getDataMapValue(String attribute) {
        final Object v = data.get(attribute);
        if (v instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) v;
        }
        return Collections.emptyMap();
    }

    @NotNull
    public List<Object> getDataListValue(String attribute) {
        final Object v = data.get(attribute);
        if (v instanceof List) {
            //noinspection unchecked
            return (List<Object>) v;
        }
        return Collections.emptyList();
    }

    public String getConnId() {
        return connId;
    }

    public String toJson(MessageStamp stamp) {
        dateTimeStamp = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US).format(new Date());
        posCounter = stamp.getPosCounter()+1;
        connId = stamp.getConnId();

        if (!needsEncryption) {
            // Unencrypted Messages need PosID inside the message
            posId = stamp.getPosId();
        }

        this.decryptedJson = GSON.toJson(new MessageEnvelope(this));

        if (!needsEncryption) {
            return this.decryptedJson;
        }

        final String encMsg;
        try {
            encMsg = Crypto.aesEncrypt(stamp.getSecrets().getEncKeyBytes(), this.decryptedJson);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        final String hmacSig = Crypto.hmacSignature(stamp.getSecrets().getHmacKeyBytes(), encMsg);
        final MessageEnvelope encrMessageEnvelope = new MessageEnvelope(encMsg, hmacSig, stamp.getPosId());
        return GSON.toJson(encrMessageEnvelope);
    }

    public enum SuccessState {UNKNOWN, SUCCESS, FAILED}

    public interface Compatible {
        Message toMessage();
    }

}
