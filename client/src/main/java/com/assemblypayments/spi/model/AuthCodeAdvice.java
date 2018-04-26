package com.assemblypayments.spi.model;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class AuthCodeAdvice implements Message.Compatible {

    private final String posRefId;
    private final String authCode;

    public AuthCodeAdvice(String posRefId, String authCode) {
        this.posRefId = posRefId;
        this.authCode = authCode;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getAuthCode() {
        return authCode;
    }

    @Override
    public Message toMessage() {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("pos_ref_id", getPosRefId());
        data.put("auth_code", getAuthCode());
        return new Message(RequestIdHelper.id("authad"), Events.AUTH_CODE_ADVICE, data, true);
    }

}
