package com.assemblypayments.spi;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

class Connection {

    private static final Logger LOG = LoggerFactory.getLogger("spi");

    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(4);
    private EventHandler eventHandler;
    private State state;
    private String address;
    private WebSocket webSocket;
    private OkHttpClient httpClient;

    public Connection(String address) {
        this.address = address;
        this.state = State.DISCONNECTED;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public boolean isConnected() {
        return state == State.CONNECTED;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
        if (eventHandler != null) eventHandler.onConnectionStateChanged(state);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void connect() {
        // already connected or connecting. disconnect first.
        if (state == State.CONNECTED || state == State.CONNECTING) return;

        // This one is non-blocking
        setState(State.CONNECTING);

        // Check address protocol prefix
        final String prefix = "ws://";
        String address = getAddress();
        if (!address.startsWith(prefix)) {
            address = prefix + address;
        }

        // Create a new socket instance specifying the url, SPI protocol and web socket to use.
        // This will create a TCP/IP socket connection to the provided URL and perform HTTP web socket negotiation
        webSocket = httpClient.newWebSocket(new Request.Builder()
                .url(address)
                .addHeader("Sec-WebSocket-Protocol", "spi." + Spi.PROTOCOL_VERSION)
                .build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Connection.this.onOpen();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                onMessageReceived(text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Connection.this.onClose();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (t instanceof SocketTimeoutException) {
                    // Existing users may be checking for TimeoutException,
                    // so this is required for backwards compatibility.
                    t = new TimeoutException(CONNECTION_TIMEOUT);
                }
                Connection.this.onError(t);
                if (state == State.CONNECTING) {
                    onClose();
                }
            }
        });
    }

    public void send(String message) {
        webSocket.send(message);
    }

    public void disconnect() {
        if (this.state != State.DISCONNECTED) {
            closeSession(1001);
            onClose();
        }
    }

    public void dispose() {
        disconnect();
    }

    private void onOpen() {
        setState(State.CONNECTED);
    }

    private void onClose() {
        webSocket = null;
        setState(State.DISCONNECTED);
    }

    private void onMessageReceived(String message) {
        if (eventHandler != null) eventHandler.onMessageReceived(message);
    }

    private void onError(Throwable thr) {
        if (eventHandler != null) eventHandler.onError(thr);
    }

    private void closeSession(int code) {
        if (webSocket != null) {
            webSocket.close(code, null);
        }
    }

    public enum State {CONNECTED, DISCONNECTED, CONNECTING}

    public interface EventHandler {
        void onConnectionStateChanged(State state);

        void onMessageReceived(String message);

        void onError(Throwable thr);
    }

    /**
     * Exception raised when connection has timed out.
     */
    public static class TimeoutException extends RuntimeException {

        private TimeoutException(long timeoutMillis) {
            super("Connection timeout of " + timeoutMillis + "ms has been reached.");
        }
    }

}
