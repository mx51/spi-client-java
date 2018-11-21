package com.assemblypayments.spi.util;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class DeviceService {

    public DeviceAddressStatus retrieveService(String serialNumber, String apiKey, String acquirerCode, boolean isTestMode) throws IOException {
        String deviceAddressUrl;

        if (isTestMode) {
            deviceAddressUrl = "https://device-address-api-sb." + acquirerCode + ".msp.assemblypayments.com/v1/" + serialNumber + "/ip";
        } else {
            deviceAddressUrl = "https://device-address-api." + acquirerCode + ".msp.assemblypayments.com/v1/" + serialNumber + "/ip";
        }

        URL obj = new URL(deviceAddressUrl);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        String apiKeyHeader = "ASM-MSP-DEVICE-ADDRESS-API-KEY";
        con.setRequestProperty(apiKeyHeader, apiKey);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return new Gson().fromJson(response.toString(), DeviceAddressStatus.class);
    }
}
