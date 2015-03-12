package common;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Observable;

/**
 * Created by Ludde on 2015-03-12.
 */
public class Connection extends Observable {
    private final InetAddress inet;
    private final int port;
    private final String userName;
    private String connectedToUserName = "";
    private LinkedList<Message> outgoingMessages;
    private LinkedList<Message> ingoingMessages;
    private Socket socket;
    private final Sender sender;
    private final Receiver receiver;

    public Connection(String userName, InetAddress inet, int port) {
        this.userName = userName;
        this.inet = inet;
        this.port = port;
        outgoingMessages = new LinkedList<Message>();
        ingoingMessages = new LinkedList<Message>();
        sender = new Sender();
        receiver = new Receiver();
    }

    public void openConnection(Socket socket) {
        try {
            if (socket != null) {
                this.socket = socket;
            } else {
                this.socket = new Socket(inet, port);
            }
            System.out.println("Opened socket: " + this.socket.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open socket on IP: " + inet.toString() + " and port: " + port);
        }
        sender.start();
        receiver.start();
    }

    public void sendMessage(Message message) {
        outgoingMessages.add(message);
    }

    public Message recieveMessage() throws NoSuchElementException {
        return ingoingMessages.pop();
    }

    public String getConnectedToUserName() {
        return connectedToUserName;
    }

    private class Sender extends Thread {
        public void run() {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //handshake, du talar om för den som är på andra sidan vem du är.
                oos.writeObject(new Message(userName, userName, userName));
                oos.flush();
                System.out.println("Sender Started");
                while (true) {
                    sleep(200); //FULFIX, detta är BIG no no egentligen, man borde göra denna som observer
                    // och klient/server som observable. men det är ett realtidsproblem så
                    // skit i det for now u will learn lat0r
                    if (!(outgoingMessages.size() == 0)) {
                        System.out.println("popping message to send");
                        Message poppedMessage = outgoingMessages.pop();
                        System.out.println("Sent message: " + poppedMessage.toString());
                        oos.writeObject(poppedMessage);
                        oos.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver extends Thread {

        public void run() {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //handshake, när du mottar en uppkoppling, vem är på andra sidan?
                connectedToUserName = ((Message) ois.readObject()).getMessage();
                System.out.println("Receiver started with other side: " + connectedToUserName);
                while (true) {
                    Message receivedMessage = (Message) ois.readObject();
                    System.out.println("Received: " + receivedMessage.toString());
                    ingoingMessages.add(receivedMessage);
                    setChanged();
                    notifyObservers();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
