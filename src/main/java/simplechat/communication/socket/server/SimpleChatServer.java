package simplechat.communication.socket.server;

import com.sun.org.apache.bcel.internal.generic.IFNE;
import simplechat.communication.MessageProtocol;
import simplechat.server.SimpleChat;

import static java.util.logging.Level.*;
import static simplechat.communication.MessageProtocol.Commands.EXIT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * SimpleChatServer listens to incoming SimpleChatClients with the choosen communication protocol and initiates a UI.
 * <br>
 * Default settings for the main attributes will be: host="localhost" port=5050 and backlog=5
 */
public class SimpleChatServer extends Thread {

    private Integer port = 5050;
    private String host = "localhost";
    private final Integer backlog = 5;
    private ServerSocket serverSocket = null;

    private boolean listening = false;
    private SimpleChat server;

    private ConcurrentHashMap<ClientWorker, String> workerList = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * Initializes host, port and callback for UserInterface interactions.
     *
     * @param host   String representation of hostname, on which the server should listen
     * @param port   Integer for the listening port
     * @param server UserInterface callback reference for user interactions
     */
    public SimpleChatServer(String host, Integer port, SimpleChat server) {
        if (host != null) this.host = host;
        if (port != null) this.port = port;
        this.server = server;
        this.listening=true;
        SimpleChat.serverLogger.log(INFO, "Init: host=" + this.host + " port=" + this.port);
    }

    /**
     * Initiating the ServerSocket with already defined Parameters and starts accepting incoming
     * requests. If client connects to the ServerSocket a new ClientWorker will be created and passed
     * to the ExecutorService for immediate concurrent action.
     */
    public void run() {
        try {
            SimpleChat.serverLogger.log(INFO, "... starting Thread ...");
            this.serverSocket = new ServerSocket(this.port, this.backlog);
        }
        catch(IOException ioE){
            SimpleChat.serverLogger.log(INFO,""+ioE.getMessage());
        }
        try {
            while (listening) {
                SimpleChat.serverLogger.log(INFO, "Listening...");
                ClientWorker c = new ClientWorker(this.serverSocket.accept(), this);
                workerList.put(c, this.server.addClient("Client "+(workerList.size()+1)));
                executorService.execute(c);
                SimpleChat.serverLogger.log(INFO, "ClientWorker added");

            }

        }catch(IOException e){
            SimpleChat.serverLogger.log(INFO,"Error adding Client: \n "+e.getMessage());
            e.printStackTrace();
        }
        SimpleChat.serverLogger.log(INFO,"Shutdown Thread!!!!");

    }

    /**
     * Callback method for client worker to inform server of new message arrival
     *
     * @param plainMessage MessageText sent to server without Client information
     * @param sender       {@link ClientWorker} which received the message
     */
    public void received(String plainMessage, ClientWorker sender) {

        SimpleChat.serverLogger.log(INFO,"Command YES/NO "+plainMessage.startsWith("!"));

       if(plainMessage.startsWith("!")){
           SimpleChat.serverLogger.log(INFO,"Command has been send");
           String[] texts = plainMessage.substring(1).split(" ",2);
           SimpleChat.serverLogger.log(INFO,"Command: "+texts[0]);
           MessageProtocol.Commands cmd = MessageProtocol.getCommand(texts[0]);
           switch (cmd){
               case EXIT:
                   SimpleChat.serverLogger.log(INFO,"Client is shutdowned");
                   this.removeClient(sender);

                   break;
               case PRIVATE:
                   break;
               case CHATNAME:
                   SimpleChat.serverLogger.log(INFO,"Changing Name...");
                   if(texts.length>1) {
                       this.setName(texts[1], sender);
                       sender.send("Your new chatname is "+this.workerList.get(sender));
                   }else {
                       sender.send("Enter a Chatname");
                   }
                   break;
           }


       }else {

           plainMessage = MessageProtocol.textMessage(plainMessage, workerList.get(sender));
           this.server.incomingMessage(plainMessage);
           for (ClientWorker o : workerList.keySet())
                   o.send(plainMessage);
       }

    }

    /**
     * Sending messages to clients through communication framework
     *
     * @param message MessageText with sender ChatName
     */
    public void send(String message) {
        for(ClientWorker o: workerList.keySet())
            o.send(message);

    }

    /**
     * Sending message to one client through communication framework
     *
     * @param message  MessageText with sender ChatName
     * @param receiver ChatName of receiving Client
     */
    public void send(String message, Object receiver) {
    }

