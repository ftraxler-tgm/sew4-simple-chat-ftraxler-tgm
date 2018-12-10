package simplechat.communication;

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageProtocolTest {

    @Test
    public void getExitMessage() {
        assertEquals(MessageProtocol.getMessage(MessageProtocol.Commands.EXIT),
                "!" + MessageProtocol.Commands.EXIT);
    }

    @Test
    public void getExitCommandThroughString() {
        assertEquals(MessageProtocol.getCommand("!EXIT"), MessageProtocol.Commands.EXIT);
    }

    @Test
    public void getExitCommandThroughInteger() {
        assertEquals(MessageProtocol.getCommand("!" + MessageProtocol.Commands.EXIT),
                MessageProtocol.Commands.EXIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNotImplementedCommand() {
        MessageProtocol.getCommand("!NOT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSmallWrittenCommand() {
        MessageProtocol.getCommand("!exit");
    }
}
