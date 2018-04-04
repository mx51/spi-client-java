package com.assemblypayments.spi.util;

/**
 * Events statically declares the various event names in messages.
 */
public final class Events {

    public static final String PAIR_REQUEST = "pair_request";
    public static final String KEY_REQUEST = "key_request";
    public static final String KEY_RESPONSE = "key_response";
    public static final String KEY_CHECK = "key_check";
    public static final String PAIR_RESPONSE = "pair_response";

    public static final String LOGIN_REQUEST = "login_request";
    public static final String LOGIN_RESPONSE = "login_response";

    public static final String PING = "ping";
    public static final String PONG = "pong";

    public static final String PURCHASE_REQUEST = "purchase";
    public static final String PURCHASE_RESPONSE = "purchase_response";
    public static final String CANCEL_TRANSACTION_REQUEST = "cancel_transaction";
    public static final String GET_LAST_TRANSACTION_REQUEST = "get_last_transaction";
    public static final String GET_LAST_TRANSACTION_RESPONSE = "last_transaction";
    public static final String REFUND_REQUEST = "refund";
    public static final String REFUND_RESPONSE = "refund_response";
    public static final String SIGNATURE_REQUIRED = "signature_required";
    public static final String SIGNATURE_DECLINED = "signature_decline";
    public static final String SIGNATURE_ACCEPTED = "signature_accept";

    public static final String SETTLE_REQUEST = "settle";
    public static final String SETTLE_RESPONSE = "settle_response";

    public static final String KEY_ROLL_REQUEST = "request_use_next_keys";
    public static final String KEY_ROLL_RESPONSE = "response_use_next_keys";

    public static final String ERROR = "error";

    public static final String INVALID_HMAC_SIGNATURE = "_INVALID_SIGNATURE_";

    private Events() {
    }

}
