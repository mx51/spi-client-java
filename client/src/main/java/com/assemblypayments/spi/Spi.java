package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.websocket.DeploymentException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * SPI integration client, used to manage connection to the terminal.
 */
public class Spi {

    //region Private state

    private static final Logger LOG = LogManager.getLogger("spi");

    private String posId;
    private String eftposAddress;
    private Secrets secrets;
    private MessageStamp spiMessageStamp;

    private Connection conn;
    private long pongTimeout = TimeUnit.SECONDS.toMillis(5);
    private long pingFrequency = TimeUnit.SECONDS.toMillis(18);

    private SpiStatus currentStatus;
    private SpiFlow currentFlow;
    private PairingFlowState currentPairingFlowState;
    private TransactionFlowState currentTxFlowState;
    private EventHandler<SpiStatus> statusChangedHandler;
    private EventHandler<PairingFlowState> pairingFlowStateChangedHandler;
    private EventHandler<TransactionFlowState> txFlowStateChangedHandler;
    private EventHandler<Secrets> secretsChangedHandler;

    private boolean readyToTransact;
    private Message mostRecentPingSent;
    private Message mostRecentPongReceived;
    private int missedPongsCount;
    private Thread periodicPingThread;
    private Thread transactionMonitoringThread;
    private LoginResponse mostRecentLoginResponse;

    private final Object txLock = new Object();
    private long txMonitorCheckFrequency = TimeUnit.SECONDS.toMillis(1);
    private long checkOnTxFrequency = TimeUnit.SECONDS.toMillis(20);
    private long maxWaitForCancelTx = TimeUnit.SECONDS.toMillis(10);
    private long missedPongsToDisconnect = 2;

    private Timer reconnectTimer;

    //endregion

    //region Setup methods

    /**
     * Create a new SPI instance.
     * <p>
     * If you provide secrets, it will start in paired connecting status; otherwise it will start in unpaired status.
     *
     * @param posId         Uppercase alphanumeric string that identifies your POS instance.
     *                      This value is displayed on the EFTPOS screen.
     * @param eftposAddress The IP address of the target EFTPOS.
     * @param secrets       The pairing secrets, if you know it already, or null otherwise
     * @throws CompatibilityException Thrown if JDK compatibility check has been failed. Includes cause exception
     *                                explained in document for {@link Crypto#checkCompatibility()}.
     */
    public Spi(@NotNull String posId, @NotNull String eftposAddress, @Nullable Secrets secrets)
            throws CompatibilityException {

        try {
            Crypto.checkCompatibility();
            LOG.info("Compatibility check passed");
        } catch (GeneralSecurityException e) {
            throw new CompatibilityException("JDK configuration incompatible with SPI", e);
        }

        this.posId = posId;
        this.eftposAddress = "ws://" + eftposAddress;
        this.secrets = secrets;

        // Our stamp for signing outgoing messages
        spiMessageStamp = new MessageStamp(this.posId, this.secrets, 0);

        // We will maintain some state
        mostRecentPingSent = null;
        mostRecentPongReceived = null;
        missedPongsCount = 0;
        mostRecentLoginResponse = null;
    }

    /**
     * Call this method after constructing an instance of the class and subscribing to events.
     * It will start background maintenance threads.
     * <p>
     * Most importantly, it connects to the EFTPOS server if it has secrets.
     */
    public void start() {
        reconnectTimer = new Timer();

        resetConn();
        startTransactionMonitoring();

        setCurrentFlow(SpiFlow.IDLE);
        if (secrets != null) {
            currentStatus = SpiStatus.PAIRED_CONNECTING;
            try {
                conn.connect(); // This is non-blocking
            } catch (DeploymentException e) {
                LOG.error("Failed to connect", e);
            }
        } else {
            currentStatus = SpiStatus.UNPAIRED;
        }
    }

    /**
     * Stops all running processes and resets to state before starting.
     * <p>
     * Call this method when finished with SPI, e.g. when closing the application.
     */
    public void dispose() {
        // Clean up threads
        stopPeriodicPing();
        stopTransactionMonitoring();

        // Clean up connection
        conn.dispose();

        // Clean up timer
        reconnectTimer.cancel();
        reconnectTimer = null;
    }

