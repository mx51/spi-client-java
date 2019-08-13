package com.assemblypayments.spi;

import org.junit.Assert;
import org.junit.Test;

public class SpiTest {
    @Test
    public void testRetriesBeforeResolvingDeviceAddressOnValidValueChecked() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final int retriesBeforeResolvingDeviceAddress = 3;

        // act
        Spi spi = new Spi("", "", "", null);

        // assert
        Assert.assertEquals(retriesBeforeResolvingDeviceAddress, SpiClientTestUtils.getInstanceField(spi, "retriesBeforeResolvingDeviceAddress"));
    }
}
