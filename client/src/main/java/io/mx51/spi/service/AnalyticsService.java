package io.mx51.spi.service;

import com.google.gson.Gson;
import io.mx51.spi.model.TransactionReport;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AnalyticsService
    {
        private static final Gson GSON = new Gson();
        private final String ApiKeyHeader = "ASM-MSP-DEVICE-ADDRESS-API-KEY";
        private final OkHttpClient okHttpClient;

        public AnalyticsService() {
            okHttpClient = new OkHttpClient.Builder().build();
        }

        public Response reportTransaction(TransactionReport transactionReport, String apiKey, String acquirerCode, boolean isTestMode) throws ExecutionException, InterruptedException, IOException {
            String transactionServiceUri = String.format("https://spi-analytics-api%s.%s.mspenv.io/v1/report-transaction", isTestMode ? "-sb" : "", acquirerCode);
            Map<String, Object> message = transactionReport.toMessage();

            Request request = new Request.Builder()
                    .url(transactionServiceUri)
                    .addHeader(ApiKeyHeader, apiKey)
                    .post(RequestBody.create(MediaType.parse("application/json"), GSON.toJson(message)))
                    .build();
            return  okHttpClient.newCall(request).execute();

        }


    }