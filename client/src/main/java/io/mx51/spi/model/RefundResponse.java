package io.mx51.spi.model;

import org.jetbrains.annotations.NotNull;

public class RefundResponse extends AbstractChargeResponse {

    public RefundResponse(@NotNull Message m) {
        super(m);
    }

    public int getRefundAmount() {
        return m.getDataIntValue("refund_amount");
    }

}
