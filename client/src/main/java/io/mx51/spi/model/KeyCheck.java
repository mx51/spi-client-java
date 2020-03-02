package io.mx51.spi.model;

/**
 * Pairing Interaction 4: Incoming
 */
public class KeyCheck {

    public final String confirmationCode;

    public KeyCheck(Message m) {
        this.confirmationCode = m.getIncomingHmac().substring(0, 6);
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

}
