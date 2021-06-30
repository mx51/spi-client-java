package io.mx51.spi.model;

import com.google.gson.annotations.SerializedName;

/**
 * The Spi instance can be in one of these flows at any point in time.
 */
public class DeviceAddressStatus {
    @SerializedName("ip")
    private String address;
    @SerializedName("last_updated")
    private String lastUpdated;
    private int responseCode;
    private String responseMessage;
    private DeviceAddressResponseCode deviceAddressResponseCode;

    public String getAddress() {
        return address;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public DeviceAddressResponseCode getDeviceAddressResponseCode() {
        return deviceAddressResponseCode;
    }

    public void setDeviceAddressResponseCode(DeviceAddressResponseCode deviceAddressResponseCode) {
        this.deviceAddressResponseCode = deviceAddressResponseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
