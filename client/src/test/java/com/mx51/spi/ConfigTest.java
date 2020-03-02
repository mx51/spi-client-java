package io.mx51.spi;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void testVersion() {
        String version = Spi.getVersion();
        Assert.assertNotNull(version);
        Assert.assertNotEquals(0, version.length());
    }

}
