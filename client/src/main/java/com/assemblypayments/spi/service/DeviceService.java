package com.assemblypayments.spi.service;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.*;

import java.io.IOException;

public class DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) {
        String deviceAddressUrl;
        OkHttpClient client = new OkHttpClient();

        try {
            String envSuffix = "";

            if (isTestMode) {
                envSuffix = "-sb";
            }

            deviceAddressUrl = String.format("https://device-address-api%s.%s.msp.assemblypayments.com/v1/%s/ip", envSuffix, acquirerCode, serialNumber);

            String apiKeyHeader = "ASM-MSP-DEVICE-ADDRESS-API-KEY";

            Request request = new Request.Builder()
                    .url(deviceAddressUrl)
                    .addHeader(apiKeyHeader, apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return new Gson().fromJson(response.body() != null ? response.body().string() : null, DeviceAddressStatus.class);
            }
        } catch (IOException ex) {
            LOG.error("DeviceAddressStatus: " + ex.getMessage());
            return null;
        }
    }
}
