package io.mx51.spi.model;

import java.util.HashMap;
import java.util.Map;

public class TransactionReport
    {
        public String posVendorId;
        public String posVersion;
        public String libraryLanguage;
        public String libraryVersion;
        public String posRefId;
        public String serialNumber;
        public String event;
        public String txType;
        public String txResult;
        public long txStartTime;
        public long txEndTime;
        public int durationMs;
        public String currentFlow;
        public String currentTxFlowState;
        public String currentStatus;


        public String getPosVendorId() {
            return posVendorId;
        }

        public void setPosVendorId(String posVendorId) {
            this.posVendorId = posVendorId;
        }

        public String getPosVersion() {
            return posVersion;
        }

        public void setPosVersion(String posVersion) {
            this.posVersion = posVersion;
        }

        public String getLibraryLanguage() {
            return libraryLanguage;
        }

        public void setLibraryLanguage(String libraryLanguage) {
            this.libraryLanguage = libraryLanguage;
        }

        public String getLibraryVersion() {
            return libraryVersion;
        }

        public void setLibraryVersion(String libraryVersion) {
            this.libraryVersion = libraryVersion;
        }

        public String getPosRefId() {
            return posRefId;
        }

        public void setPosRefId(String posRefId) {
            this.posRefId = posRefId;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getTxType() {
            return txType;
        }

        public void setTxType(String txType) {
            this.txType = txType;
        }

        public String getTxResult() {
            return txResult;
        }

        public void setTxResult(String txResult) {
            this.txResult = txResult;
        }

        public long getTxStartTime() {
            return txStartTime;
        }

        public void setTxStartTime(long txStartTime) {
            this.txStartTime = txStartTime;
        }

        public long getTxEndTime() {
            return txEndTime;
        }

        public void setTxEndTime(long txEndTime) {
            this.txEndTime = txEndTime;
        }

        public int getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(int durationMs) {
            this.durationMs = durationMs;
        }

        public String getCurrentFlow() {
            return currentFlow;
        }

        public void setCurrentFlow(String currentFlow) {
            this.currentFlow = currentFlow;
        }

        public String getCurrentTxFlowState() {
            return currentTxFlowState;
        }

        public void setCurrentTxFlowState(String currentTxFlowState) {
            this.currentTxFlowState = currentTxFlowState;
        }

        public String getCurrentStatus() {
            return currentStatus;
        }

        public void setCurrentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
        }


        public Map<String, Object> toMessage()
        {
            final Map<String, Object> data = new HashMap<String, Object>();
            data.put("pos_vendor_id", posVendorId);
            data.put("pos_version", posVersion);
            data.put("library_language", libraryLanguage);
            data.put("library_version", libraryVersion);
            data.put("pos_ref_id", posRefId);
            data.put("serial_number", serialNumber);
            data.put("event", event);
            data.put("tx_type", txType);
            data.put("tx_result", txResult);
            data.put("tx_start_ts_ms", txStartTime);
            data.put("tx_end_ts_ms", txEndTime);
            data.put("duration_ms", durationMs);
            data.put("current_flow", currentFlow);
            data.put("current_tx_flow_state", currentTxFlowState);
            data.put("current_status", currentStatus);

            return data;
        }
    }