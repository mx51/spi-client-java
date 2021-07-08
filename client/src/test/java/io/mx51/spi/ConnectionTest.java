package io.mx51.spi;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConnectionTest {
    @Test
    public void TestConnectionStatusChanged_Disconnected() throws InterruptedException {

        final AutoResetEvent are = new AutoResetEvent(false);

        Connection conn = new Connection("ws://127.0.0.1");

        final List<Connection.State> connectionStateList = new ArrayList<>();
        conn.setEventHandler(new Connection.EventHandler() {
            @Override
            public void onConnectionStateChanged(Connection.State state) {
                connectionStateList.add(state);
            }

            @Override
            public void onMessageReceived(String message) {
            }

            @Override
            public void onError(Throwable thr) {
                Assert.assertNotNull(thr.getMessage());
            }
        });

        conn.connect();

//        conn.disconnect();

        are.waitOne(20);

        Assert.assertEquals(Connection.State.CONNECTING, connectionStateList.get(0));
//        Assert.assertEquals(Connection.State.DISCONNECTED, connectionStateList.get(1));
        Assert.assertFalse(conn.isConnected());
    }
}