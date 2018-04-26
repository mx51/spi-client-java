package com.assemblypayments.spi.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Settlement extends AbstractTransactionResponse {

    public Settlement(Message m) {
        super(m);
    }

    public int getSettleByAquirerCount() {
        return m.getDataIntValue("accumulated_settle_by_acquirer_count");
    }

    public int getSettleByAquirerValue() {
        return m.getDataIntValue("accumulated_settle_by_acquirer_value");
    }

    public int getTotalCount() {
        return m.getDataIntValue("accumulated_total_count");
    }

    public int getTotalValue() {
        return m.getDataIntValue("accumulated_total_value");
    }

    public long getPeriodStartTime() {
        final String timeStr = m.getDataStringValue("settlement_period_start_time"); // "05:00"
        final String dateStr = m.getDataStringValue("settlement_period_start_date"); // "05Oct17"
        try {
            return new SimpleDateFormat("HH:mmddMMMyy", Locale.US).parse(timeStr + dateStr).getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Cannot parse PeriodStartTime", e);
        }
    }

    public long getPeriodEndTime() {
        final String timeStr = m.getDataStringValue("settlement_period_end_time"); // "05:00"
        final String dateStr = m.getDataStringValue("settlement_period_end_date"); // "05Oct17"
        try {
            return new SimpleDateFormat("HH:mmddMMMyy", Locale.US).parse(timeStr + dateStr).getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Cannot parse PeriodEndTime", e);
        }
    }

    public long getTriggeredTime() {
        final String timeStr = m.getDataStringValue("settlement_triggered_time"); // "05:00:45"
        final String dateStr = m.getDataStringValue("settlement_triggered_date"); // "05Oct17"
        try {
            return new SimpleDateFormat("HH:mm:ssddMMMyy", Locale.US).parse(timeStr + dateStr).getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Cannot parse TriggeredTime", e);
        }
    }

    public String getResponseText() {
        return super.getResponseText();
    }

    /**
     * @deprecated Use {@link #getMerchantReceipt()} instead.
     */
    @Deprecated
    public String getReceipt() {
        return getMerchantReceipt();
    }

    public String getTransactionRange() {
        return m.getDataStringValue("transaction_range");
    }

    public String getTerminalId() {
        return m.getDataStringValue("terminal_id");
    }

    public Iterable<SchemeSettlementEntry> getSchemeSettlementEntries() {
        final List<Object> schemes = m.getDataListValue("schemes");
        final List<SchemeSettlementEntry> entries = new ArrayList<SchemeSettlementEntry>();
        for (Object scheme : schemes) {
            if (scheme instanceof Map) {
                //noinspection unchecked
                entries.add(new SchemeSettlementEntry((Map<String, Object>) scheme));
            }
        }
        return entries;
    }

}
