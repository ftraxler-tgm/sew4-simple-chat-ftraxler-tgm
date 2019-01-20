# "Java GUI & Socket Programmierung - Simple Chat"

## Aufgabenstellung
Die detaillierte [Aufgabenstellung](TASK.md) beschreibt die notwendigen Schritte zur Realisierung.


## Implementierung

### Client

#### SimpleChatClient

##### SimpleChatClient:


    if (name != null) this.name = name;
    if (host != null) this.host = host;
    if (port != null) this.port = port;
    SimpleChat.clientLogger.log(INFO, "Init: host=" + this.host + " port="
            + this.port + " chatName=" + this.name);
    this.client = client;//setzt den übergebenen client
    this.socketAddress = new InetSocketAddress(host,port);
    //erstellt eine socketAddress mit dem übergebenen host und port


##### run:


SimpleChat.clientLogger.log(INFO, "Run des SimpleChatClients");

  try {
      this.socket=new Socket();
      this.socket.connect(this.socketAddress, 2000);//connect zur Address und zum Port
      this.socket.setKeepAlive(true);

      try {
          out = new PrintWriter(socket.getOutputStream(), true);
          //erstellt den OutputStream
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          //erstellt den InputStream
      } catch (IOException e) {
          SimpleChat.clientLogger.log(SEVERE,e.getMessage()+ " IN/OUT");
      }

      listening = true; // damit die Laufbedingung stimmt

      //SimpleChat.clientLogger.log(INFO, "Name wird an Socket geschickt");
      //out.println(MessageProtocol.getMessage(CHATNAME)+" "+name);
      //wenn ein Chatname übergeben wird beim Argumente Aufruf wird dieser Direkt an den Server gesendet

      while(listening){
          if((currentMessage = in.readLine() )!= null) {
              SimpleChat.clientLogger.log(INFO, "Client got a message: " + currentMessage);
              this.received();//ruft die received Methode auf wenn eine Nachricht gekommen ist
          }
      }
  } catch(Exception E) {
      SimpleChat.clientLogger.log(SEVERE,E.getMessage()+" SOCKET");

      if (socket.isConnected() && socket.isBound() ) {
          this.shutdown();//Im Fehlerfall wird alles geschlossen
      }
  }

##### received:

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
            }
        }else{
            SimpleChat.clientLogger.log(INFO,"The Client got the message "+this.currentMessage);
            this.client.incomingMessage(this.currentMessage);
        }

    }

Überprüft ob ein !EXIT Command gesendet wurde wenn ja wird die shutdown Methode in der Klasse SimpleChatClient auf aufgerufen.
Ansonsten wird die Nachricht einfach weitergeleitet.

##### send:
    if(listening)
        out.println(message);

Sendet eine Nachricht in den OutputStream



##### shutdown:

Beendet alles sauber und clean und schließt alle offenen Ressourcen

##### isListening:
    return listening
gibt zurück ob listening true ist
#### SimpleChat

##### listen:

    clientLogger.log(INFO, "Initiating SimpleChatClient ...");
    client.start();
    //Start den SimpleChatClient-Thread
##### setController:


    this.controller = controller;
    //Setzt den erstellten Controller

##### stop:

    this.client.shutdown();
    //Ruft die shutdown-Methode der Klasse SimpleChatClient auf
##### isConnected:

    return client.isAlive();
    //checkt ob der Thread läuft

##### sendMessage:

    if(isConnected())
        clientLogger.log(INFO, "UI gave me this message: " + message);
        this.client.send(message);
        //Ruft die send Methode in der SimpleChatClient Klasse auf und übergibt die Message

##### incomingMessage:

    if(controller != null){
        this.controller.updateTextAreaWithText(message);
        //übergibt die empfangene Nachricht an den Controller weiter
    }



#### Controller


#####  handleMessageButtonAction

    event.consume(); //Konsumiert es damit sich das Event nicht vermehren kann
    this.sendMessage();//ruft die send Mehtode im Controller auf
    simpleChat.clientLogger.log(INFO,"Send Button pressed");
    this.textField.setText("");//Setzt das textField auf einen leeren String

##### stop:

    this.simpleChat.stop();//Ruft die stop Methode vom SimpleChat auf
    scheduledExecutorService.shutdown(); //Schaltet den scheduledExecutorService aus

#### setSimpleChat:

    this.simpleChat=simpleChat;
    //Setzt das SimpleChat Attribut
#### updateTextArearWithText:

    this.textArea.setText(this.textArea.getText()+"\n"+text);
    //Setzt den neuen Text indem es sich den alten holt und den neuen hinzugefügt.
#### sendMessage:

    this.simpleChat.sendMessage(this.textField.getText());
    //Ruft die send Methode in der SimpleChat Klasse auf und übergibt den Text aus dem Textfeld


