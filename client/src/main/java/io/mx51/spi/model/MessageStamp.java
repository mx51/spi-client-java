package io.mx51.spi.model;

/**
 * Message stamp represents what is required to turn an outgoing message into JSON
 * including encryption and date setting.
 */
public class MessageStamp {

    private String posId;

    private Secrets secrets;

    private String connId;

    private Integer posCounter;

    public MessageStamp(String posId, Secrets secrets) {
        this.posId = posId;
        this.secrets = secrets;
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

    public void resetConnection() {
        Integer min = 100;
        Integer max = 99999;

        setConnectionId("");
        this.posCounter = min + (int)(Math.random() * ((max - min) + 1));
    }

    public void setConnectionId(String connId) {
        if (connId != null)
            this.connId = connId;
    }

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public Integer getPosCounter() {
        return posCounter;
    }

    public void setPosCounter(Integer posCounter) {
        this.posCounter = posCounter;
    }
}
