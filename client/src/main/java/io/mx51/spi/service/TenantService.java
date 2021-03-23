package io.mx51.spi.service;

import io.mx51.spi.model.Tenants;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TenantService {

    private static final Logger LOG = LoggerFactory.getLogger("spi");
    private static final Gson GSON = new Gson();
    private final OkHttpClient okHttpClient;
    private static final long CONNECTION_TIMEOUT_SECS = 8;

    public TenantService() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT_SECS, TimeUnit.SECONDS)
                .build();
    }

    public Tenants retrieveService(String posVendorId, String apiKey, String countryCode) {
        String tenantServiceUri;
        Tenants tenants = null;

        tenantServiceUri = String.format("https://spi.integration.mspenv.io/tenants?country-code=%spos-vendor-id=%s&api-key=%s", countryCode, posVendorId, apiKey);

        try {
            Request request = new Request.Builder()
                    .url(tenantServiceUri)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.body() != null) {
                tenants = new Gson().fromJson(response.body().string(), Tenants.class);
            }

            return tenants;
        } catch (IOException ex) {
            LOG.error("Error with TenantService: " + ex.getMessage());
            return null;
        }
    }
}
