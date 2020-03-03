package io.mx51.spi.model;

import com.google.gson.annotations.SerializedName;

/**
 * Message envelope represents the outer structure of any message that is exchanged
 * between the POS and the PIN pad and vice versa.
 * See http://www.simplepaymentapi.com/#/api/message-encryption
 */
public class MessageEnvelope {

    @SerializedName("message")
    private Message message;

    @SerializedName("enc")
    private String enc;

    @SerializedName("hmac")
    private String hmac;

    @SerializedName("pos_id")
    private String posId;

    public MessageEnvelope(Message message, String enc, String hmac) {
        this.message = message;
        this.enc = enc;
        this.hmac = hmac;
    }

    public MessageEnvelope(Message message) {
        this.message = message;
    }

    public MessageEnvelope(String enc, String hmac, String posId) {
        this.enc = enc;
        this.hmac = hmac;
        this.posId = posId;
    }

    /**
     * The message field is set only when in unencrypted form.
     * In fact, it is the only field in an envelope in the unencrypted form.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * The enc field is set only when in Encrypted form.
     * It contains the encrypted JSON of another message envelope.
     */
    public String getEnc() {
        return enc;
    }

    /**
     * The HMAC field is set only when in encrypted form.
     * It is the signature of the enc field.
     */
    public String getHmac() {
        return hmac;
    }

    /**
     * The POS ID field is only filled for outgoing encrypted messages.
     */
    public String getPosId() {
        return posId;
    }

}
