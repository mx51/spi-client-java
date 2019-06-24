package com.assemblypayments.spi;

import com.assemblypayments.spi.model.SubmitAuthCodeResult;
import org.junit.Assert;
import org.junit.Test;

public class SpiTest {
    @Test
    public void testSubmitAuthCode() throws Spi.CompatibilityException {
        String authCode = "123456";
        boolean expectedValidFormat = false;
        String expectedMessage = "Was not waiting for one.";
        Spi spi = new Spi("", "", "", null);
        SubmitAuthCodeResult submitAuthCodeResult = spi.submitAuthCode(authCode);

        Assert.assertEquals(submitAuthCodeResult.isValidFormat(), expectedValidFormat);
        Assert.assertEquals(submitAuthCodeResult.getMessage(), expectedMessage);
    }

    @Test
    public void testSubmitAuthCode_Length() throws Spi.CompatibilityException {
        String authCode = "1234567";
        boolean expectedValidFormat = false;
        String expectedMessage = "Not a 6-digit code.";
        Spi spi = new Spi("", "", "", null);
        SubmitAuthCodeResult submitAuthCodeResult = spi.submitAuthCode(authCode);

        Assert.assertEquals(submitAuthCodeResult.isValidFormat(), expectedValidFormat);
        Assert.assertEquals(submitAuthCodeResult.getMessage(), expectedMessage);
    }
}
