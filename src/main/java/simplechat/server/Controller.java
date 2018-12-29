package simplechat.server;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private SimpleChat simpleChat;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    private ListView listView;

    @FXML
    private Text actionTarget = null;

    @FXML
    protected void handleMessageButtonAction(ActionEvent event) {
    }

    @FXML
    protected void handleRemoveButtonAction(ActionEvent event) {
    }

    public void initialize() {
        this.simpleChat = new SimpleChat("localhost",5050);
        this.scheduledExecutorService.submit(clearText);
    }

    public void stop() {
        this.simpleChat.stop();
    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        this.textArea.setText(text);
    }

    public void addUser(String user) {
        ObservableList<String> items=this.listView.getItems();
        items.add(user);
        this.listView.setItems(items);

    }

    public void removeUser(String user) {
        ObservableList<String> items=this.listView.getItems();
        for(int i=0;i<items.size();i++){
            if(items.get(i).equals("user")){
                items.remove(i);
            }
        }
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
