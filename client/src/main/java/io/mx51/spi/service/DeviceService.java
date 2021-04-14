package io.mx51.spi.service;

import io.mx51.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");
    private static final Gson GSON = new Gson();
    private final OkHttpClient okHttpClient;
    private static final long CONNECTION_TIMEOUT_SECS = 8;

    public DeviceService() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT_SECS, TimeUnit.SECONDS)
                .build();
    }

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) {
        String envSuffix = "";
        String deviceAddressUrl;

        DeviceAddressStatus deviceAddressStatus = new DeviceAddressStatus();

        if (isTestMode) {
            envSuffix = "-sb";
        }

        deviceAddressUrl = String.format("https://device-address-api%s.%s.mspenv.io/v1/%s/ip", envSuffix, acquirerCode, serialNumber);

        try {
            Request request = new Request.Builder()
                    .url(deviceAddressUrl)
                    .addHeader("ASM-MSP-DEVICE-ADDRESS-API-KEY", apiKey)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.body() != null) {
                deviceAddressStatus = GSON.fromJson(response.body().string(), DeviceAddressStatus.class);
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
