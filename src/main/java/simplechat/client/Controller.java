package simplechat.client;

import javafx.application.Platform;
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
    protected void handleMessageButtonAction(ActionEvent event) {
        this.sendMessage();
        simpleChat.clientLogger.log(INFO,"Button Pressed");
    }

    public void initialize() {

        simpleChat = new SimpleChat("ftraxler","localhost",5050);
        this.scheduledExecutorService.submit(clearText);
    }

    public void stop() {
        this.simpleChat.stop();
        scheduledExecutorService.shutdown();

    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat=simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        this.textArea.setText(this.textArea.getText()+"\n"+text);
    }

    public void sendMessage() {
        this.simpleChat.sendMessage(this.textField.getText());
        this.updateTextAreaWithText(this.textField.getText());
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
