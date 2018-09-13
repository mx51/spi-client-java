package com.assemblypayments.spi;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest {

    @Test
    public void testVersion() {
        String version = Spi.getVersion();
        assertNotNull(version);
        assertNotEquals(0, version.length());
    }

}
