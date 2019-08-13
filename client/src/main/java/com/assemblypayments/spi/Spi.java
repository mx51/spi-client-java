package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.service.DeviceService;
import com.assemblypayments.spi.util.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * SPI integration client, used to manage connection to the terminal.
 */
public class Spi {

    //region Private state

    private static final Logger LOG = LoggerFactory.getLogger("spi");

    static final String PROTOCOL_VERSION = "2.6.0";

    private static final long RECONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
    private static final long TX_MONITOR_CHECK_FREQUENCY = TimeUnit.SECONDS.toMillis(1);
    private static final long CHECK_ON_TX_FREQUENCY = TimeUnit.SECONDS.toMillis(20);
    private static final long MAX_WAIT_FOR_CANCEL_TX = TimeUnit.SECONDS.toMillis(10);
    private static final long PONG_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    private static final long PING_FREQUENCY = TimeUnit.SECONDS.toMillis(18);

    private String posId;
    private String eftposAddress;
    private String serialNumber;
    private String deviceApiKey;
    private String acquirerCode;
    private boolean inTestMode;
    private boolean autoAddressResolutionEnabled;
    private Secrets secrets;
    private MessageStamp spiMessageStamp;
    private String posVendorId;
    private String posVersion;
    private boolean hasSetInfo;

    private Connection conn;

    private SpiStatus currentStatus;
    private SpiFlow currentFlow;
    private PairingFlowState currentPairingFlowState;
    private TransactionFlowState currentTxFlowState;
    private DeviceAddressStatus currentDeviceStatus;
    private EventHandler<SpiStatus> statusChangedHandler;
    private EventHandler<PairingFlowState> pairingFlowStateChangedHandler;
    private EventHandler<TransactionFlowState> txFlowStateChangedHandler;
    private EventHandler<Secrets> secretsChangedHandler;
    private EventHandler<DeviceAddressStatus> deviceAddressChangedHandler;

    private PrintingResponseDelegate printingResponseDelegate;
    private TerminalStatusResponseDelegate terminalStatusResponseDelegate;
    private BatteryLevelChangedDelegate batteryLevelChangedDelegate;
    private TerminalConfigurationResponseDelegate terminalConfigurationResponseDelegate;

    private Message mostRecentPingSent;
    private long mostRecentPingSentTime;
    private Message mostRecentPongReceived;
    private int missedPongsCount;
    private int retriesSinceLastDeviceAddressResolution = 0;
    private Thread periodicPingThread;
    private Thread transactionMonitoringThread;

    private final Object txLock = new Object();
    private final long missedPongsToDisconnect = 2;
    private final int retriesBeforeResolvingDeviceAddress = 3;

    private SpiPayAtTable spiPat;

    private SpiPreauth spiPreauth;

    private ScheduledThreadPoolExecutor reconnectExecutor;
    private ScheduledFuture reconnectFuture;

    final SpiConfig config = new SpiConfig();

    private final Pattern regexItemsForEftposAddress = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
    private final Pattern regexItemsForPosId = Pattern.compile("[a-zA-Z0-9]*$");

    private int retriesSinceLastPairing = 0;
    private final int retriesBeforePairing = 3;

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
    public Spi(@NotNull String posId, @NotNull String serialNumber, @NotNull String eftposAddress, @Nullable Secrets secrets)
            throws CompatibilityException {

        try {
            Crypto.checkCompatibility();
            LOG.info("Compatibility check passed");
        } catch (GeneralSecurityException e) {
            throw new CompatibilityException("JDK configuration incompatible with SPI", e);
        }

        posId = validatePosId(posId);
        validateEftposAddress(eftposAddress);

        this.posId = posId;
        this.eftposAddress = "ws://" + eftposAddress;
        this.secrets = secrets;
        this.serialNumber = serialNumber;

        // Default state
        currentStatus = SpiStatus.UNPAIRED;
        currentFlow = SpiFlow.IDLE;

        // Our stamp for signing outgoing messages
        spiMessageStamp = new MessageStamp(this.posId, this.secrets, 0);

        // We will maintain some state
        mostRecentPingSent = null;
        mostRecentPongReceived = null;
        missedPongsCount = 0;
    }

    public SpiPayAtTable enablePayAtTable() {
        spiPat = new SpiPayAtTable(this);
        return spiPat;
    }

    public SpiPreauth enablePreauth() {
        spiPreauth = new SpiPreauth(this, txLock);
        return spiPreauth;
    }

    /**
     * Call this method after constructing an instance of the class and subscribing to events.
     * It will start background maintenance threads.
     * <p>
     * Most importantly, it connects to the EFTPOS server if it has secrets.
     */
    public void start() {
        if (StringUtils.isBlank(posVendorId) || StringUtils.isBlank(posVersion)) {
            // POS information is now required to be set
            LOG.warn("Missing POS vendor ID and version. posVendorId and posVersion are required before starting");
            throw new IllegalArgumentException("Missing POS vendor ID and version. posVendorId and posVersion are required before starting");
        }

        reconnectExecutor = new ScheduledThreadPoolExecutor(5);
        reconnectFuture = null;

        resetConn();
        startTransactionMonitoring();

        setCurrentFlow(SpiFlow.IDLE);
        if (secrets != null) {
            LOG.info("Starting in paired state");
            currentStatus = SpiStatus.PAIRED_CONNECTING;
            conn.connect(); // This is non-blocking
        } else {
            LOG.info("Starting in unpaired state");
            currentStatus = SpiStatus.UNPAIRED;
        }
    }

    /**
     * Set the acquirer code of your bank, please contact Assembly's Integration Engineers for acquirer code.
     */
    public void setAcquirerCode(String acquirerCode) {
        this.acquirerCode = acquirerCode;
    }

    /**
     * Set the api key used for auto address discovery feature, please contact Assembly's Integration Engineers for Api key.
     */
    public void setDeviceApiKey(String deviceApiKey) {
        this.deviceApiKey = deviceApiKey;
    }

    /**
     * Allows you to set the serial number of the Eftpos
     */
    public boolean setSerialNumber(String serialNumber) {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) return false;

        String was = this.serialNumber;
        this.serialNumber = serialNumber;

        if (hasSerialNumberChanged(was)) {
            autoResolveEftposAddress();
        } else {
            getCurrentDeviceStatus().setDeviceAddressResponseCode(DeviceAddressResponseCode.SERIAL_NUMBER_NOT_CHANGED);
            deviceStatusChanged(getCurrentDeviceStatus());
        }

        return true;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Allows you to set the auto address discovery feature.
     */
    public boolean setAutoAddressResolution(boolean autoAddressResolutionEnable) {
        if (getCurrentStatus() == SpiStatus.PAIRED_CONNECTED) return false;

        boolean was = this.autoAddressResolutionEnabled;
        this.autoAddressResolutionEnabled = autoAddressResolutionEnable;
        if (autoAddressResolutionEnable && !was) {
            // we're turning it on
            autoResolveEftposAddress();
        }

        return true;
    }

    public boolean isAutoAddressResolutionEnabled() {
        return this.autoAddressResolutionEnabled;
    }

    /**
     * Call this method to set the client library test mode.
     * Set it to true only while you are developing the integration.
     * It defaults to false. For a real merchant, always leave it set to false.
     */
    public boolean setTestMode(boolean testMode) {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) return false;

        if (testMode == inTestMode) return true;

