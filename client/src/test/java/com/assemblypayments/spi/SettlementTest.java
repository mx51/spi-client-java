package com.assemblypayments.spi;

import com.assemblypayments.spi.model.Message;
import com.assemblypayments.spi.model.Settlement;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SettlementTest {

    @Test
    public void testParseDate_startTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_period_start_time", "05:01");
        data.put("settlement_period_start_date", "05Oct17");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long startTime = r.getPeriodStartTime();
        Calendar startTimeCalendar = Calendar.getInstance();
        startTimeCalendar.set(Calendar.YEAR, 2017);
        startTimeCalendar.set(Calendar.MONTH, 9);
        startTimeCalendar.set(Calendar.DAY_OF_MONTH, 5);
        startTimeCalendar.set(Calendar.HOUR_OF_DAY, 5);
        startTimeCalendar.set(Calendar.MINUTE, 1);
        startTimeCalendar.set(Calendar.SECOND, 0);
        startTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(startTimeCalendar.getTimeInMillis(), startTime);
    }

    @Test
    public void testParseDate_endTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_period_end_time", "06:02");
        data.put("settlement_period_end_date", "06Nov18");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long endTime = r.getPeriodEndTime();
        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(Calendar.YEAR, 2018);
        endTimeCalendar.set(Calendar.MONTH, 10);
        endTimeCalendar.set(Calendar.DAY_OF_MONTH, 6);
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endTimeCalendar.set(Calendar.MINUTE, 2);
        endTimeCalendar.set(Calendar.SECOND, 0);
        endTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(endTimeCalendar.getTimeInMillis(), endTime);
    }

    @Test
    public void testParseDate_trigTime() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("settlement_triggered_time", "07:03:45");
        data.put("settlement_triggered_date", "07Dec19");

        Message m = new Message("77", "event_y", data, false);

        Settlement r = new Settlement(m);

        long trigTime = r.getTriggeredTime();
        Calendar trigTimeCalendar = Calendar.getInstance();
        trigTimeCalendar.set(Calendar.YEAR, 2019);
        trigTimeCalendar.set(Calendar.MONTH, 11);
        trigTimeCalendar.set(Calendar.DAY_OF_MONTH, 7);
        trigTimeCalendar.set(Calendar.HOUR_OF_DAY, 7);
        trigTimeCalendar.set(Calendar.MINUTE, 3);
        trigTimeCalendar.set(Calendar.SECOND, 45);
        trigTimeCalendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(trigTimeCalendar.getTimeInMillis(), trigTime);
    }

}
