package simplechat.client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;

public class Controller {

    private SimpleChat simpleChat;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    private Text actionTarget = null;

    @FXML
    public void onEnter(ActionEvent ae){
        ae.consume();
        SimpleChat.clientLogger.log(INFO,"Enter Pressed");
        this.sendMessage();
        this.textField.setText("");
    }
    @FXML
    protected void handleMessageButtonAction(ActionEvent event) {
        event.consume();
        this.sendMessage();
        simpleChat.clientLogger.log(INFO,"Send Button pressed");
        this.textField.setText("");
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
        scheduledExecutorService.shutdown();

    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat=simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        SimpleChat.clientLogger.log(INFO,"Adding Message "+text);
        this.textArea.setText(this.textArea.getText()+"\n"+text);
        this.textArea.appendText("");
    }

    public void sendMessage() {
        this.simpleChat.sendMessage(this.textField.getText());

    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
