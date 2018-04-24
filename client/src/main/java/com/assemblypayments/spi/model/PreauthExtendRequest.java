package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class PreauthExtendRequest implements Message.Compatible {

    private final String preauthId;
    private final String posRefId;

    public PreauthExtendRequest(String preauthId, String posRefId) {
        this.preauthId = preauthId;
        this.posRefId = posRefId;
    }

    public String getPreauthId() {
        return preauthId;
    }

    public String getPosRefId() {
        return posRefId;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", posRefId);
        data.put("preauth_id", preauthId);

        return new Message(RequestIdHelper.id("prext"), Events.PREAUTH_EXTEND_REQUEST, data, true);
    }

}
