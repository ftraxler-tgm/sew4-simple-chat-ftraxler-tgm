package simplechat.server;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.logging.Level;

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
        event.consume();
        this.simpleChat.sendMessage(this.textField.getText());
        this.textField.setText("");
    }

    @FXML
    protected void handleRemoveButtonAction(ActionEvent event) {
        event.consume();
        String name = (String)listView.getSelectionModel().getSelectedItem();
        SimpleChat.serverLogger.log(Level.INFO,"Removing... :"+name);
        this.simpleChat.shutdownClient(name);


    }

    public void initialize() {

        //Wenn sich etwas an der TextArea ändert, wird dieser ChangeListener aktiv
        this.textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //Durch MAX_VALUE wird immer nach unten gescrollt, mit MIN_VALUE würde es nach oben scrollen.
                textArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    public void stop() {

        this.simpleChat.stop();
        this.scheduledExecutorService.shutdown();

    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        SimpleChat.serverLogger.log(Level.INFO,"Textarea:"+this.textArea.getText());
        SimpleChat.serverLogger.log(Level.INFO,"New Text:"+text);
        this.textArea.setText(this.textArea.getText()+"\n"+text);
        this.textArea.appendText("");
    }

    public void addUser(String user) {
        ObservableList<String> users = this.listView.getItems();

        Platform.runLater(() ->
        {
            users.add(user);
            this.listView.setItems(users);
        });



    }

    public void removeUser(String user) {

        ObservableList<String> users = this.listView.getItems();

        Platform.runLater(() ->
        {
            for(int i=0;i<users.size();i++){
                if(users.get(i).equals(user)){
                    users.remove(i);

                }
            }
            this.listView.setItems(users);
        });
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
