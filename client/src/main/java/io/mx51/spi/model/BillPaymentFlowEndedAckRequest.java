package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class BillPaymentFlowEndedAckRequest
    {
        public int id;
        public String billId;

        public int getId() {
            return id;
        }

        public String getBillId() {
            return billId;
        }

        public BillPaymentFlowEndedAckRequest(String billId)
        {
            this.billId = billId;
//            this.id = Id;
        }

        public Message ToMessage()
        {
            Map<String, Object> data = new HashMap<>();
            data.put("bill_id", this.billId);

            return new Message(RequestIdHelper.id("authad"), Events.PAY_AT_TABLE_BILL_PAYMENT_FLOW_ENDED_ACK, data, true);
        }
    }