package com.assemblypayments.acmepos;

import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.model.LoginResponse;
import com.assemblypayments.spi.model.Message;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginTest {

    @Test
    public void testExpiringSoon() throws ParseException {
        String expiresAt = new SimpleDateFormat(Message.DATE_TIME_FORMAT)
                .format(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(9)));
        Map<String, Object> data = new HashMap<>();
        data.put("expires_datetime", expiresAt);
        data.put("success", true);
        LoginResponse loginResp = new LoginResponse(
                new Message("lr", Events.LOGIN_RESPONSE, data, true));

        assertTrue(loginResp.expiringSoon(0));
    }

    @Test
    public void testExpiringSoonNot() throws ParseException {
        String expiresAt = new SimpleDateFormat(Message.DATE_TIME_FORMAT)
                .format(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)));
        Map<String, Object> data = new HashMap<>();
        data.put("expires_datetime", expiresAt);
        data.put("success", true);
        LoginResponse loginResp = new LoginResponse(
                new Message("lr", Events.LOGIN_RESPONSE, data, true));

        assertFalse(loginResp.expiringSoon(0));
    }

}
