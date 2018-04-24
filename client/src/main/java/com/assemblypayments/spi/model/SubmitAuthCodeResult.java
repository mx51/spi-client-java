package com.assemblypayments.spi.model;

/**
 * Used as a return in the SubmitAuthCode method to signify whether Code is valid.
 */
public class SubmitAuthCodeResult {

    private final boolean validFormat;
    private final String message;

    public SubmitAuthCodeResult(boolean validFormat, String message) {
        this.validFormat = validFormat;
        this.message = message;
    }

    public boolean isValidFormat() {
        return validFormat;
    }

    /**
     * @return Text that gives reason for Invalidity.
     */
    public String getMessage() {
        return message;
    }

}
