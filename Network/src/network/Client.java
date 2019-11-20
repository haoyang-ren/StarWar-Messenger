/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryan
 */
public class Client extends Thread {

	Socket clientSocket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	static String username;
	static String password;
	static String auth = "false";

	public Client(Socket clientSocket, ObjectInputStream ois) {
		this.clientSocket = clientSocket;
		this.ois = ois;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: java Client server_IP server_port");
			System.exit(1);
		}
		// Get the server_IP and server_port from user input
		InetAddress server_IP = InetAddress.getByName(args[0]);
		int server_port = Integer.parseInt(args[1]);

		// Create a new client socket and connect with Server
		Socket clientSocket = new Socket(server_IP, server_port);

		// Create the input and output part
		Scanner input = new Scanner(System.in);
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

		// Start the new thread
		Client client = new Client(clientSocket, ois);
		client.start();

		while (true) {
			Thread.sleep(50);

			if (auth.equals("false")) {
				// Store the username and password for login
				System.out.println("Please Enter your Username: ");
				username = input.nextLine();
				System.out.println("Please Enter your Password: ");
				password = input.nextLine();
				String login = "User is trying to log in";
				Packet loginPacket = new Packet(auth, "login", username, password, login);

				oos.writeObject(Packet.buildString(loginPacket));
			} else {
				String command = input.nextLine();
				// Press "Enter" to check the available command list
				if (command.length() == 0) {
					System.out.println(
							"Available Command List: {logout message broadcast whoelse whoelsesince block unblock}");
					continue;
				}
				String[] commandList = command.split(" ");
				
				// Use conditions to handle the users' requests
				if (commandList[0].equals("logout")) {
					Packet logoutPacket = new Packet(auth, commandList[0], username, null, null);
					oos.writeObject(Packet.buildString(logoutPacket));
					continue;

				} else if (commandList[0].equals("whoelse")) {
					Packet whoelsePacket = new Packet(auth, commandList[0], username, null, null);
					oos.writeObject(Packet.buildString(whoelsePacket));
					continue;

				} else if (commandList[0].equals("whoelsesince")) {
					Packet whoelsesincePacket = new Packet(auth, commandList[0], username, "0", commandList[1]);
					oos.writeObject(Packet.buildString(whoelsesincePacket));
					continue;

				} else if (commandList[0].equals("message")) {
					List<String> strList = new ArrayList<String>(Arrays.asList(commandList));
					strList.remove(0);
					String message = String.join(" ", strList);
					Packet messagePacket = new Packet(auth, commandList[0], username, "0", message);
					oos.writeObject(Packet.buildString(messagePacket));
					continue;

				} else if (commandList[0].equals("broadcast")) {
					List<String> strList = new ArrayList<String>(Arrays.asList(commandList));
					strList.remove(0);
					String broadcast = String.join(" ", strList);
					Packet broadcastPacket = new Packet(auth, commandList[0], username, "0", broadcast);
					oos.writeObject(Packet.buildString(broadcastPacket));
					continue;

				} else if (commandList[0].equals("block") || commandList[0].equals("unblock")) {
					Packet blockPacket = new Packet(auth, commandList[0], username, "0", commandList[1]);
					oos.writeObject(Packet.buildString(blockPacket));
					continue;

				} else {
					Packet otherPacket = new Packet(auth, commandList[0], username, "0", null);
					oos.writeObject(Packet.buildString(otherPacket));
					continue;
				}
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			String readBuffer = null;
			// Create a readBuffer to print feedback from the server 
			try {
				readBuffer = ois.readObject().toString();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (readBuffer == null) {
				continue;
			}

			Packet receivedPacket = Packet.fromString(readBuffer);

			System.out.println(receivedPacket.getMessage().toString());
			// Set the user status to online after login
			if (receivedPacket.getAuth().equals("true") && auth.equals("false")
					&& receivedPacket.getRequest().equals("login")) {
				auth = "true";
			}
			// Set the user status to offline after timeout or logout
			if (receivedPacket.getRequest().equals("timeout")) {
				auth = "false";
			}
			if (receivedPacket.getRequest().equals("logout")) {
				auth = "false";
			}
		}
	}
}
