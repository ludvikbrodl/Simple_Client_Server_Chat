package server;

import common.Connection;
import common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Ludde on 2015-03-12.
 */
public class Server implements Runnable, Observer {
    private ServerSocket serverSocket;
    private static LinkedList<Connection> connections;


    public Server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
        connections = new LinkedList<Connection>();
    }

    public void run() {
        try {
            Socket socket = serverSocket.accept();
            newListener(); //så fort en ny socket öppnas(dvs en klient connectar)->
            // starta en ny tråd(som också kör run)

            Connection thisThreadsConnection = new Connection("Server", socket.getInetAddress(), socket.getPort());
            thisThreadsConnection.addObserver(this);
            thisThreadsConnection.openConnection(socket);

            connections.add(thisThreadsConnection);
            System.out.println("Number of connected clients: " + connections.size());

        } catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void newListener() {
        (new Thread(this)).start();
    } // calls run()

    public static void main(String args[]) {
        int port = 6000;
        System.out.println("\nServer Started\n" + "on port: " + port);
        try {
            ServerSocket ss = new ServerSocket((port));
            new Server(ss);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Server updating send messages");
        for(Connection connection : connections) {
            Message message = null;
            try {
                message = connection.recieveMessage();
                //om klienter skriver all som username skickas meddelande till alla
                if (message.getUserNameTo().equals("all")) {
                    sendMessageToAllConnections(message);
                } else {
                    //om inte, måste vi kolla alla connections och se vilken connection
                    for (Connection connectionToSendTo : connections) {
                        if (connectionToSendTo.equals(message.getUserNameTo())) {
                            connectionToSendTo.sendMessage(message);
                        }
                    }
                }

            } catch (NoSuchElementException e) {
                //do nothing if there was no messages on this connection
            }
        }
    }

    private void sendMessageToAllConnections(Message message) {
        for(Connection connection : connections) {
            connection.sendMessage(message);
        }
    }
}
