package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetOpenTablesResponse {
    private String openTablesEntryData;

    private static final Gson GSON = new GsonBuilder().create();

    public String getOpenTablesEntryData() {
        return openTablesEntryData;
    }

    public void setOpenTablesEntryData(String openTablesEntryData) {
        this.openTablesEntryData = openTablesEntryData;
    }

    private List<OpenTablesEntry> getOpenTables() {
        final String openTablesData = openTablesEntryData;
        if (openTablesData == null || StringUtils.isWhitespace(openTablesData)) return new ArrayList<>();

        final byte[] bdArray = Base64.decodeBase64(openTablesData);
        final String bdStr = new String(bdArray, Charsets.UTF_8);
        return GSON.fromJson(bdStr, OpenTablesEntry.ListType.class);
    }

    public static String toOpenTablesData(List<OpenTablesEntry> ph) {
        if (ph.isEmpty()) return "";

        final String bphStr = GSON.toJson(ph);
        return Base64.encodeBase64String(bphStr.getBytes(Charsets.UTF_8));
    }

    public Message toMessage(String messageId) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("tables", getOpenTables());
        return new Message(messageId, Events.PAY_AT_TABLE_OPEN_TABLES, data, true);
    }
}
