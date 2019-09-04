package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.*;
import org.junit.Assert;
import org.junit.Test;

public class SpiModelsTest {
    @Test
    public void testTransactionFlowState() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");

        Assert.assertEquals(transactionFlowState.getPosRefId(), "1");
        Assert.assertEquals(transactionFlowState.getId(), "1");
        Assert.assertEquals(transactionFlowState.getType(), TransactionType.SETTLEMENT_ENQUIRY);
        Assert.assertEquals(transactionFlowState.getAmountCents(), 0);
        Assert.assertFalse(transactionFlowState.isAwaitingSignatureCheck());
        Assert.assertFalse(transactionFlowState.isRequestSent());
        Assert.assertFalse(transactionFlowState.isFinished());
        Assert.assertEquals(transactionFlowState.getSuccess(), Message.SuccessState.UNKNOWN);
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "Waiting for EFTPOS connection to make a settlement enquiry");
    }

    @Test
    public void testTransactionFlowStateSent() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.sent("Sent");

        Assert.assertNotNull(transactionFlowState.getRequestTime());
        Assert.assertNotNull(transactionFlowState.getLastStateRequestTime());
        Assert.assertTrue(transactionFlowState.isRequestSent());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "Sent");
    }

    @Test
    public void testTransactionFlowStateCancelling() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.cancelling("Cancelling");

        Assert.assertTrue(transactionFlowState.isAttemptingToCancel());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "Cancelling");
    }

    @Test
    public void testTransactionFlowStateCancelFailed() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.cancelFailed("CancelFailed");

        Assert.assertFalse(transactionFlowState.isAttemptingToCancel());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "CancelFailed");
    }

    @Test
    public void testTransactionFlowStateCallingGlt() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.callingGlt("25");

        Assert.assertTrue(transactionFlowState.isAwaitingGltResponse());
        Assert.assertNotNull(transactionFlowState.getLastStateRequestTime());
        Assert.assertEquals(transactionFlowState.getLastGltRequestId(), "25");
    }

    @Test
    public void testTransactionFlowStateGotGltResponse() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.gotGltResponse();

        Assert.assertFalse(transactionFlowState.isAwaitingGltResponse());
    }

    @Test
    public void testTransactionFlowStateFailed() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.failed(stlEnqMsg, "Failed");

        Assert.assertEquals(transactionFlowState.getResponse(), stlEnqMsg);
        Assert.assertTrue(transactionFlowState.isFinished());
        Assert.assertEquals(transactionFlowState.getSuccess(), Message.SuccessState.FAILED);
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "Failed");
    }

    @Test
    public void testTransactionFlowSignatureRequired() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"merchant_receipt\": \"\\nEFTPOS FROM WESTPAC\\nVAAS Product 2\\n275 Kent St\\nSydney 2000\\nAustralia\\n\\n\\nMID         02447506\\nTSP     100381990116\\nTIME 26APR17   11:29\\nRRN     170426000358\\nTRAN 000358   CREDIT\\nAmex               S\\nCARD............4477\\nAUTH          764167\\n\\nPURCHASE   AUD100.00\\nTIP          AUD5.00\\n\\nTOTAL      AUD105.00\\n\\n\\n (001) APPROVE WITH\\n     SIGNATURE\\n\\n\\n\\n\\n\\n\\nSIGN:_______________\\n\\n\\n\\n\\n\\n\\n\\n\",\"pos_ref_id\":\"prchs-06-06-2019-11-49-05\"},\"datetime\": \"2017-04-26T11:30:21.000\",\"event\": \"signature_required\",\"id\": \"24\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        SignatureRequired response = new SignatureRequired(msg);

        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, msg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.signatureRequired(response, "SignatureRequired");

        Assert.assertEquals(transactionFlowState.getSignatureRequiredMessage(), response);
        Assert.assertTrue(transactionFlowState.isAwaitingSignatureCheck());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "SignatureRequired");
    }

    @Test
    public void testTransactionFlowSignatureResponded() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.signatureResponded("SignatureResponded");

        Assert.assertFalse(transactionFlowState.isAwaitingSignatureCheck());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "SignatureResponded");
    }

    @Test
    public void testTransactionFlowPhoneForAuthRequired() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"event\":\"authorisation_code_required\",\"id\":\"20\",\"datetime\":\"2017-11-01T06:09:33.918\",\"data\":{\"merchant_id\":\"12345678\",\"auth_centre_phone_number\":\"1800999999\",\"pos_ref_id\": \"xyz\"}}}";

        Message msg = Message.fromJson(jsonStr, secrets);
        PhoneForAuthRequired request = new PhoneForAuthRequired(msg);

        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, msg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.phoneForAuthRequired(request, "PhoneForAuthRequired");

        Assert.assertEquals(transactionFlowState.getPhoneForAuthRequiredMessage(), request);
        Assert.assertTrue(transactionFlowState.isAwaitingPhoneForAuth());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "PhoneForAuthRequired");
    }

    @Test
    public void testTransactionFlowAuthCodeSent() {
        Message stlEnqMsg = new SettlementEnquiryRequest(RequestIdHelper.id("stlenq")).toMessage();
        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, stlEnqMsg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.authCodeSent("AuthCodeSent");

        Assert.assertFalse(transactionFlowState.isAwaitingPhoneForAuth());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "AuthCodeSent");
    }

    @Test
    public void testTransactionFlowCompleted() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"merchant_receipt\": \"\\nEFTPOS FROM WESTPAC\\nVAAS Product 2\\n275 Kent St\\nSydney 2000\\nAustralia\\n\\n\\nMID         02447506\\nTSP     100381990116\\nTIME 26APR17   11:29\\nRRN     170426000358\\nTRAN 000358   CREDIT\\nAmex               S\\nCARD............4477\\nAUTH          764167\\n\\nPURCHASE   AUD100.00\\nTIP          AUD5.00\\n\\nTOTAL      AUD105.00\\n\\n\\n (001) APPROVE WITH\\n     SIGNATURE\\n\\n\\n\\n\\n\\n\\nSIGN:_______________\\n\\n\\n\\n\\n\\n\\n\\n\",\"pos_ref_id\":\"prchs-06-06-2019-11-49-05\"},\"datetime\": \"2017-04-26T11:30:21.000\",\"event\": \"signature_required\",\"id\": \"24\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);

        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, msg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.completed(Message.SuccessState.SUCCESS, msg, "Completed");

        Assert.assertEquals(transactionFlowState.getSuccess(), Message.SuccessState.SUCCESS);
        Assert.assertEquals(transactionFlowState.getResponse(), msg);
        Assert.assertTrue(transactionFlowState.isFinished());
        Assert.assertFalse(transactionFlowState.isAttemptingToCancel());
        Assert.assertFalse(transactionFlowState.isAwaitingGltResponse());
        Assert.assertFalse(transactionFlowState.isAwaitingSignatureCheck());
        Assert.assertFalse(transactionFlowState.isAwaitingPhoneForAuth());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "Completed");
    }

    @Test
    public void testTransactionFlowUnknownCompleted() {
        Secrets secrets = SpiClientTestUtils.setTestSecrets(null, null);

        String jsonStr = "{\"message\":{\"data\":{\"merchant_receipt\": \"\\nEFTPOS FROM WESTPAC\\nVAAS Product 2\\n275 Kent St\\nSydney 2000\\nAustralia\\n\\n\\nMID         02447506\\nTSP     100381990116\\nTIME 26APR17   11:29\\nRRN     170426000358\\nTRAN 000358   CREDIT\\nAmex               S\\nCARD............4477\\nAUTH          764167\\n\\nPURCHASE   AUD100.00\\nTIP          AUD5.00\\n\\nTOTAL      AUD105.00\\n\\n\\n (001) APPROVE WITH\\n     SIGNATURE\\n\\n\\n\\n\\n\\n\\nSIGN:_______________\\n\\n\\n\\n\\n\\n\\n\\n\",\"pos_ref_id\":\"prchs-06-06-2019-11-49-05\"},\"datetime\": \"2017-04-26T11:30:21.000\",\"event\": \"signature_required\",\"id\": \"24\"}}";

        Message msg = Message.fromJson(jsonStr, secrets);

        TransactionFlowState transactionFlowState = new TransactionFlowState("1", TransactionType.SETTLEMENT_ENQUIRY, 0, msg, "Waiting for EFTPOS connection to make a settlement enquiry");
        transactionFlowState.unknownCompleted("UnknownCompleted");

        Assert.assertEquals(transactionFlowState.getSuccess(), Message.SuccessState.UNKNOWN);
        Assert.assertNull(transactionFlowState.getResponse());
        Assert.assertTrue(transactionFlowState.isFinished());
        Assert.assertFalse(transactionFlowState.isAttemptingToCancel());
        Assert.assertFalse(transactionFlowState.isAwaitingGltResponse());
        Assert.assertFalse(transactionFlowState.isAwaitingSignatureCheck());
        Assert.assertFalse(transactionFlowState.isAwaitingGltResponse());
        Assert.assertEquals(transactionFlowState.getDisplayMessage(), "UnknownCompleted");
    }
}
