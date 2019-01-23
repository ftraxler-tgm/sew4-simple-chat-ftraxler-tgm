package GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import simplechat.server.FXApplication;
import simplechat.server.SimpleChat;

import java.util.ArrayList;

public class FXApplicationMockTest extends ApplicationTest {

    private Parent root;
    private ObservableList<String> text = FXCollections.observableArrayList();
    private MockClient mockClient;
    private static SimpleChat simpleChat;
    private static int iteration = 0;
    private static ArrayList<MockClient> arrayMockClient;

    @Override
    public void start(Stage stage) throws Exception {

        for (; iteration == 0; ++iteration) {
            simpleChat = new SimpleChat("localhost", 8888);
            simpleChat.listen();
            arrayMockClient = new ArrayList<MockClient>();
        }

        FXApplication fxApplication = new FXApplication();
        fxApplication.setSimpleChat(simpleChat);

        fxApplication.start(stage);

        sleep(1000);
        mockClient = new MockClient();
        arrayMockClient.add(mockClient);
    }


    @Before
    public void setUp() throws Exception {
    }


    @After
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }


    @Test
    public void testIsClientConnected() {
        // is the latest client connected?
        assert (mockClient.cS.isConnected());
    }

    @Test
    public void testClientSentMessage() {
        String msg = "This is a test!";
        mockClient.send(msg);
        TextArea textArea = (TextArea) GuiTest.find("#textArea");
        sleep(1000);
        String text = textArea.getText();
        assert (text.endsWith("[Client] "+msg));
    }

    @Test
    public void testChangeName(){
        String name = "Franzi";
        mockClient.send("!CHATNAME " + name);
        sleep(2000);
        String[] actual = simpleChat.getClients();
        assert(actual[0].equals(name));
    }

    @Test
    public void testClientDisconnect(){
        String exit = "!EXIT";
        mockClient.send(exit);
        sleep(2000);
        assert(mockClient.isConnected());
    }

    private class MockClient {
        public simplechat.client.SimpleChat cS;

        public MockClient(){
            this.cS = new simplechat.client.SimpleChat("", "localhost", 8888);
        }

        public void send(String msg) {
            cS.sendMessage(msg);
        }

        public boolean isConnected(){
            return cS.isConnected();
        }
    }
}
