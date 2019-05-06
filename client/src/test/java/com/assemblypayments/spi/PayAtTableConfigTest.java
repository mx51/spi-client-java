package com.assemblypayments.spi;

import com.assemblypayments.spi.model.Message;
import com.assemblypayments.spi.model.PayAtTableConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PayAtTableConfigTest {

    @Test
    public void testSetPayAtTableEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setPayAtTableEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isPayAtTableEnabled(), msg.getDataBooleanValue("pay_at_table_enabled", false));
    }

    @Test
    public void testSetOperatorIdEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setOperatorIdEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isOperatorIdEnabled(), msg.getDataBooleanValue("operator_id_enabled", false));
    }

    @Test
    public void testSetSplitByAmountEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setSplitByAmountEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isSplitByAmountEnabled(), msg.getDataBooleanValue("split_by_amount_enabled", false));
    }

    @Test
    public void testSetEqualSplitEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setEqualSplitEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isEqualSplitEnabled(), msg.getDataBooleanValue("equal_split_enabled", false));
    }

    @Test
    public void testSetTippingEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setTippingEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isTippingEnabled(), msg.getDataBooleanValue("tipping_enabled", false));
    }

    @Test
    public void testSetSummaryReportEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setSummaryReportEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isSummaryReportEnabled(), msg.getDataBooleanValue("summary_report_enabled", false));
    }

    @Test
    public void testSetLabelPayButton() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setLabelPayButton("PAT");

        Message msg = config.toMessage("111");
        assertEquals(config.getLabelPayButton(), msg.getDataStringValue("pay_button_label"));
    }

    @Test
    public void testSetLabelOperatorId() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setLabelOperatorId("12");

        Message msg = config.toMessage("111");
        assertEquals(config.getLabelOperatorId(), msg.getDataStringValue("operator_id_label"));
    }

    @Test
    public void testSetLabelTableId() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setLabelTableId("12");

        Message msg = config.toMessage("111");
        assertEquals(config.getLabelTableId(), msg.getDataStringValue("table_id_label"));
    }

    @Test
    public void testSetAllowedOperatorIds() {
        PayAtTableConfig config = new PayAtTableConfig();
        List<String> allowedStringList = new ArrayList<>();
        allowedStringList.add("1");
        allowedStringList.add("2");
        config.setAllowedOperatorIds(allowedStringList);

        Message msg = config.toMessage("111");
        assertEquals(config.getAllowedOperatorIds(), msg.getDataListValue("operator_id_list"));
        assertEquals(config.getAllowedOperatorIds().size(), 2);
    }

    @Test
    public void testSetTableRetrievalEnabled() {
        PayAtTableConfig config = new PayAtTableConfig();
        config.setTableRetrievalEnabled(true);

        Message msg = config.toMessage("111");
        assertEquals(config.isTableRetrievalEnabled(), msg.getDataBooleanValue("table_retrieval_enabled", false));
    }
}
