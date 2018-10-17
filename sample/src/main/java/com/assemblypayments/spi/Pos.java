package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.RequestIdHelper;
import com.assemblypayments.spi.util.SystemHelper;

import java.util.Date;
import java.util.Scanner;

public class Pos {

    private String posId = "ACMEPOS";
    private String eftposAddress = "emulator-prod.herokuapp.com";
    private Secrets spiSecrets;

    private Spi spi;

    public static void main(String[] args) {
        new Pos().start();
    }

    private void start() {
        try {
            spi = new Spi(posId, eftposAddress, spiSecrets);
        } catch (Spi.CompatibilityException e) {
            System.out.println("# JDK compatibility check failed: " + e.getCause().getMessage());
            return;
        }

        try {
            spi.setPosInfo("assembly", "2.3.0");
            spi.setStatusChangedHandler(new Spi.EventHandler<SpiStatus>() {
                @Override
                public void onEvent(SpiStatus value) {
                    onStatusChanged();
                }
            });
            spi.setSecretsChangedHandler(new Spi.EventHandler<Secrets>() {
                @Override
                public void onEvent(Secrets value) {
                    onSecretsChanged(value);
                }
            });
            spi.setPairingFlowStateChangedHandler(new Spi.EventHandler<PairingFlowState>() {
                @Override
                public void onEvent(PairingFlowState value) {
                    onPairingFlowStateChanged(value);
                }
            });
            spi.setTxFlowStateChangedHandler(new Spi.EventHandler<TransactionFlowState>() {
                @Override
                public void onEvent(TransactionFlowState value) {
                    onTxFlowStateChanged(value);
                }
            });
            spi.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        // And Now we just accept user input and display to the user what is happening.
        SystemHelper.clearConsole();
        System.out.println("# Welcome! My name is " + posId + ".");
        System.out.println("# Version: " + Spi.getVersion());

        printStatusAndActions();
        acceptUserInput();

        // Cleanup
        spi.dispose();
    }

    private void onStatusChanged() {
        if (spi.getCurrentFlow() == SpiFlow.IDLE) SystemHelper.clearConsole();
        System.out.println("# ------- STATUS UPDATE -----------");
        printStatusAndActions();
    }

    private void onPairingFlowStateChanged(PairingFlowState pairingFlowState) {
        SystemHelper.clearConsole();
        System.out.println("# --------- PAIRING FLOW UPDATE -----------");
        System.out.println("# Message: " + pairingFlowState.getMessage());

        final String confirmationCode = pairingFlowState.getConfirmationCode();
        if (confirmationCode != null && confirmationCode.length() > 0) {
            System.out.println("# Confirmation Code: " + pairingFlowState.getConfirmationCode());
        }
        printStatusAndActions();
    }

    private void onTxFlowStateChanged(TransactionFlowState txFlowState) {
        SystemHelper.clearConsole();
        System.out.println("# --------- TRANSACTION FLOW UPDATE -----------");
        System.out.println("# Id: " + txFlowState.getId());
        System.out.println("# Type: " + txFlowState.getType());
        System.out.println("# RequestSent: " + txFlowState.isRequestSent());
        System.out.println("# WaitingForSignature: " + txFlowState.isAwaitingSignatureCheck());
        System.out.println("# Attempting to Cancel : " + txFlowState.isAttemptingToCancel());
        System.out.println("# Finished: " + txFlowState.isFinished());
        System.out.println("# Success: " + txFlowState.getSuccess());
        System.out.println("# Display Message: " + txFlowState.getDisplayMessage());

        if (txFlowState.isAwaitingSignatureCheck()) {
            // We need to print the receipt for the customer to sign.
            System.out.println(txFlowState.getSignatureRequiredMessage().getMerchantReceipt().trim());
        }

        // If the transaction is finished, we take some extra steps.
        if (txFlowState.isFinished()) {
            if (txFlowState.getSuccess() == Message.SuccessState.UNKNOWN) {
                // TH-4T, TH-4N, TH-2T - This is the dge case when we can't be sure what happened to the transaction.
                // Invite the merchant to look at the last transaction on the EFTPOS using the documented shortcuts.
                // Now offer your merchant user the options to:
                // A. Retry the transaction from scratch or pay using a different method - If Merchant is confident that tx didn't go through.
                // B. Override Order as Paid in you POS - If Merchant is confident that payment went through.
                // C. Cancel out of the order all together - If the customer has left / given up without paying
                System.out.println("# NOT SURE IF WE GOT PAID OR NOT. CHECK LAST TRANSACTION MANUALLY ON EFTPOS!");
            } else {
                // We have a result...
                switch (txFlowState.getType()) {
                    // Depending on what type of transaction it was, we might act differently or use different data.
                    case PURCHASE:
                        if (txFlowState.getResponse() != null) {
                            final PurchaseResponse purchaseResponse = new PurchaseResponse(txFlowState.getResponse());
                            System.out.println("# Scheme: " + purchaseResponse.getSchemeName());
                            System.out.println("# Response: " + purchaseResponse.getResponseText());
                            System.out.println("# RRN: " + purchaseResponse.getRRN());
                            System.out.println("# Error: " + txFlowState.getResponse().getError());
                            System.out.println("# Customer Receipt:");
                            System.out.println(purchaseResponse.getCustomerReceipt().trim());
                            //} else {
                            // We did not even get a response, like in the case of a time-out.
                        }
                        if (txFlowState.getSuccess() == Message.SuccessState.SUCCESS) {
                            // TH-6A
                            System.out.println("# HOORAY WE GOT PAID (TH-7A). CLOSE THE ORDER!");
                        } else {
                            // TH-6E
                            System.out.println("# WE DIDN'T GET PAID. RETRY PAYMENT (TH-5R) OR GIVE UP (TH-5C)!");
                        }
                        break;
                    case REFUND:
                        if (txFlowState.getResponse() != null) {
                            final RefundResponse refundResponse = new RefundResponse(txFlowState.getResponse());
                            System.out.println("# Scheme: " + refundResponse.getSchemeName());
                            System.out.println("# Response: " + refundResponse.getResponseText());
                            System.out.println("# RRN: " + refundResponse.getRRN());
                            System.out.println("# Error: " + txFlowState.getResponse().getError());
                            System.out.println("# Customer Receipt:");
                            System.out.println(refundResponse.getCustomerReceipt().trim());
                            //} else {
                            // We did not even get a response, like in the case of a time-out.
                        }
                        break;
                    case SETTLE:
                        if (txFlowState.getResponse() != null) {
                            final Settlement settleResponse = new Settlement(txFlowState.getResponse());
                            System.out.println("# Response: " + settleResponse.getResponseText());
                            System.out.println("# Error: " + txFlowState.getResponse().getError());
                            System.out.println("# Merchant Receipt:");
                            System.out.println(settleResponse.getMerchantReceipt().trim());
                            //} else {
                            // We did not even get a response, like in the case of a time-out.
                        }
                        break;
                    case GET_LAST_TRANSACTION:
                        if (txFlowState.getResponse() != null) {
                            GetLastTransactionResponse gltResponse = new GetLastTransactionResponse(txFlowState.getResponse());
                            Message.SuccessState success = spi.gltMatch(gltResponse, TransactionType.PURCHASE, 10000, new Date().getTime() - 60000, "MYORDER123");

                            if (success == Message.SuccessState.UNKNOWN) {
                                System.out.println("# Did not retrieve Expected Transaction of 10000c from a minute ago ;).");
                            } else {
                                System.out.println("# Tx Matched Expected Purchase Request.");
                                System.out.println("# Result: " + success);
                                PurchaseResponse purchaseResponse = new PurchaseResponse(txFlowState.getResponse());
                                System.out.println("# Scheme: " + purchaseResponse.getSchemeName());
                                System.out.println("# Response: " + purchaseResponse.getResponseText());
                                System.out.println("# RRN: " + purchaseResponse.getRRN());
                                System.out.println("# Error: " + txFlowState.getResponse().getError());
                                System.out.println("# Customer Receipt:");
                                System.out.println(purchaseResponse.getCustomerReceipt().trim());
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        // Let's show the user what options he has at this stage.
        printStatusAndActions();
    }

    private void onSecretsChanged(Secrets newSecrets) {
        spiSecrets = newSecrets;
        if (spiSecrets != null) {
            System.out.println("\n\n\n# --------- I GOT NEW SECRETS -----------");
            System.out.println("# ---------- PERSIST THEM SAFELY ----------");
            System.out.println("# " + spiSecrets.getEncKey() + ":" + spiSecrets.getHmacKey());
            System.out.println("# -----------------------------------------");
        } else {
            System.out.println("\n\n\n# --------- THE SECRETS HAVE BEEN VOIDED -----------");
            System.out.println("# ---------- CONSIDER ME UNPAIRED ----------");
            System.out.println("# -----------------------------------------");
        }
    }

    private void printStatusAndActions() {
        System.out.println("# ----------- AVAILABLE ACTIONS ------------");

        // Available Actions depend on the current status (Unpaired/PairedConnecting/PairedConnected)
        switch (spi.getCurrentStatus()) {
            case UNPAIRED: //Unpaired...
                switch (spi.getCurrentFlow()) {
                    case IDLE: // Unpaired, Idle
                        System.out.println("# [pos_id:MYPOSNAME] - sets your POS instance ID");
                        System.out.println("# [eftpos_address:10.10.10.10] - sets IP address of target EFTPOS");
                        System.out.println("# [pair] - start pairing");
                        break;

                    case PAIRING: // Unpaired, PairingFlow
                        final PairingFlowState pairingState = spi.getCurrentPairingFlowState();
                        if (pairingState.isAwaitingCheckFromPos()) {
                            System.out.println("# [pair_confirm] - confirm the code matches");
                        }
                        if (!pairingState.isFinished()) {
                            System.out.println("# [pair_cancel] - cancel pairing process");
                        } else {
                            System.out.println("# [ok] - acknowledge");
                        }
                        break;

                    case TRANSACTION: // Unpaired, TransactionFlow - Should never be the case!
                    default:
                        System.out.println("# .. Unexpected Flow .. " + spi.getCurrentFlow());
                        break;
                }
                break;

            case PAIRED_CONNECTED:
                printStatusPairedConnected();
                break;

            case PAIRED_CONNECTING: // This is still considered as a Paired kind of state, but...
                // .. we give user the option of changing IP address, just in case the EFTPOS got a new one in the meanwhile
                System.out.println("# [eftpos_address:10.161.110.247] - change IP address of target EFTPOS");
                // .. but otherwise we give the same options as PairedConnected
                printStatusPairedConnected();
                break;

            default:
                System.out.println("# .. Unexpected State .. " + spi.getCurrentStatus());
                break;
        }
        System.out.println("# [status] - reprint buttons/status");
        System.out.println("# [bye] - exit");
        System.out.println();
        System.out.println("# --------------- STATUS ------------------");
        System.out.println("# " + posId + " <--> " + eftposAddress);
        System.out.println("# " + spi.getCurrentStatus() + ":" + spi.getCurrentFlow());
        System.out.println("# -----------------------------------------");
        System.out.print("> ");
    }

    private void printStatusPairedConnected() {
        switch (spi.getCurrentFlow()) {
            case IDLE: // Paired, Idle
                System.out.println("# [purchase:1981] - initiate a payment of $19.81");
                System.out.println("# [refund:1891] - initiate a refund of $18.91");
                System.out.println("# [settle] - Initiate Settlement");
                System.out.println("# [unpair] - unpair and disconnect");
                break;
            case TRANSACTION: // Paired, Transaction
                if (spi.getCurrentTxFlowState().isAwaitingSignatureCheck()) {
                    System.out.println("# [tx_sign_accept] - Accept Signature");
                    System.out.println("# [tx_sign_decline] - Decline Signature");
                }
                if (!spi.getCurrentTxFlowState().isFinished()) {
                    System.out.println("# [tx_cancel] - Attempt to Cancel Transaction");
                } else {
                    System.out.println("# [ok] - acknowledge");
                }
                break;
            case PAIRING: // Paired, Pairing - we have just finished the pairing flow. OK to ack.
                System.out.println("# [ok] - acknowledge");
                break;
            default:
                System.out.println("# .. Unexpected Flow .. " + spi.getCurrentFlow());
                break;
        }
    }

    private void acceptUserInput() {
        final Scanner scanner = new Scanner(System.in);
        boolean bye = false;
        while (!bye && scanner.hasNext()) {
            final String input = scanner.next();
            final String[] spInput = input.split(":");
            final String command = spInput[0];
            if ("pos_id".equals(command)) {
                posId = spInput[1];
                spi.setPosId(posId);
                SystemHelper.clearConsole();
                printStatusAndActions();
            } else if ("eftpos_address".equals(command)) {
                eftposAddress = spInput[1];
                spi.setEftposAddress(eftposAddress);
                SystemHelper.clearConsole();
                printStatusAndActions();
            } else if ("pair".equals(command)) {
                spi.pair();
            } else if ("pair_confirm".equals(command)) {
                spi.pairingConfirmCode();
            } else if ("pair_cancel".equals(command)) {
                spi.pairingCancel();
            } else if ("unpair".equals(command)) {
                spi.unpair();
            } else if ("purchase".equals(command)) {
                final InitiateTxResult pres = spi.initiatePurchaseTx(RequestIdHelper.id("prchs"), Integer.parseInt(spInput[1]));
                System.out.println(pres.isInitiated() ?
                        "# Purchase initiated. Will be updated with progress." :
                        "# Could not initiate purchase: " + pres.getMessage() + ". Please retry.");
            } else if ("refund".equals(command)) {
                final InitiateTxResult rres = spi.initiateRefundTx(RequestIdHelper.id("rfnd"), Integer.parseInt(spInput[1]));
                System.out.println(rres.isInitiated() ?
                        "# Refund initiated. Will be updated with progress." :
                        "# Could not initiate refund: " + rres.getMessage() + ". Please retry.");
            } else if ("settle".equals(command)) {
                final InitiateTxResult sres = spi.initiateSettleTx(RequestIdHelper.id("settle"));
                System.out.println(sres.isInitiated() ?
                        "# Settle initiated. Will be updated with progress." :
                        "# Could not initiate settle: " + sres.getMessage() + ". Please retry.");
            } else if ("glt".equals(command)) {
                final InitiateTxResult gltres = spi.initiateGetLastTx();
                System.out.println(gltres.isInitiated() ?
                        "# GLT initiated. Will be updated with progress." :
                        "# Could not initiate settle: " + gltres.getMessage() + ". Please retry.");
            } else if ("tx_sign_accept".equals(command)) {
                spi.acceptSignature(true);
            } else if ("tx_sign_decline".equals(command)) {
                spi.acceptSignature(false);
            } else if ("tx_cancel".equals(command)) {
                spi.cancelTransaction();
            } else if ("ok".equals(command)) {
                spi.ackFlowEndedAndBackToIdle();
                SystemHelper.clearConsole();
                printStatusAndActions();
            } else if ("status".equals(command)) {
                SystemHelper.clearConsole();
                printStatusAndActions();
            } else if ("bye".equals(command)) {
                bye = true;
            } else {
                SystemHelper.clearConsole();
                System.out.println("# I don't understand. Sorry.");
                printStatusAndActions();
            }
        }
        System.out.println("# BaBye!");
    }

}
