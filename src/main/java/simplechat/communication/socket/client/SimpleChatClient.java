package simplechat.communication.socket.client;

import simplechat.client.SimpleChat;
import simplechat.communication.MessageProtocol;

import static simplechat.communication.MessageProtocol.Commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static java.util.logging.Level.*;

/**
 * SimpleChatClient connects to SimpleChatServer with the choosen communication protocol and initiates a UI.
 * <br>
 * Default settings for the main attributes will be: name="Client" host="localhost" and port=5050
 */
public class SimpleChatClient extends Thread {

    private String name = null;
    private String host = null;
    private Integer port = null;

    private InetSocketAddress socketAddress;
    private Socket socket = null;
    private PrintWriter out;
    private BufferedReader in;

    private boolean listening = false;
    private String currentMessage;

    private SimpleChat client;

    /**
     * Initializes host, port and callback for UserInterface interactions.
     *
     * @param name   String representation of chatName
     * @param host   String representation of hostname, on which the server should listen
     * @param port   Integer for the listening port
     * @param client UserInterface callback reference for user interactions
     */
    public SimpleChatClient(String name, String host, Integer port, SimpleChat client) {
        if (name != null) this.name = name;
        if (host != null) this.host = host;
        if (port != null) this.port = port;
        SimpleChat.clientLogger.log(INFO, "Init: host=" + this.host + " port="
                + this.port + " chatName=" + this.name);
        this.client = client;
        this.socketAddress = new InetSocketAddress(host,port);





    }

    /**
     * Initiating the Socket with already defined Parameters (host, port). Also a timeout of 2000 ms is set at connect.
     * The {@link java.net.Socket#setKeepAlive(boolean)} is set to true.
     * <br>
     * After activating {@link #listening}, the Chatname will be sent to the Server and the reading loop is started,
     * checking for the {@link BufferedReader#readLine()} and the {@link #listening} flag.
     * <br>
     * In case of an Exception the Thread will be interrupted and if the socket was connected and bound,
     * the {@link #shutdown()} method will be called.
     */
    public void run() {


        SimpleChat.clientLogger.log(INFO, "Run des SimpleChatClients");

        try {
            this.socket=new Socket();
            this.socket.connect(this.socketAddress, 2000);
            this.socket.setKeepAlive(true);

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                SimpleChat.clientLogger.log(SEVERE,e.getMessage()+ " IN/OUT");
            }

            listening = true;

            //SimpleChat.clientLogger.log(INFO, "Name wird an Socket geschickt");
            //out.println(MessageProtocol.getMessage(CHATNAME)+" "+name);

            while(listening){
                if((currentMessage = in.readLine() )!= null) {
                    SimpleChat.clientLogger.log(INFO, "Client got a message: " + currentMessage);
                    this.received();
                }
            }
        } catch(Exception E) {
            SimpleChat.clientLogger.log(SEVERE,E.getMessage()+" SOCKET");

            if (socket.isConnected()&& socket.isBound()) {
                this.shutdown();
            }
        }
    }

    /**
     * Analyzing received messages.
     * <br>
     * If Server sends proper {@link simplechat.communication.MessageProtocol.Commands} this method will act accordingly.
     * <br>
     * {@link simplechat.communication.MessageProtocol.Commands#EXIT} will set listening to false
     * and then calls {@link #shutdown()}
     * <br>
     * If there is now Command (no "!" as first character),
     * the message will be passed to {@link simplechat.client.SimpleChat#incomingMessage(String)}
     */

    private void received() {
        if(isListening()){


            if(currentMessage.startsWith("!")){
                SimpleChat.clientLogger.log(INFO,"Command has been send");
                String[] texts = currentMessage.substring(1).split(" ",2);
                SimpleChat.clientLogger.log(INFO,"Command: "+texts[0]);
                MessageProtocol.Commands cmd = MessageProtocol.getCommand(texts[0]);
                switch (cmd){
                    case EXIT:
                        this.listening=false;
                        this.shutdown();
                    case PRIVATE:
                        break;
                    case CHATNAME:

                        //TODO
                        break;
                }


            }else{
                SimpleChat.clientLogger.log(INFO,"The Client got the message "+this.currentMessage);
                this.client.incomingMessage(this.currentMessage);
            }

        }

    }

    /**
     * Sending message to the server through network
     *
     * @param message Public message for server intercommunication
     */
    public void send(String message) {
        if(listening)
            out.println(message);

    }

    /**
     * Sending message to the server through network for private Message
     *
     * @param message  Private message for client-to-client intercommunication
     * @param chatName Name of receiver
     */
    public void send(String message, String chatName) {


    }

    /**
     * Clean shutdown of Client
     * <br>
     * If listening was still true, we are sending a {@link MessageProtocol.Commands#EXIT} to the server.
     * Finally we are closing all open resources.
     */
    public void shutdown() {
        if (listening) {
            SimpleChat.clientLogger.log(INFO,"Sending SHUTDOWN");
            this.client.sendMessage("!EXIT");
        }else if (socket.isConnected()) {

                SimpleChat.clientLogger.log(INFO,"Shutdown Socket");
                try {

                    out.close();
                }

                finally {
                    try {
                        in.close();
                    }catch (IOException ioe){
                        SimpleChat.clientLogger.log(SEVERE,ioe.getMessage());
                    }

                    finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            SimpleChat.clientLogger.log(SEVERE,e.getMessage());
                        }
                    }
                }
        }


    }

    /**
     * @return True if still listening and online
     */
    public boolean isListening() {
        return listening;
    }
}
