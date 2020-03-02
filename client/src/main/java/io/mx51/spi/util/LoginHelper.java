package io.mx51.spi.util;

import io.mx51.spi.model.LoginRequest;

@Deprecated
public final class LoginHelper {

    private LoginHelper() {
    }

    public static LoginRequest newLoginRequest() {
        return new LoginRequest();
    }

}