### Server

#### Controller:
##### handleMessageButtonAction:
    event.consume();
    this.simpleChat.sendMessage(this.textField.getText());
    this.textField.setText("");

Ruft die Send Methode auf und übergibt den zu sendenen Text.
##### handleRemoveButtonAction:

     event.consume();
     String name = (String)listView.getSelectionModel().getSelectedItem();
     SimpleChat.serverLogger.log(Level.INFO,"Removing... :"+name);
     this.simpleChat.shutdownClient(name);

Gibt den ausgewählten Username an die shutdownClient Methode vom SimpleChatServer weiter.
##### initialize

    this.simpleChat = new SimpleChat("localhost",5050);


erstellt ein neues SimpleChat Object und setzt das Attribut.
##### stop
    this.simpleChat.stop();
    this.scheduledExecutorService.shutdown();

Ruft die Stop-Methode vom SimpleChat auf und schließt den scheduledExecutorService.
##### setSimpleChat

    this.simpleChat = simpleChat;

Setzt das SimpleChat Attribut.
##### updateTextAreaWithText:
    SimpleChat.serverLogger.log(Level.INFO,"Textarea:"+this.textArea.getText());
    SimpleChat.serverLogger.log(Level.INFO,"New Text:"+text);
    this.textArea.setText(this.textArea.getText()+"\n"+text);

Fügt den übergebenen Text dem Chatarea hinzu.
##### addUser:


    ObservableList<String> users = this.listView.getItems();

    Platform.runLater(() ->
    {
        users.add(user);
        this.listView.setItems(users);
    });
Fügt den übergebenen User zur GUI hinzu.
##### removeUser:

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
Entfernt den übergebenen User aus der GUI.

#### SimpleChat

#### SimpleChat(Konstruktor):

    server = new simplechat.communication.socket.server.SimpleChatServer(host, port, this);
    users = new ConcurrentSkipListSet<>();
    receivedMessages = new ConcurrentLinkedQueue<>();
    sentMessages = new ConcurrentLinkedQueue<>();

Erzeug ein SimpleChatServer Object und speichert es im Attribute server.

##### setController:

    this.controller = controller;

Setzt das Controller Attribut.
##### listen:

    serverLogger.log(INFO, "Initiating SimpleChatServer ...");
    this.server.start();

Startet den Server-Thread
#### stop:
    this.server.shutdown();
Ruft die shutdown-Methode vom SimpleChatServer auf.
##### isConnected:
    return this.server.isAlive();
Gib des Status des SimpleChatServer-Threads zurück.
##### sendMessage:
    if(isConnected())
        serverLogger.log(INFO, "UI gave me this message: " + message);

        this.server.send(message);
        this.sentMessages.add(message);
        this.controller.updateTextAreaWithText(message);

Sendet eine Nachricht fügt sie zur Liste hinzu und aktualisiert die GUI.
##### incomingMessage:

        this.receivedMessages.add(message);
        this.controller.updateTextAreaWithText(message);

Fügt die empfangene Nachricht zur Liste hinzu und aktualisiert die GUI.
##### addClient:

        String name = chatName.equals("") ? "User#"+(users.size()+1) : chatName;
        users.add(chatName);
        this.controller.addUser(chatName);
        return name;


Setzt einen Namen für den Client wenn noch keiner gesetzt wurde und fügt den user zur Userliste hinzu.
Danach wird noch die GUI geupdated und dann wird der Name zurück gegeben.
##### renameClient:

        SimpleChat.serverLogger.log(INFO,"Trying to change name");

        if(users.remove(oldChatName)&& users.add(newChatName)){
            serverLogger.log(INFO, "Renaming Client...");
            this.controller.removeUser(oldChatName);
            SimpleChat.serverLogger.log(INFO,"Updating the GUI");
           return this.addClient(newChatName);
        }
        return null;
Ändert den Namen eines Users.
##### removeClient:


        serverLogger.log(INFO, "Client ("+chatName+") removed...");
        users.remove(chatName);
        this.controller.removeUser(chatName);

Entfernt einen User aus der GUI und der userliste
##### shutdownClient:

        this.server.removeClient(chatName);
        removeClient(chatName);
Entfernt einen Client indem die removeClient Methode aus der Klasse SimpleChatServer aufgerufen wird.
#### SimpleChatServer
##### SimpleChatServer(Konstruktor):
      if (host != null) this.host = host;
             if (port != null) this.port = port;
             this.server = server;
             this.listening=true;
             SimpleChat.serverLogger.log(INFO, "Init: host=" + this.host + " port=" + this.port);
