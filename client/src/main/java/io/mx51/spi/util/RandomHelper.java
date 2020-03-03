package io.mx51.spi.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class RandomHelper {

    private static final SecureRandom RANDOM_GEN = new SecureRandom();

    private RandomHelper() {
    }

    public static BigInteger randomBigIntMethod1(BigInteger max) {
        // The below code is a technique to generate random positive big integer up to max.
        // You can use different techniques to generate such random number, but be careful
        // about performance. Generating big random numbers could be processing intensive.
        final byte[] bytes = max.toByteArray();
        BigInteger randBigInt;
        do {
            RANDOM_GEN.nextBytes(bytes);
            // Java may give us negative bytes so we need to force sign to be positive
            bytes[bytes.length - 1] &= (byte) 0x7F;
            randBigInt = new BigInteger(bytes);
        } while (randBigInt.compareTo(max) >= 0);
        return randBigInt;
    }

    public static BigInteger randomBigIntMethod2(BigInteger max) {
        final String maxIntString = max.toString(); // this is our maximum number represented as a string, example "2468"
        final StringBuilder randomIntString = new StringBuilder(); // we will build our random number as a string

        boolean below = false; // this indicates whether we've gone below a significant digit yet.

        // we go through the digits from most significant to least significant.
        for (int i = 0, iN = maxIntString.length(); i < iN; i++) {
            final char thisDigit = maxIntString.charAt(i);

            final int maxDigit;
            if (!below) {
                // we need to pick a digit that is equal or smaller than this one.
                maxDigit = thisDigit - '0';
            } else {
                // we've already gone below. So we can pick any digit from 0-9.
                maxDigit = 9;
            }

            int rDigit = RANDOM_GEN.nextInt(10);
            if (rDigit > maxDigit) {
                rDigit = 0;
            }
            randomIntString.append(rDigit);
            if (rDigit < maxDigit) {
                // We've picked a digit smaller than the corresponding one in the maximum.
                // So from now on, any digit is good.
                below = true;
            }
        }
        return new BigInteger(randomIntString.toString());
    }

}
