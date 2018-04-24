package com.assemblypayments.spi.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class LoginResponse {

    private final Boolean success;
    private final String expires;

    public LoginResponse(Message m) {
        this.success = (Boolean) m.getData().get("success");
        this.expires = (String) m.getData().get("expires_datetime");
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * @deprecated Use {@link #isSuccess()} instead.
     */
    @Deprecated
    public Boolean getSuccess() {
        return isSuccess();
    }

    public String getExpires() {
        return expires;
    }

    public boolean expiringSoon(long serverTimeDelta) throws ParseException {
        long now = System.currentTimeMillis();
        long nowServerTime = now + serverTimeDelta;
        long expiresAt = new SimpleDateFormat(Message.DATE_TIME_FORMAT).parse(expires).getTime();

        return expiresAt < (nowServerTime + TimeUnit.MINUTES.toMillis(10));
    }

}
