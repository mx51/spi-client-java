package com.assemblypayments.spi.model;

import com.google.gson.annotations.SerializedName;

/**
 * The Spi instance can be in one of these flows at any point in time.
 */
public class DeviceAddressStatus {
    @SerializedName("ip")
    private String address;
    @SerializedName("last_updated")
    private String lastUpdated;
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
}
