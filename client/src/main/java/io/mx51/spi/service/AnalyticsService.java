package io.mx51.spi.service;

import com.google.gson.Gson;
import io.mx51.spi.model.TransactionReport;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AnalyticsService
    {
        private static final Gson GSON = new Gson();
        private final String ApiKeyHeader = "ASM-MSP-DEVICE-ADDRESS-API-KEY";

        public Future<Response> reportTransaction(TransactionReport transactionReport, String apiKey, String acquirerCode, boolean isTestMode) throws ExecutionException, InterruptedException {
            String transactionServiceUri = isTestMode ? "https://spi-analytics-api-sb."+acquirerCode+".mspenv.io/v1/report-transaction" : "https://spi-analytics-api."+acquirerCode+".mspenv.io/v1/report-transaction";

            Map<String, Object> message = transactionReport.toMessage();

            AsyncHttpClient client = Dsl.asyncHttpClient();
            BoundRequestBuilder getRequest = client.preparePost(transactionServiceUri);
            getRequest.addHeader("Content-Type", "application/json");
            getRequest.setBody(GSON.toJson(message));
            getRequest.addHeader(ApiKeyHeader, apiKey);

            return getRequest.execute();
        }


    }