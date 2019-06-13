package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.RequestIdHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpiPayAtTable {

    private static final Logger LOG = LoggerFactory.getLogger("spipat");

    private final Spi spi;

    private final PayAtTableConfig config;

    private GetBillStatusDelegate getBillStatusDelegate;
    private BillPaymentReceivedDelegate billPaymentReceivedDelegate;
    private GetOpenTablesDelegate getOpenTablesDelegate;
    private BillPaymentFlowEndedDelegate billPaymentFlowEndedDelegate;

    SpiPayAtTable(Spi spi) {
        this.spi = spi;
        config = new PayAtTableConfig();
    }

    public PayAtTableConfig getConfig() {
        return config;
    }

    public void pushPayAtTableConfig() {
        spi.send(config.toMessage(RequestIdHelper.id("patconf")));
    }

    public void setGetBillStatusDelegate(GetBillStatusDelegate getBillStatusDelegate) {
        this.getBillStatusDelegate = getBillStatusDelegate;
    }

    public void setBillPaymentReceivedDelegate(BillPaymentReceivedDelegate billPaymentReceivedDelegate) {
        this.billPaymentReceivedDelegate = billPaymentReceivedDelegate;
    }

    public void setGetOpenTablesDelegate(GetOpenTablesDelegate getOpenTablesDelegate) {
        this.getOpenTablesDelegate = getOpenTablesDelegate;
    }

    public void setBillPaymentFlowEndedDelegate(BillPaymentFlowEndedDelegate billPaymentFlowEndedDelegate) {
        this.billPaymentFlowEndedDelegate = billPaymentFlowEndedDelegate;
    }

    void handleGetBillDetailsRequest(@NotNull Message m) {
        String operatorId = m.getDataStringValue("operator_id");
        String tableId = m.getDataStringValue("table_id");
        boolean paymentFlowStarted = m.getDataBooleanValue("payment_flow_started", false);


        // Ask POS for Bill Details for this tableId, including encoded PaymentData
        BillStatusResponse billStatus = getBillStatusDelegate.getBillStatus(null, tableId, operatorId, paymentFlowStarted);
        billStatus.setTableId(tableId);
        if (billStatus.getTotalAmount() <= 0) {
            LOG.info("Table has 0 total amount, not sending it to EFTPOS");
            billStatus.setResult(BillRetrievalResult.INVALID_TABLE_ID);
        }

        spi.send(billStatus.toMessage(m.getId()));
    }

    void handleBillPaymentAdvice(@NotNull Message m) {
        BillPayment billPayment = new BillPayment(m);

        assert getBillStatusDelegate != null;

        // Ask POS for Bill Details, including encoded PaymentData
        BillStatusResponse existingBillStatus = getBillStatusDelegate.getBillStatus(billPayment.getBillId(), billPayment.getTableId(), billPayment.getOperatorId(), billPayment.getPaymentFlowStarted());
        if (existingBillStatus.getResult() != BillRetrievalResult.SUCCESS) {
            LOG.warn("Could not retrieve bill status for payment advice, sending error to EFTPOS");
            spi.send(existingBillStatus.toMessage(m.getId()));
        }

        List<PaymentHistoryEntry> existingPaymentHistory = existingBillStatus.getBillPaymentHistory();

        PaymentHistoryEntry foundExistingEntry = null;
        String referenceId = billPayment.getPurchaseResponse().getTerminalReferenceId();
        if (referenceId != null) {
            for (PaymentHistoryEntry phe : existingPaymentHistory) {
                if (referenceId.equals(phe.getTerminalRefId())) {
                    foundExistingEntry = phe;
                    break;
                }
            }
        }

        if (foundExistingEntry != null) {
            // We have already processed this payment.
            // Perhaps EFTPOS did get our acknowledgement.
            // Let's update EFTPOS.
            LOG.warn("Had already received this bill_payment advice from EFTPOS, ignoring");
            spi.send(existingBillStatus.toMessage(m.getId()));
            return;
        }

        assert billPaymentReceivedDelegate != null;

        // Let's add the new entry to the history
        List<PaymentHistoryEntry> updatedHistoryEntries = new ArrayList<PaymentHistoryEntry>(existingPaymentHistory);
        updatedHistoryEntries.add(new PaymentHistoryEntry(
                billPayment.getPaymentType().toString().toLowerCase(),
                billPayment.getPurchaseResponse().toPaymentSummary()
        ));
        String updatedBillData = BillStatusResponse.toBillData(updatedHistoryEntries);

        // Advise POS of new payment against this bill, and the updated BillData to Save.
        BillStatusResponse updatedBillStatus = billPaymentReceivedDelegate.getBillReceived(billPayment, updatedBillData);

        // Just in case client forgot to set these:
        updatedBillStatus.setBillId(billPayment.getBillId());
        updatedBillStatus.setTableId(billPayment.getTableId());

        if (updatedBillStatus.getResult() != BillRetrievalResult.SUCCESS) {
            LOG.warn("POS threw error when being advised of payment, letting EFTPOS know, and sending existing bill data");
            updatedBillStatus.setBillData(existingBillStatus.getBillData());
        } else {
            updatedBillStatus.setBillData(updatedBillData);
        }

        spi.send(updatedBillStatus.toMessage(m.getId()));
    }

    void handleGetTableConfig(@NotNull Message m) {
        spi.send(config.toMessage(m.getId()));
    }

    void handleGetOpenTablesRequest(@NotNull Message m) {
        assert getOpenTablesDelegate != null;

        String operatorId = m.getDataStringValue("operator_id");

        GetOpenTablesResponse openTablesResponse = getOpenTablesDelegate.getOpenTables(operatorId);
        if (openTablesResponse.getOpenTablesEntries().size() <= 0) {
            LOG.info("There is no open table.");
        }

        for (OpenTablesEntry openTablesEntry : openTablesResponse.getOpenTablesEntries()) {
            if (openTablesEntry.getTableId().length() > 20) {
                LOG.info(openTablesEntry.getTableId() + " Table Id is greater than 20 characters!");
                openTablesEntry.setTableId(openTablesEntry.getTableId().substring(0, 20));
            }
        }

        spi.send(openTablesResponse.toMessage(m.getId()));
    }

    void handleBillPaymentFlowEnded(@NotNull Message m) {
        assert billPaymentFlowEndedDelegate != null;

        billPaymentFlowEndedDelegate.getBillPaymentFlowEnded(m);
    }

    public interface GetBillStatusDelegate {
        /**
         * This delegate will be called when the EFTPOS needs to know the current state of a bill for a table.
         *
         * @param billId             The unique identifier of the bill. If empty, it means that the PayAtTable flow on
         *                           the EFTPOS is just starting, and the lookup is by tableId.
         * @param tableId            The identifier of the table that the bill is for.
         * @param operatorId         The id of the operator entered on the EFTPOS.
         * @param paymentFlowStarted The flag of the payment flow started.
         * @return You need to return the current state of the bill.
         */
        BillStatusResponse getBillStatus(String billId, String tableId, String operatorId, boolean paymentFlowStarted);
    }

    public interface BillPaymentReceivedDelegate {
        BillStatusResponse getBillReceived(BillPayment billPayment, String updatedBillData);
    }

    public interface GetOpenTablesDelegate {
        GetOpenTablesResponse getOpenTables(String operatorId);
    }

    public interface BillPaymentFlowEndedDelegate {
        void getBillPaymentFlowEnded(Message message);
    }
}
