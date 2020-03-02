package io.mx51.spi.model;

/**
 * Used as a return in calls mid transaction to let you know whether the call was valid or not.
 */
public class MidTxResult {

    private final boolean valid;
    private final String message;

    public MidTxResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * @return Whether your call was valid in the current state.
     * When true, you can expect updated to your registered callback.
     * When false, typically you have made the call when it was not being waited on.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return Text that gives reason for the Valid flag, especially in case of false.
     */
    public String getMessage() {
        return message;
    }

}
