package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) {
        String deviceAddressUrl;
        StringBuilder response;

        try {
            String envSuffix = "";

            if (isTestMode) {
                envSuffix = "-sb";
            }

            deviceAddressUrl = String.format("https://device-address-api%s.%s.msp.assemblypayments.com/v1/%s/ip", envSuffix, acquirerCode, serialNumber);

            URL obj = new URL(deviceAddressUrl);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            String apiKeyHeader = "ASM-MSP-DEVICE-ADDRESS-API-KEY";
            con.setRequestProperty(apiKeyHeader, apiKey);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
        } catch (IOException ex) {
            LOG.error("DeviceAddressStatus: " + ex.getMessage());
            return null;
        }

        return new Gson().fromJson(response.toString(), DeviceAddressStatus.class);
    }
}
