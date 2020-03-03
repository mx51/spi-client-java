package io.mx51.spi.model;

/**
 * Used as a return in the InitiateTx methods to signify whether
 * the transaction was initiated or not, and a reason to go with it.
 */
public class InitiateTxResult {

    private boolean initiated;
    private String message;

    public InitiateTxResult(boolean initiated, String message) {
        this.initiated = initiated;
        this.message = message;
    }

    /**
     * Whether the tx was initiated.
     * When true, you can expect updated to your registered callback.
     * When false, you can retry calling the InitiateX method.
     */
    public boolean isInitiated() {
        return initiated;
    }

    /**
     * Whether the tx was initiated.
     * When true, you can expect updated to your registered callback.
     * When false, you can retry calling the InitiateX method.
     */
    public void setInitiated(boolean initiated) {
        this.initiated = initiated;
    }

    /**
     * Text that gives reason for the Initiated flag, especially in case of false.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Text that gives reason for the Initiated flag, especially in case of false.
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
