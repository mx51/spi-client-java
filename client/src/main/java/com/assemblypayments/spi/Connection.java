package com.assemblypayments.spi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.tyrus.client.ClientManager;
import org.jetbrains.annotations.NotNull;

import javax.websocket.*;
import javax.websocket.CloseReason.CloseCodes;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class Connection {

    private static final Logger LOG = LogManager.getLogger("spi");

    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(8);
    private EventHandler eventHandler;
    private State state;
    private String address;
    private Session wsSession;
    private Timer connectionTimer;

    public Connection(String address) {
        this.address = address;
        this.state = State.DISCONNECTED;
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

    public void connect() throws DeploymentException {
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

        try {
            // Create a new socket instance specifying the url, SPI protocol and web socket to use.
            // This will create a TCP/IP socket connection to the provided URL and perform HTTP web socket negotiation
            final Future<Session> sessionFuture = ClientManager.createClient().asyncConnectToServer(
                    new Endpoint() {
                        @Override
                        public void onOpen(Session session, EndpointConfig config) {
                            Connection.this.onOpen(session);
                        }

                        @Override
                        public void onClose(Session session, CloseReason closeReason) {
                            Connection.this.onClose();
                        }

                        @Override
                        public void onError(Session session, Throwable thr) {
                            Connection.this.onError(thr);
                        }
                    },
                    ClientEndpointConfig.Builder.create()
                            .preferredSubprotocols(Collections.singletonList("spi." + Spi.PROTOCOL_VERSION))
                            .build(),
                    URI.create(address));

            // Wait for a connection...
            cancelConnectionTimer();
            connectionTimer = new Timer();
            connectionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sessionFuture.cancel(true);
                    onError(new TimeoutException(CONNECTION_TIMEOUT));
                    onClose();
                }
            }, CONNECTION_TIMEOUT);
        } catch (DeploymentException e) {
            onError(e);
            onClose();
            throw e;
        }
    }

    public void disconnect() {
        if (this.state == State.DISCONNECTED) return;
        cancelConnectionTimer();
        closeSession(CloseCodes.GOING_AWAY);
        onClose();
    }

    public void send(String message) {
        wsSession.getAsyncRemote().sendText(message);
    }

    public void dispose() {
        disconnect();
    }

    private void onOpen(Session session) {
        cancelConnectionTimer();

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                onMessageReceived(message);
            }
        });

        wsSession = session;
        setState(State.CONNECTED);
    }

    private void onClose() {
        wsSession = null;
        setState(State.DISCONNECTED);
    }

    private void onMessageReceived(String message) {
        if (eventHandler != null) eventHandler.onMessageReceived(message);
    }

    private void onError(Throwable thr) {
        if (eventHandler != null) eventHandler.onError(thr);
    }

    private void cancelConnectionTimer() {
        if (connectionTimer != null) {
            connectionTimer.cancel();
            connectionTimer = null;
        }
    }

    private void closeSession(@NotNull CloseReason.CloseCode code) {
        if (wsSession != null) {
            try {
                wsSession.close(new CloseReason(code, null));
            } catch (IOException e) {
                LOG.warn("Error closing session", e);
            }
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