    /**
     * ClientWorker has the possibility to change the ChatName. This method asks the UI
     * to rename the Client and stores the returned Name in the ClientWorker-Collection
     *
     * @param chatName new Name of Client
     * @param worker   ClientWorker Thread which was initiating the renaming
     */
    void setName(String chatName, ClientWorker worker) {
        String newName= this.server.renameClient(this.workerList.get(worker),chatName);
        SimpleChat.serverLogger.log(INFO,"NEWNAME: "+newName);
        if(newName != null){
            this.workerList.replace(worker,newName);
            SimpleChat.serverLogger.log(INFO,"Name has been replaced to "+this.workerList.get(worker));
        }else{
            SimpleChat.serverLogger.log(WARNING,"Error while changing the name");
        }

    }

    /**
     * Remove only this worker from the list,
     * shutdown the ClientWorker and also inform GUI about removal.
     *
     * @param worker ClientWorker which should be removed
     */
    void removeClient(ClientWorker worker) {
        this.server.shutdownClient(workerList.get(worker));
        this.workerList.remove(worker);
        worker.shutdown();
    }

    /**
     * Gets the ClientWorker of the given chatName and calls the private Method {@link #removeClient(String)}
     * This method will remove the worker from the list shutdown the ClientWorker and also inform GUI about removal.
     *
     * @param chatName Client name which should be removed
     */
    public void removeClient(String chatName) {
        this.server.removeClient(chatName);
        for (ClientWorker o : workerList.keySet())
             if(workerList.get(o).equals(chatName)){
                 this.workerList.remove(o);
             }


    }

    /**
     * Clean shutdown of all connected Clients.<br>
     * ExecutorService will stop accepting new Thread inits.
     * After notifying all clients, ServerSocket will be closed and ExecutorService will try to shutdown all
     * active ClientWorker Threads.
     */
    public void shutdown() {
        this.listening=false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try{
            for(ClientWorker o: workerList.keySet())
                o.shutdown();
            SimpleChat.serverLogger.log(INFO,"All Clients have been shutdowned");


        }finally {
            try {
                this.serverSocket.close();
                SimpleChat.serverLogger.log(INFO,"Serversocket closed");
            } catch (IOException e) {
                SimpleChat.serverLogger.log(SEVERE,e.getMessage());
            }finally {
                this.executorService.shutdown();
            }
        }
        SimpleChat.serverLogger.log(INFO,"Everthing shutdowned");

    }
}

/**
 * Thread for client socket connection.<br>
 * Every client has to be handled by an own Thread.
 */
class ClientWorker implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;

    private SimpleChatServer callback;
    private boolean listening = true;

    /**
     * Init of ClientWorker-Thread for socket intercommunication
     *
     * @param client   Socket got from ServerSocket.accept()
     * @param callback {@link simplechat.communication.socket.server.SimpleChatServer} reference
     * @throws IOException will be throwed if the init of Input- or OutputStream fails
     */
    ClientWorker(Socket client, SimpleChatServer callback) throws IOException {
        SimpleChat.serverLogger.log(INFO,"ClientWorker-Thread init");
        this.client=client;
        this.callback=callback;
        this.out = new PrintWriter(client.getOutputStream(),true);
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));

    }

    /**
     * MessageHandler for incoming Messages on Client Socket
     * <br>
     * The InputSocket will be read synchronous through readLine()
     * Incoming messages first will be checked if they start with any Commands, which will be executed properly.
     * Otherwise text messages will be delegated to the {@link SimpleChatServer#received(String, ClientWorker)} method.
     */
    @Override
    public void run() {

        SimpleChat.serverLogger.log(INFO,"ClientWorker laeuft");
        while(listening){
            try {
                String message =in.readLine();
                callback.received(message,this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clean shutdown of ClientWorker
     * <br>
     * If listening was still true, we are sending a {@link MessageProtocol.Commands#EXIT} to the client.
     * Finally we are closing all open resources.
     */
    void shutdown() {
        if(listening){
            listening=false;
            SimpleChat.serverLogger.log(INFO, "Shutting down ClientWorker ... listening=" + listening);
            this.send(MessageProtocol.getMessage(EXIT));
            if (client.isConnected()) {

                SimpleChat.serverLogger.log(INFO,"Shutdown Socket");
                try {
                    out.close();
                }

                finally {
                    try {
                        in.close();
                    }catch (IOException ioe){
                        SimpleChat.serverLogger.log(SEVERE,ioe.getMessage());
                    }

                    finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            SimpleChat.serverLogger.log(SEVERE,e.getMessage());
                        }
                    }
                }
            }

        }
    }

    /**
     * Sending message through Socket OutputStream {@link #out}
     *
     * @param message MessageText for Client
     */
    void send(String message) {
        this.out.println(message);
        SimpleChat.serverLogger.log(INFO,"Server send a message: "+message);
    }


}
