package io.mx51.spi.util;

public final class RequestIdHelper {

    private static int counter = 1;

    private RequestIdHelper() {
    }

    public static String id(String prefix) {
        return prefix + counter++;
    }

}
