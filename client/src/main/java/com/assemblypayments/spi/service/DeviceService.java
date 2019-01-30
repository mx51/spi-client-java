package com.assemblypayments.spi.service;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.*;

import java.io.IOException;

public class DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) {
        String envSuffix = "";
        String deviceAddressUrl;
        OkHttpClient okHttpClient = new OkHttpClient();
        DeviceAddressStatus deviceAddressStatus = new DeviceAddressStatus();

        if (isTestMode) {
            envSuffix = "-sb";
        }

        deviceAddressUrl = String.format("https://device-address-api%s.%s.msp.assemblypayments.com/v1/%s/ip", envSuffix, acquirerCode, serialNumber);

        try {
            Request request = new Request.Builder()
                    .url(deviceAddressUrl)
                    .addHeader("ASM-MSP-DEVICE-ADDRESS-API-KEY", apiKey)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.body() != null) {
                deviceAddressStatus = new Gson().fromJson(response.body().string(), DeviceAddressStatus.class);
            }

            return deviceAddressStatus;
        } catch (IOException ex) {
            LOG.error("DeviceAddressStatus: " + ex.getMessage());
            return deviceAddressStatus;
        }
    }
}
