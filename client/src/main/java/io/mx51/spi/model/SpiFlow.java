package io.mx51.spi.model;

/**
 * The Spi instance can be in one of these flows at any point in time.
 */
public enum SpiFlow {

    /**
     * Currently going through the Pairing Process Flow.
     * Happens during the Unpaired SpiStatus.
     */
    PAIRING,

    /**
     * Currently going through the transaction Process Flow.
     * Cannot happen in the Unpaired SpiStatus.
     */
    TRANSACTION,

    /**
     * Not in any of the other states.
     */
    IDLE

}
