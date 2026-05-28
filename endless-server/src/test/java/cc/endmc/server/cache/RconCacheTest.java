package cc.endmc.server.cache;

import cc.endmc.server.common.rconclient.RconClient;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RconCacheTest {

    @After
    public void tearDown() {
        RconCache.clear();
    }

    @Test
    public void removeShouldEvictAndCloseClient() throws Exception {
        RconClient client = newTestClient();
        RconCache.put("server-1", client);

        RconCache.remove("server-1");

        assertFalse(RconCache.containsKey("server-1"));
        assertFalse(client.isSocketChannelOpen());
    }

    @Test
    public void closeAllShouldCloseAndClearCache() throws Exception {
        RconClient firstClient = newTestClient();
        RconClient secondClient = newTestClient();
        RconCache.put("server-1", firstClient);
        RconCache.put("server-2", secondClient);

        RconCache.closeAll();

        assertTrue(RconCache.isEmpty());
        assertFalse(firstClient.isSocketChannelOpen());
        assertFalse(secondClient.isSocketChannelOpen());
    }

    private RconClient newTestClient() throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        try {
            Constructor<RconClient> constructor = RconClient.class.getDeclaredConstructor(
                    String.class,
                    int.class,
                    String.class,
                    SocketChannel.class,
                    boolean.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance("127.0.0.1", 25575, "password", socketChannel, false);
        } catch (Exception e) {
            socketChannel.close();
            throw e;
        }
    }
}