package client;

import common.Connection;
import common.Message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

/**
 * Created by Ludde on 2015-03-12.
 */
public class Client implements Runnable, Observer {
    private static Connection connection;
    private final String userName;
    private static Scanner scan;

    public Client(Connection connection, String userName) throws UnknownHostException {
        this.connection = connection;
        this.userName = userName;
        System.out.println("InetAddress: " + InetAddress.getByName("localhost"));
        connection.openConnection(null);
        connection.addObserver(this);

    }

    public void showMessage(Message message) {
        System.out.println(message.toString());
    }

    public static void main(String[] args) {
        scan = new Scanner(System.in);
        System.out.println("Enter userName: ");
        String userName = scan.nextLine();
        System.out.println("Enter port: ");
        int port = Integer.parseInt(scan.nextLine());
        try {
           Client client = new Client(new Connection(userName, InetAddress.getByName("localhost"), port), userName);
           (client).run();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void update(Observable o, Object arg) {
        showMessage(connection.recieveMessage());
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Input message: format: <userTO> <message without space>");
            String[] input = scan.nextLine().split(" ");
            Message message = new Message(userName, input[0], input[1]);
            connection.sendMessage(message);
        }
    }
}
