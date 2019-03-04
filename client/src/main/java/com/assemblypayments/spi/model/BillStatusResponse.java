package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.google.gson.Gson;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the BillDetails that the POS will be asked for throughout a PayAtTable flow.
 */
public class BillStatusResponse {

    private static final Gson GSON = new Gson();

    private BillRetrievalResult result;
    private String billId;
    private String tableId;
    private String operatorId;
    private int totalAmount;
    private int outstandingAmount;
    private String billData;

    /**
     * @return Set this error accordingly if you are not able to return the BillDetails that were asked from you.
     */
    public BillRetrievalResult getResult() {
        return result;
    }

    /**
     * @param result Set this error accordingly if you are not able to return the BillDetails that were asked from you.
     */
    public void setResult(BillRetrievalResult result) {
        this.result = result;
    }

    /**
     * @return This is a unique identifier that you assign to each bill. It might be for example,
     * the timestamp of when the cover was opened.
     */
    public String getBillId() {
        return billId;
    }

    /**
     * @param billId This is a unique identifier that you assign to each bill. It might be for example,
     *               the timestamp of when the cover was opened.
     */
    public void setBillId(String billId) {
        this.billId = billId;
    }

    /**
     * @return This is the table ID that this bill was for. The waiter will enter it on the EFTPOS at the
     * start of the PayAtTable flow and the EFTPOS will retrieve the bill using the table ID.
     */
    public String getTableId() {
        return tableId;
    }

    /**
     * @param tableId This is the table ID that this bill was for. The waiter will enter it on the EFTPOS at the
     *                start of the PayAtTable flow and the Eftpos will retrieve the bill using the table ID.
     */
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    /**
     * @return The total amount on this bill, in cents.
     */
    public int getTotalAmount() {
        return totalAmount;
    }

    /**
     * @param totalAmount The total amount on this bill, in cents.
     */
    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * @return The currently outstanding amount on this bill, in cents.
     */
    public int getOutstandingAmount() {
        return outstandingAmount;
    }

    /**
     * @param outstandingAmount The currently outstanding amount on this bill, in cents.
     */
    public void setOutstandingAmount(int outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    /**
     * @return Your POS is required to persist some state on behalf of the EFTPOS so the EFTPOS can recover state.
     * It is just a piece of string that you save against your billId.
     * Whenever you're asked for BillDetails, make sure you return this piece of data if you have it.
     */
    public String getBillData() {
        return billData;
    }

    /**
     * @param billData Your POS is required to persist some state on behalf of the EFTPOS so the EFTPOS can recover state.
     *                 It is just a piece of string that you save against your billId.
     *                 Whenever you're asked for BillDetails, make sure you return this piece of data if you have it.
     */
    public void setBillData(String billData) {
        this.billData = billData;
    }

    public List<PaymentHistoryEntry> getBillPaymentHistory() {
        final String billData = this.billData;
        if (billData == null || StringUtils.isWhitespace(billData)) return new ArrayList<PaymentHistoryEntry>();

        final byte[] bdArray = Base64.decodeBase64(billData);
        final String bdStr = new String(bdArray, Charsets.UTF_8);
        return GSON.fromJson(bdStr, PaymentHistoryEntry.ListType.class);
    }

    public static String toBillData(List<PaymentHistoryEntry> ph) {
        if (ph.isEmpty()) return "";

        final String bphStr = GSON.toJson(ph);
        return Base64.encodeBase64String(bphStr.getBytes(Charsets.UTF_8));
    }

    public Message toMessage(String messageId) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("success", result == BillRetrievalResult.SUCCESS);

        final String billId = this.billId;
        if (!(billId == null || StringUtils.isWhitespace(billId))) {
            data.put("bill_id", billId);
        }

        final String tableId = this.tableId;
        if (!(tableId == null || StringUtils.isWhitespace(tableId))) {
            data.put("table_id", tableId);
        }

        final BillRetrievalResult result = this.result;
        if (result == BillRetrievalResult.SUCCESS) {
            data.put("bill_total_amount", totalAmount);
            data.put("bill_outstanding_amount", outstandingAmount);
            data.put("bill_payment_history", getBillPaymentHistory());
        } else {
            data.put("error_reason", result.toString());
            data.put("error_detail", result.toString());
        }

        return new Message(messageId, Events.PAY_AT_TABLE_BILL_DETAILS, data, true);
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
}
