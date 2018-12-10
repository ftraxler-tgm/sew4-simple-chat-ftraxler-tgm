# "Java GUI & Socket Programmierung - Simple Chat" - Taskdescription

## Die Aufgabenstellung
Erstelle ein einfaches Chatprogramm unter Anwendung des MVC Design Patterns!

### Grundanforderungen
* GUI's mittels JavaFX erstellen
* Trennung jeweils von View und Controller sowie der Kommunikationsimplementierung
* Javadoc-Dokumentation

#### Client
Die GUI soll ein Eingabefeld für Nachrichten und einen Senden-Button implementieren.

Verbindung zum Server Port 5050 wird beim Starten hergestellt -
im Fehlerfall wird eine MessageBox mit dem Fehler angezeigt und das Programm wieder geschlossen.

Bei einem Klick auf "Senden" wird die Nachricht an den Server geschickt und das Eingabe-Textfeld geleert.

Ein zweiter Thread liest eingehende Nachrichten und fügt sie in den Chatbereich ein.

Wird die Verbindung geschlossen, schließt sich das Programm ohne offene Resourcen zu hinterlassen.
Das Close-Event wird abgefangen, dem Server wird das Kommando *!EXIT* geschickt
und der Socket wird sauber geschlossen.

#### Server
Horcht auf Port 5050 in einem Thread auf eingehende Verbindungen. Es ist wichtig,
dass *accept()* in einem eigenen Thread aufgerufen wird.

Im Fehlerfall wird eine entsprechende MessageBox angezeigt und das Programm beendet.

In der Grundfunktionalität ist ein anonymes Chatten möglich, wobei der Chat-Text mit einem automatisch 
vergebenen Client-Namen definiert gehört (z.B. Client, Client#1, Client#2).

Neue Nachrichten an den Server werden an alle Clients verteilt und im eigenen Chat Feld angezeigt
sowie in einem eigenen Messagespeicher abgelegt, der nicht persistiert werden muss. 

Wenn der Server geschlossen wird, wird die Verbindung zu allen Clients sauber beendet
und die Clients werden daher automatisch geschlossen. Dabei soll jeder Client 
mit dem Befehl *!EXIT* benachrichtigt werden.

Achtung: GUI-spezifische Operationen (z.B. Anzeigen einer MessageBox oder Bearbeiten
eines GUI-Elements) dürfen nur vom GUI-Thread durchgeführt werden.

### Erweiterungen
Der User kann den Chatnamen bei Programmaufruf mitgeben:

    java simplechat.client.SimpleChat --name Franz --host localhost --port 5050

Bei Aufruf mit Gradle sind die Argumente in folgender Weise zu übergeben:

    gradle server --args="--host localhost --port 5050 -v"
    gradle client --args="--name Franz --host localhost --port 5050"

Ein Client kann dem Server seinen Chatnamen mittels des Kommandos *!CHATNAME Franz* übermitteln.

Es wird immer **[ChatName] text** angezeigt, also **[Client#1] Hallo!** oder **[Franz] Servus!**

Neue Clients werden der Client-Liste hinzugefügt, sobald sie sich verbinden.
Für jeden neuen Client wird ein neuer Thread erstellt, welcher auf eingehende Nachrichten wartet.
Wenn ein Client die Verbindung beendet, wird er aus der Client-Liste entfernt.

Der ServerAdmin kann einen Client entfernen indem er ihn in der UserListBox anlickt
und dann den Disconnect Button drückt. Der Server kann also verbundene Clients aus
der Client-Liste entfernen und die Verbindung zu ihnen beenden.

Der Client muss konfiguriert werden, bevor er die Verbindung mit dem Server herstellt:
Parameter sind ChatName, Host und Port.

Im Falle einer langen chat Historie soll die Chat-Textbox automatisch ganz
nach unten scrollen damit man bequem mitlesen kann.

Die grafische Oberfläche muss bei einem Resize entsprechend skalieren.

Halte deine Erkenntnisse im [Readme](README.md) fest und dokumentiere deine Lösung!

### Umgebung und Tests

Es wird Gradle als Build-Umgebung verwendet. Um eine Liste an Tasks zu erhalten können folgende Befehle ausgeführt werden:

    gradle tasks

Um die Umgebung auch für IntelliJ oder eclipse einzurichten, kann der folgende Task ausgeführt werden:

    gradle idea eclipse

Das Beispiel wird entsprechend der nachgereichten Tests bewertet werden.
Es ist jedoch notwendig eigene Tests zur Funktionsüberprüfung zu schreiben.
Hierzu sind einige Testbeispiele schon vorhanden.

Folgender Befehl wird bei der Abnahme ausgeführt und bewertet:

    gradle clean test jacocoTestReport javadoc

## Bewertung 

## Quellen
\[1] JavaFX Manual <https://docs.oracle.com/javase/8/javase-clienttechnologies.htm>  
\[2] Java Sockets Tutorial <https://docs.oracle.com/javase/tutorial/networking/sockets/index.html>
