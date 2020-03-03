package io.mx51.spi.model;

/**
 * Represents the 3 Pairing statuses that the Spi instance can be in.
 */
public enum SpiStatus {

    /**
     * Paired and Connected
     */
    PAIRED_CONNECTED,

    /**
     * Paired but trying to establish a connection
     */
    PAIRED_CONNECTING,

    /**
     * Unpaired
     */
    UNPAIRED

}
