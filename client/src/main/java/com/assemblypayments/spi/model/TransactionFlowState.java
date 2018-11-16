package com.assemblypayments.spi.model;

/**
 * Represents the State during a TransactionFlow.
 */
public class TransactionFlowState {

    private String posRefId;
    private TransactionType type;
    private String displayMessage;
    private int amountCents;
    private boolean requestSent;
    private long requestTime;
    private long lastStateRequestTime;
    private String lastGltRequestId;
    private boolean attemptingToCancel;
    private boolean awaitingSignatureCheck;
    private boolean awaitingPhoneForAuth;
    private boolean finished;
    private Message.SuccessState success;
    private Message response;
    private SignatureRequired signatureRequiredMessage;
    private PhoneForAuthRequired phoneForAuthRequiredMessage;
    private long cancelAttemptTime;
    private Message request;
    private boolean awaitingGltResponse;
    private String gltResponsePosRefId;

    public TransactionFlowState(String posRefId, TransactionType type, int amountCents, Message message, String msg) {
        this.posRefId = posRefId;
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

    public void cancelFailed(String msg) {
        this.attemptingToCancel = false;
        this.displayMessage = msg;
    }

    public void callingGlt(String gltRequestId) {
        this.awaitingGltResponse = true;
        this.lastStateRequestTime = System.currentTimeMillis();
        this.lastGltRequestId = gltRequestId;
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

    public void phoneForAuthRequired(PhoneForAuthRequired spiMessage, String msg) {
        this.phoneForAuthRequiredMessage = spiMessage;
        this.awaitingPhoneForAuth = true;
        this.displayMessage = msg;
    }

    public void authCodeSent(String msg) {
        this.awaitingPhoneForAuth = false;
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
     * @return The id given to this transaction.
     */
    public String getPosRefId() {
        return posRefId;
    }

    /**
     * @param posRefId The id given to this transaction.
     */
    public void setPosRefId(String posRefId) {
        this.posRefId = posRefId;
    }

    /**
     * @return The id given to this transaction.
     * @deprecated Use {@link #getPosRefId()} instead.
     */
    @Deprecated
    public String getId() {
        return getPosRefId();
    }

    /**
     * @param id The id given to this transaction.
     * @deprecated Use {@link #getPosRefId()} instead.
     */
    @Deprecated
    public void setId(String id) {
        setPosRefId(id);
    }

    /**
     * @return Purchase/Refund/Settle/...
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * @param type Purchase/Refund/Settle/...
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * @return A text message to display on your Transaction Flow Screen.
     */
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * @param displayMessage A text message to display on your Transaction Flow Screen.
     */
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    /**
     * @return Amount in cents for this transaction.
     */
    public int getAmountCents() {
        return amountCents;
    }

    /**
     * @param amountCents Amount in cents for this transaction.
     */
    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    /**
     * @return Whether the request has been sent to the EFTPOS yet or not.
     * In the PairedConnecting state, the transaction is initiated
     * but the request is only sent once the connection is recovered.
     */
    public boolean isRequestSent() {
        return requestSent;
    }

    /**
     * @param requestSent Whether the request has been sent to the EFTPOS yet or not.
     *                    In the PairedConnecting state, the transaction is initiated
     *                    but the request is only sent once the connection is recovered.
     */
    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    /**
     * @return The time when the request was sent to the EFTPOS.
     */
    public long getRequestTime() {
        return requestTime;
    }

    /**
     * @param requestTime requestTime The time when the request was sent to the EFTPOS.
     */
    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * @return The time when we last asked for an update, including the original request at first.
     */
    public long getLastStateRequestTime() {
        return lastStateRequestTime;
    }

    /**
     * @param lastStateRequestTime The time when we last asked for an update, including the original request at first.
     */
    public void setLastStateRequestTime(long lastStateRequestTime) {
        this.lastStateRequestTime = lastStateRequestTime;
    }

    /**
     * @return The id of the last glt request message that was sent. used to match with the response.
     */
    public String getLastGltRequestId() {
        return lastGltRequestId;
    }

    /**
     * @param lastGltRequestId The id of the last glt request message that was sent. used to match with the response.
     */
    public void setLastGltRequestId(String lastGltRequestId) {
        this.lastGltRequestId = lastGltRequestId;
    }


    /**
     * @return Whether we're currently attempting to Cancel the transaction.
     */
    public boolean isAttemptingToCancel() {
        return attemptingToCancel;
    }

    /**
     * @param attemptingToCancel Whether we're currently attempting to Cancel the transaction.
     */
    public void setAttemptingToCancel(boolean attemptingToCancel) {
        this.attemptingToCancel = attemptingToCancel;
    }

    /**
     * @return When this flag is on, you need to display the signature accept/decline buttons in your
     * transaction flow screen.
     */
    public boolean isAwaitingSignatureCheck() {
        return awaitingSignatureCheck;
    }

    /**
     * @param awaitingSignatureCheck When this flag is on, you need to display the signature accept/decline buttons in your
     *                               transaction flow screen.
     */
    public void setAwaitingSignatureCheck(boolean awaitingSignatureCheck) {
        this.awaitingSignatureCheck = awaitingSignatureCheck;
    }

    /**
     * @return When this flag is on, you need to show your user the phone number to call to get the authorisation code.
     * Then you need to provide your user means to enter that given code and submit it via SubmitAuthCode().
     */
    public boolean isAwaitingPhoneForAuth() {
        return awaitingPhoneForAuth;
    }

    /**
     * @param awaitingPhoneForAuth When this flag is on, you need to show your user the phone number to call to get the authorisation code.
     *                             Then you need to provide your user means to enter that given code and submit it via SubmitAuthCode().
     */
    public void setAwaitingPhoneForAuth(boolean awaitingPhoneForAuth) {
        this.awaitingPhoneForAuth = awaitingPhoneForAuth;
    }

    /**
     * @return Whether this transaction flow is over or not.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished Whether this transaction flow is over or not.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * @return The success state of this transaction. Starts off as Unknown.
     * When finished, can be Success, Failed OR Unknown.
     */
    public Message.SuccessState getSuccess() {
        return success;
    }

    /**
     * @param success The success state of this transaction. Starts off as Unknown.
     *                When finished, can be Success, Failed OR Unknown.
     */
    public void setSuccess(Message.SuccessState success) {
        this.success = success;
    }

    /**
     * @return The response at the end of the transaction.
     * Might not be present in all edge cases.
     * You can then turn this Message into the appropriate structure,
     * such as PurchaseResponse, RefundResponse, etc.
     */
    public Message getResponse() {
        return response;
    }

    /**
     * @param response The response at the end of the transaction.
     *                 Might not be present in all edge cases.
     *                 You can then turn this Message into the appropriate structure,
     *                 such as PurchaseResponse, RefundResponse, etc.
     */
    public void setResponse(Message response) {
        this.response = response;
    }

    /**
     * @return The message the we received from EFTPOS that told us that signature is required.
     */
    public SignatureRequired getSignatureRequiredMessage() {
        return signatureRequiredMessage;
    }

    /**
     * @param signatureRequiredMessage The message the we received from EFTPOS that told us that signature is required.
     */
    public void setSignatureRequiredMessage(SignatureRequired signatureRequiredMessage) {
        this.signatureRequiredMessage = signatureRequiredMessage;
    }

    /**
     * @return The message the we received from EFTPOS that told us that Phone For Auth is required.
     */
    public PhoneForAuthRequired getPhoneForAuthRequiredMessage() {
        return phoneForAuthRequiredMessage;
    }

    /**
     * @param phoneForAuthRequiredMessage The message the we received from EFTPOS that told us that Phone For Auth is required.
     */
    public void setPhoneForAuthRequiredMessage(PhoneForAuthRequired phoneForAuthRequiredMessage) {
        this.phoneForAuthRequiredMessage = phoneForAuthRequiredMessage;
    }

    /**
     * @return The time when the cancel attempt was made.
     */
    public long getCancelAttemptTime() {
        return cancelAttemptTime;
    }

    /**
     * @param cancelAttemptTime The time when the cancel attempt was made.
     */
    public void setCancelAttemptTime(long cancelAttemptTime) {
        this.cancelAttemptTime = cancelAttemptTime;
    }

    /**
     * @return The request message that we are sending/sent to the server.
     */
    public Message getRequest() {
        return request;
    }

    /**
     * @param request The request message that we are sending/sent to the server.
     */
    public void setRequest(Message request) {
        this.request = request;
    }

    /**
     * @return Whether we're currently waiting for a Get Last Transaction Response to get an update.
     */
    public boolean isAwaitingGltResponse() {
        return awaitingGltResponse;
    }

    /**
     * @param awaitingGltResponse Whether we're currently waiting for a Get Last Transaction Response to get an update.
     */
    public void setAwaitingGltResponse(boolean awaitingGltResponse) {
        this.awaitingGltResponse = awaitingGltResponse;
    }

    /**
     * The pos ref id  when Get Last Transaction response. The pos ref id  when Get Last Transaction response.
     */
    public String getGltResponsePosRefId() {
        return gltResponsePosRefId;
    }

    /**
     * @param gltResponsePosRefId The gltResponsePosRefId given to this transaction.
     */
    public void setGltResponsePosRefId(String gltResponsePosRefId) {
        this.gltResponsePosRefId = gltResponsePosRefId;
    }
}
