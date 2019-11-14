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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryan
 */
public class Client extends Thread {

	static Socket clientSocket;
	static String auth = "false";
	static ObjectOutputStream oos;
	static ObjectInputStream ois;

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
		// String server_Name = "localhost";
		Scanner input = new Scanner(System.in);
		// Create a new client socket
		clientSocket = new Socket(server_IP, server_port);
		// Start the new thread
		System.out.println("Prepare to get in the new thread");
		oos = new ObjectOutputStream(clientSocket.getOutputStream());
		ois = new ObjectInputStream(clientSocket.getInputStream());
		Client client = new Client(clientSocket, ois);
		client.start();

		while (true) {
			Thread.sleep(50);
			if (auth.equals("false")) {

				System.out.println("Please Enter your Username: ");
				String username = input.nextLine();
				System.out.println("Please Enter your Password: ");
				String password = input.nextLine();
				String login = "User is trying to log in";
				Packet loginPacket = new Packet(auth, "login", username, password, login);
				// System.out.println("packet has already created.");
				// ObjectOutputStream oos = new
				// ObjectOutputStream(clientSocket.getOutputStream());
				oos.writeObject(Packet.buildString(loginPacket));
				// System.out.println("packet has already sent.");
			} else {
				String command = input.nextLine();
				if (command.length() == 0) {
					System.out.println("--Type help to see the available command--");
					continue;
				}
				String[] commandList = command.split(" ");
				if (commandList[0].equals("help")) {
					System.out.println(
							"Available Command List: logout message broadcast whoelse whoelsesince block unblock");
					continue;
				}
				if (commandList[0].equals("logout") || commandList[0].equals("whoelse")) {
					Packet requestPacket = new Packet(auth, commandList[0], null, null, null);
					// ObjectOutputStream oos = new
					// ObjectOutputStream(clientSocket.getOutputStream());
					oos.writeObject(Packet.buildString(requestPacket));
					continue;

				} else if (commandList[0].equals("message")) {
					String sentence = "";
					for (int i = 0; i < commandList.length; i++) {
						if (sentence != null) {
							sentence = commandList[i];
						} else if (i >= 2 && sentence == null) {
							sentence = sentence + " " + commandList[i];
						}
					}
					
					String message = commandList[1] + sentence;
					Packet messagePacket = new Packet(auth, commandList[0], "0", "0", message);
					// ObjectOutputStream oos = new
					// ObjectOutputStream(clientSocket.getOutputStream());
					oos.writeObject(Packet.buildString(messagePacket));
					continue;

				} else if (commandList[0].equals("whoelsesince") || commandList[0].equals("block")
						|| commandList[0].equals("unblock")) {
					Packet requestPacket = new Packet(auth, commandList[0], "0", "0", commandList[1]);
					// ObjectOutputStream oos = new
					// ObjectOutputStream(clientSocket.getOutputStream());
					oos.writeObject(Packet.buildString(requestPacket));
					continue;
				} else if (commandList[0].equals("broadcast")) {
					String sentence = "";
					for (int i = 0; i < commandList.length; i++) {
						if (sentence != null) {
							sentence = commandList[i];
						} else if (i >= 1 && sentence == null) {
							sentence = sentence + " " + commandList[i];
						}
					}
					Packet messagePacket = new Packet(auth, commandList[0], "0", "0", sentence);
					// ObjectOutputStream oos = new
					// ObjectOutputStream(clientSocket.getOutputStream());
					oos.writeObject(Packet.buildString(messagePacket));
					continue;
				} else {
					Packet otherPacket = new Packet(auth, commandList[0], "0", "0", null);
					// ObjectOutputStream oos = new
					// ObjectOutputStream(clientSocket.getOutputStream());
					oos.writeObject(Packet.buildString(otherPacket));
					continue;
				}

			}
		}

	}

	@Override
	public void run() {
		System.out.println("Prepare to read the packet");

		//System.out.println(receivedPacket.getMessage().toString());

			String readBuffer;
			try {
				readBuffer = (String)ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			//Packet receivedPacket = Packet.fromString(readBuffer);
			System.out.println(Packet.fromString(readBuffer).getMessage().toString());
		while (true) {
			//System.out.println("Analyse the packet");

			Packet receivedPacket = Packet.fromString(readBuffer);
			//try {
				//receivedPacket = Packet.fromString(readBuffer);
			//} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//return;
			//}
			
			System.out.println(receivedPacket.getMessage().toString());
			//System.out.println("Get the request");
			if (receivedPacket.getAuth().equals("true") && auth.equals("false")
					&& receivedPacket.getRequest().equals("login")) {
				auth = "true";
				System.out.println(receivedPacket.getMessage().toString() + " at if condition");
			}
			if (receivedPacket.getRequest().equals("timeout")) {
				auth = "false";
				System.out.println(receivedPacket.getMessage().toString());
			}
			if (receivedPacket.getRequest().equals("logout")) {
				auth = "false";
				System.out.println(receivedPacket.getMessage().toString());
			}

		}
	}

}
