package io.mx51.spi;

import io.mx51.spi.model.DeviceAddressStatus;
import io.mx51.spi.model.SpiStatus;
import io.mx51.spi.service.DeviceService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class AutoAddressResolutionTest {
    @Test
    public void testSetSerialNumber() throws Spi.CompatibilityException {
        String serialnumber = "111-111-111";
        Spi spi = new Spi("", "", "", null);
        spi.setSerialNumber(serialnumber);
        Assert.assertEquals(serialnumber, spi.getSerialNumber());
    }

    @Test
    public void testSetAutoAddressResolution() throws Spi.CompatibilityException {
        boolean autoAddressResolutionEnable = true;
        Spi spi = new Spi("", "", "", null);
        spi.setCurrentStatus(SpiStatus.UNPAIRED);
        spi.setAutoAddressResolution(autoAddressResolutionEnable);
        Assert.assertEquals(autoAddressResolutionEnable, spi.isAutoAddressResolutionEnabled());
    }

    @Test
    public void testAutoResolveEftposAddressWithIncorectSerialNumber() {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "111-111-111";

        DeviceService deviceService = new DeviceService();
        DeviceAddressStatus addressResponse = deviceService.retrieveDeviceAddress(serialNumber, apiKey, acquirerCode, true);

        Assert.assertNotNull(addressResponse);
        Assert.assertNull(addressResponse.getAddress());
        Assert.assertNull(addressResponse.getLastUpdated());
    }

    @Test
    public void testAutoResolveEftposAddressWithValidSerialNumber() {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "321-404-842";//should be valid serial number

        DeviceService deviceService = new DeviceService();
        DeviceAddressStatus addressResponse = deviceService.retrieveDeviceAddress(serialNumber, apiKey, acquirerCode, true);

        Assert.assertNotNull(addressResponse);
        Assert.assertNotNull(addressResponse.getAddress());
        Assert.assertNotNull(addressResponse.getLastUpdated());
        Assert.assertNotNull(addressResponse.getResponseMessage());
        Assert.assertNotNull(addressResponse.getResponseCode());
    }

    @Test
    public void testGetTerminalAddressOnRegisteredSerialNumber() throws Spi.CompatibilityException {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "328-513-254"; //should be valid serial number

        Spi spi = new Spi("Pos1", serialNumber, "", null);
        spi.setCurrentStatus(SpiStatus.UNPAIRED);
        spi.setTestMode(true);
        spi.setDeviceApiKey(apiKey);
        spi.setAcquirerCode(acquirerCode);

        String ipAddress = spi.getTerminalAddress();

        // assert
        Assert.assertNotNull(ipAddress);
    }

    @Test
    public void testGetTerminalAddressOnNotRegisteredSerialNumber() throws Spi.CompatibilityException {
        String apiKey = "RamenPosDeviceAddressApiKey";
        String acquirerCode = "wbc";
        String serialNumber = "123-456-789";

        Spi spi = new Spi("Pos1", serialNumber, "", null);
        spi.setCurrentStatus(SpiStatus.UNPAIRED);
        spi.setTestMode(true);
        spi.setDeviceApiKey(apiKey);
        spi.setAcquirerCode(acquirerCode);

        String ipAddress = spi.getTerminalAddress();

        // assert
        Assert.assertNull(ipAddress);
    }
}
