package io.mx51.spi.model;

public class TransactionUpdate {

    public String displayMessageCode;
    public String displayMessageText;

    public String getDisplayMessageCode() {
        return displayMessageCode;
    }

    public String getDisplayMessageText() {
        return displayMessageText;
    }

    public TransactionUpdate() { }

        public TransactionUpdate(Message m)
        {
            displayMessageCode = m.getDataStringValue("display_message_code");
            displayMessageCode = m.getDataStringValue("display_message_text");
        }
}