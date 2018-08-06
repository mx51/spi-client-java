package com.assemblypayments.spi.model;

import java.util.HashMap;
import java.util.Map;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

public class SetPosInfoRequest {

    public final String version;
    public final String vendorId;
    public final String libraryLanguage;
    public final String libraryVersion;
    public final Map<String, String> otherInfo;
    
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
        

        return new Message(RequestIdHelper.id("prav"), Events.SET_POSINFO_REQUEST, data, true);
    }
}
