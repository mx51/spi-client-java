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

public class GetOpenTablesResponse {
    private String tableData;

    private static final Gson GSON = new Gson();

    public String getTableData() {
        return tableData;
    }

    public void setTableData(String tableData) {
        this.tableData = tableData;
    }

    public List<OpenTablesEntry> getOpenTables() {
        final String tableData = this.tableData;
        if (tableData == null || StringUtils.isWhitespace(tableData)) return new ArrayList<OpenTablesEntry>();

        final byte[] bdArray = Base64.decodeBase64(tableData);
        final String bdStr = new String(bdArray, Charsets.UTF_8);
        return GSON.fromJson(bdStr, OpenTablesEntry.ListType.class);
    }

    public Message toMessage(String messageId) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("tables", getOpenTables());
        return new Message(messageId, Events.PAY_AT_TABLE_OPEN_TABLES, data, true);
    }
}
