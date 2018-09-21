package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayAtTableConfig {

    private boolean payAtTableEnabled;
    private boolean operatorIdEnabled;
    private boolean splitByAmountEnabled;
    private boolean equalSplitEnabled;
    private boolean tippingEnabled;
    private boolean summaryReportEnabled;
    private String labelPayButton;
    private String labelOperatorId;
    private String labelTableId;
    private List<String> allowedOperatorIds;

    public boolean isPayAtTableEnabled() {
        return payAtTableEnabled;
    }

    public void setPayAtTableEnabled(boolean payAtTableEnabled) {
        this.payAtTableEnabled = payAtTableEnabled;
    }

    public boolean isOperatorIdEnabled() {
        return operatorIdEnabled;
    }

    public void setOperatorIdEnabled(boolean operatorIdEnabled) {
        this.operatorIdEnabled = operatorIdEnabled;
    }

    public boolean isSplitByAmountEnabled() {
        return splitByAmountEnabled;
    }

    public void setSplitByAmountEnabled(boolean splitByAmountEnabled) {
        this.splitByAmountEnabled = splitByAmountEnabled;
    }

    public boolean isEqualSplitEnabled() {
        return equalSplitEnabled;
    }

    public void setEqualSplitEnabled(boolean equalSplitEnabled) {
        this.equalSplitEnabled = equalSplitEnabled;
    }

    public boolean isTippingEnabled() {
        return tippingEnabled;
    }

    public void setTippingEnabled(boolean tippingEnabled) {
        this.tippingEnabled = tippingEnabled;
    }

    public boolean isSummaryReportEnabled() {
        return summaryReportEnabled;
    }

    public void setSummaryReportEnabled(boolean summaryReportEnabled) {
        this.summaryReportEnabled = summaryReportEnabled;
    }

    public String getLabelPayButton() {
        return labelPayButton;
    }

    public void setLabelPayButton(String labelPayButton) {
        this.labelPayButton = labelPayButton;
    }

    public String getLabelOperatorId() {
        return labelOperatorId;
    }

    public void setLabelOperatorId(String labelOperatorId) {
        this.labelOperatorId = labelOperatorId;
    }

    public String getLabelTableId() {
        return labelTableId;
    }

    public void setLabelTableId(String labelTableId) {
        this.labelTableId = labelTableId;
    }

    /**
     * Fill in with operator ids that the eftpos terminal will validate against.
     * Leave Empty to allow any operator_id through.
     */
    public List<String> getAllowedOperatorIds() {
        return allowedOperatorIds;
    }

    /**
     * Fill in with operator ids that the eftpos terminal will validate against.
     * Leave Empty to allow any operator_id through.
     */
    public void setAllowedOperatorIds(List<String> allowedOperatorIds) {
        this.allowedOperatorIds = allowedOperatorIds;
    }

    public Message toMessage(String messageId) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pay_at_table_enabled", payAtTableEnabled);
        data.put("operator_id_enabled", operatorIdEnabled);
        data.put("split_by_amount_enabled", splitByAmountEnabled);
        data.put("equal_split_enabled", equalSplitEnabled);
        data.put("tipping_enabled", tippingEnabled);
        data.put("summary_report_enabled", summaryReportEnabled);
        data.put("pay_button_label", labelPayButton);
        data.put("operator_id_label", labelOperatorId);
        data.put("table_id_label", labelTableId);
        data.put("operator_id_list", allowedOperatorIds);

        return new Message(messageId, Events.PAY_AT_TABLE_SET_TABLE_CONFIG, data, true);
    }

    public static Message featureDisableMessage(String messageId) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pay_at_table_enabled", false);

        return new Message(messageId, Events.PAY_AT_TABLE_SET_TABLE_CONFIG, data, true);
    }

}
