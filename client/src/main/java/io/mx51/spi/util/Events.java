package io.mx51.spi.util;

/**
 * Events statically declares the various event names in messages.
 */
public final class Events {

    public static final String PAIR_REQUEST = "pair_request";
    public static final String KEY_REQUEST = "key_request";
    public static final String KEY_RESPONSE = "key_response";
    public static final String KEY_CHECK = "key_check";
    public static final String PAIR_RESPONSE = "pair_response";
    public static final String DROP_KEYS_ADVICE = "drop_keys";

    public static final String LOGIN_REQUEST = "login_request";
    public static final String LOGIN_RESPONSE = "login_response";

    public static final String PING = "ping";
    public static final String PONG = "pong";

    public static final String PURCHASE_REQUEST = "purchase";
    public static final String PURCHASE_RESPONSE = "purchase_response";
    public static final String CANCEL_TRANSACTION_REQUEST = "cancel_transaction";
    public static final String CANCEL_TRANSACTION_RESPONSE = "cancel_response";
    public static final String GET_LAST_TRANSACTION_REQUEST = "get_last_transaction";
    public static final String GET_LAST_TRANSACTION_RESPONSE = "last_transaction";
    public static final String REVERSAL_REQUEST = "reverse_transaction";
    public static final String REVERSAL_RESPONSE = "reverse_transaction_response";
    public static final String REFUND_REQUEST = "refund";
    public static final String REFUND_RESPONSE = "refund_response";
    public static final String SIGNATURE_REQUIRED = "signature_required";
    public static final String SIGNATURE_DECLINED = "signature_decline";
    public static final String SIGNATURE_ACCEPTED = "signature_accept";
    public static final String AUTH_CODE_REQUIRED = "authorisation_code_required";
    public static final String AUTH_CODE_ADVICE = "authorisation_code_advice";

    public static final String CASHOUT_ONLY_REQUEST = "cash";
    public static final String CASHOUT_ONLY_RESPONSE = "cash_response";

    public static final String MOTO_PURCHASE_REQUEST = "moto_purchase";
    public static final String MOTO_PURCHASE_RESPONSE = "moto_purchase_response";

    public static final String SETTLE_REQUEST = "settle";
    public static final String SETTLE_RESPONSE = "settle_response";
    public static final String SETTLEMENT_ENQUIRY_REQUEST = "settlement_enquiry";
    public static final String SETTLEMENT_ENQUIRY_RESPONSE = "settlement_enquiry_response";

    public static final String SET_POS_INFO_REQUEST = "set_pos_info";
    public static final String SET_POS_INFO_RESPONSE = "set_pos_info_response";

    public static final String KEY_ROLL_REQUEST = "request_use_next_keys";
    public static final String KEY_ROLL_RESPONSE = "response_use_next_keys";

    public static final String ERROR = "error";

    public static final String INVALID_HMAC_SIGNATURE = "_INVALID_SIGNATURE_";

    public static final String TRANSACTION_UPDATE_MESSAGE = "txn_update_message";

    //region Pay At Table

    public static final String PAY_AT_TABLE_GET_TABLE_CONFIG = "get_table_config"; // Incoming. When EFTPOS wants to ask us for P@T configuration.
    public static final String PAY_AT_TABLE_SET_TABLE_CONFIG = "set_table_config"; // Outgoing. When we want to instruct eftpos with the P@T configuration.
    public static final String PAY_AT_TABLE_GET_BILL_DETAILS = "get_bill_details"; // Incoming. When EFTPOS wants to retrieve the bill for a table.
    public static final String PAY_AT_TABLE_BILL_DETAILS = "bill_details";        // Outgoing. We reply with this when eftpos requests to us get_bill_details.
    public static final String PAY_AT_TABLE_BILL_PAYMENT = "bill_payment";        // Incoming. When the EFTPOS advices.
    public static final String PAY_AT_TABLE_GET_OPEN_TABLES = "get_open_tables";
    public static final String PAY_AT_TABLE_OPEN_TABLES = "open_tables";
    public static final String PAY_AT_TABLE_BILL_PAYMENT_FLOW_ENDED = "bill_payment_flow_ended";

    //endregion

    //region Preauth

    public static final String ACCOUNT_VERIFY_REQUEST = "account_verify";
    public static final String ACCOUNT_VERIFY_RESPONSE = "account_verify_response";

    public static final String PREAUTH_OPEN_REQUEST = "preauth";
    public static final String PREAUTH_OPEN_RESPONSE = "preauth_response";

    public static final String PREAUTH_TOPUP_REQUEST = "preauth_topup";
    public static final String PREAUTH_TOPUP_RESPONSE = "preauth_topup_response";

    public static final String PREAUTH_EXTEND_REQUEST = "preauth_extend";
    public static final String PREAUTH_EXTEND_RESPONSE = "preauth_extend_response";

    public static final String PREAUTH_PARTIAL_CANCELLATION_REQUEST = "preauth_partial_cancellation";
    public static final String PREAUTH_PARTIAL_CANCELLATION_RESPONSE = "preauth_partial_cancellation_response";

    public static final String PREAUTH_CANCELLATION_REQUEST = "preauth_cancellation";
    public static final String PREAUTH_CANCELLATION_RESPONSE = "preauth_cancellation_response";

    public static final String PREAUTH_COMPLETE_REQUEST = "completion";
    public static final String PREAUTH_COMPLETE_RESPONSE = "completion_response";

    public static final String PRINTING_REQUEST = "print";
    public static final String PRINTING_RESPONSE = "print_response";

    public static final String TERMINAL_STATUS_REQUEST = "get_terminal_status";
    public static final String TERMINAL_STATUS_RESPONSE = "terminal_status";

    public static final String TERMINAL_CONFIGURATION_REQUEST = "get_terminal_configuration";
    public static final String TERMINAL_CONFIGURATION_RESPONSE = "terminal_configuration";

    public static final String BATTERY_LEVEL_CHANGED = "battery_level_changed";

    //endregion

    private Events() {
    }

}
