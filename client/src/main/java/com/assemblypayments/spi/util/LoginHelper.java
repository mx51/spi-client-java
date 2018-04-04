package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.LoginRequest;

public final class LoginHelper {

    private LoginHelper() {
    }

    public static LoginRequest newLoginRequest() {
        return new LoginRequest();
    }

}
