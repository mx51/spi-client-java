package io.mx51.spi.util;

/**
 * Helper methods for language and platform-specific operations.
 */
public final class SystemHelper {

    private SystemHelper() {
    }

    /**
     * Clears contents of the console.
     */
    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
    }

}
