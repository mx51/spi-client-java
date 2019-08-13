package com.assemblypayments.spi.service;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");
    private Gson gson = new Gson();
    private OkHttpClient.Builder okHttpClient;
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(8);

    public DeviceService() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) {
        String envSuffix = "";
        String deviceAddressUrl;

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

            Response response = okHttpClient.build().newCall(request).execute();

            if (response.body() != null) {
                deviceAddressStatus = gson.fromJson(response.body().string(), DeviceAddressStatus.class);
            }

            deviceAddressStatus.setResponseCode(response.code());
            deviceAddressStatus.setResponseMessage(response.message());
            return deviceAddressStatus;
        } catch (IOException ex) {
            LOG.error("DeviceAddressStatus: " + ex.getMessage());
            return null;
        }
    }
}
