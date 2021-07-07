package io.mx51.spi.model;

import io.mx51.spi.util.Events;
import io.mx51.spi.util.RequestIdHelper;

import java.util.HashMap;
import java.util.Map;

public class ReversalResponse {

    private Boolean success;
    private String posRefId;

    private Message _m;

    public ReversalResponse() {
    }

    public ReversalResponse(Message _m) {
        this._m = _m;

        this.posRefId = _m.getDataStringValue("pos_ref_id");
        this.success = _m.getSuccessState() == Message.SuccessState.SUCCESS;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getPosRefId() {
        return posRefId;
    }

    public String getErrorReason()
    {
        return _m.getDataStringValue("error_reason");
    }

    public String getErrorDetail()
    {
        return _m.getDataStringValue("error_detail");
    }
}
