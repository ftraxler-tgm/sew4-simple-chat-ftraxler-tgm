package simplechat.communication.socket.server;

import com.sun.org.apache.bcel.internal.generic.IFNE;
import simplechat.communication.MessageProtocol;
import simplechat.server.SimpleChat;
import sun.java2d.pipe.SpanShapeRenderer;

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
import java.util.concurrent.TimeUnit;


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
        try{
            SimpleChat.serverLogger.log(INFO, "... starting Thread ...");
            this.serverSocket = new ServerSocket(this.port, this.backlog);


            while (listening) {
                SimpleChat.serverLogger.log(INFO, "Listening...");
                ClientWorker c = new ClientWorker(this.serverSocket.accept(), this);
                workerList.put(c, this.server.addClient(""));
                if(!this.executorService.isShutdown())
                    executorService.execute(c);
                SimpleChat.serverLogger.log(INFO, "ClientWorker added"+this.workerList.get(c));

            }

        }catch(IOException e){
            SimpleChat.serverLogger.log(SEVERE,"Error adding Client: \n "+e.getMessage());
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


       if(plainMessage.startsWith("!")){
           SimpleChat.serverLogger.log(INFO,"Command has been send");
           String[] texts = plainMessage.substring(1).split(" ",3);
           SimpleChat.serverLogger.log(INFO,"Command: "+texts[0]);
           try{
               MessageProtocol.Commands cmd = MessageProtocol.getCommand(texts[0]);
               switch (cmd){
                   case EXIT:
                       SimpleChat.serverLogger.log(INFO,"Client is shutdowned");
                       this.removeClient(sender);

                       break;
                   case PRIVATE:
                       SimpleChat.serverLogger.log(INFO,""+texts[2]);
                       if(texts.length>2){
                           if(plainMessage.contains("{")&&plainMessage.contains("}")){

                               String usersnames = plainMessage.substring(plainMessage.indexOf("{"),plainMessage.indexOf("}"));
                               String message = plainMessage.substring((plainMessage.indexOf("}")+1));
                               SimpleChat.serverLogger.log(INFO, "" + message);
                               plainMessage = MessageProtocol.privateMessage(message, this.workerList.get(sender));
                               //plainMessage = plainMessage.substring(2);
                               if(usersnames.contains(",")) {
                                   SimpleChat.serverLogger.log(INFO,"Private Message to more users");

                                   usersnames = usersnames.replace(" ", "");
                                   usersnames = usersnames.replace("{", "");
                                   String[] names = usersnames.split(",", 10);

                                   SimpleChat.serverLogger.log(INFO, "User1: " + names[0]);
                                   SimpleChat.serverLogger.log(INFO, "User1: " + names[1]);


                                   sender.send(plainMessage);
                                   for (ClientWorker o : workerList.keySet()) {

                                       for (int i = 0; i < names.length; i++) {
                                           if (this.workerList.get(o).equals(names[i])) {
                                               o.send(plainMessage);

                                           }
                                       }
                                   }
                               }else{
                                   SimpleChat.serverLogger.log(INFO,"Private Message for one user");
                                   usersnames= usersnames.replace("{","");
                                   usersnames = usersnames.replace("{","");
                                   sender.send(plainMessage);
                                   for(ClientWorker o: workerList.keySet()){
                                       if(this.workerList.get(o).equals(usersnames)){
                                           o.send(plainMessage);
                                       }
                                   }

                               }


                           }


                       }else {
                           sender.send("Enter amother Chatname");
                       }
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
           }catch (IllegalArgumentException e){
               SimpleChat.serverLogger.log(WARNING,"Wrong Commang");
               sender.send("Command doesn't exist");
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
        message = MessageProtocol.textMessage(message,"SERVER");
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
        SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client");
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
        SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client");
        for (ClientWorker o : workerList.keySet())
             if(workerList.get(o).equals(chatName)){
                 SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client shutdowned");
                 this.workerList.remove(o);
                 o.shutdown();
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


        for (ClientWorker o : workerList.keySet()) {
            this.removeClient(o);
        }

        SimpleChat.serverLogger.log(INFO, "All Clients have been shutdowned");

        try {
            if(!serverSocket.isClosed()){
                Socket close = new Socket(host,port);
                this.serverSocket.close();
                close.close();

            }

        } catch (Exception e) {
                SimpleChat.serverLogger.log(SEVERE, e.getMessage());

        }
        this.executorService.shutdownNow();
        SimpleChat.serverLogger.log(INFO, "Serversocket closed "+serverSocket.isClosed());
        SimpleChat.serverLogger.log(INFO,"EXECUTORSERVICE "+this.executorService.isShutdown());
        SimpleChat.serverLogger.log(INFO,""+this.executorService.isShutdown());
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
                String message;
                if((message = in.readLine() )!= null)
                    callback.received(message,this);
            } catch (IOException e) {
                SimpleChat.serverLogger.log(SEVERE,e.getMessage());
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
        if(listening) {
            listening = false;
            this.send(MessageProtocol.getMessage(EXIT));
            SimpleChat.serverLogger.log(INFO, "Shutting down ClientWorker ... listening=" + listening);
        }

        try {
            Thread.sleep(1000);
            if (client.isConnected()) {

                SimpleChat.serverLogger.log(INFO,"Shutdown Socket");
                try {
                    out.close();
                    in.close();
                }
                finally {
                    client.close();
                }
            }
        }catch (Exception e){
            SimpleChat.serverLogger.log(SEVERE,e.getMessage());
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
