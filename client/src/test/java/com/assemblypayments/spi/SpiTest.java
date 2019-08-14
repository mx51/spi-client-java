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
        MessageStamp messageStamp = new MessageStamp("", null, 0);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);
        SpiClientTestUtils.setInstanceField(spi, "spiMessageStamp", messageStamp);

        // act
        spi.setPosId(posId);
        Object value = SpiClientTestUtils.getInstanceField(spi, "posId");

        // assert
        Assert.assertNotEquals(posId, String.valueOf(value));
        Assert.assertEquals(lengthOfPosId, String.valueOf(value).length());
    }

    @Test
    public void testSpiInitateOnInvalidLengthForPosIdIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "12345678901234567";
        final int lengthOfPosId = 16;
        Spi spi = new Spi(posId, "", "", null);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);

        // act
        Object value = SpiClientTestUtils.getInstanceField(spi, "posId");

        // assert
        Assert.assertNotEquals(posId, String.valueOf(value));
        Assert.assertEquals(lengthOfPosId, String.valueOf(value).length());
    }

    @Test
    public void testSetPosIdOnValidCharactersIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "RamenPos@";
        final Pattern regexItemsForPosId = Pattern.compile("[a-zA-Z0-9]*$");
        Spi spi = new Spi("", "", "", null);
        MessageStamp messageStamp = new MessageStamp("", null, 0);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);
        SpiClientTestUtils.setInstanceField(spi, "spiMessageStamp", messageStamp);

        // act
        spi.setPosId(posId);
        Object value = SpiClientTestUtils.getInstanceField(spi, "posId");

        // assert
        Assert.assertEquals(posId, value);
        Assert.assertFalse(regexItemsForPosId.matcher(posId).matches());
        Assert.assertEquals(regexItemsForPosId.matcher(posId).matches(), regexItemsForPosId.matcher(String.valueOf(value)).matches());
    }

    @Test
    public void testSpiInitateOnValidCharactersForPosIdIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String posId = "RamenPos@";
        final Pattern regexItemsForPosId = Pattern.compile("[a-zA-Z0-9]*$");
        Spi spi = new Spi(posId, "", "", null);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);

        // act
        Object value = SpiClientTestUtils.getInstanceField(spi, "posId");

        // assert
        Assert.assertEquals(posId, value);
        Assert.assertFalse(regexItemsForPosId.matcher(posId).matches());
        Assert.assertEquals(regexItemsForPosId.matcher(posId).matches(), regexItemsForPosId.matcher(String.valueOf(value)).matches());
    }

    @Test
    public void testSetEftposAddressOnValidCharactersIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String eftposAddress = "10.20";
        final Pattern regexItemsForEftposAddress = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
        Spi spi = new Spi("", "", "", null);
        Connection conn = new Connection(eftposAddress);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);
        SpiClientTestUtils.setInstanceField(spi, "conn", conn);

        // act
        spi.setEftposAddress(eftposAddress);
        Object value = SpiClientTestUtils.getInstanceField(spi, "eftposAddress");
        value = String.valueOf(value).replaceAll("ws://", "");

        // assert
        Assert.assertFalse(regexItemsForEftposAddress.matcher(eftposAddress).matches());
        Assert.assertEquals(eftposAddress, value);
        Assert.assertEquals(regexItemsForEftposAddress.matcher(eftposAddress).matches(), regexItemsForEftposAddress.matcher(String.valueOf(value)).matches());
    }

    @Test
    public void testSpiInitateOnValidCharactersForEftposAddressIsSet() throws Spi.CompatibilityException, IllegalAccessException {
        // arrange
        final String eftposAddress = "10.20";
        final Pattern regexItemsForEftposAddress = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
        Spi spi = new Spi("", "", eftposAddress, null);
        SpiClientTestUtils.setInstanceField(spi, "currentStatus", SpiStatus.UNPAIRED);

        Object value = SpiClientTestUtils.getInstanceField(spi, "eftposAddress");
        value = String.valueOf(value).replaceAll("ws://", "");

        // assert
        Assert.assertFalse(regexItemsForEftposAddress.matcher(eftposAddress).matches());
        Assert.assertEquals(eftposAddress, value);
        Assert.assertEquals(regexItemsForEftposAddress.matcher(eftposAddress).matches(), regexItemsForEftposAddress.matcher(String.valueOf(value)).matches());
    }
}
