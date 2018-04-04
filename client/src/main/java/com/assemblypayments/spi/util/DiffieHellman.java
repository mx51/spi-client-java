package com.assemblypayments.spi.util;

import java.math.BigInteger;

/**
 * This class implements the Diffie-Hellman algorithm using BigIntegers.
 * It can do the 3 main things:
 * 1. Generate a random Private Key for you.
 * 2. Generate your Public Key based on your Private Key.
 * 3. Generate the Secret given their Public Key and your Private Key
 * p and g are the shared constants for the algorithm, aka primeP and primeG.
 */
public final class DiffieHellman {

    private DiffieHellman() {
    }

    /**
     * Generates a random private key that you can use.
     *
     * @return Random private key.
     */
    public static BigInteger randomPrivateKey(BigInteger p) {
        final BigInteger max = p.subtract(BigInteger.valueOf(1));
        BigInteger randBigInt = RandomHelper.randomBigIntMethod2(max);

        // The above could give us 0 or 1, but we need min 2. So quick, albeit slightly biasing, cheat below.
        final BigInteger min = BigInteger.valueOf(2);
        if (randBigInt.compareTo(min) < 0) {
            randBigInt = min;
        }

        return randBigInt;
    }

    /**
     * Calculates the public key from a private key.
     *
     * @return Public key.
     */
    public static BigInteger publicKey(BigInteger p, BigInteger g, BigInteger privateKey) {
        // A = g**a mod p
        return g.modPow(privateKey, p);
    }

    /**
     * Calculates the shared secret given their public key (A) and your private key (B).
     *
     * @return Shared secret.
     */
    public static BigInteger secret(BigInteger p, BigInteger theirPublicKey, BigInteger yourPrivateKey) {
        // s = A**b mod p
        return theirPublicKey.modPow(yourPrivateKey, p);
    }

}
