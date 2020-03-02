package io.mx51.spi;

import io.mx51.spi.model.*;
import io.mx51.spi.util.Events;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiPreauth {

    private static final Logger LOG = LoggerFactory.getLogger("spipreauth");

    private final Spi spi;
    private final Object txLock;

    SpiPreauth(Spi spi, Object txLock) {
        this.spi = spi;
        this.txLock = txLock;
    }

    public InitiateTxResult initiateAccountVerifyTx(String posRefId) {
        Message verifyMsg = new AccountVerifyRequest(posRefId).toMessage();
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.ACCOUNT_VERIFY, 0, verifyMsg,
                "Waiting for EFTPOS connection to make account verify request");
        String sentMsg = "Asked EFTPOS to verify account";
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiateOpenTx(String posRefId, int amountCents) {
        return initiateOpenTx(posRefId, amountCents, null);
    }

    public InitiateTxResult initiateOpenTx(String posRefId, int amountCents, TransactionOptions options) {
        PreauthOpenRequest preauthOpenRequest = new PreauthOpenRequest(amountCents, posRefId);
        preauthOpenRequest.setConfig(spi.config);
        preauthOpenRequest.setOptions(options);
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, amountCents, preauthOpenRequest.toMessage(),
                String.format("Waiting for EFTPOS connection to make preauth request for %.2f", amountCents / 100.0));
        String sentMsg = String.format("Asked EFTPOS to create preauth for %.2f", amountCents / 100.0);
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiateTopupTx(String posRefId, String preauthId, int amountCents) {
        return initiateTopupTx(posRefId, preauthId, amountCents, null);
    }

    public InitiateTxResult initiateTopupTx(String posRefId, String preauthId, int amountCents, TransactionOptions options) {
        PreauthTopupRequest preauthTopupRequest = new PreauthTopupRequest(preauthId, amountCents, posRefId);
        preauthTopupRequest.setConfig(spi.config);
        preauthTopupRequest.setOptions(options);
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, amountCents, preauthTopupRequest.toMessage(),
                String.format("Waiting for EFTPOS connection to make preauth topup request for %.2f", amountCents / 100.0));
        String sentMsg = String.format("Asked EFTPOS to make preauth topup for %.2f", amountCents / 100.0);
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiatePartialCancellationTx(String posRefId, String preauthId, int amountCents) {
        return initiatePartialCancellationTx(posRefId, preauthId, amountCents, null);
    }

    public InitiateTxResult initiatePartialCancellationTx(String posRefId, String preauthId, int amountCents, TransactionOptions options) {
        PreauthPartialCancellationRequest preauthPartialCancellationRequest = new PreauthPartialCancellationRequest(preauthId, amountCents, posRefId);
        preauthPartialCancellationRequest.setConfig(spi.config);
        preauthPartialCancellationRequest.setOptions(options);
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, amountCents, preauthPartialCancellationRequest.toMessage(),
                String.format("Waiting for EFTPOS connection to make preauth partial cancellation request for %.2f", amountCents / 100.0));
        String sentMsg = String.format("Asked EFTPOS to make preauth partial cancellation for %.2f", amountCents / 100.0);
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiateExtendTx(String posRefId, String preauthId) {
        return initiateExtendTx(posRefId, preauthId, null);
    }

    public InitiateTxResult initiateExtendTx(String posRefId, String preauthId, TransactionOptions options) {
        PreauthExtendRequest preauthExtendRequest = new PreauthExtendRequest(preauthId, posRefId);
        preauthExtendRequest.setConfig(spi.config);
        preauthExtendRequest.setOptions(options);
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, 0, preauthExtendRequest.toMessage(),
                "Waiting for EFTPOS connection to make preauth extend request");
        String sentMsg = "Asked EFTPOS to make preauth extend request";
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiateCompletionTx(String posRefId, String preauthId, int amountCents) {
        return initiateCompletionTx(posRefId, preauthId, amountCents, 0);
    }

    public InitiateTxResult initiateCompletionTx(String posRefId, String preauthId, int amountCents, int surchargeAmount) {
        return initiateCompletionTx(posRefId, preauthId, amountCents, surchargeAmount, null);
    }

    public InitiateTxResult initiateCompletionTx(String posRefId, String preauthId, int amountCents, int surchargeAmount, TransactionOptions options) {
        PreauthCompletionRequest request = new PreauthCompletionRequest(preauthId, amountCents, posRefId);
        request.setSurchargeAmount(surchargeAmount);
        request.setConfig(spi.config);
        request.setOptions(options);
        Message msg = request.toMessage();
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, amountCents, msg,
                String.format("Waiting for EFTPOS connection to make preauth completion request for %.2f", amountCents / 100.0));
        String sentMsg = String.format("Asked EFTPOS to make preauth completion for %.2f", amountCents / 100.0);
        return initiatePreauthTx(tfs, sentMsg);
    }

    public InitiateTxResult initiateCancelTx(String posRefId, String preauthId) {
        return initiateCancelTx(posRefId, preauthId, null);
    }

    public InitiateTxResult initiateCancelTx(String posRefId, String preauthId, TransactionOptions options) {
        PreauthCancelRequest preauthCancelRequest = new PreauthCancelRequest(preauthId, posRefId);
        preauthCancelRequest.setConfig(spi.config);
        preauthCancelRequest.setOptions(options);
        TransactionFlowState tfs = new TransactionFlowState(
                posRefId, TransactionType.PREAUTH, 0, preauthCancelRequest.toMessage(),
                "Waiting for EFTPOS connection to make preauth cancellation request");
        String sentMsg = "Asked EFTPOS to make preauth cancellation request";
        return initiatePreauthTx(tfs, sentMsg);
    }

    private InitiateTxResult initiatePreauthTx(TransactionFlowState tfs, String sentMsg) {
        if (spi.getCurrentStatus() == SpiStatus.UNPAIRED) return new InitiateTxResult(false, "Not Paired");

        synchronized (txLock) {
            if (spi.getCurrentFlow() != SpiFlow.IDLE) return new InitiateTxResult(false, "Not Idle");

            spi.setCurrentFlow(SpiFlow.TRANSACTION);
            spi.setCurrentTxFlowState(tfs);
            if (spi.send(tfs.getRequest())) {
                spi.getCurrentTxFlowState().sent(sentMsg);
            }
        }
        spi.txFlowStateChanged();
        return new InitiateTxResult(true, "Preauth Initiated");
    }

    void handlePreauthMessage(@NotNull Message m) {
        if (Events.ACCOUNT_VERIFY_RESPONSE.equals(m.getEventName())) {
            handleAccountVerifyResponse(m);
        } else if (Events.PREAUTH_OPEN_RESPONSE.equals(m.getEventName()) ||
                Events.PREAUTH_TOPUP_RESPONSE.equals(m.getEventName()) ||
                Events.PREAUTH_PARTIAL_CANCELLATION_RESPONSE.equals(m.getEventName()) ||
                Events.PREAUTH_EXTEND_RESPONSE.equals(m.getEventName()) ||
                Events.PREAUTH_COMPLETE_RESPONSE.equals(m.getEventName()) ||
                Events.PREAUTH_CANCELLATION_RESPONSE.equals(m.getEventName())) {
            handlePreauthResponse(m);
        } else {
            LOG.info("I don't understand preauth event: " + m.getEventName() + ", " + m.getData() + ", perhaps I have not implemented it yet");
        }
    }

    private void handleAccountVerifyResponse(@NotNull Message m) {
        synchronized (txLock) {
            String incomingPosRefId = m.getDataStringValue("pos_ref_id");
            TransactionFlowState currentTxFlowState = spi.getCurrentTxFlowState();
            if (spi.getCurrentFlow() != SpiFlow.TRANSACTION || currentTxFlowState.isFinished() || !currentTxFlowState.getPosRefId().equals(incomingPosRefId)) {
                LOG.info("Received Account Verify response but I was not waiting for one, incoming Pos Ref ID: " + incomingPosRefId);
                return;
            }
            // TH-1A, TH-2A

            currentTxFlowState.completed(m.getSuccessState(), m, "Account Verify Transaction Ended.");
            // TH-6A, TH-6E
        }
        spi.txFlowStateChanged();
    }

    private void handlePreauthResponse(@NotNull Message m) {
        synchronized (txLock) {
            String incomingPosRefId = m.getDataStringValue("pos_ref_id");
            TransactionFlowState currentTxFlowState = spi.getCurrentTxFlowState();
            if (spi.getCurrentFlow() != SpiFlow.TRANSACTION || currentTxFlowState.isFinished() || !currentTxFlowState.getPosRefId().equals(incomingPosRefId)) {
                LOG.info("Received preauth response but I was not waiting for one, incoming Pos Ref ID: " + incomingPosRefId);
                return;
            }
            // TH-1A, TH-2A

            currentTxFlowState.completed(m.getSuccessState(), m, "Preauth Transaction Ended.");
            // TH-6A, TH-6E
        }
        spi.txFlowStateChanged();
    }

    static boolean isPreauthEvent(@NotNull String eventName) {
        return eventName.startsWith("preauth")
                || Events.PREAUTH_COMPLETE_RESPONSE.equals(eventName)
                || Events.PREAUTH_COMPLETE_REQUEST.equals(eventName)
                || Events.ACCOUNT_VERIFY_REQUEST.equals(eventName)
                || Events.ACCOUNT_VERIFY_RESPONSE.equals(eventName);
    }

}