        // we're changing mode
        inTestMode = testMode;
        autoResolveEftposAddress();
        return true;
    }

    /**
     * Allows you to set the pos ID, which identifies this instance of your POS.
     * Can only be called in the unpaired state.
     */
    public boolean setPosId(@NotNull String id) {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) return false;
        posId = validatePosId(id);
        spiMessageStamp.setPosId(id);
        return true;
    }

    /**
     * Allows you to set the PIN pad address only if auto address is not enabled. Sometimes the PIN pad might change IP address (we recommend
     * reserving static IPs if possible). Either way you need to allow your User to enter the IP address
     * of the PIN pad.
     */
    public boolean setEftposAddress(String address) {
        if (getCurrentStatus() == SpiStatus.PAIRED_CONNECTED || autoAddressResolutionEnabled) return false;
        validateEftposAddress(address);
        eftposAddress = "ws://" + address;
        conn.setAddress(eftposAddress);
        return true;
    }

    /**
     * Sets values used to identify the POS software to the EFTPOS terminal.
     * <p>
     * Must be set before starting!
     *
     * @param posVendorId Vendor identifier of the POS itself.
     * @param posVersion  Version string of the POS itself.
     */
    public void setPosInfo(String posVendorId, String posVersion) {
        this.posVendorId = posVendorId;
        this.posVersion = posVersion;
    }

    /**
     * Retrieves package version of the SPI client library.
     *
     * @return Full version (e.g. '2.0.1') or, when running locally, protocol version (e.g. '2.0.0-PROTOCOL').
     */
    @NotNull
    public static String getVersion() {
        return Config.VERSION;
    }

    //endregion

    //region Properties and events

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
    public void setCurrentStatus(@NotNull SpiStatus value) {
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
    void setCurrentFlow(@NotNull SpiFlow value) {
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
    void setCurrentTxFlowState(TransactionFlowState state) {
        currentTxFlowState = state;
    }

    public DeviceAddressStatus getCurrentDeviceStatus() {
        return currentDeviceStatus;
    }

    private void setCurrentDeviceStatus(DeviceAddressStatus state) {
        currentDeviceStatus = state;
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

    /**
     * Subscribe to this event when you want to know if the address of the device have changed
     */
    public void setDeviceAddressChangedHandler(@Nullable EventHandler<DeviceAddressStatus> handler) {
        deviceAddressChangedHandler = handler;
    }

    public void setPrintingResponseDelegate(PrintingResponseDelegate printingResponseDelegate) {
        this.printingResponseDelegate = printingResponseDelegate;
    }

    public void setTerminalStatusResponseDelegate(TerminalStatusResponseDelegate terminalStatusResponseDelegate) {
        this.terminalStatusResponseDelegate = terminalStatusResponseDelegate;
    }

    public void setBatteryLevelChangedDelegate(BatteryLevelChangedDelegate batteryLevelChangedDelegate) {
        this.batteryLevelChangedDelegate = batteryLevelChangedDelegate;
    }

    public void setTerminalConfigurationResponseDelegate(TerminalConfigurationResponseDelegate terminalConfigurationResponseDelegate) {
        this.terminalConfigurationResponseDelegate = terminalConfigurationResponseDelegate;
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

    void txFlowStateChanged() {
        if (txFlowStateChangedHandler != null) {
            txFlowStateChangedHandler.onEvent(getCurrentTxFlowState());
        }
    }

    private void secretsChanged(Secrets value) {
        if (secretsChangedHandler != null) {
            secretsChangedHandler.onEvent(value);
        }
    }

    private void deviceStatusChanged(DeviceAddressStatus value) {
        if (deviceAddressChangedHandler != null) {
            deviceAddressChangedHandler.onEvent(value);
        }
    }

    public SpiConfig getConfig() {
        return config;
    }

    public void printReport(String key, String payload) {
        send(new PrintingRequest(key, payload).toMessage());
    }

    public void getTerminalStatus() {
        send(new TerminalStatusRequest().toMessage());
    }

    public void getTerminalConfiguration() {
        send(new TerminalConfigurationRequest().toMessage());
    }

    //endregion

    //region Flow management methods

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

    //endregion

    //region Pairing flow methods

    /**
     * This will connect to the EFTPOS and start the pairing process.
     * <p>
     * Only call this if you are in the {@link SpiStatus#UNPAIRED} state.
     * <p>
     * Subscribe to {@link #setPairingFlowStateChangedHandler(EventHandler)} to get updates on the pairing process.
     *
     * @return Whether pairing has initiated or not.
     */
    public boolean pair() {
        if (getCurrentStatus() != SpiStatus.UNPAIRED) {
            LOG.warn("Tried to pair but we're already paired");
            return false;
        }

        if (StringUtils.isBlank(posId) || StringUtils.isBlank(eftposAddress)) {
            LOG.warn("Tried to pair but missing posId and/or eftposAddress");
            return false;
        }

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

        conn.connect(); // Non-Blocking
        return true;
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
            LOG.info("Pair code confirmed from POS side, but I'm still waiting for confirmation from EFTPOS");
            getCurrentPairingFlowState().setMessage("Click YES on EFTPOS if code is: " + getCurrentPairingFlowState().getConfirmationCode());
            pairingFlowStateChanged();
        } else {
            // Already confirmed from EFTPOS - So all good now. We're Paired also from the POS perspective.
            LOG.info("Pair code confirmed from POS side, and was already confirmed from EFTPOS side, pairing finalized");
            onPairingSuccess();
            onReadyToTransact();
        }
    }

    /**
     * Call this if your user clicks 'Cancel' or 'No' during the pairing process.
     */
    public void pairingCancel() {
        if (getCurrentFlow() != SpiFlow.PAIRING || getCurrentPairingFlowState().isFinished()) return;

        if (getCurrentPairingFlowState().isAwaitingCheckFromPos() &&
                !getCurrentPairingFlowState().isAwaitingCheckFromEftpos()) {
            // This means that the Eftpos already thinks it's paired.
            // Let's tell it to drop keys
            send(new DropKeysRequest().toMessage());
        }

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

        // Best effort letting the EFTPOS know that we're dropping the keys, so it can drop them as well.
        send(new DropKeysRequest().toMessage());
        doUnpair();
        return true;
    }

    //endregion

    //region Transaction methods

    /**
     * Initiates a purchase transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId       Alphanumeric identifier for your purchase.
     * @param purchaseAmount Amount in cents to charge.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiatePurchaseTx(String posRefId, int purchaseAmount) {
        return initiatePurchaseTx(posRefId, purchaseAmount, 0, 0, false, null, 0);
    }

    /**
     * Initiates a purchase transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId         Alphanumeric identifier for your purchase.
     * @param purchaseAmount   Amount in cents to charge.
     * @param tipAmount        The Tip Amount in cents.
     * @param cashoutAmount    The cashout Amount in cents.
     * @param promptForCashout Whether to prompt your customer for cashout on the EFTPOS.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiatePurchaseTx(String posRefId, int purchaseAmount, int tipAmount, int cashoutAmount, boolean promptForCashout) {
        return initiatePurchaseTx(posRefId, purchaseAmount, tipAmount, cashoutAmount, promptForCashout, null, 0);
    }

    /**
     * Initiates a purchase transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId         Alphanumeric identifier for your purchase.
     * @param purchaseAmount   Amount in cents to charge.
     * @param tipAmount        The Tip Amount in cents.
     * @param cashoutAmount    The cashout Amount in cents.
     * @param promptForCashout Whether to prompt your customer for cashout on the EFTPOS.
     * @param options          Additional options applied on per-transaction basis.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiatePurchaseTx(String posRefId, int purchaseAmount, int tipAmount, int cashoutAmount, boolean promptForCashout, TransactionOptions options) {
        return initiatePurchaseTx(posRefId, purchaseAmount, tipAmount, cashoutAmount, promptForCashout, options, 0);
    }

    /**
     * Initiates a purchase transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId         Alphanumeric identifier for your purchase.
     * @param purchaseAmount   Amount in cents to charge.
     * @param tipAmount        The Tip Amount in cents.
     * @param cashoutAmount    The cashout Amount in cents.
     * @param promptForCashout Whether to prompt your customer for cashout on the EFTPOS.
     * @param options          Additional options applied on per-transaction basis.
     * @param surchargeAmount  The Surcharge Amount in cents.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiatePurchaseTx(String posRefId, int purchaseAmount, int tipAmount, int cashoutAmount, boolean promptForCashout, TransactionOptions options, int surchargeAmount) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        if (tipAmount > 0 && (cashoutAmount > 0 || promptForCashout))
            return new InitiateTxResult(false, "Cannot Accept Tips and Cashout at the same time.");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");
            setCurrentFlow(SpiFlow.TRANSACTION);

            final PurchaseRequest request = PurchaseHelper.createPurchaseRequest(purchaseAmount, posRefId, tipAmount, cashoutAmount, promptForCashout, surchargeAmount);
            request.setConfig(config);
            request.setOptions(options);
            final Message message = request.toMessage();

            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.PURCHASE, purchaseAmount, message,
                    "Waiting for EFTPOS connection to make payment request. " + request.amountSummary()));

            if (send(message)) {
                getCurrentTxFlowState().sent("Asked EFTPOS to accept payment for " + request.amountSummary());
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
     * @param posRefId     Alphanumeric identifier for your refund.
     * @param refundAmount Amount in cents to charge.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiateRefundTx(String posRefId, int refundAmount) {
        return initiateRefundTx(posRefId, refundAmount, false);
    }

    /**
     * Initiates a refund transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId                 Alphanumeric identifier for your refund.
     * @param refundAmount             Amount in cents to charge.
     * @param suppressMerchantPassword Allow to control the Merchant Password in VAA
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiateRefundTx(String posRefId, int refundAmount, boolean suppressMerchantPassword) {
        return initiateRefundTx(posRefId, refundAmount, suppressMerchantPassword, null);
    }

    /**
     * Initiates a refund transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     *
     * @param posRefId                 Alphanumeric identifier for your refund.
     * @param refundAmount             Amount in cents to charge.
     * @param suppressMerchantPassword Allow to control the Merchant Password in VAA
     * @param options                  Additional options applied on per-transaction basis.
     * @return Initiation result {@link InitiateTxResult}.
     */
    @NotNull
    public InitiateTxResult initiateRefundTx(String posRefId, int refundAmount, boolean suppressMerchantPassword, TransactionOptions options) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final RefundRequest request = PurchaseHelper.createRefundRequest(refundAmount, posRefId, suppressMerchantPassword);
            request.setConfig(config);
            request.setOptions(options);
            final Message message = request.toMessage();

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.REFUND, refundAmount, message,
                    String.format("Waiting for EFTPOS connection to make refund request for %.2f", refundAmount / 100.0)));

            if (send(message)) {
                getCurrentTxFlowState().sent(String.format("Asked EFTPOS to refund %.2f", refundAmount / 100.0));
            }
        }

        txFlowStateChanged();
        return new InitiateTxResult(true, "Refund Initiated");
    }

    /**
     * Let the EFTPOS know whether merchant accepted or declined the signature.
     *
     * @param accepted Whether merchant accepted the signature from customer or not.
     * @return MidTxResult - false only if you called it in the wrong state.
     */
    @NotNull
    public MidTxResult acceptSignature(boolean accepted) {
        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION ||
                    getCurrentTxFlowState().isFinished() ||
                    !getCurrentTxFlowState().isAwaitingSignatureCheck()) {
                LOG.info("Asked to accept signature but I was not waiting for one.");
                return new MidTxResult(false, "Asked to accept signature but I was not waiting for one.");
            }

            getCurrentTxFlowState().signatureResponded(accepted ? "Accepting Signature..." : "Declining Signature...");
            final SignatureRequired sigReqMsg = getCurrentTxFlowState().getSignatureRequiredMessage();
            final String sigReqId = sigReqMsg.getRequestId();
            send((accepted ? new SignatureAccept(sigReqId) : new SignatureDecline(sigReqId)).toMessage());
        }
        txFlowStateChanged();
        return new MidTxResult(true, "");
    }

    /**
     * Submit the Code obtained by your user when phoning for auth.
     * It will return immediately to tell you whether the code has a valid format or not.
     * If valid==true is returned, no need to do anything else. Expect updates via standard callback.
     * If valid==false is returned, you can show your user the accompanying message, and invite them to enter another code.
     *
     * @param authCode The code obtained by your user from the merchant call centre. It should be a 6-character alpha-numeric value.
     * @return Whether code has a valid format or not.
     */
    @NotNull
    public SubmitAuthCodeResult submitAuthCode(String authCode) {
        if (authCode.length() != 6) {
            return new SubmitAuthCodeResult(false, "Not a 6-digit code.");
        }

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION ||
                    getCurrentTxFlowState().isFinished() ||
                    !getCurrentTxFlowState().isAwaitingPhoneForAuth()) {
                LOG.info("Asked to send auth code but I was not waiting for one.");
                return new SubmitAuthCodeResult(false, "Was not waiting for one.");
            }

            getCurrentTxFlowState().authCodeSent("Submitting Auth Code " + authCode);
            send(new AuthCodeAdvice(getCurrentTxFlowState().getPosRefId(), authCode).toMessage());
        }
        txFlowStateChanged();
        return new SubmitAuthCodeResult(true, "Valid Code.");
    }

    /**
     * Attempts to cancel a transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to see how it goes.
     * <p>
     * Wait for the transaction to be finished and then see whether cancellation was successful or not.
     *
     * @return MidTxResult - false only if you called it in the wrong state.
     */
    @NotNull
    public MidTxResult cancelTransaction() {
        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.TRANSACTION || getCurrentTxFlowState().isFinished()) {
                LOG.info("Asked to cancel transaction but I was not in the middle of one.");
                return new MidTxResult(false, "Asked to cancel transaction but I was not in the middle of one.");
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
        return new MidTxResult(true, "");
    }

    /**
     * Initiates a cashout only transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} event to get updates on the process.
     *
     * @param posRefId    Alphanumeric identifier for your transaction.
     * @param amountCents Amount in cents to cash out.
     */
    @NotNull
    public InitiateTxResult initiateCashoutOnlyTx(String posRefId, int amountCents) {
        return initiateCashoutOnlyTx(posRefId, amountCents, 0);
    }

    /**
     * Initiates a cashout only transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} event to get updates on the process.
     *
     * @param posRefId        Alphanumeric identifier for your transaction.
     * @param amountCents     Amount in cents to cash out.
     * @param surchargeAmount Amount in cents to surcharge.
     */
    @NotNull
    public InitiateTxResult initiateCashoutOnlyTx(String posRefId, int amountCents, int surchargeAmount) {
        return initiateCashoutOnlyTx(posRefId, amountCents, surchargeAmount, null);
    }

    /**
     * Initiates a cashout only transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} event to get updates on the process.
     *
     * @param posRefId        Alphanumeric identifier for your transaction.
     * @param amountCents     Amount in cents to cash out.
     * @param surchargeAmount Amount in cents to surcharge.
     * @param options         Additional options applied on per-transaction basis.
     */
    @NotNull
    public InitiateTxResult initiateCashoutOnlyTx(String posRefId, int amountCents, int surchargeAmount, TransactionOptions options) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final CashoutOnlyRequest cashoutOnlyRequest = new CashoutOnlyRequest(amountCents, posRefId);
            cashoutOnlyRequest.setSurchargeAmount(surchargeAmount);
            cashoutOnlyRequest.setConfig(config);
            cashoutOnlyRequest.setOptions(options);
            final Message cashoutMsg = cashoutOnlyRequest.toMessage();

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.CASHOUT_ONLY, amountCents, cashoutMsg,
                    String.format("Waiting for EFTPOS connection to send cashout request for %.2f", amountCents / 100.0)));

            if (send(cashoutMsg)) {
                getCurrentTxFlowState().sent(String.format("Asked EFTPOS to do cashout for %.2f", amountCents / 100.0));
            }
        }
        txFlowStateChanged();
        return new InitiateTxResult(true, "Cashout Initiated");
    }

    /**
     * Initiates a Mail Order / Telephone Order Purchase Transaction.
     *
     * @param posRefId    Alphanumeric identifier for your transaction.
     * @param amountCents Amount in cents
     */
    @NotNull
    public InitiateTxResult initiateMotoPurchaseTx(String posRefId, int amountCents) {
        return initiateMotoPurchaseTx(posRefId, amountCents, 0);
    }

    /**
     * Initiates a Mail Order / Telephone Order Purchase Transaction.
     *
     * @param posRefId        Alphanumeric identifier for your transaction.
     * @param amountCents     Amount in cents
     * @param surchargeAmount Surcharge amount in cents.
     */
    @NotNull
    public InitiateTxResult initiateMotoPurchaseTx(String posRefId, int amountCents, int surchargeAmount) {
        return initiateMotoPurchaseTx(posRefId, amountCents, surchargeAmount, false);
    }

    /**
     * Initiates a Mail Order / Telephone Order Purchase Transaction.
     *
     * @param posRefId                 Alphanumeric identifier for your transaction.
     * @param amountCents              Amount in cents
     * @param surchargeAmount          Surcharge amount in cents.
     * @param suppressMerchantPassword Allow to control the Merchant Password in VAA.
     */
    @NotNull
    public InitiateTxResult initiateMotoPurchaseTx(String posRefId, int amountCents, int surchargeAmount, boolean suppressMerchantPassword) {
        return initiateMotoPurchaseTx(posRefId, amountCents, surchargeAmount, suppressMerchantPassword, null);
    }

    /**
     * Initiates a Mail Order / Telephone Order Purchase Transaction.
     *
     * @param posRefId                 Alphanumeric identifier for your transaction.
     * @param amountCents              Amount in cents
     * @param surchargeAmount          Surcharge amount in cents.
     * @param suppressMerchantPassword Allow to control the Merchant Password in VAA.
     * @param options                  Additional options applied on per-transaction basis.
     */
    @NotNull
    public InitiateTxResult initiateMotoPurchaseTx(String posRefId, int amountCents, int surchargeAmount, boolean suppressMerchantPassword, TransactionOptions options) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final MotoPurchaseRequest request = new MotoPurchaseRequest(amountCents, posRefId);
            request.setSurchargeAmount(surchargeAmount);
            request.setConfig(config);
            request.setOptions(options);
            request.setSuppressMerchantPassword(suppressMerchantPassword);
            final Message message = request.toMessage();

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.MOTO, amountCents, message,
                    String.format("Waiting for EFTPOS connection to send MOTO request for %.2f", amountCents / 100.0)));

            if (send(message)) {
                getCurrentTxFlowState().sent(String.format("Asked EFTPOS do MOTO for %.2f", amountCents / 100.0));
            }
        }
        txFlowStateChanged();
        return new InitiateTxResult(true, "MOTO Initiated");
    }

    /**
     * Initiates a settlement transaction.
     * <p>
     * Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     */
    @NotNull
    public InitiateTxResult initiateSettleTx(String posRefId) {
        return initiateSettleTx(posRefId, null);
    }

    /**
     * Initiates a settlement transaction.
     *
     * @param options Additional options applied on per-transaction basis.
     *                Be subscribed to {@link #setTxFlowStateChangedHandler(EventHandler)} to get updates on the process.
     */
    @NotNull
    public InitiateTxResult initiateSettleTx(String posRefId, TransactionOptions options) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final SettleRequest settleRequest = new SettleRequest(RequestIdHelper.id("settle"));
            settleRequest.setConfig(config);
            settleRequest.setOptions(options);
            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.SETTLE, 0, settleRequest.toMessage(),
                    "Waiting for EFTPOS connection to make a settle request"));

            if (send(settleRequest.toMessage())) {
                getCurrentTxFlowState().sent("Asked EFTPOS to settle.");
            }
        }
        txFlowStateChanged();
        return new InitiateTxResult(true, "Settle Initiated");
    }

    /**
     * Initiates settlement enquiry operation.
     */
    @NotNull
    public InitiateTxResult initiateSettlementEnquiry(String posRefId) {
        return initiateSettlementEnquiry(posRefId, null);
    }

    /**
     * Initiates settlement enquiry operation.
     *
     * @param options Additional options applied on per-transaction basis.
     */
    @NotNull
    public InitiateTxResult initiateSettlementEnquiry(String posRefId, TransactionOptions options) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final SettlementEnquiryRequest settleEnqRequest = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq"));
            settleEnqRequest.setConfig(config);
            settleEnqRequest.setOptions(options);

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, TransactionType.SETTLEMENT_ENQUIRY, 0, settleEnqRequest.toMessage(),
                    "Waiting for EFTPOS connection to make a settlement enquiry"));

            if (send(settleEnqRequest.toMessage())) {
                getCurrentTxFlowState().sent("Asked EFTPOS to make a settlement enquiry.");
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

            final Message message = new GetLastTransactionRequest().toMessage();

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    message.getId(), TransactionType.GET_LAST_TRANSACTION, 0, message,
                    "Waiting for EFTPOS connection to make a Get-Last-Transaction request"));

            getCurrentTxFlowState().callingGlt(message.getId());

            if (send(message)) {
                getCurrentTxFlowState().sent("Asked EFTPOS to Get Last Transaction.");
            }
        }
        txFlowStateChanged();
        return new InitiateTxResult(true, "GLT Initiated");
    }

    /**
     * This is useful to recover from your POS crashing in the middle of a transaction.
     * When you restart your POS, if you had saved enough state, you can call this method to recover the client library state.
     * You need to have the posRefId that you passed in with the original transaction, and the transaction type.
     * This method will return immediately whether recovery has started or not.
     * If recovery has started, you need to bring up the transaction modal to your user a be listening to TxFlowStateChanged.
     *
     * @param posRefId The is that you had assigned to the transaction that you are trying to recover.
     * @param txType   The transaction type.
     */
    @NotNull
    public InitiateTxResult initiateRecovery(String posRefId, TransactionType txType) {
        if (getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            final Message message = new GetLastTransactionRequest().toMessage();

            setCurrentFlow(SpiFlow.TRANSACTION);
            setCurrentTxFlowState(new TransactionFlowState(
                    posRefId, txType, 0, message,
                    "Waiting for EFTPOS connection to attempt recovery."));

            if (send(message)) {
                getCurrentTxFlowState().sent("Asked EFTPOS to recover state.");
            }
        }
        txFlowStateChanged();
        return new InitiateTxResult(true, "Recovery Initiated");
    }

    /**
     * GltMatch attempts to conclude whether a gltResponse matches an expected transaction and returns the outcome.
     * If Success/Failed is returned, it means that the gltResponse did match, and that transaction was successful/failed.
     * If Unknown is returned, it means that the gltResponse does not match the expected transaction.
     *
     * @param gltResponse The GetLastTransactionResponse message to check.
     * @param posRefId    The Reference Id that you passed in with the original request.
     */
    @NotNull
    public Message.SuccessState gltMatch(@NotNull GetLastTransactionResponse gltResponse, @NotNull String posRefId) {
        LOG.info("GLT CHECK: PosRefId: " + posRefId + "->" + gltResponse.getPosRefId());

        if (!posRefId.equals(gltResponse.getPosRefId())) {
            return Message.SuccessState.UNKNOWN;
        }

        return gltResponse.getSuccessState();
    }

    /**
     * Attempts to conclude whether a gltResponse matches an expected transaction and returns the outcome.
     * <p>
     * If {@link Message.SuccessState#SUCCESS}/{@link Message.SuccessState#FAILED} is returned, it means that
     * the GLT response did match, and that transaction was successful/failed.
     * <p>
     * If {@link Message.SuccessState#UNKNOWN} is returned, it means that the gltResponse does not match the
     * expected transaction.
     *
     * @param gltResponse    The {@link GetLastTransactionResponse} message to check.
     * @param expectedType   The expected type (e.g. Purchase, Refund).
     * @param expectedAmount The expected amount in cents.
     * @param requestTime    The time you made your request.
     * @param posRefId       The reference ID that you passed in with the original request. Currently not used.
     * @deprecated Use {@link #gltMatch(GetLastTransactionResponse, String)} instead.
     */
    @Deprecated
    @NotNull
    public Message.SuccessState gltMatch(@NotNull GetLastTransactionResponse gltResponse, @NotNull TransactionType expectedType,
                                         int expectedAmount, long requestTime, String posRefId) {
        return gltMatch(gltResponse, posRefId);
    }

    /**
     * Attempts to conclude whether a gltResponse matches an expected transaction and returns the outcome.
     * <p>
     * If {@link Message.SuccessState#SUCCESS}/{@link Message.SuccessState#FAILED} is returned, it means that
     * the GLT response did match, and that transaction was successful/failed.
     * <p>
     * If {@link Message.SuccessState#UNKNOWN} is returned, it means that the gltResponse does not match the
     * expected transaction.
     *
     * @param gltResponse    The {@link GetLastTransactionResponse} message to check.
     * @param expectedAmount The expected amount in cents.
     * @param requestTime    The time you made your request.
     * @param posRefId       The reference ID that you passed in with the original request. Currently not used.
     */
    @NotNull
    public Message.SuccessState gltMatch(@NotNull GetLastTransactionResponse gltResponse, int expectedAmount, long requestTime, String posRefId) {
        LOG.info("GLT CHECK: PosRefId: " + posRefId + "->" + gltResponse.getPosRefId());
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        Date gltBankDate = null;
        Date requestDateTime = null;

        try {
            gltBankDate = sdf.parse(gltResponse.getBankDateTimeString());
            requestDateTime = sdf.parse(String.valueOf(requestTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int compare = requestDateTime.compareTo(gltBankDate);

        if (!posRefId.equals(gltResponse.getPosRefId())) {
            return Message.SuccessState.UNKNOWN;
        }

        if (gltResponse.getTxType().toUpperCase().equals("PURCHASE") && gltResponse.getBankNonCashAmount() != expectedAmount && compare > 0) {
            return Message.SuccessState.UNKNOWN;
        }

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
        if (pairResp.isSuccess()) {
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

    private void handleDropKeysAdvice(Message m) {
        LOG.info("EFTPOS was unpaired. I shall unpair from my end as well.");
        doUnpair();
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

    private void doUnpair() {
        setCurrentStatus(SpiStatus.UNPAIRED);
        conn.disconnect();
        secrets = null;
        spiMessageStamp.setSecrets(null);
        secretsChanged(secrets);
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
     * The PIN pad server will send us this message when a customer signature is required.
     * We need to ask the customer to sign the incoming receipt.
     * And then tell the pin pad whether the signature is ok or not.
     */
    private void handleSignatureRequired(@NotNull Message m) {
        synchronized (txLock) {
            if (isTxResponseUnexpected(m, "Signature Required", true)) return;

            getCurrentTxFlowState().signatureRequired(new SignatureRequired(m), "Ask Customer to Sign the Receipt");
        }
        txFlowStateChanged();
    }

    /**
     * The PIN pad server will send us this message when an auth code is required.
     */
    private void handleAuthCodeRequired(@NotNull Message m) {
        synchronized (txLock) {
            if (isTxResponseUnexpected(m, "Auth Code Required", true)) return;

            final PhoneForAuthRequired phoneForAuthRequired = new PhoneForAuthRequired(m);
            final String msg = "Auth Code Required. Call " + phoneForAuthRequired.getPhoneNumber() +
                    " and quote merchant id " + phoneForAuthRequired.getMerchantId();
            getCurrentTxFlowState().phoneForAuthRequired(phoneForAuthRequired, msg);
        }
        txFlowStateChanged();
    }

    /**
     * The PIN pad server will reply to our {@link PurchaseRequest} with a {@link PurchaseResponse}.
     */
    private void handlePurchaseResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.PURCHASE, true);
    }

    /**
     * The PIN pad server will reply to our {@link CashoutOnlyRequest} with a {@link CashoutOnlyResponse}.
     */
    private void handleCashoutOnlyResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.CASHOUT_ONLY, true);
    }

    /**
     * The PIN pad server will reply to our {@link MotoPurchaseRequest} with a {@link MotoPurchaseResponse}.
     */
    private void handleMotoPurchaseResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.MOTO, true);
    }

    /**
     * The PIN pad server will reply to our {@link RefundRequest} with a {@link RefundResponse}.
     */
    private void handleRefundResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.REFUND, true);
    }

    /**
     * Handle the {@link Settlement} response received from the PIN pad.
     */
    private void handleSettleResponse(@NotNull Message m) {
        handleTxResponse(m, TransactionType.SETTLE, false);
    }

    /**
     * Handle the Settlement Enquiry Response received from the PIN pad.
     */
    private void handleSettlementEnquiryResponse(Message m) {
        handleTxResponse(m, TransactionType.SETTLEMENT_ENQUIRY, false);
    }

    private void handleTxResponse(@NotNull Message m, @NotNull TransactionType type, boolean checkPosRefId) {
        synchronized (txLock) {
            if (isTxResponseUnexpected(m, type.getName(), checkPosRefId)) return;

            getCurrentTxFlowState().completed(m.getSuccessState(), m, type + " transaction ended.");
            // TH-6A, TH-6E
        }
        txFlowStateChanged();
    }

    /**
     * Verifies transaction response (TH-1A, TH-2A).
     *
     * @return True if transaction response is unexpected and should be ignored, false if all is well.
     */
    private boolean isTxResponseUnexpected(@NotNull Message m, @NotNull String typeName, boolean checkPosRefId) {
        final TransactionFlowState currentState = getCurrentTxFlowState();

        final String incomingPosRefId;
        final boolean posRefIdMatched;
        if (checkPosRefId) {
            incomingPosRefId = m.getDataStringValue("pos_ref_id");
            posRefIdMatched = currentState.getPosRefId().equals(incomingPosRefId);
        } else {
            incomingPosRefId = null;
            posRefIdMatched = true;
        }

        if (getCurrentFlow() != SpiFlow.TRANSACTION || currentState.isFinished() || !posRefIdMatched) {
            String trace = checkPosRefId ? "Incoming Pos Ref ID: " + incomingPosRefId : m.getDecryptedJson();
            LOG.info("Received " + typeName + " response but I was not waiting for one. " + trace);
            return true;
        }
        return false;
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
            TransactionFlowState txState = getCurrentTxFlowState();
            if (getCurrentFlow() != SpiFlow.TRANSACTION || txState.isFinished()) {
                LOG.info("Received glt response but we were not in the middle of a tx. ignoring.");
                return;
            }

            if (!getCurrentTxFlowState().isAwaitingGltResponse()) {
                LOG.info("received a glt response but we had not asked for one within this transaction. Perhaps leftover from previous one. ignoring.");
                return;
            }

            if (!getCurrentTxFlowState().getLastGltRequestId().equals(m.getId())) {
                LOG.info("received a glt response but the message id does not match the glt request that we sent. strange. ignoring.");
                return;
            }

            // TH-4 We were in the middle of a transaction.
            // Let's attempt recovery. This is step 4 of transaction processing handling
            LOG.info("Got last transaction..");
            txState.gotGltResponse();
            GetLastTransactionResponse gltResponse = new GetLastTransactionResponse(m);
            txState.setGltResponsePosRefId(gltResponse.getPosRefId());
            if (!gltResponse.wasRetrievedSuccessfully()) {
                if (gltResponse.isStillInProgress(txState.getPosRefId())) {
                    // TH-4E - Operation In Progress
                    if (gltResponse.isWaitingForSignatureResponse() && !txState.isAwaitingSignatureCheck()) {
                        LOG.info("EFTPOS is waiting for us to send it signature accept/decline, but we were not aware of this. " +
                                "The user can only really decline at this stage as there is no receipt to print for signing.");
                        getCurrentTxFlowState().signatureRequired(
                                new SignatureRequired(txState.getPosRefId(), m.getId(), "MISSING RECEIPT\n DECLINE AND TRY AGAIN."),
                                "Recovered in Signature Required but we don't have receipt. You may Decline then Retry.");
                    } else if (gltResponse.isWaitingForAuthCode() && !txState.isAwaitingPhoneForAuth()) {
                        LOG.info("EFTPOS is waiting for us to send it auth code, but we were not aware of this. " +
                                "We can only cancel the transaction at this stage as we don't have enough information to recover from this.");
                        getCurrentTxFlowState().phoneForAuthRequired(
                                new PhoneForAuthRequired(txState.getPosRefId(), m.getId(), "UNKNOWN", "UNKNOWN"),
                                "Recovered mid phone-for-auth but don't have details. You may cancel then retry.");
                    } else {
                        LOG.info("Operation still in progress... keep waiting.");
                        // No need to publish txFlowStateChanged. Can return;
                        return;
                    }
                } else if (gltResponse.wasTimeOutOfSyncError()) {
                    // Let's not give up based on a TOOS error.
                    // Let's log it, and ignore it.
                    LOG.info("Time-Out-Of-Sync error in Get Last Transaction response. Let's ignore it and we'll try again.");
                    // No need to publish txFlowStateChanged. Can return;
                    return;
                } else {
                    // TH-4X - Unexpected response when recovering
                    LOG.info("Unexpected Response in get last transaction during - received posRefId:" + gltResponse.getPosRefId() + " error:" + m.getError() + ". Ignoring.");
                    return;
                }
            } else {
                if (txState.getType() == TransactionType.GET_LAST_TRANSACTION) {
                    // THIS WAS A PLAIN GET LAST TRANSACTION REQUEST, NOT FOR RECOVERY PURPOSES.
                    LOG.info("Retrieved last transaction as asked directly by the user.");
                    gltResponse.copyMerchantReceiptToCustomerReceipt();
                    txState.completed(m.getSuccessState(), m, "Last transaction retrieved");
                } else {
                    // TH-4A - Let's try to match the received last transaction against the current transaction
                    Message.SuccessState successState = gltMatch(gltResponse, txState.getAmountCents(), txState.getRequestTime(), txState.getPosRefId());
                    if (successState == Message.SuccessState.UNKNOWN) {
                        // TH-4N: Didn't Match our transaction. Consider unknown state.
                        LOG.info("Did not match transaction.");
                        txState.unknownCompleted("Failed to recover transaction status. Check EFTPOS. ");
                    } else {
                        // TH-4Y: We Matched, transaction finished, let's update ourselves
                        gltResponse.copyMerchantReceiptToCustomerReceipt();
                        txState.completed(successState, m, "Transaction ended.");
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
                            if (txState.isAttemptingToCancel() && System.currentTimeMillis() > txState.getCancelAttemptTime() + MAX_WAIT_FOR_CANCEL_TX) {
                                // TH-2T - too long since cancel attempt - Consider unknown
                                LOG.info("Been too long waiting for transaction to cancel.");
                                txState.unknownCompleted("Waited long enough for cancel transaction result. Check EFTPOS. ");
                                needsPublishing = true;
                            } else if (txState.isRequestSent() && System.currentTimeMillis() > txState.getLastStateRequestTime() + CHECK_ON_TX_FREQUENCY) {
                                // TH-1T, TH-4T - It's been a while since we received an update, let's call a GLT
                                LOG.info("Checking on our transaction. Last we asked was at " + txState.getLastStateRequestTime() + "...");
                                callGetLastTransaction();
                            }
                        }
                    }
                    if (needsPublishing) txFlowStateChanged();

                    try {
                        Thread.sleep(TX_MONITOR_CHECK_FREQUENCY);
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

    /**
     * When the transaction cancel response is returned.
     */
    private void handleCancelTransactionResponse(@NotNull Message m) {
        synchronized (txLock) {
            if (isTxResponseUnexpected(m, "Cancel", true)) return;

            final TransactionFlowState txState = getCurrentTxFlowState();
            final CancelTransactionResponse response = new CancelTransactionResponse(m);

            if (response.isSuccess()) return;

            LOG.warn("Failed to cancel transaction: reason=" + response.getErrorReason() + ", detail=" + response.getErrorDetail());

            txState.cancelFailed("Failed to cancel transaction: " + response.getErrorDetail() + ". Check EFTPOS.");

            txFlowStateChanged();
        }
    }

    /**
     * When the result response for the POS info is returned.
     */
    private void handleSetPosInfoResponse(@NotNull Message m) {
        synchronized (txLock) {
            final SetPosInfoResponse response = new SetPosInfoResponse(m);

            if (response.isSuccess()) {
                this.hasSetInfo = true;
                LOG.info("Setting POS info successful");
            } else {
                LOG.warn("Setting POS info failed: reason=" + response.getErrorReason() + ", detail=" + response.getErrorDetail());
            }
        }
    }

    private void handlePrintingResponse(@NotNull Message m) {
        if (printingResponseDelegate != null) {
            printingResponseDelegate.printingResponse(m);
        }
    }

    private void handleTerminalStatusResponse(@NotNull Message m) {
        if (terminalStatusResponseDelegate != null) {
            terminalStatusResponseDelegate.terminalStatusResponse(m);
        }
    }

    private void handleBatteryLevelChanged(@NotNull Message m) {
        if (batteryLevelChangedDelegate != null) {
            batteryLevelChangedDelegate.batteryLevelChanged(m);
        }
    }

    private void handleTerminalConfigurationResponse(@NotNull Message m) {
        if (terminalConfigurationResponseDelegate != null) {
            terminalConfigurationResponseDelegate.terminalConfigurationResponse(m);
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
                retriesSinceLastDeviceAddressResolution = 0;

                if (getCurrentFlow() == SpiFlow.PAIRING && getCurrentStatus() == SpiStatus.UNPAIRED) {
                    getCurrentPairingFlowState().setMessage("Requesting to pair...");
                    pairingFlowStateChanged();
                    final PairRequest pr = PairingHelper.newPairRequest();
                    send(pr.toMessage());
                } else {
                    LOG.info("I'm connected to " + eftposAddress + "...");
                    spiMessageStamp.setSecrets(secrets);
                    startPeriodicPing();

                    // Clean up timer
                    cleanReconnectFuture();
                }
                break;

            case DISCONNECTED:
                // Let's reset some lifecycle related to connection state, ready for next connection
                LOG.info("I'm disconnected from " + eftposAddress + "...");
                mostRecentPingSent = null;
                mostRecentPongReceived = null;
                missedPongsCount = 0;
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

                    if (conn == null) return; // This means the instance has been disposed. Aborting.
                    if (reconnectExecutor == null) {
                        LOG.warn("reconnectExecutor null. Possibly this is still running after dispose?");
                        return;
                    }
                    LOG.info("Will try to reconnect in {}s...", RECONNECTION_TIMEOUT / 1000);
                    cleanReconnectFuture();

                    reconnectFuture = reconnectExecutor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (autoAddressResolutionEnabled) {
                                if (retriesSinceLastDeviceAddressResolution >= retriesBeforeResolvingDeviceAddress) {
                                    autoResolveEftposAddress();
                                    retriesSinceLastDeviceAddressResolution = 0;
                                } else {
                                    retriesSinceLastDeviceAddressResolution++;
                                }
                            }

                            if (getCurrentStatus() != SpiStatus.UNPAIRED) {
                                // This is non-blocking
                                Connection conn = Spi.this.conn;
                                if (conn != null) {
                                    conn.connect();
                                }
                            }
                        }
                    }, RECONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
                } else if (getCurrentFlow() == SpiFlow.PAIRING) {
                    if (retriesSinceLastPairing < retriesBeforePairing) {
                        LOG.info("Will try to re-pair in {}s...", RECONNECTION_TIMEOUT / 1000);
                        cleanReconnectFuture();
                    }

                    reconnectFuture = reconnectExecutor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (currentPairingFlowState.isFinished()) return;

                            if (retriesSinceLastPairing >= retriesBeforePairing) {
                                retriesSinceLastPairing = 0;
                                LOG.warn("Lost connection during pairing.");
                                onPairingFailed();
                                pairingFlowStateChanged();
                            } else {
                                if (getCurrentStatus() != SpiStatus.PAIRED_CONNECTED) {
                                    // This is non-blocking
                                    Connection conn = Spi.this.conn;
                                    if (conn != null) {
                                        conn.connect();
                                    }
                                }

                                retriesSinceLastPairing++;
                            }
                        }
                    }, RECONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
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
                        Thread.sleep(PONG_TIMEOUT);
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
                        break;
                    }

                    missedPongsCount = 0;
                    try {
                        Thread.sleep(PING_FREQUENCY - PONG_TIMEOUT);
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
        LOG.info("On Ready To Transact!");

        // So, we have just made a connection, pinged and logged in successfully.
        setCurrentStatus(SpiStatus.PAIRED_CONNECTED);

        synchronized (txLock) {
            if (getCurrentFlow() == SpiFlow.TRANSACTION && !getCurrentTxFlowState().isFinished()) {
                if (getCurrentTxFlowState().isRequestSent()) {
                    // TH-3A - We've just reconnected and were in the middle of Tx.
                    // Let's get the last transaction to check what we might have missed out on.
                    callGetLastTransaction();
                } else {
                    // TH-3AR - We had not even sent the request yet. Let's do that now
                    send(getCurrentTxFlowState().getRequest());
                    getCurrentTxFlowState().sent("Asked EFTPOS to accept payment for " + (getCurrentTxFlowState()).getAmountCents() / 100.0);
                    txFlowStateChanged();
                }
            } else {
                if (!hasSetInfo) {
                    callSetPosInfo();
                }
                final SpiPayAtTable spiPat = this.spiPat;
                if (spiPat != null) {
                    spiPat.pushPayAtTableConfig();
                }
            }
        }
    }

    private void callSetPosInfo() {
        final SetPosInfoRequest setPosInfoRequest = new SetPosInfoRequest(posVersion, posVendorId, "java", getVersion(), DeviceInfo.getAppDeviceInfo());
        send(setPosInfoRequest.toMessage());
    }

    /**
     * Send a ping to the server.
     */
    private void doPing() {
        final Message ping = PingHelper.generatePingRequest();
        mostRecentPingSent = ping;
        send(ping);
        mostRecentPingSentTime = System.currentTimeMillis();
    }

    /**
     * Received a pong from the server.
     */
    private void handleIncomingPong(Message m) {
        // We need to maintain this time delta otherwise the server will not accept our messages.
        spiMessageStamp.setServerTimeDelta(m.getServerTimeDelta());

        if (mostRecentPongReceived == null) {
            // First pong received after a connection, and after the pairing process is fully finalised.
            if (getCurrentStatus() != SpiStatus.UNPAIRED) {
                LOG.info("First pong of connection and in paired state");
                onReadyToTransact();
            } else {
                LOG.info("First pong of connection but pairing process not finalised yet.");
            }
        }

        mostRecentPongReceived = m;
        LOG.debug("PongLatency:" + (System.currentTimeMillis() - mostRecentPingSentTime));
    }

    /**
     * The server will also send us pings. We need to reply with a pong so it doesn't disconnect us.
     */
    private void handleIncomingPing(@NotNull Message m) {
        send(PongHelper.generatePongResponse(m));
    }

    /**
     * Ask the PIN pad to tell us what the most recent transaction was.
     */
    private void callGetLastTransaction() {
        final Message gltMessage = new GetLastTransactionRequest().toMessage();
        getCurrentTxFlowState().callingGlt(gltMessage.getId());
        send(gltMessage);
    }

    /**
     * This method will be called whenever we receive a message from the connection.
     */
    private void onSpiMessageReceived(@NotNull String messageJson) {
        // First we parse the incoming message
        final Message m = Message.fromJson(messageJson, secrets);
        LOG.debug("Received: " + m.getDecryptedJson());

        if (SpiPreauth.isPreauthEvent(m.getEventName())) {
            final SpiPreauth spiPreauth = this.spiPreauth;
            if (spiPreauth != null) {
                spiPreauth.handlePreauthMessage(m);
            }
            return;
        }

        // And then we switch on the event type.
        final String eventName = m.getEventName();
        if (Events.KEY_REQUEST.equals(eventName)) {
            handleKeyRequest(m);
        } else if (Events.KEY_CHECK.equals(eventName)) {
            handleKeyCheck(m);
        } else if (Events.PAIR_RESPONSE.equals(eventName)) {
            handlePairResponse(m);
        } else if (Events.DROP_KEYS_ADVICE.equals(eventName)) {
            handleDropKeysAdvice(m);
        } else if (Events.PURCHASE_RESPONSE.equals(eventName)) {
            handlePurchaseResponse(m);
        } else if (Events.REFUND_RESPONSE.equals(eventName)) {
            handleRefundResponse(m);
        } else if (Events.CASHOUT_ONLY_RESPONSE.equals(eventName)) {
            handleCashoutOnlyResponse(m);
        } else if (Events.MOTO_PURCHASE_RESPONSE.equals(eventName)) {
            handleMotoPurchaseResponse(m);
        } else if (Events.SIGNATURE_REQUIRED.equals(eventName)) {
            handleSignatureRequired(m);
        } else if (Events.AUTH_CODE_REQUIRED.equals(eventName)) {
            handleAuthCodeRequired(m);
        } else if (Events.GET_LAST_TRANSACTION_RESPONSE.equals(eventName)) {
            handleGetLastTransactionResponse(m);
        } else if (Events.SETTLEMENT_ENQUIRY_RESPONSE.equals(eventName)) {
            handleSettlementEnquiryResponse(m);
        } else if (Events.SETTLE_RESPONSE.equals(eventName)) {
            handleSettleResponse(m);
        } else if (Events.PING.equals(eventName)) {
            handleIncomingPing(m);
        } else if (Events.PONG.equals(eventName)) {
            handleIncomingPong(m);
        } else if (Events.KEY_ROLL_REQUEST.equals(eventName)) {
            handleKeyRollingRequest(m);
        } else if (Events.CANCEL_TRANSACTION_RESPONSE.equals(eventName)) {
            handleCancelTransactionResponse(m);
        } else if (Events.SET_POS_INFO_RESPONSE.equals(eventName)) {
            handleSetPosInfoResponse(m);
        } else if (Events.PAY_AT_TABLE_GET_TABLE_CONFIG.equals(eventName)) {
            final SpiPayAtTable spiPat = this.spiPat;
            if (spiPat != null) {
                spiPat.handleGetTableConfig(m);
            } else {
                send(PayAtTableConfig.featureDisableMessage(RequestIdHelper.id("patconf")));
            }
        } else if (Events.PAY_AT_TABLE_GET_BILL_DETAILS.equals(eventName)) {
            final SpiPayAtTable spiPat = this.spiPat;
            if (spiPat != null) {
                spiPat.handleGetBillDetailsRequest(m);
            }
        } else if (Events.PAY_AT_TABLE_BILL_PAYMENT.equals(eventName)) {
            final SpiPayAtTable spiPat = this.spiPat;
            if (spiPat != null) {
                spiPat.handleBillPaymentAdvice(m);
            }
        } else if (Events.PAY_AT_TABLE_GET_OPEN_TABLES.equals(eventName)) {
            final SpiPayAtTable spiPat = this.spiPat;
            if (spiPat != null) {
                spiPat.handleGetOpenTablesRequest(m);
            }
        } else if (Events.PAY_AT_TABLE_BILL_PAYMENT_FLOW_ENDED.equals(eventName)) {
            final SpiPayAtTable spiPat = this.spiPat;
            if (spiPat != null) {
                spiPat.handleBillPaymentFlowEnded(m);
            }
        } else if (Events.PRINTING_RESPONSE.equals(eventName)) {
            handlePrintingResponse(m);
        } else if (Events.TERMINAL_STATUS_RESPONSE.equals(eventName)) {
            handleTerminalStatusResponse(m);
        } else if (Events.BATTERY_LEVEL_CHANGED.equals(eventName)) {
            handleBatteryLevelChanged(m);
        } else if (Events.TERMINAL_CONFIGURATION_RESPONSE.equals(eventName)) {
            handleTerminalConfigurationResponse(m);
        } else if (Events.ERROR.equals(eventName)) {
            handleErrorEvent(m);
        } else if (Events.INVALID_HMAC_SIGNATURE.equals(eventName)) {
            LOG.info("I could not verify message from EFTPOS. You might have to un-pair EFTPOS and then reconnect.");
        } else {
            LOG.info("I don't understand event: " + eventName + ", " + m.getData() + ". Perhaps I have not implemented it yet.");
        }
    }

    private void onWsErrorReceived(@Nullable Throwable error) {
        LOG.error("Received WS error", error);
    }

    boolean send(Message message) {
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

    //region Internals for validations

    private String validatePosId(String posId) {
        if (!StringUtils.isBlank(posId) & posId.length() > 16) {
            posId = posId.substring(0, 16);
            LOG.warn("The Pos Id should be equal or less than 16 characters! It has been truncated");
        }

        if (!StringUtils.isBlank(posId) & !regexItemsForPosId.matcher(posId).matches()) {
            LOG.warn("The Pos Id can not include special characters!");
        }

        return posId;
    }

    private void validateEftposAddress(String eftposAddress) {
        if (!StringUtils.isBlank(eftposAddress) & !regexItemsForEftposAddress.matcher(eftposAddress).matches()) {
            LOG.warn("The Eftpos Address is not in correct format!");
        }
    }

    //endregion

    //region Device Management

    private boolean hasSerialNumberChanged(String updatedSerialNumber) {
        return !serialNumber.equals(updatedSerialNumber);
    }

    private boolean hasEftposAddressChanged(String updatedEftposAddress) {
        return !eftposAddress.equals(updatedEftposAddress);
    }

    private void autoResolveEftposAddress() {
        if (!autoAddressResolutionEnabled) return;

        if (serialNumber == null || StringUtils.isWhitespace(serialNumber) || deviceApiKey == null || StringUtils.isWhitespace(deviceApiKey)) {
            LOG.error("Missing serialNumber and/or deviceApiKey. Need to set them before for Auto Address to work.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                DeviceService deviceService = new DeviceService();
                DeviceAddressStatus addressResponse = deviceService.retrieveService(serialNumber, deviceApiKey, acquirerCode, inTestMode);

                if (addressResponse == null || (addressResponse.getAddress() == null && (addressResponse.getResponseCode() != 200 && addressResponse.getResponseCode() != 404))) {
                    DeviceAddressStatus state = new DeviceAddressStatus();
                    state.setDeviceAddressResponseCode(DeviceAddressResponseCode.DEVICE_SERVICE_ERROR);
                    setCurrentDeviceStatus(state);

                    deviceStatusChanged(getCurrentDeviceStatus());
                    return;
                }

                if (addressResponse.getResponseCode() == 404) {
                    DeviceAddressStatus state = new DeviceAddressStatus();
                    state.setDeviceAddressResponseCode(DeviceAddressResponseCode.INVALID_SERIAL_NUMBER);
                    setCurrentDeviceStatus(state);

                    deviceStatusChanged(getCurrentDeviceStatus());
                    return;
                }

                if (!hasEftposAddressChanged(addressResponse.getAddress())) {
                    getCurrentDeviceStatus().setDeviceAddressResponseCode(DeviceAddressResponseCode.ADDRESS_NOT_CHANGED);
                    deviceStatusChanged(getCurrentDeviceStatus());
                    return;
                }

                // update device and connection address
                eftposAddress = "ws://" + addressResponse.getAddress();
                conn.setAddress(eftposAddress);

                DeviceAddressStatus state = new DeviceAddressStatus();
                state.setAddress(addressResponse.getAddress());
                state.setLastUpdated(addressResponse.getLastUpdated());
                state.setDeviceAddressResponseCode(DeviceAddressResponseCode.SUCCESS);
                setCurrentDeviceStatus(state);

                deviceStatusChanged(getCurrentDeviceStatus());
            }
        }).start();
    }

    //endregion

    //region Disposal

    /**
     * Stops all running processes and resets to state before starting.
     * <p>
     * Call this method when finished with SPI, e.g. when closing the application.
     */
    public void dispose() {
        LOG.info("Disposing...");

        // Clean up threads
        stopPeriodicPing();
        stopTransactionMonitoring();

        // Clean up connection
        conn.dispose();
        conn = null;

        // Clean up timer
        cleanReconnectFuture();
        reconnectExecutor.shutdownNow();
        reconnectExecutor = null;
    }

    private void cleanReconnectFuture() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
            reconnectFuture = null;
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

    public interface PrintingResponseDelegate {
        void printingResponse(@NotNull Message message);
    }

    public interface TerminalStatusResponseDelegate {
        void terminalStatusResponse(@NotNull Message message);
    }

    public interface BatteryLevelChangedDelegate {
        void batteryLevelChanged(@NotNull Message message);
    }

    public interface TerminalConfigurationResponseDelegate {
        void terminalConfigurationResponse(@NotNull Message message);
    }
}
