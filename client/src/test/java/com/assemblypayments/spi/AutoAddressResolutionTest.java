package com.assemblypayments.spi;

import com.assemblypayments.spi.model.DeviceAddressStatus;
import com.assemblypayments.spi.service.DeviceService;
import org.junit.Test;

import static org.junit.Assert.*;

public class AutoAddressResolutionTest {
    @Test
    public void testSetSerialNumber() throws Spi.CompatibilityException {
        String serialnumber = "111-111-111";
        Spi spi = new Spi("", "", "", null);
        spi.setSerialNumber(serialnumber);
        assertEquals(serialnumber, spi.getSerialNumber());
    }

    @Test
    public void testSetAutoAddressResolution() throws Spi.CompatibilityException {
        boolean autoAddressResolutionEnable = true;
        Spi spi = new Spi("", "", "", null);
        spi.setAutoAddressResolution(autoAddressResolutionEnable);
        assertEquals(autoAddressResolutionEnable, spi.isAutoAddressResolutionEnabled());
    }

    @Test
    public void testAutoResolveEftposAddressWithIncorectSerialNumber() {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "111-111-111";

        DeviceService deviceService = new DeviceService();
        DeviceAddressStatus addressResponse = deviceService.retrieveService(serialNumber, apiKey, acquirerCode, true);

        assertNotNull(addressResponse);
        assertNull(addressResponse.getAddress());
        assertNull(addressResponse.getLastUpdated());
    }

    @Test
    public void testAutoResolveEftposAddressWithValidSerialNumber() {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "321-404-842";//should be valid serial number

        DeviceService deviceService = new DeviceService();
        DeviceAddressStatus addressResponse = deviceService.retrieveService(serialNumber, apiKey, acquirerCode, true);

        assertNotNull(addressResponse);
        assertNotNull(addressResponse.getAddress());
        assertNotNull(addressResponse.getLastUpdated());
    }
}
