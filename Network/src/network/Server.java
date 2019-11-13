/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryan
 */
public class Server extends Thread {

    /**
     * @param args the command line arguments
     */
    static ServerSocket serverSocket;
    static ArrayList<User> users = new ArrayList<User>();
    static String serverName;
    static int serverPort;
    static int currentPort;
    static float blockDuration;
    static int timeout;
    private Socket connectionSocket;

    public Server(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        if (args.length != 3) {
            System.out.println("Usage: java Server server_port block_duration timeout");
            System.exit(1);
        }
        // Get the arguments value
        serverPort = Integer.parseInt(args[0]);
        currentPort = serverPort;
        // currentPort = Integer.parseInt(args[0]);
        blockDuration = Float.parseFloat(args[1]);
        timeout = Integer.parseInt(args[2]);
        // Create the new server socket
        serverSocket = new ServerSocket(serverPort);
        serverName = "localhost";
        // Create all the users and add them to the users
        String root = System.getProperty("user.dir");
        String FileName = "credentials.txt";
        String filePath = root + File.separator + File.separator + FileName;
        // FileReader fr = new FileReader(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] word = line.split(" ");
            System.out.println("name is " + word[0]);
            System.out.println("password is " + word[1]);
            User user = new User(word[0], word[1], 0, null, false, null, null, null, 0);
            users.add(user);
            line = reader.readLine();

        }
        reader.close();
        // System.out.println("Program has arrived here");
        while (true) {
            // Get the connection socket and address
            Socket connectionSocket = serverSocket.accept();
            SocketAddress connectionAddress = connectionSocket.getRemoteSocketAddress();
            // Use connection socket to send current port number
            /*
			 * DataOutputStream outToClient = new
			 * DataOutputStream(connectionSocket.getOutputStream()); String clientPortNumber
			 * = String.valueOf(currentPort); outToClient.writeBytes(clientPortNumber);
			 * connectionSocket.close();
             */

            Server server = new Server(connectionSocket);
            server.start();
            // currentPort++;
        }

    }

    public ArrayList<String> login(String username, String password, Socket connectionSocket, int currentPort) {
        System.out.println("pass in user name is " + username + " over");
        System.out.println("pass in user name is " + password + " over");
        ArrayList<String> result = new ArrayList<String>();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("Valid username " + user.getUsername());
                System.out.println("Valid password " + user.getPassword());
                if (user.isOnline() == true) {
                    result.add("false");
                    result.add("Sorry, you have been blocked by the server");

                    return result;
                }
                if (user.getBlock() >= 3) {
                    result.add("false");
                    result.add("Sorry, you have been blocked by the server");

                    return result;
                }
                if (user.getPassword().equals(password) == false) {
                    int newBlock = user.getBlock() + 1;
                    user.setBlock(newBlock);
                    if (user.getBlock() >= 3) {
                        // use timer to reset the block number = 0 after the duration
                        result.add("false");
                        System.out.print("block number is" + user.getBlock());
                        result.add("Invalid password");
                        return result;
                    }
                }
                if (user.getPassword().equals(password)) {
                    user.setOnline(true);
                    user.setSocket(connectionSocket);
                    user.setBlock(0);
                    user.setTime(null);

                    result.add("true");
                    result.add("Welcome come to STAR WAR message application!");
                    return result;
                }
            }
        }
        result.add("false");
        result.add("Invalid username");
        return result;
    }

    public ArrayList<String> sendMessage(String source, String destination, String content) throws IOException {
        String auth = "true";
        ArrayList<String> result = new ArrayList<String>();
        if (source.equals(destination) == true) {
            result.add("false");
            result.add("Can not send message to yourself!");
            return result;
        } else {
            for (User user : users) {
                if (user.getUsername().equals(destination) && user.getBlackList().contains(source)) {
                    result.add("false");
                    result.add("You have been blocked by" + destination);
                    return result;
                }
                if (user.isOnline() == true) {
                    Packet messeagePacket = new Packet(auth, "message", "0", "0", content);
                    ObjectOutputStream oos = new ObjectOutputStream(user.getSocket().getOutputStream());
                    oos.writeObject(Packet.buildString(messeagePacket));
                    result.add("true");
                    result.add("online");
                    return result;
                } else {
                    String pending = "Pending :" + source + " " + "content";
                    user.getPending().add(pending);
                    result.add("true");
                    result.add("offline");
                    return result;
                }

            }

        }
        result.add("false");
        result.add("Invalid user");
        return result;
    }

    public ArrayList<String> broadcast(String source, String content) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        String auth = "True";
        for (User user : users) {
            if (user.isOnline() == true && user.getUsername().equals(source) == false) {
                if (user.getBlackList().contains(source)) {
                    result.add("false");
                    result.add("Your message can not be broadcast to somme users");
                    return result;
                } else {
                    Packet broadcastPacket = new Packet(auth, "broadcast", "0", "0", content);
                    ObjectOutputStream oos = new ObjectOutputStream(user.getSocket().getOutputStream());
                    oos.writeObject(Packet.buildString(broadcastPacket));

                    result.add("true");
                    result.add("Your message has been broadcast successfully!");
                    return result;
                }
            }
        }
        return result;
    }

    public ArrayList<String> whoelse(String source) {
        ArrayList<String> result = new ArrayList<String>();
        for (User user : users) {
            if (user.isOnline() == true && user.getUsername().equals(source) == false) {
                result.add(user.getUsername());
            }
        }
        return result;
    }

    public ArrayList<String> whoelsesince(String source, float second) {
        ArrayList<String> result = new ArrayList<String>();
        for (User user : users) {
            if (user.getUsername().equals(source) == false) {

                if (user.isOnline() == true) {
                    result.add(user.getUsername());
                } else if (user.getTime() != null) {
                    LocalDateTime now = LocalDateTime.now();
                    int duration = now.compareTo(user.getTime());
                    if ((float) duration <= second) {
                        result.add(user.getUsername());
                    }
                }
            }
        }
        return result;
    }

    public ArrayList<String> block(String source, String destination) {
        ArrayList<String> result = new ArrayList<String>();
        boolean find = false;
        if (source.equals(destination)) {
            result.add("false");
            result.add("You can not block yourself!");
            return result;
        }

        for (User user : users) {
            if (user.getUsername().equals(destination)) {
                find = true;
                break;
            }
        }
        if (find == true) {
            for (User user : users) {
                if (user.getUsername().equals(source)) {
                    user.getBlackList().add(destination);
                    result.add("true");
                    result.add("Suceessfully block " + destination);
                    return result;
                }
            }
        } else {
            result.add("false");
            result.add("Invalid username " + destination);
            return result;
        }

        return result;
    }

    public ArrayList<String> unblock(String source, String destination) {
        ArrayList<String> result = new ArrayList<String>();
        boolean find = false;
        if (source.equals(destination)) {
            result.add("false");
            result.add("You can not unblock yourself!");
            return result;
        }

        for (User user : users) {
            if (user.getUsername().equals(destination)) {
                find = true;
                break;
            }
        }
        if (find == true) {
            for (User user : users) {
                if (user.getUsername().equals(source)) {
                    if (user.getBlackList().contains(destination)) {
                        user.getBlackList().remove(destination);
                        result.add("true");
                        result.add("Successfully remove " + destination + " from your blacklist!");
                        return result;
                    } else {
                        result.add("false");
                        result.add(destination + " is not in your blacklist!");
                        return result;
                    }
                }
            }
        } else {
            result.add("false");
            result.add("Invalid username " + destination);
            return result;
        }

        return result;
    }

    @Override
    public void run() {
        String[] command = {"login", "message", "logout", "broadcast", "block", "unblock", "whoelse", "whoelsesince"};
        List<String> commandList = Arrays.asList(command);
            String auth = "false";
            while (true) {
                System.out.println("Recevied packet from client");
                try {
                    ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                    Packet receivedPacket = Packet.fromString((String) ois.readObject());
                    System.out.println("Analyse request!");
                    // Try to login the system
                    if (auth.equals("false") == true && receivedPacket.getRequest().toString().equals("login")) {
                        ArrayList<String> fl = login(receivedPacket.getUsername().toString(),
                                receivedPacket.getPassword().toString(), connectionSocket, currentPort);
                        
                        if (fl.get(0).equals("false")) {
                            Packet errorPacket = new Packet(auth, "error", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(errorPacket));
                        } else {
                            auth = "true";
                            Packet welcomePacket = new Packet(auth, "login", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(welcomePacket));
                            //System.out.println("Recevied the welcome packet!");
                            Thread.sleep(50);
                            for (User user : users) {
                                if (user.getUsername().equals(receivedPacket.getUsername())) {
                                    if (user.getPending() != null) {
                                        for (String pending : user.getPending()) {
                                            Packet pendingPacket = new Packet(auth, "message", "0", "0", pending);
                                            ObjectOutputStream newoos = new ObjectOutputStream(
                                                    connectionSocket.getOutputStream());
                                            newoos.writeObject(Packet.buildString(pendingPacket));
                                        }
                                        user.setPending(null);
                                    }
                                }
                            }
                            for (User user : users) {
                                if (user.isOnline() == true
                                        && user.getUsername().equals(receivedPacket.getUsername()) == false) {
                                    Socket userSocket = user.getSocket();
                                    String duplicateLogin = receivedPacket.getUsername().toString() + "has logged in";
                                    Packet duplicateLoginPacket = new Packet(auth, "notification", "0", "0",
                                            duplicateLogin);
                                    ObjectOutputStream newoos = new ObjectOutputStream(userSocket.getOutputStream());
                                    newoos.writeObject(Packet.buildString(duplicateLoginPacket));
                                }
                            }
                            connectionSocket.setSoTimeout(timeout);
                        }
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("message") == true) {
                        String strArray[] = receivedPacket.getMessage().toString().split(" ");
                        String peername = strArray[0];
                        String information = strArray[1];
                        ArrayList<String> fl = sendMessage(receivedPacket.getUsername().toString(), peername,
                                information);
                        if (fl.get(0).equals("false")) {
                            Packet messagePacket = new Packet(auth, "messageError", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(messagePacket));
                        }
                        connectionSocket.setSoTimeout(timeout);
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("broadcast") == true) {

                        ArrayList<String> fl = broadcast(receivedPacket.getUsername().toString(),
                                receivedPacket.getMessage().toString());
                        if (fl.get(0).equals("false")) {
                            Packet errorPacket = new Packet(auth, "broadcastError", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(errorPacket));
                        }
                        connectionSocket.setSoTimeout(timeout);
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("whoelse") == true) {

                        ArrayList<String> fl = whoelse(receivedPacket.getUsername().toString());

                        Packet whoelsePacket = new Packet(auth, "whoelse", "0", "0", fl.get(1));
                        ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(whoelsePacket));

                        connectionSocket.setSoTimeout(timeout);
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("whoelsesince") == true) {
                        ArrayList<String> fl = whoelsesince(receivedPacket.getUsername().toString(),
                                Float.valueOf(receivedPacket.getMessage().toString()));

                        Packet whoelsesincePacket = new Packet(auth, "whoelse", "0", "0", fl.get(1));
                        ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(whoelsesincePacket));

                        connectionSocket.setSoTimeout(timeout);
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("block") == true) {

                        ArrayList<String> fl = block(receivedPacket.getUsername().toString(),
                                receivedPacket.getMessage().toString());
                        if (fl.get(0).equals("true")) {

                            Packet blockPacket = new Packet(auth, "block", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(blockPacket));

                            connectionSocket.setSoTimeout(timeout);
                        } else {
                            Packet blockErrorPacket = new Packet(auth, "blockError", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(blockErrorPacket));

                            connectionSocket.setSoTimeout(timeout);
                        }
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("unblock") == true) {

                        ArrayList<String> fl = unblock(receivedPacket.getUsername().toString(),
                                receivedPacket.getMessage().toString());
                        if (fl.get(0).equals("true")) {

                            Packet blockPacket = new Packet(auth, "unblock", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(blockPacket));

                            connectionSocket.setSoTimeout(timeout);
                        } else {
                            Packet unblockErrorPacket = new Packet(auth, "unblockError", "0", "0", fl.get(1));
                            ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos.writeObject(Packet.buildString(unblockErrorPacket));

                            connectionSocket.setSoTimeout(timeout);
                        }
                        continue;
                    }
                    if (auth.equals("true") && receivedPacket.getRequest().toString().equals("logout") == true) {
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                user.setOnline(false);
                                user.setSocket(null);
                                user.setTime(LocalDateTime.now());
                            }
                        }
                        System.out.println("try to handle log out!");
                        String logoutInfo = receivedPacket.getUsername() + " has successfully logged out!";
                        Packet logoutPacket = new Packet(auth, "logout", "0", "0", logoutInfo);
                        ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(logoutPacket));
                        System.out.println("notify other users!");
                        for (User user : users) {
                            if (user.isOnline() == true) {
                                String notification = receivedPacket.getUsername() + "has already logged out!";
                                Packet notificationPacket = new Packet(auth, "logout", "0", "0", notification);
                                ObjectOutputStream newoos = new ObjectOutputStream(connectionSocket.getOutputStream());
                                newoos.writeObject(Packet.buildString(notificationPacket));
                            }
                        }
                        auth = "false";
                        continue;
                    }

                    if (commandList.contains(receivedPacket) == false) {
                        Packet unknownPacket = new Packet(auth, "error", "0", "0", "Unkown Command");
                        ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(unknownPacket));
                        connectionSocket.setSoTimeout(timeout);
                        continue;
                    }

                } catch (SocketTimeoutException ex) {
                    String exceptionInfo = "Your connection has been closed due to Timeout!";
                    /*Packet errorPacket = new Packet(auth, "timeout", "0", "0", exceptionInfo);
                    ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                    oos.writeObject(Packet.buildString(errorPacket));
                    connectionSocket.close();*/
                    System.exit(1);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    /*String exceptionInfo = "Your packet can not be accepted by server!";
                    Packet errorPacket = new Packet(auth, "timeout", "0", "0", exceptionInfo);
                    ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                    oos.writeObject(Packet.buildString(errorPacket));
                    connectionSocket.close();*/
                    System.exit(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }

    }
}