Setzt das Server Attribut(SimpleChat) und listening auf true.
##### run:
      try{
          SimpleChat.serverLogger.log(INFO, "... starting Thread ...");
          this.serverSocket = new ServerSocket(this.port, this.backlog);


          while (listening) {
              SimpleChat.serverLogger.log(INFO, "Listening...");
              ClientWorker c = new ClientWorker(this.serverSocket.accept(), this);
              workerList.put(c, this.server.addClient(""));
              if(!this.executorService.isShutdown())
                  executorService.execute(c);
              SimpleChat.serverLogger.log(INFO, "ClientWorker added");

          }

      }catch(IOException e){
          SimpleChat.serverLogger.log(SEVERE,"Error adding Client: \n "+e.getMessage());
      }
      SimpleChat.serverLogger.log(INFO,"Shutdown Thread!!!!");
Erstellt einen ServerSocket und accept alle ClientSocket.
Erstellt ein Clientworker Object und start den Thread.
##### received:

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
Diese Methode interpretiert die Nachricht so dass sie korrekt und an den richtigen geschickt wird
##### send:
     message = MessageProtocol.textMessage(message,"SERVER");
            for(ClientWorker o: workerList.keySet())
                o.send(message);
Sendet die Nachricht an alle Clients.
##### setName:
    String newName= this.server.renameClient(this.workerList.get(worker),chatName);
            SimpleChat.serverLogger.log(INFO,"NEWNAME: "+newName);
            if(newName != null){
                this.workerList.replace(worker,newName);
                SimpleChat.serverLogger.log(INFO,"Name has been replaced to "+this.workerList.get(worker));
            }else{
                SimpleChat.serverLogger.log(WARNING,"Error while changing the name");
            }
Ändert den Name des Clients in der workerListe.
##### removeClient(Clientworker):
    SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client");
    this.server.shutdownClient(workerList.get(worker));
    this.workerList.remove(worker);
    worker.shutdown();
    
Entfernt einen Client indem es die shutdownClient Methode vom SimpleChat aufruft den Clientworker aus der Liste entfernt und in sauber beendet.
##### removeClient(String)
    SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client");
    for (ClientWorker o : workerList.keySet())
         if(workerList.get(o).equals(chatName)){
             SimpleChat.serverLogger.log(INFO,"Remove/Shutdown Client shutdowned");
             this.workerList.remove(o);
             o.shutdown();
         }
Entfert einen Client mittels seines Chatnames.
##### shutdown:
        
    this.listening=false;


    for (ClientWorker o : workerList.keySet()) {
        this.removeClient(o);
    }

    SimpleChat.serverLogger.log(INFO, "All Clients have been shutdowned");


    try {
        Thread.sleep(1000);
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
            
Saubers schließen des Servers. Als ersters werden alle Clients geschlossen danach wird der Server sauber geschlossen.
#### ClientWorker:
##### ClientWorker(Konstruktor):
    SimpleChat.serverLogger.log(INFO,"ClientWorker-Thread init");
    this.client=client;
    this.callback=callback;
    this.out = new PrintWriter(client.getOutputStream(),true);
    this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
Setzt den Client socket den Server(callback) und erzeugt den Input- und Outputstream.
##### run:
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
Die selbe run Methode wie beim wie in der SimpleChatClient run Methode.
##### shutdown:
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
                                SimpleChat.serverLogger.log(INFO,"Client Socket: "+client.isClosed());
                            } catch (IOException e) {
                                SimpleChat.serverLogger.log(SEVERE,e.getMessage());
                            }
                        }
                    }
                }
            }catch (Exception e){
                SimpleChat.serverLogger.log(SEVERE,e.getMessage());
            }
Beendet den Client sauber. Wenn das Listening true war wird  !EXIT an den Client geschickt ansonsten werden direkt die Streams und der Socket geschlossen.
##### send:
    this.out.println(message);
    SimpleChat.serverLogger.log(INFO,"Server send a message: "+message);
Sendet eine Nachricht an den Client.









### Messageprotokoll

##### getMessage:

     return "!" + command; //Gibt das übergeben Command als String zurück

##### getCommand:

    if(command.startsWith("!")){//Damit man das Command auch ohne ! bekommen kann
           return Commands.valueOf(command.substring(1)); //Gibt das Command zurück
       }
       Commands c= Commands.valueOf(command);
       return c;
##### textMessage:


     return "[" + chatName + "] " + plainMessage;
     //Gibt eine für den Chat bereite String message zurück

##### privateMessage:


     return "[PRIVATE]"+ textMessage(plainMessage,chatNam);
     //Fügt noch ein "[PRIVATE]" zur Chatnachricht hinzu


## Quellen


\[1] Sockets close <https://coderanch.com/t/697134/java/socket-client-stops-Socket-closed>  
\[2] Sockets <https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html>  
\[3] Server sockets <https://docs.oracle.com/javase/7/docs/api/java/net/ServerSocket.html>  
\[4] Socket Example <https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html>
