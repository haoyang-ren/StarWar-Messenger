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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    ServerSocket serverSocket;
    static ArrayList<User> users = new ArrayList<User>();
    static String serverName;
    static int serverPort;
    static int currentPort;
    static float blockDuration;
    static int timeout;
    private Socket connectionSocket;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    public Server(Socket connectionSocket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.connectionSocket = connectionSocket;
        this.oos = oos;
        this.ois = ois;
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
        ServerSocket serverSocket = new ServerSocket(serverPort);
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
            //System.out.println("name is " + word[0]);
            //System.out.println("password is " + word[1]);
            User user = new User(word[0], word[1], 0, null, false, null, null, null, 0);
            users.add(user);

        }
        reader.close();

        // System.out.println("Program has arrived here");
        while (true) {
            // Get the connection socket and address
            Socket connectionSocket = serverSocket.accept();
            SocketAddress connectionAddress = connectionSocket.getRemoteSocketAddress();
            OutputStream os = connectionSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            InputStream is = connectionSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            // Use connection socket to send current port number
            /*
			 * DataOutputStream outToClient = new
			 * DataOutputStream(connectionSocket.getOutputStream()); String clientPortNumber
			 * = String.valueOf(currentPort); outToClient.writeBytes(clientPortNumber);
			 * connectionSocket.close();
             */
            //Server server = new Server(connectionSocket, oos, ois);
            Server server = new Server(connectionSocket, oos, ois);
            server.setDaemon(true);

            server.start();
            // currentPort++;
        }

    }

    public ArrayList<String> login(String username, String password, Socket connectionSocket, int currentPort) throws IOException {
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
                    user.setOis(ois);
                    user.setOos(oos);
                    if (user.getSocket() == null) {
                        System.out.println("user don't have socket at login!");
                    } else {
                        System.out.println("user already have socket at login");
                    }
                    //System.out.println("user socket is " + user.getSocket().getInetAddress().toString() + user.getSocket().getLocalPort());
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
            result.add("Error. Cannot message self");
            System.out.println("In the first condition");
            return result;
        } else {
            for (User user : users) {
                if (user.getUsername().equals(destination)) {
                    if (user.getBlackList() != null) {
                        if (user.getBlackList().contains(source)) {
                            result.add("false");
                            result.add("Your message could not be delivered as the recipient has blocked you");
                            System.out.println("In the second condition");
                            return result;
                        }
                    }
                    if (user.isOnline() == true) {
                        Packet messeagePacket = new Packet(auth, "message", "0", "0", source + ": " + content);
                        //oos = new ObjectOutputStream(user.getSocket().getOutputStream());
                        //System.out.println("!!!user socket is " + user.getSocket().toString());
                        /*System.out.println("In the third condition");
                        if (user.getSocket() == null) {
                            System.out.println("user don't have socket!");
                        } else {
                            System.out.println("user already have socket!");
                        }
                        System.out.println("receiver user name is " + user.getUsername());*/
                        oos = user.getOos();

                        //ObjectOutputStream oos = user.getOos();
                        //connectionSocket.setKeepAlive(true);
                        //oos.writeObject(Packet.buildString(messeagePacket));
                        //Thread sendThread = new ClientHandler(user, messeagePacket);
                        //sendThread.start();
                        if (oos == null) {
                            System.out.println("Nothing in oos!");
                        } else {
                            System.out.println("Something inside oos!");
                        }
                        oos.writeObject(Packet.buildString(messeagePacket));
                        //connectionSocket.setKeepAlive(true);
                        result.add("true");
                        result.add("online");
                        System.out.println("In the forth condition");
                        return result;
                    } else {
                        String pending = "{Offline message} " + source + ": " + content;
                        if (user.getPending() == null) {
                            ArrayList<String> pendingList = new ArrayList<String>();
                            pendingList.add(pending);
                            user.setPending(pendingList);
                        } else {
                            ArrayList<String> pendingList = user.getPending();
                            pendingList.add(pending);
                            user.setPending(pendingList);
                        }
                        result.add("true");
                        result.add("offline");
                        System.out.println("In the fifth condition");
                        return result;
                    }

                }
            }

        }
        result.add("false");
        result.add("Invalid user");
        System.out.println("In the last condition");
        return result;
    }

    public ArrayList<String> broadcast(String source, String content) throws IOException {
        ArrayList<String> result = new ArrayList<String>();

        boolean skip = false;
        for (User user : users) {
            if (user.getUsername().equals(source) == false && user.isOnline() == true) {
                //if (user.getBlackList() == null) {

                //}
                if (user.getBlackList() != null) {
                    if (user.getBlackList().contains(source) == true) {
                        //System.out.println("In the blacklist condition!!!!! skip is " + skip);
                        //System.out.println(user.getUsername() + " has the balcklist" + Arrays.toString(user.getBlackList().toArray()));
                        skip = true;
                        break;
                    }
                }
                Packet broadcastPacket = new Packet("True", "broadcast", "0", "0", content);
                skip = false;
                //System.out.println("In the broadcast condition!!!!! skip is " + skip);
                //if(user.getBlackList() != null){
                //    System.out.println(user.getUsername() + " has the balcklist" + Arrays.toString(user.getBlackList().toArray()));
                //}
                oos = user.getOos();
                oos.writeObject(Packet.buildString(broadcastPacket));
            }
        }
        if (skip == false) {
            result.add("true");
            result.add("Your message has been broadcast successfully!");
            //System.out.println("In the success return");
            return result;
        } else {
            result.add("false");
            result.add("Your message could not be delivered to some recipients");
            //System.out.println("In the failure condition");
        }
        //System.out.println("get out of the loop---------");
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
                    LocalDateTime currentTime = LocalDateTime.now();
                    long duration = ChronoUnit.SECONDS.between(currentTime, user.getTime());
                    if ((float) duration <= second) {
                        result.add(user.getUsername());
                    }
                }
            }
        }
        return result;
    }

    public ArrayList<String> block(String source, String destination) {
        System.out.println("the source is " + source);
        System.out.println("the destination is " + destination);

        ArrayList<String> result = new ArrayList<String>();
        boolean find = false;
        if (source.equals(destination)) {
            result.add("false");
            result.add("Error. Cannot block self");
            return result;
        }

        for (User user : users) {
            if (user.getUsername().equals(destination)) {
                find = true;
            }
        }

        if (find == true) {
            System.out.println("The find boolean is true");
            for (User user : users) {

                if (user.getUsername().equals(source)) {
                    System.out.println("find the user");
                    ArrayList<String> newBlackList = new ArrayList<String>();
                    newBlackList.add(destination);
                    user.setBlackList(newBlackList);
                    result.add("true");
                    result.add(destination + " is blocked");
                    System.out.println(user.getUsername() + "=============has the BlackList is " + user.getBlackList().get(0));
                    System.out.println("result[0] is " + result.get(0));
                    System.out.println("result[1] is " + result.get(1));
                    return result;
                }

            }
        } else {
            System.out.println("The find boolean is false");
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
            }
        }
        if (find == true) {
            for (User user : users) {
                if (user.getUsername().equals(source)) {
                    if (user.getBlackList().contains(destination) == false) {
                        result.add("false");
                        result.add("Error. " + destination + " was not blocked");
                        return result;
                    }
                    ArrayList<String> newBlackList = user.getBlackList();
                    newBlackList.remove(destination);
                    user.setBlackList(newBlackList);
                    result.add("true");
                    result.add(destination + " is unblocked");
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

    @Override
    public void run() {
        String[] command = {"login", "message", "logout", "broadcast", "block", "unblock", "whoelse", "whoelsesince"};
        List<String> commandList = Arrays.asList(command);
        String auth = "false";
        while (true) {
            System.out.println("Recevied packet from client");
            try {
                //ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                Packet receivedPacket = Packet.fromString((String) this.ois.readObject());
                System.out.println("Analyse request!!!!!!!!!!!!!!!!!!!!");
                System.out.println("auth is " + receivedPacket.getAuth());
                System.out.println("request is " + receivedPacket.getRequest());
                System.out.println("username is " + receivedPacket.getUsername());
                System.out.println("password is " + receivedPacket.getPassword());
                System.out.println("message is " + receivedPacket.getMessage());
                System.out.println("End of Analyse request!!!!!!!!!!!!!!!!!!!!");
                // Try to login the system
                if (auth.equals("false") == true && receivedPacket.getRequest().toString().equals("login")) {
                    ArrayList<String> fl = login(receivedPacket.getUsername().toString(),
                            receivedPacket.getPassword().toString(), connectionSocket, currentPort);

                    if (fl.get(0).equals("false")) {
                        Packet errorPacket = new Packet(auth, "error", "0", "0", fl.get(1));
                        //ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(errorPacket));
                    } else {
                        auth = "true";
                        Packet welcomePacket = new Packet(auth, "login", "0", "0", fl.get(1));
                        //ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        oos.writeObject(Packet.buildString(welcomePacket));
                        //System.out.println("Recevied the welcome packet!");
                        Thread.sleep(50);
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                if (user.getPending() != null) {
                                    for (String pending : user.getPending()) {
                                        Packet pendingPacket = new Packet(auth, "message", "0", "0", pending);
                                        //ObjectOutputStream newoos = new ObjectOutputStream(
                                        //connectionSocket.getOutputStream());
                                        oos.writeObject(Packet.buildString(pendingPacket));
                                    }
                                    user.setPending(null);
                                }
                            }
                        }
                        for (User user : users) {
                            if (user.isOnline() == true
                                    && user.getUsername().equals(receivedPacket.getUsername()) == false) {
                                Socket userSocket = user.getSocket();
                                //user.setOos(oos);
                                //user.setOis(ois);
                                String duplicateLogin = receivedPacket.getUsername().toString() + " has logged in";
                                Packet duplicateLoginPacket = new Packet(auth, "notification", "0", "0",
                                        duplicateLogin);
                                oos = user.getOos();
                                oos.writeObject(Packet.buildString(duplicateLoginPacket));
                            }
                        }
                        //connectionSocket.setSoTimeout(timeout);
                    }
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("message") == true) {
                    //System.out.println("message is "+ receivedPacket.getMessage());
                    String strArray[] = receivedPacket.getMessage().toString().split(" ");
                    List<String> strList = new ArrayList<String>(Arrays.asList(strArray));
                    String receiver = strList.get(0);
                    strList.remove(0);
                    String content = String.join(" ", strList);

                    //System.out.println("receiver name is "+receiver);
                    //System.out.println("content is "+ content);
                    ArrayList<String> fl = sendMessage(receivedPacket.getUsername().toString(), receiver,
                            content);
                    if (fl.get(0).equals("false")) {
                        Packet messagePacket = new Packet(auth, "messageError", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(messagePacket));
                    }
                    //connectionSocket.setSoTimeout(timeout);
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("broadcast") == true) {

                    ArrayList<String> fl = broadcast(receivedPacket.getUsername(), receivedPacket.getUsername() + ": "
                            + receivedPacket.getMessage());
                    if (fl.get(0).equals("false")) {
                        Packet errorPacket = new Packet(auth, "broadcastError", "0", "0", fl.get(1));
                        //ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(errorPacket));
                    }
                    //connectionSocket.setSoTimeout(timeout);
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("whoelse") == true) {

                    ArrayList<String> fl = whoelse(receivedPacket.getUsername().toString());
                    String strArray[] = new String[fl.size()];
                    for (int i = 0; i < fl.size(); i++) {
                        strArray[i] = fl.get(i);
                    }
                    String result = Arrays.toString(strArray);
                    result = result.substring(1, result.length() - 1);
                    if (result.equals("")) {
                        result = "[ ]";
                    }
                    Packet whoelsePacket = new Packet(auth, "whoelse", "0", "0", result);
                    for (User user : users) {
                        if (user.getUsername().equals(receivedPacket.getUsername())) {
                            oos = user.getOos();
                            break;
                        }
                    }
                    oos.writeObject(Packet.buildString(whoelsePacket));

                    //connectionSocket.setSoTimeout(timeout);
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("whoelsesince") == true) {
                    ArrayList<String> fl = whoelsesince(receivedPacket.getUsername().toString(),
                            Float.valueOf(receivedPacket.getMessage().toString()));
                    String strArray[] = new String[fl.size()];
                    for (int i = 0; i < fl.size(); i++) {
                        strArray[i] = fl.get(i);
                    }
                    String result = Arrays.toString(strArray);
                    result = result.substring(1, result.length() - 1);
                    if (result.equals("")) {
                        result = "[ ]";
                    }
                    Packet whoelsesincePacket = new Packet(auth, "whoelsesince", "0", "0", result);
                    for (User user : users) {
                        if (user.getUsername().equals(receivedPacket.getUsername())) {
                            oos = user.getOos();
                            break;
                        }
                    }
                    oos.writeObject(Packet.buildString(whoelsesincePacket));

                    //connectionSocket.setSoTimeout(timeout);
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("block") == true) {

                    ArrayList<String> fl = block(receivedPacket.getUsername().toString(),
                            receivedPacket.getMessage().toString());
                    if (fl.get(0).equals("true")) {
                        Packet blockPacket = new Packet(auth, "block", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(blockPacket));
                    } else {
                        Packet blockErrorPacket = new Packet(auth, "blockError", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(blockErrorPacket));
                    }
                    continue;
                }
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("unblock") == true) {

                    ArrayList<String> fl = unblock(receivedPacket.getUsername().toString(),
                            receivedPacket.getMessage().toString());
                    if (fl.get(0).equals("true")) {

                        Packet blockPacket = new Packet(auth, "unblock", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(blockPacket));
                    } else {
                        Packet unblockErrorPacket = new Packet(auth, "unblockError", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(unblockErrorPacket));

                        //connectionSocket.setSoTimeout(timeout);
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
                    //System.out.println("try to handle log out!");
                    //String logoutInfo = receivedPacket.getUsername() + " has successfully logged out!";
                    Packet logoutPacket = new Packet(auth, "logout", "0", "0", "You have successfully logged out!");
                    //ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                    for (User user : users) {
                        if (user.getUsername().equals(receivedPacket.getUsername())) {
                            oos = user.getOos();
                            break;
                        }
                    }

                    oos.writeObject(Packet.buildString(logoutPacket));
                    //System.out.println("notify other users!");
                    for (User user : users) {
                        if (user.isOnline() == true) {
                            String notification = receivedPacket.getUsername() + " logged out";
                            Packet notificationPacket = new Packet(auth, "logout", "0", "0", notification);
                            //ObjectOutputStream newoos = new ObjectOutputStream(connectionSocket.getOutputStream());
                            oos = user.getOos();
                            oos.writeObject(Packet.buildString(notificationPacket));
                        }
                    }
                    auth = "false";
                    //System.out.println("Get the end of log out");
                    //connectionSocket.setSoTimeout(99999999);
                    //break;
                    continue;
                }

                if (commandList.contains(receivedPacket) == false) {
                    Packet unknownPacket = new Packet(auth, "error", "0", "0", "Unkown Command");
                    //ObjectOutputStream oos = new ObjectOutputStream(connectionSocket.getOutputStream());
                    oos.writeObject(Packet.buildString(unknownPacket));
                    //connectionSocket.setSoTimeout(timeout);
                    continue;
                }

            } catch (SocketTimeoutException ex) {
                String exceptionInfo = "Your connection has been automatically logged out due to Timeout!";
                Packet errorPacket = new Packet(auth, "timeout", "0", "0", exceptionInfo);
                try {
                    oos.writeObject(Packet.buildString(errorPacket));
                } catch (IOException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
                auth = "False";
                Packet receivedPacket = null;
                try {
                    receivedPacket = Packet.fromString((String) this.ois.readObject());
                } catch (IOException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                } catch (ClassNotFoundException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
                for (User user : users) {
                    if (user.getUsername().equals(receivedPacket.getUsername())) {
                        user.setOnline(false);
                        user.setSocket(null);
                        user.setTime(LocalDateTime.now());
                    }
                }
                try {
                    connectionSocket.setSoTimeout(timeout);
                } catch (SocketException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class
                        .getName()).log(Level.SEVERE, null, ex);

            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("At the end of while loop!");
        }

    }
}
