package com.assemblypayments.spi;

import com.assemblypayments.spi.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class SpiTest {
    @Test
    public void testSetPosIdOnInvalidLengthIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "12345678901234567";
        final int lengthOfPosId = 16;
        Spi spi = new Spi("", "", "", null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.spiMessageStamp = new MessageStamp("", null, 0);

        // act
        spi.setPosId(posId);

        // assert
        Assert.assertNotEquals(posId, spi.posId);
        Assert.assertEquals(lengthOfPosId, spi.posId.length());
    }

    @Test
    public void testSpiInitateOnInvalidLengthForPosIdIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "12345678901234567";
        final int lengthOfPosId = 16;

        // act
        Spi spi = new Spi(posId, "", "", null);

        // assert
        Assert.assertNotEquals(posId, spi.posId);
        Assert.assertEquals(lengthOfPosId, spi.posId.length());
    }

    @Test
    public void testSetPosIdOnValidCharactersIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "RamenPos@";
        final Pattern regexItemsForPosId = Pattern.compile("[a-zA-Z0-9]*$");
        Spi spi = new Spi("", "", "", null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.spiMessageStamp = new MessageStamp("", null, 0);

        // act
        spi.setPosId(posId);

        // assert
        Assert.assertEquals(posId, spi.posId);
        Assert.assertFalse(regexItemsForPosId.matcher(posId).matches());
        Assert.assertEquals(regexItemsForPosId.matcher(posId).matches(), regexItemsForPosId.matcher(spi.posId).matches());
    }

    @Test
    public void testSpiInitateOnValidCharactersForPosIdIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "RamenPos@";
        final Pattern regexItemsForPosId = Pattern.compile("[a-zA-Z0-9]*$");

        // act
        Spi spi = new Spi(posId, "", "", null);

        // assert
        Assert.assertEquals(posId, spi.posId);
        Assert.assertFalse(regexItemsForPosId.matcher(posId).matches());
        Assert.assertEquals(regexItemsForPosId.matcher(posId).matches(), regexItemsForPosId.matcher(spi.posId).matches());
    }

    @Test
    public void testSetEftposAddressOnValidCharactersIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String eftposAddress = "10.20";
        final Pattern regexItemsForEftposAddress = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
        Spi spi = new Spi("", "", "", null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.conn = new Connection(eftposAddress);

        // act
        spi.setEftposAddress(eftposAddress);
        final String spiEftposAddress = spi.eftposAddress.replaceAll("ws://", "");

        // assert
        Assert.assertFalse(regexItemsForEftposAddress.matcher(eftposAddress).matches());
        Assert.assertEquals(eftposAddress, spiEftposAddress);
        Assert.assertEquals(regexItemsForEftposAddress.matcher(eftposAddress).matches(), regexItemsForEftposAddress.matcher(spiEftposAddress).matches());
    }

    @Test
    public void testSpiInitateOnValidCharactersForEftposAddressIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String eftposAddress = "10.20";
        final Pattern regexItemsForEftposAddress = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");

        // act
        Spi spi = new Spi("", "", eftposAddress, null);
        final String spiEftposAddress = spi.eftposAddress.replaceAll("ws://", "");

        // assert
        Assert.assertFalse(regexItemsForEftposAddress.matcher(eftposAddress).matches());
        Assert.assertEquals(eftposAddress, spiEftposAddress);
        Assert.assertEquals(regexItemsForEftposAddress.matcher(eftposAddress).matches(), regexItemsForEftposAddress.matcher(spiEftposAddress).matches());
    }

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
