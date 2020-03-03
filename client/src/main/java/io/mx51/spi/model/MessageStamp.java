package io.mx51.spi.model;

/**
 * Message stamp represents what is required to turn an outgoing message into JSON
 * including encryption and date setting.
 */
public class MessageStamp {

    private String posId;

    private Secrets secrets;

    private long serverTimeDelta;

    public MessageStamp(String posId, Secrets secrets, long serverTimeDelta) {
        this.posId = posId;
        this.secrets = secrets;
        this.serverTimeDelta = serverTimeDelta;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public Secrets getSecrets() {
        return secrets;
    }

    public void setSecrets(Secrets secrets) {
        this.secrets = secrets;
    }

    public long getServerTimeDelta() {
        return serverTimeDelta;
    }

    public void setServerTimeDelta(long serverTimeDelta) {
        this.serverTimeDelta = serverTimeDelta;
    }

}