    /**
     * Allows you to set the pos ID, which identifies this instance of your POS.
     * Can only be called in the unpaired state.
     */
    public boolean setPosId(@NotNull String id) {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) return false;
        posId = id;
        spiMessageStamp.setPosId(id);
        return true;
    }

    /**
     * Allows you to set the PIN pad address. Sometimes the PIN pad might change IP address (we recommend
     * reserving static IPs if possible). Either way you need to allow your User to enter the IP address
     * of the PIN pad.
     */
    public boolean setEftposAddress(String address) {
        if (getCurrentStatus() == SpiStatus.PAIRED_CONNECTED) return false;
        eftposAddress = "ws://" + address;
        conn.setAddress(eftposAddress);
        return true;
    }

    //endregion

    //region Public properties and events

    /**
     * The current status of this SPI instance.
     *
     * @return Status value {@link SpiStatus}.
     */
    @NotNull
    public SpiStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * The current status of this SPI instance.
     *
     * @param value Status value {@link SpiStatus}.
     */
    private void setCurrentStatus(@NotNull SpiStatus value) {
        if (currentStatus == value) return;
        currentStatus = value;
        statusChanged();
    }

    /**
     * The current flow that this SPI instance is currently in.
     *
     * @return Current flow value {@link SpiFlow}.
     */
    @NotNull
    public SpiFlow getCurrentFlow() {
        return currentFlow;
    }

    /**
     * The current flow that this SPI instance is currently in.
     */
    private void setCurrentFlow(@NotNull SpiFlow value) {
        currentFlow = value;
    }

    /**
     * When current flow is {@link SpiFlow#PAIRING}, this represents the state of the pairing process.
     */
    public PairingFlowState getCurrentPairingFlowState() {
        return currentPairingFlowState;
    }

    /**
     * When current flow is {@link SpiFlow#PAIRING}, this represents the state of the pairing process.
     */
    private void setCurrentPairingFlowState(PairingFlowState state) {
        currentPairingFlowState = state;
    }

    /**
     * When current flow is {@link SpiFlow#TRANSACTION}, this represents the state of the transaction process.
     */
    public TransactionFlowState getCurrentTxFlowState() {
        return currentTxFlowState;
    }

    /**
     * When current flow is {@link SpiFlow#TRANSACTION}, this represents the state of the transaction process.
     */
    private void setCurrentTxFlowState(TransactionFlowState state) {
        currentTxFlowState = state;
    }

    /**
     * Subscribe to this event to know when the status has changed.
     */
    public void setStatusChangedHandler(@Nullable EventHandler<SpiStatus> handler) {
        statusChangedHandler = handler;
    }

    /**
     * Subscribe to this event to know when the current pairing flow state has changed.
     */
    public void setPairingFlowStateChangedHandler(@Nullable EventHandler<PairingFlowState> handler) {
        pairingFlowStateChangedHandler = handler;
    }

    /**
     * Subscribe to this event to know when the current pairing flow state changes
     */
    public void setTxFlowStateChangedHandler(@Nullable EventHandler<TransactionFlowState> handler) {
        txFlowStateChangedHandler = handler;
    }

    /**
     * Subscribe to this event to know when the secrets change, such as at the end of the pairing process,
     * or every time that the keys are periodically rolled.
     * <p>
     * You then need to persist the secrets safely so you can instantiate SPI with them next time around.
     */
    public void setSecretsChangedHandler(@Nullable EventHandler<Secrets> handler) {
        secretsChangedHandler = handler;
    }

    private void statusChanged() {
        if (statusChangedHandler != null) {
            statusChangedHandler.onEvent(getCurrentStatus());
        }
    }

    private void pairingFlowStateChanged() {
        if (pairingFlowStateChangedHandler != null) {
            pairingFlowStateChangedHandler.onEvent(getCurrentPairingFlowState());
        }
    }

    private void txFlowStateChanged() {
        if (txFlowStateChangedHandler != null) {
            txFlowStateChangedHandler.onEvent(getCurrentTxFlowState());
        }
    }

    private void secretsChanged(Secrets value) {
        if (secretsChangedHandler != null) {
            secretsChangedHandler.onEvent(value);
        }
    }

    //endregion

    /**
     * Call this one when a flow is finished and you want to go back to idle state.
     * <p>
     * Typically when your user clicks the "OK" button to acknowledge that pairing is finished, or that
     * transaction is finished. When true, you can dismiss the flow screen and show back the idle screen.
     *
     * @return <code>true</code> means we have moved back to the {@link SpiFlow#IDLE} state,
     * <code>false</code> means current flow was not finished yet.
     */
    public boolean ackFlowEndedAndBackToIdle() {
        if (getCurrentFlow() == SpiFlow.IDLE) return true; // already idle

        if (getCurrentFlow() == SpiFlow.PAIRING && getCurrentPairingFlowState().isFinished()) {
            setCurrentFlow(SpiFlow.IDLE);
            return true;
        }

        if (getCurrentFlow() == SpiFlow.TRANSACTION && getCurrentTxFlowState().isFinished()) {
            setCurrentFlow(SpiFlow.IDLE);
            return true;
        }

        return false;
    }

    //region Pairing flow methods

    /**
     * This will connect to the EFTPOS and start the pairing process.
     * <p>
     * Only call this if you are in the {@link SpiStatus#UNPAIRED} state.
     * <p>
     * Subscribe to {@link #setPairingFlowStateChangedHandler(EventHandler)} to get updates on the pairing process.
     */
    public void pair() {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) return;

        setCurrentFlow(SpiFlow.PAIRING);

        final PairingFlowState pairingFlowState = new PairingFlowState();
        pairingFlowState.setSuccessful(false);
        pairingFlowState.setFinished(false);
        pairingFlowState.setMessage("Connecting...");
        pairingFlowState.setAwaitingCheckFromEftpos(false);
        pairingFlowState.setAwaitingCheckFromPos(false);
        pairingFlowState.setConfirmationCode("");
        setCurrentPairingFlowState(pairingFlowState);

        pairingFlowStateChanged();

        try {
            conn.connect(); // Non-Blocking
        } catch (DeploymentException e) {
            LOG.error("Failed to connect", e);
        }
    }

    /**
     * Call this when your user clicks 'Yes' to confirm the pairing code on your screen matches the one on the EFTPOS.
     */
    public void pairingConfirmCode() {
        if (!getCurrentPairingFlowState().isAwaitingCheckFromPos()) {
            // We weren't expecting this
            return;
        }

        getCurrentPairingFlowState().setAwaitingCheckFromPos(false);
        if (getCurrentPairingFlowState().isAwaitingCheckFromEftpos()) {
            // But we are still waiting for confirmation from EFTPOS side.
            getCurrentPairingFlowState().setMessage("Click YES on EFTPOS if code is: " + getCurrentPairingFlowState().getConfirmationCode());
            pairingFlowStateChanged();
        } else {
            // Already confirmed from EFTPOS - So all good now. We're Paired also from the POS perspective.
            onPairingSuccess();
            onReadyToTransact();
        }
    }

    /**
     * Call this if your user clicks 'Cancel' or 'No' during the pairing process.
     */
    public void pairingCancel() {
        if (getCurrentFlow() != SpiFlow.PAIRING || getCurrentPairingFlowState().isFinished()) return;

        onPairingFailed();
    }

    /**
     * Call this when your uses clicks the 'Unpair' button.
     * <p>
     * This will disconnect from the EFTPOS and forget the secrets.
     * The current state is then changed to {@link SpiStatus#UNPAIRED}.
     * <p>
     * Call this only if you are not yet in the {@link SpiStatus#UNPAIRED} state.
     */
    public boolean unpair() {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return false;

        if (getCurrentFlow() != SpiFlow.IDLE) return false;

        setCurrentStatus(SpiStatus.UNPAIRED);

        conn.disconnect();
        secrets = null;
        spiMessageStamp.setSecrets(null);
        secretsChanged(secrets);

        return true;
    }

    //endregion

    //region Transaction methods

    /**
     * Initiates a purchase transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param id          Alphanumeric identifier for your purchase.
     * @param amountCents Amount in cents to charge.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiatePurchaseTx(String id, int amountCents) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final double amountDollars = amountCents / 100.0;

            final PurchaseRequest purchase = PurchaseHelper.createPurchaseRequest(amountCents, id);
            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    id, TransactionType.PURCHASE, amountCents, purchase.toMessage(),
                    "Waiting for EFTPOS connection to make payment request for " + amountDollars));

            if (send(purchase.toMessage())) {
                getCurrentTxFlowState().sent("Asked EFTPOS to accept payment for " + amountDollars);
            }
        }

        txFlowStateChanged();
        return new InitiateTxResult(true, "Purchase Initiated");
    }

    /**
     * Initiates a refund transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param id          Alphanumeric identifier for your refund.
     * @param amountCents Amount in cents to charge.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiateRefundTx(String id, int amountCents) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final double amountDollars = amountCents / 100.0;

            final RefundRequest purchase = PurchaseHelper.createRefundRequest(amountCents, id);
            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    id, TransactionType.REFUND, amountCents, purchase.toMessage(),
                    "Waiting for EFTPOS connection to make refund request for " + amountDollars));

            if (send(purchase.toMessage())) {
                getCurrentTxFlowState().sent("Asked EFTPOS to refund " + amountDollars);
            }
        }

        txFlowStateChanged();
        return new InitiateTxResult(true, "Refund Initiated");
    }

    /**
     * Let the EFTPOS know whether merchant accepted or declined the signature.
     *
     * @param accepted Whether merchant accepted the signature from customer or not.
     */
    public void acceptSignature(boolean accepted) {
        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION || getCurrentTxFlowState().isFinished() || !getCurrentTxFlowState().isAwaitingSignatureCheck()) {
                LOG.info("Asked to accept signature but I was not waiting for one.");
                return;
            }

            getCurrentTxFlowState().signatureResponded(accepted ? "Accepting Signature..." : "Declining Signature...");
            final SignatureRequired sigReqMsg = getCurrentTxFlowState().getSignatureRequiredMessage();
            final String sigReqId = sigReqMsg.getRequestId();
            send((accepted ? new SignatureAccept(sigReqId) : new SignatureDecline(sigReqId)).toMessage());
        }
        txFlowStateChanged();
    }

    /**
     * Attempts to cancel a transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to see how it goes.
     * <p>
     * Wait for the transaction to be finished and then see whether cancellation was successful or not.
     */
    public void cancelTransaction() {
        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION || getCurrentTxFlowState().isFinished()) {
                LOG.info("Asked to cancel transaction but I was not in the middle of one.");
                return;
            }

            // TH-1C, TH-3C - Merchant pressed cancel
            if (getCurrentTxFlowState().isRequestSent()) {
                final CancelTransactionRequest cancelReq = new CancelTransactionRequest();
                getCurrentTxFlowState().cancelling("Attempting to Cancel Transaction...");
                send(cancelReq.toMessage());
            } else {
                // We Had Not Even Sent Request Yet. Consider as known failed.
                getCurrentTxFlowState().failed(null, "Transaction Cancelled. Request Had not even been sent yet.");
            }
        }

        txFlowStateChanged();
    }

    /**
     * Initiates a settlement transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     */
    @NotNull
    public InitiateTxResult initiateSettleTx(String id) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final SettleRequest settleRequest = new SettleRequest(RequestIdHelper.id("settle"));
            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    id, TransactionType.SETTLE, 0, settleRequest.toMessage(),
                    "Waiting for EFTPOS connection to make a settle request"));

            if (send(settleRequest.toMessage())) {
                getCurrentTxFlowState().sent("Asked EFTPOS to settle.");
            }
        }

        txFlowStateChanged();
        return new InitiateTxResult(true, "Settle Initiated");
    }

    /**
     * Initiates a get last transaction operation. Use this when you want to retrieve the most recent transaction
     * that was processed by the EFTPOS.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     */
    @NotNull
    public InitiateTxResult initiateGetLastTx() {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (currentFlow != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final Message gltRequestMsg = new GetLastTransactionRequest().toMessage();
            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    gltRequestMsg.getId(), TransactionType.GET_LAST_TRANSACTION, 0, gltRequestMsg,
                    "Waiting for EFTPOS connection to make a Get-Last-Transaction request"));

            if (send(gltRequestMsg)) {
                getCurrentTxFlowState().sent("Asked EFTPOS to Get Last Transaction.");
            }
        }

        txFlowStateChanged();
        return new InitiateTxResult(true, "GLT Initiated");
    }

    /**
     * Attempts to conclude whether a gltResponse matches an expected transaction and returns the outcome.
     * <p>
     * If {@link Message.SuccessState#SUCCESS}/{@link Message.SuccessState#FAILED} is returned, it means that
     * the GTL response did match, and that transaction was successful/failed.
     * <p>
     * If {@link Message.SuccessState#UNKNOWN} is returned, it means that the gtlResponse does not match the
     * expected transaction.
     *
     * @param gltResponse    The {@link GetLastTransactionResponse} message to check.
     * @param expectedType   The expected type (e.g. Purchase, Refund).
     * @param expectedAmount The expected amount in cents.
     * @param requestTime    The time you made your request.
     * @param posRefId       The reference ID that you passed in with the original request. Currently not used.
     */
    @NotNull
    public Message.SuccessState gltMatch(@NotNull GetLastTransactionResponse gltResponse, @NotNull TransactionType expectedType,
                                         int expectedAmount, long requestTime, String posRefId) {
        final long gtlBankTime;
        try {
            gtlBankTime = new SimpleDateFormat("ddMMyyyyHHmmss").parse(gltResponse.getBankDateTimeString()).getTime();
        } catch (ParseException e) {
            LOG.error("Cannot parse date: " + gltResponse.getBankDateTimeString());
            return Message.SuccessState.UNKNOWN;
        }

        // adjust request time for serverTime and also give 5 seconds slack.
        final long reqServerTime = requestTime + spiMessageStamp.getServerTimeDelta() - 5000;
        final int gltAmount = gltResponse.getTransactionAmount();

        // For now we use amount and date to match as best we can.
        // In the future we will be able to pass our own pos_ref_id in the tx request that will be returned here.
        LOG.info("GLT CHECK: Type: {" + expectedType + "}->{" + gltResponse.getTxType() + "} Amount: {" + expectedAmount + "}->{" + gltAmount + "}, Date: {" + reqServerTime + "}->{" + gtlBankTime + "}");

        if (gltAmount != expectedAmount) return Message.SuccessState.UNKNOWN;

        final String txType = gltResponse.getTxType();
        if ("PURCHASE".equals(txType)) {
            if (expectedType != TransactionType.PURCHASE) return Message.SuccessState.UNKNOWN;
        } else if ("REFUND".equals(txType)) {
            if (expectedType != TransactionType.REFUND) return Message.SuccessState.UNKNOWN;
        } else {
            return Message.SuccessState.UNKNOWN;
        }

        if (reqServerTime > gtlBankTime) return Message.SuccessState.UNKNOWN;

        return gltResponse.getSuccessState();
    }

    //endregion

    //region Internals for pairing flow

    /**
     * Handling the 2nd interaction of the pairing process, i.e. an incoming {@link KeyRequest}.
     *
     * @param m Incoming message.
     */
    private void handleKeyRequest(@NotNull Message m) {
        final PairingFlowState currentState = getCurrentPairingFlowState();
        currentState.setMessage("Negotiating pairing...");
        pairingFlowStateChanged();

        // Use the helper. It takes the incoming request, and generates the secrets and the response.
        final SecretsAndKeyResponse result = PairingHelper.generateSecretsAndKeyResponse(new KeyRequest(m));
        secrets = result.getSecrets(); // we now have secrets, although pairing is not fully finished yet.
        spiMessageStamp.setSecrets(secrets); // updating our stamp with the secrets so can encrypt messages later.
        send(result.getKeyResponse().toMessage()); // send the key_response, i.e. interaction 3 of pairing.
    }

    /**
     * Handling the 4th interaction of the pairing process i.e. an incoming {@link KeyCheck}.
     */
    private void handleKeyCheck(@NotNull Message m) {
        final KeyCheck keyCheck = new KeyCheck(m);
        final PairingFlowState currentState = getCurrentPairingFlowState();
        currentState.setConfirmationCode(keyCheck.getConfirmationCode());
        currentState.setAwaitingCheckFromEftpos(true);
        currentState.setAwaitingCheckFromPos(true);
        currentState.setMessage("Confirm that the following code is showing on the terminal");
        pairingFlowStateChanged();
    }

    /**
     * Handling the 5th and final interaction of the pairing process, i.e. an incoming {@link PairResponse}.
     */
    private void handlePairResponse(@NotNull Message m) {
        final PairResponse pairResp = new PairResponse(m);

        final PairingFlowState currentState = getCurrentPairingFlowState();
        currentState.setAwaitingCheckFromEftpos(false);
        if (pairResp.getSuccess()) {
            if (currentState.isAwaitingCheckFromPos()) {
                // Still Waiting for User to say yes on POS
                currentState.setMessage("Confirm that the following Code is what the EFTPOS showed");
                pairingFlowStateChanged();
            } else {
                onPairingSuccess();
            }
            // I need to ping/login even if the pos user has not said yes yet,
            // because otherwise within 5 seconds connecting will be dropped by EFTPOS.
            startPeriodicPing();
        } else {
            onPairingFailed();
        }
    }

    private void onPairingSuccess() {
        final PairingFlowState currentState = getCurrentPairingFlowState();
        currentState.setSuccessful(true);
        currentState.setFinished(true);
        currentState.setMessage("Pairing Successful!");
        setCurrentStatus(SpiStatus.PAIRED_CONNECTED);
        secretsChanged(secrets);
        pairingFlowStateChanged();
    }

    private void onPairingFailed() {
        secrets = null;
        spiMessageStamp.setSecrets(null);
        conn.disconnect();

        final PairingFlowState currentState = getCurrentPairingFlowState();
        setCurrentStatus(SpiStatus.UNPAIRED);
        currentState.setMessage("Pairing Failed");
        currentState.setFinished(true);
        currentState.setSuccessful(false);
        currentState.setAwaitingCheckFromPos(false);
        pairingFlowStateChanged();
    }

    /**
     * Sometimes the server asks us to roll our secrets.
     */
    private void handleKeyRollingRequest(@NotNull Message m) {
        // we calculate the new ones...
        final KeyRollingResult krRes = KeyRollingHelper.performKeyRolling(m, secrets);
        secrets = krRes.getNewSecrets(); // and update our secrets with them
        spiMessageStamp.setSecrets(secrets); // and our stamp
        send(krRes.getKeyRollingConfirmation()); // and we tell the server that all is well.
        secretsChanged(secrets);
    }

    //endregion

    //region Internals for transaction management

    /**
     * The pin pad server will send us this message when a customer signature is required.
     * We need to ask the customer to sign the incoming receipt.
     * And then tell the pin pad whether the signature is ok or not.
     */
    private void handleSignatureRequired(@NotNull Message m) {
        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION || getCurrentTxFlowState().isFinished()) {
                LOG.info("Received Signature Required but I was not waiting for one. " + m.getDecryptedJson());
                return;
            }
            getCurrentTxFlowState().signatureRequired(new SignatureRequired(m), "Ask Customer to Sign the Receipt");
        }
        txFlowStateChanged();
    }

    /**
     * The PIN pad server will reply to our {@link PurchaseRequest} with a {@link PurchaseResponse}.
     */
    private void handlePurchaseResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.PURCHASE);
    }

    /**
     * The PIN pad server will reply to our {@link RefundRequest} with a {@link RefundResponse}.
     */
    private void handleRefundResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.REFUND);
    }

    // TODO: Handle the settlement response received from the PIN pad
    private void handleSettleResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.SETTLE);
    }

    private void handleTxResponse(@NotNull Message m, @NotNull TransactionType type) {
        synchronized (txLock) {
            final TransactionFlowState currentState = getCurrentTxFlowState();

            if (getCurrentFlow() != SpiFlow.TRANSACTION || currentState.isFinished()) {
                LOG.info("Received " + type + " response but I was not waiting for one. " + m.getDecryptedJson());
                return;
            }
            // TH-1A, TH-2A

            currentState.completed(m.getSuccessState(), m, type + " transaction ended.");
            // TH-6A, TH-6E
        }
        txFlowStateChanged();
    }

    /**
     * Sometimes we receive event type "error" from the server, such as when calling cancel_transaction
     * and there is no transaction in progress.
     */
    private void handleErrorEvent(@NotNull Message m) {
        synchronized (txLock) {
            if (getCurrentFlow() == SpiFlow.TRANSACTION
                    && !getCurrentTxFlowState().isFinished()
                    && getCurrentTxFlowState().isAttemptingToCancel()
                    && "NO_TRANSACTION".equals(m.getError())) {
                // TH-2E
                LOG.info("Was trying to cancel a transaction but there is nothing to cancel. Calling GLT to see what's up");
                callGetLastTransaction();
            } else {
                LOG.info("Received error event, but don't know what to do with it. " + m.getDecryptedJson());
            }
        }
    }

    /**
     * When the PIN pad returns to us what the last transaction was.
     */
    private void handleGetLastTransactionResponse(@NotNull Message m) {
        synchronized (txLock) {
            final TransactionFlowState txState = getCurrentTxFlowState();
            if (getCurrentFlow() != SpiFlow.TRANSACTION || txState.isFinished()) {
                // We were not in the middle of a transaction, who cares?
                return;
            }

            // TH-4 We were in the middle of a transaction.
            // Let's attempt recovery. This is step 4 of Transaction Processing Handling
            LOG.info("Got last transaction.. Attempting recovery.");
            txState.gotGltResponse();
            final GetLastTransactionResponse gltResponse = new GetLastTransactionResponse(m);
            if (!gltResponse.wasRetrievedSuccessfully()) {
                if (gltResponse.wasOperationInProgressError()) {
                    // TH-4E - Operation In Progress
                    LOG.info("Operation still in progress... Stay waiting.");
                    // No need to publish txFlowStateChanged. Can return;
                    return;
                } else {
                    // TH-4X - Unexpected Error when recovering
                    LOG.info("Unexpected error in get last transaction response during transaction recovery: " + m.getError());
                    txState.unknownCompleted("Unexpected error when recovering transaction status. Check EFTPOS. ");
                }
            } else {
                if (txState.getType() == TransactionType.GET_LAST_TRANSACTION) {
                    // THIS WAS A PLAIN GET LAST TRANSACTION REQUEST, NOT FOR RECOVERY PURPOSES.
                    LOG.info("Retrieved last transaction as asked directly by the user.");
                    gltResponse.copyMerchantReceiptToCustomerReceipt();
                    txState.completed(m.getSuccessState(), m, "Last transaction retrieved");
                } else {
                    // TH-4A - Let's try to match the received last transaction against the current transaction
                    final Message.SuccessState successState = gltMatch(gltResponse, txState.getType(),
                            txState.getAmountCents(), txState.getRequestTime(), "_NOT_IMPL_YET");
                    if (successState == Message.SuccessState.UNKNOWN) {
                        // TH-4N: Didn't Match our transaction. Consider Unknown State.
                        LOG.info("Did not match transaction.");
                        txState.unknownCompleted("Failed to recover transaction Status. Check EFTPOS. ");
                    } else {
                        // TH-4Y: We Matched, transaction finished, let's update ourselves
                        gltResponse.copyMerchantReceiptToCustomerReceipt();
                        txState.completed(m.getSuccessState(), m, "Transaction ended.");
                    }
                }
            }
        }
        txFlowStateChanged();
    }

    private void startTransactionMonitoring() {
        stopTransactionMonitoring();

        transactionMonitoringThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    boolean needsPublishing = false;
                    synchronized (txLock) {
                        final TransactionFlowState txState = getCurrentTxFlowState();
                        if (getCurrentFlow() == SpiFlow.TRANSACTION && !txState.isFinished()) {
                            if (txState.isAttemptingToCancel() && System.currentTimeMillis() > txState.getCancelAttemptTime() + maxWaitForCancelTx) {
                                // TH-2T - too long since cancel attempt - Consider unknown
                                LOG.info("Been too long waiting for transaction to cancel.");
                                txState.unknownCompleted("Waited long enough for cancel transaction result. Check EFTPOS. ");
                                needsPublishing = true;
                            } else if (txState.isRequestSent() && System.currentTimeMillis() > txState.getLastStateRequestTime() + checkOnTxFrequency) {
                                // TH-1T, TH-4T - It's been a while since we received an update, let's call a GLT
                                LOG.info("Checking on our transaction. Last we asked was at " + txState.getLastStateRequestTime() + "...");
                                txState.callingGlt();
                                callGetLastTransaction();
                            }
                        }
                    }
                    if (needsPublishing) txFlowStateChanged();

                    try {
                        Thread.sleep(txMonitorCheckFrequency);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        transactionMonitoringThread.start();
    }

    private void stopTransactionMonitoring() {
        if (transactionMonitoringThread != null) {
            // If we were already set up, clean up before restarting.
            transactionMonitoringThread.interrupt();
            transactionMonitoringThread = null;
        }
    }

    //endregion

    //region Internals for connection management

    private void resetConn() {
        // Setup the connection
        conn = new Connection(eftposAddress);
        // Register our event handlers
        conn.setEventHandler(new Connection.EventHandler() {
            @Override
            public void onConnectionStateChanged(Connection.State state) {
                onSpiConnectionStatusChanged(state);
            }

            @Override
            public void onMessageReceived(String message) {
                onSpiMessageReceived(message);
            }

            @Override
            public void onError(Throwable thr) {
                onWsErrorReceived(thr);
            }
        });
    }

    /**
     * This method will be called when the connection status changes.
     * <p>
     * You are encouraged to display a PIN pad connection indicator on the POS screen.
     */
    private void onSpiConnectionStatusChanged(@NotNull Connection.State state) {
        switch (state) {
            case CONNECTING:
                LOG.info("I'm connecting to the EFTPOS at " + eftposAddress + "...");
                break;

            case CONNECTED:
                if (getCurrentFlow() == SpiFlow.PAIRING) {
                    getCurrentPairingFlowState().setMessage("Requesting to pair...");
                    pairingFlowStateChanged();
                    final PairRequest pr = PairingHelper.newPairRequest();
                    send(pr.toMessage());
                } else {
                    LOG.info("I'm connected to " + eftposAddress + "...");
                    spiMessageStamp.setSecrets(secrets);
                    startPeriodicPing();
                }
                break;

            case DISCONNECTED:
                // Let's reset some lifecycle related to connection state, ready for next connection
                LOG.info("I'm disconnected from " + eftposAddress + "...");
                mostRecentPingSent = null;
                mostRecentPongReceived = null;
                missedPongsCount = 0;
                mostRecentLoginResponse = null;
                readyToTransact = false;
                stopPeriodicPing();

                if (getCurrentStatus() != SpiStatus.UNPAIRED) {
                    setCurrentStatus(SpiStatus.PAIRED_CONNECTING);

                    synchronized (txLock) {
                        if (getCurrentFlow() == SpiFlow.TRANSACTION && !getCurrentTxFlowState().isFinished()) {
                            // we're in the middle of a transaction, just so you know!
                            // TH-1D
                            LOG.warn("Lost connection in the middle of a transaction...");
                        }
                    }

                    LOG.info("Will try to reconnect in 5s...");
                    reconnectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (getCurrentStatus() != SpiStatus.UNPAIRED) {
                                // This is non-blocking
                                try {
                                    conn.connect();
                                } catch (DeploymentException e) {
                                    LOG.error("Failed to connect", e);
                                }
                            }
                        }
                    }, TimeUnit.SECONDS.toMillis(5));
                } else if (getCurrentFlow() == SpiFlow.PAIRING) {
                    LOG.warn("Lost Connection during pairing.");
                    getCurrentPairingFlowState().setMessage("Could not Connect to Pair. Check Network and Try Again...");
                    onPairingFailed();
                    pairingFlowStateChanged();
                }
                break;
            default:
                throw new IllegalArgumentException(state.toString());
        }
    }

    /**
     * This is an important piece of the puzzle. It's a background thread that periodically
     * sends pings to the server. If it doesn't receive pongs, it considers the connection as broken
     * so it disconnects.
     */
    private void startPeriodicPing() {
        stopPeriodicPing();

        periodicPingThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted() && conn.isConnected() && secrets != null) {
                    doPing();

                    try {
                        Thread.sleep(pongTimeout);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (mostRecentPingSent != null &&
                            (mostRecentPongReceived == null || !mostRecentPongReceived.getId().equals(mostRecentPingSent.getId()))) {

                        missedPongsCount += 1;
                        LOG.warn("EFTPOS didn't reply to my ping. Missed count: " + missedPongsCount + "/" + missedPongsToDisconnect + ". ");

                        if (missedPongsCount < missedPongsToDisconnect) {
                            LOG.info("Trying another ping...");
                            continue;
                        }

                        // This means that we have reached missed pong limit.
                        // We consider this connection as broken.
                        // Let's Disconnect.
                        LOG.warn("Disconnecting...");
                        conn.disconnect();
                        readyToTransact = false;
                        break;
                    }

                    missedPongsCount = 0;
                    try {
                        Thread.sleep(pingFrequency - pongTimeout);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        periodicPingThread.start();
    }

    /**
     * When we disconnect, we should also stop the periodic ping.
     */
    private void stopPeriodicPing() {
        if (periodicPingThread != null) {
            // If we were already set up, clean up before restarting.
            periodicPingThread.interrupt();
            periodicPingThread = null;
        }
    }

    /**
     * We call this ourselves as soon as we're ready to transact with the pin pad after a connection is established.
     * This function is effectively called after we received the first login response from the PIN pad.
     */
    private void onReadyToTransact() {
        // So, we have just made a connection, pinged and logged in successfully.
        setCurrentStatus(SpiStatus.PAIRED_CONNECTED);

        synchronized (txLock) {
            if (getCurrentFlow() == SpiFlow.TRANSACTION && !getCurrentTxFlowState().isFinished()) {
                if (getCurrentTxFlowState().isRequestSent()) {
                    // TH-3A - We've just reconnected and were in the middle of Tx.
                    // Let's get the last transaction to check what we might have missed out on.
                    getCurrentTxFlowState().callingGlt();
                    callGetLastTransaction();
                } else {
                    // TH-3AR - We had not even sent the request yet. Let's do that now
                    send(getCurrentTxFlowState().getRequest());
                    getCurrentTxFlowState().sent("Asked EFTPOS to accept payment for " + (getCurrentTxFlowState()).getAmountCents() / 100.0);
                    txFlowStateChanged();
                }
            }
        }
    }

    /**
     * Send a ping to the server.
     */
    private void doPing() {
        final Message ping = PingHelper.generatePingRequest();
        mostRecentPingSent = ping;
        send(ping);
    }

    /**
     * Received a pong from the server.
     */
    private void handleIncomingPong(Message m) {
        // We need to maintain this time delta otherwise the server will not accept our messages.
        spiMessageStamp.setServerTimeDelta(m.getServerTimeDelta());

        boolean needLogin = true;
        try {
            if (mostRecentLoginResponse != null &&
                    !mostRecentLoginResponse.expiringSoon(spiMessageStamp.getServerTimeDelta())) {
                needLogin = false;
            }
        } catch (ParseException e) {
            LOG.warn("Failed to parse server time delta: " + spiMessageStamp.getServerTimeDelta());
        }

        if (needLogin) {
            // We have not logged in yet, or login expiring soon.
            doLogin();
        }
        mostRecentPongReceived = m;
    }

    /**
     * Login is a mute thing but is required.
     */
    private void doLogin() {
        final LoginRequest lr = LoginHelper.newLoginRequest();
        send(lr.toMessage());
    }

    /**
     * When the server replied to our LoginRequest with a LoginResponse, we take note of it.
     */
    private void handleLoginResponse(@NotNull Message m) {
        final LoginResponse lr = new LoginResponse(m);
        if (lr.getSuccess()) {
            mostRecentLoginResponse = lr;

            if (!readyToTransact) {
                // We are finally ready to make transactions.
                // Let's notify ourselves so we can take some actions.
                readyToTransact = true;
                LOG.info("Logged in successfully. Expires: " + lr.getExpires());
                if (getCurrentStatus() != SpiStatus.UNPAIRED)
                    onReadyToTransact();
            } else {
                LOG.info("I have just refreshed my login. Now expires: " + lr.getExpires());
            }
        } else {
            LOG.info("Logged in failure.");
            conn.disconnect();
        }
    }

    /**
     * The server will also send us pings. We need to reply with a pong so it doesn't disconnect us.
     */
    private void handleIncomingPing(@NotNull Message m) {
        final Message pong = PongHelper.generatePongResponse(m);
        send(pong);
    }

    /**
     * Ask the PIN pad to tell us what the most recent transaction was.
     */
    private void callGetLastTransaction() {
        final GetLastTransactionRequest gltRequest = new GetLastTransactionRequest();
        send(gltRequest.toMessage());
    }

    /**
     * This method will be called whenever we receive a message from the connection.
     */
    private void onSpiMessageReceived(@NotNull String messageJson) {
        // First we parse the incoming message
        final Message m = Message.fromJson(messageJson, secrets);
        LOG.debug("Received: " + m.getDecryptedJson());

        // And then we switch on the event type.
        final String eventName = m.getEventName();
        if (Events.KEY_REQUEST.equals(eventName)) {
            handleKeyRequest(m);
        } else if (Events.KEY_CHECK.equals(eventName)) {
            handleKeyCheck(m);
        } else if (Events.PAIR_RESPONSE.equals(eventName)) {
            handlePairResponse(m);
        } else if (Events.LOGIN_RESPONSE.equals(eventName)) {
            handleLoginResponse(m);
        } else if (Events.PURCHASE_RESPONSE.equals(eventName)) {
            handlePurchaseResponse(m);
        } else if (Events.REFUND_RESPONSE.equals(eventName)) {
            handleRefundResponse(m);
        } else if (Events.SIGNATURE_REQUIRED.equals(eventName)) {
            handleSignatureRequired(m);
        } else if (Events.GET_LAST_TRANSACTION_RESPONSE.equals(eventName)) {
            handleGetLastTransactionResponse(m);
        } else if (Events.SETTLE_RESPONSE.equals(eventName)) {
            handleSettleResponse(m);
        } else if (Events.PING.equals(eventName)) {
            handleIncomingPing(m);
        } else if (Events.PONG.equals(eventName)) {
            handleIncomingPong(m);
        } else if (Events.KEY_ROLL_REQUEST.equals(eventName)) {
            handleKeyRollingRequest(m);
        } else if (Events.ERROR.equals(eventName)) {
            handleErrorEvent(m);
        } else if (Events.INVALID_HMAC_SIGNATURE.equals(eventName)) {
            LOG.info("I could not verify message from EFTPOS. You might have to un-pair EFTPOS and then reconnect.");
        } else {
            LOG.info("I don't understand event: " + eventName + ", " + m.getData() + ". Perhaps I have not implemented it yet.");
        }
    }

    private void onWsErrorReceived(@Nullable Throwable error) {
        LOG.warn("Received WS error: " + error);
    }

    private boolean send(Message message) {
        final String json = message.toJson(spiMessageStamp);
        if (conn.isConnected()) {
            LOG.debug("Sending: " + message.getDecryptedJson());
            conn.send(json);
            return true;
        } else {
            LOG.debug("Asked to send, but not connected: " + message.getDecryptedJson());
            return false;
        }
    }

    //endregion

    /**
     * Typed event handler for values.
     *
     * @param <T> Value type.
     */
    public interface EventHandler<T> {
        void onEvent(T value);
    }

    /**
     * Compatibility problem detected by the client.
     */
    public static class CompatibilityException extends Exception {

        public CompatibilityException(@NotNull String message, @NotNull GeneralSecurityException cause) {
            super(message, cause);
        }

        @Override
        public synchronized GeneralSecurityException getCause() {
            return (GeneralSecurityException) super.getCause();
        }

    }

}
