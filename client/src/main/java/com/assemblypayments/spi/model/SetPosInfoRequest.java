package com.assemblypayments.spi.model;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

public class SetPosInfoRequest {

    private final String version;
    private final String vendorId;
    private final String libraryLanguage;
    private final String libraryVersion;
    private final Map<String, String> otherInfo;
    
    public SetPosInfoRequest(String version, String vendorId, String libraryLanguage, String libraryVersion, Map<String, String> otherInfo) {
        this.version = version;
        this.vendorId = vendorId;
        this.libraryLanguage = libraryLanguage;
        this.libraryVersion = libraryVersion;
        this.otherInfo = otherInfo;
    }

	public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_version", version);
        data.put("pos_vendor_id", vendorId);
        data.put("library_language", libraryLanguage);
        data.put("library_version", libraryVersion);
        data.put("other_info", otherInfo);        

        return new Message(RequestIdHelper.id("prav"), Events.SET_POS_INFO_REQUEST, data, true);
    }
}
