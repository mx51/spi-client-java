package com.assemblypayments.spi.model;

/**
 * Represents the State during a TransactionFlow.
 */
public class TransactionFlowState {

    private String id;
    private TransactionType type;
    private String displayMessage;
    private int amountCents;
    private boolean requestSent;
    private long requestTime;
    private long lastStateRequestTime;
    private boolean attemptingToCancel;
    private boolean awaitingSignatureCheck;
    private boolean finished;
    private Message.SuccessState success;
    private Message response;
    private SignatureRequired signatureRequiredMessage;
    private long cancelAttemptTime;
    private Message request;
    private boolean awaitingGltResponse;

    public TransactionFlowState(String id, TransactionType type, int amountCents, Message message, String msg) {
        this.id = id;
        this.type = type;
        this.amountCents = amountCents;
        this.requestSent = false;
        this.awaitingSignatureCheck = false;
        this.finished = false;
        this.success = Message.SuccessState.UNKNOWN;
        this.request = message;
        this.displayMessage = msg;
    }

    public void sent(String msg) {
        this.requestSent = true;
        this.requestTime = System.currentTimeMillis();
        this.lastStateRequestTime = System.currentTimeMillis();
        this.displayMessage = msg;
    }

    public void cancelling(String msg) {
        this.attemptingToCancel = true;
        this.cancelAttemptTime = System.currentTimeMillis();
        this.displayMessage = msg;
    }

    public void callingGlt() {
        this.awaitingGltResponse = true;
        this.lastStateRequestTime = System.currentTimeMillis();
    }

    public void gotGltResponse() {
        this.awaitingGltResponse = false;
    }

    public void failed(Message response, String msg) {
        this.success = Message.SuccessState.FAILED;
        this.finished = true;
        this.response = response;
        this.displayMessage = msg;
    }

    public void signatureRequired(SignatureRequired spiMessage, String msg) {
        this.signatureRequiredMessage = spiMessage;
        this.awaitingSignatureCheck = true;
        this.displayMessage = msg;
    }

    public void signatureResponded(String msg) {
        this.awaitingSignatureCheck = false;
        this.displayMessage = msg;
    }

    public void completed(Message.SuccessState state, Message response, String msg) {
        this.success = state;
        this.response = response;
        this.finished = true;
        this.attemptingToCancel = false;
        this.awaitingGltResponse = false;
        this.awaitingSignatureCheck = false;
        this.displayMessage = msg;
    }

    public void unknownCompleted(String msg) {
        this.success = Message.SuccessState.UNKNOWN;
        this.response = null;
        this.finished = true;
        this.attemptingToCancel = false;
        this.awaitingGltResponse = false;
        this.awaitingSignatureCheck = false;
        this.displayMessage = msg;
    }

    /**
     * The id given to this transaction
     */
    public String getId() {
        return id;
    }

    /**
     * The id given to this transaction
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Purchase/Refund/Settle/...
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Purchase/Refund/Settle/...
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * A text message to display on your Transaction Flow Screen
     */
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * A text message to display on your Transaction Flow Screen
     */
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    /**
     * Amount in cents for this transaction
     */
    public int getAmountCents() {
        return amountCents;
    }

    /**
     * Amount in cents for this transaction
     */
    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    /**
     * Whether the request has been sent to the EFTPOS yet or not.
     * In the PairedConnecting state, the transaction is initiated
     * but the request is only sent once the connection is recovered.
     */
    public boolean isRequestSent() {
        return requestSent;
    }

    /**
     * Whether the request has been sent to the EFTPOS yet or not.
     * In the PairedConnecting state, the transaction is initiated
     * but the request is only sent once the connection is recovered.
     */
    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    /**
     * The time when the request was sent to the EFTPOS.
     */
    public long getRequestTime() {
        return requestTime;
    }

    /**
     * The time when the request was sent to the EFTPOS.
     */
    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * The time when we last asked for an update, including the original request at first
     */
    public long getLastStateRequestTime() {
        return lastStateRequestTime;
    }

    /**
     * The time when we last asked for an update, including the original request at first
     */
    public void setLastStateRequestTime(long lastStateRequestTime) {
        this.lastStateRequestTime = lastStateRequestTime;
    }

    /**
     * Whether we're currently attempting to Cancel the transaction.
     */
    public boolean isAttemptingToCancel() {
        return attemptingToCancel;
    }

    /**
     * Whether we're currently attempting to Cancel the transaction.
     */
    public void setAttemptingToCancel(boolean attemptingToCancel) {
        this.attemptingToCancel = attemptingToCancel;
    }

    /**
     * When this flag is on, you need to display the signature accept/decline buttons in your
     * transaction flow screen.
     */
    public boolean isAwaitingSignatureCheck() {
        return awaitingSignatureCheck;
    }

    /**
     * When this flag is on, you need to display the signature accept/decline buttons in your
     * transaction flow screen.
     */
    public void setAwaitingSignatureCheck(boolean awaitingSignatureCheck) {
        this.awaitingSignatureCheck = awaitingSignatureCheck;
    }

    /**
     * Whether this transaction flow is over or not.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Whether this transaction flow is over or not.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * The success state of this transaction. Starts off as Unknown.
     * When finished, can be Success, Failed OR Unknown.
     */
    public Message.SuccessState getSuccess() {
        return success;
    }

    /**
     * The success state of this transaction. Starts off as Unknown.
     * When finished, can be Success, Failed OR Unknown.
     */
    public void setSuccess(Message.SuccessState success) {
        this.success = success;
    }

    /**
     * The response at the end of the transaction.
     * Might not be present in all edge cases.
     * You can then turn this Message into the appropriate structure,
     * such as PurchaseResponse, RefundResponse, etc
     */
    public Message getResponse() {
        return response;
    }

    /**
     * The response at the end of the transaction.
     * Might not be present in all edge cases.
     * You can then turn this Message into the appropriate structure,
     * such as PurchaseResponse, RefundResponse, etc
     */
    public void setResponse(Message response) {
        this.response = response;
    }

    /**
     * The message the we received from EFTPOS that told us that signature is required.
     */
    public SignatureRequired getSignatureRequiredMessage() {
        return signatureRequiredMessage;
    }

    /**
     * The message the we received from EFTPOS that told us that signature is required.
     */
    public void setSignatureRequiredMessage(SignatureRequired signatureRequiredMessage) {
        this.signatureRequiredMessage = signatureRequiredMessage;
    }

    /**
     * The time when the cancel attempt was made.
     */
    public long getCancelAttemptTime() {
        return cancelAttemptTime;
    }

    /**
     * The time when the cancel attempt was made.
     */
    public void setCancelAttemptTime(long cancelAttemptTime) {
        this.cancelAttemptTime = cancelAttemptTime;
    }

    /**
     * The request message that we are sending/sent to the server.
     */
    public Message getRequest() {
        return request;
    }

    /**
     * The request message that we are sending/sent to the server.
     */
    public void setRequest(Message request) {
        this.request = request;
    }

    /**
     * Whether we're currently waiting for a Get Last Transaction Response to get an update.
     */
    public boolean isAwaitingGltResponse() {
        return awaitingGltResponse;
    }

    /**
     * Whether we're currently waiting for a Get Last Transaction Response to get an update.
     */
    public void setAwaitingGltResponse(boolean awaitingGltResponse) {
        this.awaitingGltResponse = awaitingGltResponse;
    }

}
