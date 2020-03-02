package io.mx51.spi;

import io.mx51.spi.model.MessageStamp;
import io.mx51.spi.model.SpiStatus;
import io.mx51.spi.model.SubmitAuthCodeResult;
import org.junit.Assert;
import org.junit.Test;

public class SpiTest {
    @Test
    public void testSetPosIdOnInvalidLengthIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "12345678901234567";
        final String eftposAddress = "10.20.30.40";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.spiMessageStamp = new MessageStamp("", null, 0);

        // act
        spi.setPosId(posId);

        // assert
        Assert.assertNotEquals(posId, spi.posId);
    }

    @Test
    public void testStartOnInvalidLengthForPosIdIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "12345678901234567";
        final String eftposAddress = "10.20.30.40";
        final String posVendorId = "mx51";
        final String posVersion = "2.6.3";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.setPosInfo(posVendorId, posVersion);

        // act
        spi.start();

        // assert
        Assert.assertNotEquals(posId, spi.posId);
    }

    @Test
    public void testSetPosIdOnValidCharactersIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos";
        final String eftposAddress = "10.20.30.40";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.spiMessageStamp = new MessageStamp("", null, 0);

        // act
        spi.setPosId(posId);

        // assert
        Assert.assertEquals(posId, spi.posId);
    }

    @Test
    public void testStartOnValidCharactersForPosIdIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos";
        final String eftposAddress = "10.20.30.40";
        final String posVendorId = "mx51";
        final String posVersion = "2.6.3";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.setPosInfo(posVendorId, posVersion);

        // act
        spi.start();

        // assert
        Assert.assertEquals(posId, spi.posId);
    }

    @Test
    public void testSetPosIdOnInvalidCharactersIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos@";
        final String eftposAddress = "10.20.30.40";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.spiMessageStamp = new MessageStamp("", null, 0);

        // act
        spi.setPosId(posId);

        // assert
        Assert.assertNotEquals(posId, spi.posId);
    }

    @Test
    public void testStartOnInvalidCharactersForPosIdIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos@";
        final String eftposAddress = "10.20.30.40";
        final String posVendorId = "mx51";
        final String posVersion = "2.6.3";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.setPosInfo(posVendorId, posVersion);

        // act
        spi.start();

        // assert
        Assert.assertNotEquals(posId, spi.posId);
    }

    @Test
    public void testSetEftposAddressOnValidCharactersIsSet() throws Spi.CompatibilityException {
        // arrange
        final String eftposAddress = "10.20.30.40";
        Spi spi = new Spi("", "", "", null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.conn = new Connection(eftposAddress);

        // act
        spi.setEftposAddress(eftposAddress);
        final String spiEftposAddress = spi.eftposAddress.replaceAll("ws://", "");

        // assert
        Assert.assertEquals(eftposAddress, spiEftposAddress);
    }

    @Test
    public void testStartOnValidCharactersForEftposAddressIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos";
        final String eftposAddress = "10.20.30.40";
        final String posVendorId = "mx51";
        final String posVersion = "2.6.3";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.setPosInfo(posVendorId, posVersion);

        // act
        spi.start();
        final String spiEftposAddress = spi.eftposAddress.replaceAll("ws://", "");

        // assert
        Assert.assertEquals(eftposAddress, spiEftposAddress);
    }

    @Test
    public void testSetEftposAddressOnInvalidCharactersIsSet() throws Spi.CompatibilityException {
        // arrange
        final String eftposAddress = "10.20.30";
        Spi spi = new Spi("", "", "", null);
        spi.currentStatus = SpiStatus.UNPAIRED;
        spi.conn = new Connection(eftposAddress);

        // act
        spi.setEftposAddress(eftposAddress);
        final String spiEftposAddress = spi.eftposAddress.replaceAll("ws://", "");

        // assert
        Assert.assertNotEquals(eftposAddress, spiEftposAddress);
    }

    @Test
    public void testStartOnInvalidCharactersForEftposAddressIsSet() throws Spi.CompatibilityException {
        // arrange
        final String posId = "RamenPos";
        final String eftposAddress = "10.20.30";
        final String posVendorId = "mx51";
        final String posVersion = "2.6.3";
        Spi spi = new Spi(posId, "", eftposAddress, null);
        spi.setPosInfo(posVendorId, posVersion);

        // act
        spi.start();

        // assert
        Assert.assertNotEquals(eftposAddress, spi.eftposAddress);
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
