package simplechat.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleChatTest {
    private SimpleChat simpleChat;

    @Before
    public void setUp() throws Exception {
        simpleChat = new SimpleChat(null, null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getClients() {
        simpleChat.addClient("");
        assertEquals(simpleChat.getClients()[0], "Client");
    }

    @Test
    public void addClient() {
        simpleChat.addClient("");
        simpleChat.addClient("");
        simpleChat.addClient("");
        simpleChat.addClient("Franz");
        simpleChat.addClient("Franz");
        assertEquals(simpleChat.getClients()[0], "Client");
        assertEquals(simpleChat.getClients()[1], "Client#1");
        assertEquals(simpleChat.getClients()[2], "Client#2");
        assertEquals(simpleChat.getClients()[3], "Franz");
        assertEquals(simpleChat.getClients()[4], "Franz#1");
    }

    @Test
    public void renameClient() {
        simpleChat.addClient("");
        simpleChat.addClient("");
        simpleChat.renameClient("Client#1", "Franz");
        assertEquals(simpleChat.getClients()[1], "Franz");
    }

    @Test
    public void removeClient() {
        simpleChat.addClient("");
        simpleChat.addClient("");
        simpleChat.addClient("");
        simpleChat.removeClient("Client");
        assertEquals(simpleChat.getClients()[0], "Client#1");
    }
}
