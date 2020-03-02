package io.mx51.spi.model;

/**
 * Represents the Pairing Flow State during the pairing process.
 */
public class PairingFlowState {

    private String message;
    private boolean awaitingCheckFromEftpos;
    private boolean awaitingCheckFromPos;
    private String confirmationCode;
    private boolean finished;
    private boolean successful;

    /**
     * Some text that can be displayed in the Pairing Process Screen
     * that indicates what the pairing process is up to.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Some text that can be displayed in the Pairing Process Screen
     * that indicates what the pairing process is up to.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * When true, it means that the EFTPOS is showing the confirmation code,
     * and your user needs to press YES or NO on the EFTPOS.
     */
    public boolean isAwaitingCheckFromEftpos() {
        return awaitingCheckFromEftpos;
    }

    /**
     * When true, it means that the EFTPOS is showing the confirmation code,
     * and your user needs to press YES or NO on the EFTPOS.
     */
    public void setAwaitingCheckFromEftpos(boolean awaitingCheckFromEftpos) {
        this.awaitingCheckFromEftpos = awaitingCheckFromEftpos;
    }

    /**
     * When true, you need to display the YES/NO buttons on you pairing screen
     * for your user to confirm the code.
     */
    public boolean isAwaitingCheckFromPos() {
        return awaitingCheckFromPos;
    }

    /**
     * When true, you need to display the YES/NO buttons on you pairing screen
     * for your user to confirm the code.
     */
    public void setAwaitingCheckFromPos(boolean awaitingCheckFromPos) {
        this.awaitingCheckFromPos = awaitingCheckFromPos;
    }

    /**
     * This is the confirmation code for the pairing process.
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * This is the confirmation code for the pairing process.
     */
    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
     * Indicates whether the Pairing Flow has finished its job.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Indicates whether the Pairing Flow has finished its job.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Indicates whether pairing was successful or not.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Indicates whether pairing was successful or not.
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

}
