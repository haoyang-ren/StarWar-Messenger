import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Haoyang Ren z5183825
 */
public class Server extends Thread {

    /**
     * @param args the command line arguments
     */
    static ArrayList<User> users = new ArrayList<User>();
    static String serverName;
    static int serverPort;
    static int currentPort;
    static int privatePort;
    static long blockDuration;
    static int timeout;
    private Socket connectionSocket;
    ServerSocket serverSocket;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    /**
     * Constructor Constructs server to create multiple threads with socket and
     * IOputStream
     */
    public Server(Socket connectionSocket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.connectionSocket = connectionSocket;
        this.oos = oos;
        this.ois = ois;
    }

    /**
     *
     * The main method for reading file and establish connections
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        if (args.length != 3) {
            System.out.println("Usage: java Server server_port block_duration timeout");
            System.exit(1);
        }
        // Get the arguments value
        serverPort = Integer.parseInt(args[0]);
        currentPort = serverPort;
        privatePort = serverPort + 1;
        blockDuration = Long.parseLong(args[1]);
        timeout = Integer.parseInt(args[2]);

        // Create the new server socket
        ServerSocket serverSocket = new ServerSocket(serverPort);
        serverName = "localhost";

        // Read the file from current directory
        String root = System.getProperty("user.dir");
        String FileName = "credentials.txt";
        String filePath = root + File.separator + File.separator + FileName;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        // Create all the users and add them to the users
        while ((line = reader.readLine()) != null) {
            String[] word = line.split(" ");
            User user = new User(word[0], word[1], 0, null, false, null, null, null, 0);
            users.add(user);

        }
        reader.close();
        while (true) {
            // Get the connection socket and address
            Socket connectionSocket = serverSocket.accept();
            SocketAddress connectionAddress = connectionSocket.getRemoteSocketAddress();

            // Create the IOputStream for each individual socket
            OutputStream os = connectionSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            InputStream is = connectionSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            // Start a new thread to handle the requests of client
            Server server = new Server(connectionSocket, oos, ois);
            server.setDaemon(true);

            server.start();
        }

    }

    /**
     * Executes the "login" command from the client 
     * Return the authorization and message back to client
     *
     * @param username the username of client input
     * @param password the password of client input
     * @param connectionSocket the socket connect server and client
     * @param currentPort the port number used for the socket
     * @throws IOException
     *
     */
    public ArrayList<String> login(String username, String password, Socket connectionSocket, int currentPort) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (user.isOnline() == true) {
                    result.add("false");
                    result.add("Error. You have already logged in");

                    return result;
                }
                if (user.getBlock() >= 3) {
                    result.add("false");
                    result.add("Error. You have been blocked by the server due to multiple invalid password");

                    return result;
                }
                if (user.getPassword().equals(password) == false) {
                    int newBlock = user.getBlock() + 1;
                    user.setBlock(newBlock);
                    if (user.getBlock() >= 3) {
                        Task task = new Task(user);
                        Timer timer = new Timer();
                        timer.schedule(task, blockDuration);
                        result.add("false");
                        result.add("Invalid password");
                        return result;
                    }
                    result.add("false");
                    result.add("Invalid password");
                }
                if (user.getPassword().equals(password)) {
                    user.setOnline(true);
                    user.setSocket(connectionSocket);
                    user.setOis(ois);
                    user.setOos(oos);
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

    /**
     * Executes the "message" command from the client 
     * Return the authorization and message back to client
     *
     * @param source the username of message source
     * @param destination the username of message destination
     * @param content the message content be delivered
     * @throws IOException 
     *
     */
    public ArrayList<String> sendMessage(String source, String destination, String content) throws IOException {
        String auth = "true";
        ArrayList<String> result = new ArrayList<String>();
        if (source.equals(destination) == true) {
            result.add("false");
            result.add("Error. Cannot message self");
            return result;
        } else {
            for (User user : users) {
                if (user.getUsername().equals(destination)) {
                    if (user.getBlackList() != null) {
                        if (user.getBlackList().contains(source)) {
                            result.add("false");
                            result.add("Your message could not be delivered as the recipient has blocked you");

                            return result;
                        }
                    }
                    if (user.isOnline() == true) {
                        Packet messeagePacket = new Packet(auth, "message", "0", "0", source + ": " + content);
                        oos = user.getOos();
                        oos.writeObject(Packet.buildString(messeagePacket));
                        result.add("true");
                        result.add("online");

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
                        return result;
                    }
                }
            }
        }
        result.add("false");
        result.add("Invalid user");
        return result;
    }
    /**
     * Executes the "broadcast" command from the client 
     * Return the authorization and message back to client
     *
     * @param source the username of message source
     * @param content the message content be delivered
     * @throws IOException 
     *
     */
    public ArrayList<String> broadcast(String source, String content) throws IOException {
        ArrayList<String> result = new ArrayList<String>();

        boolean skip = false;
        for (User user : users) {
            if (user.getUsername().equals(source) == false && user.isOnline() == true) {
                if (user.getBlackList() != null) {
                    if (user.getBlackList().contains(source) == true) {
                        skip = true;
                        break;
                    }
                }
                Packet broadcastPacket = new Packet("True", "broadcast", "0", "0", content);
                skip = false;
                oos = user.getOos();
                oos.writeObject(Packet.buildString(broadcastPacket));
            }
        }
        if (skip == false) {
            result.add("true");
            result.add("Your message has been broadcast successfully!");
            return result;
        } else {
            result.add("false");
            result.add("Your message could not be delivered to some recipients");
        }
        return result;
    }
    /**
     * Executes the "whoelse" command from the client 
     * Return the list of username back to client
     *
     * @param source the username of message source
     * 
     *
     */
    public ArrayList<String> whoelse(String source) {
        ArrayList<String> result = new ArrayList<String>();
        for (User user : users) {
            if (user.isOnline() == true && user.getUsername().equals(source) == false) {
                result.add(user.getUsername());
            }
        }
        return result;
    }
    /**
     * Executes the "whoelsesince" command from the client 
     * Return the list of username back to client after a period of time
     *
     * @param source the username of message source
     * @param second the period of time in second
     *
     */
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
    /**
     * Executes the "block" command from the client 
     * Add the peer name into the user blackList
     *
     * @param source the username of message source
     * @param destination the blocking client
     *
     */
    public ArrayList<String> block(String source, String destination) {
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
            for (User user : users) {
                if (user.getUsername().equals(source)) {
                    if (user.getBlackList() == null) {
                        ArrayList<String> newBlackList = new ArrayList<String>();
                        newBlackList.add(destination);
                        user.setBlackList(newBlackList);
                    } else {
                        ArrayList<String> newBlackList = user.getBlackList();
                        newBlackList.add(destination);
                        user.setBlackList(newBlackList);
                    }
                    result.add("true");
                    result.add(destination + " is blocked");
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
    /**
     * Executes the "unblock" command from the client 
     * Remove the peer name from the user blackList
     *
     * @param source the username of message source
     * @param destination the unblocking client
     *
     */
    public ArrayList<String> unblock(String source, String destination) {
        ArrayList<String> result = new ArrayList<String>();
        boolean find = false;
        if (source.equals(destination)) {
            result.add("false");
            result.add("Error. Cannot unblock self");
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
                    if (user.getBlackList() == null) {
                        result.add("false");
                        result.add("Error. " + destination + " was not blocked");
                        return result;
                    }
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
/**
     * Executes the "startprivate" command from the client 
     * Establish the connections between users
     *
     * @param source the username of message source
     * @param destination the username of message destination
     *
     */
    public ArrayList<String> startprivate(String source, String destination) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        if (source.equals(destination)) {
            result.add("false");
            result.add("Error. Cannot message privately self");
            return result;
        }
        for (User user : users) {
            if (user.getUsername().equals(destination)) {
                if (user.getBlackList().contains(source)) {
                    result.add("false");
                    result.add("Your message could not be delivered as the recipient has blocked you");
                    return result;
                }
                if (user.isOnline() == false) {
                    result.add("false");
                    result.add("Error. Private messaging to " + destination + " not enabled");
                    return result;
                } else {
                    Packet privatePacket = new Packet("True", "startprivate_listen", "0", "0", "Start private messaging with " + source);
                    oos = user.getOos();
                    oos.writeObject(privatePacket);
                    privatePort++;
                    result.add("true");
                    result.add(Integer.toString(privatePort - 1));
                }
            }
        }
        result.add("false");
        result.add(destination + " is not a valid user");
        return result;
    }

    /**
     * The new thread to handle the user request
     *
     *
     * @param socket 
     * @param ObjectInputStream
     * @param ObjectOutPutStream
     * @throws IOException
     */
    @Override
    public void run() {
        String[] command = {"login", "message", "logout", "broadcast", "block", "unblock", "whoelse", "whoelsesince", "startprivate"};
        List<String> commandList = Arrays.asList(command);
        String auth = "false";
        while (true) {
            try {
                // Receive the packet from the client
                Packet receivedPacket = Packet.fromString((String) this.ois.readObject());

                // Try to login the system
                if (auth.equals("false") == true && receivedPacket.getRequest().toString().equals("login")) {
                    ArrayList<String> fl = login(receivedPacket.getUsername().toString(),
                            receivedPacket.getPassword().toString(), connectionSocket, currentPort);

                    if (fl.get(0).equals("false")) {
                        Packet errorPacket = new Packet(auth, "error", "0", "0", fl.get(1));
                        oos.writeObject(Packet.buildString(errorPacket));
                    } else {
                        auth = "true";
                        Packet welcomePacket = new Packet(auth, "login", "0", "0", fl.get(1));
                        oos.writeObject(Packet.buildString(welcomePacket));
                        Thread.sleep(50);
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                if (user.getPending() != null) {
                                    for (String pending : user.getPending()) {
                                        Packet pendingPacket = new Packet(auth, "message", "0", "0", pending);
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
                                String duplicateLogin = receivedPacket.getUsername().toString() + " has logged in";
                                Packet duplicateLoginPacket = new Packet(auth, "notification", "0", "0",
                                        duplicateLogin);
                                oos = user.getOos();
                                oos.writeObject(Packet.buildString(duplicateLoginPacket));
                            }
                        }
                        connectionSocket.setSoTimeout(timeout);
                    }
                    continue;
                }

                // Request to send message to other clients
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("message") == true) {
                    String strArray[] = receivedPacket.getMessage().toString().split(" ");
                    List<String> strList = new ArrayList<String>(Arrays.asList(strArray));
                    String receiver = strList.get(0);
                    strList.remove(0);
                    String content = String.join(" ", strList);
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
                    continue;
                }
                
                // Request to broadcast message to other clients
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("broadcast") == true) {

                    ArrayList<String> fl = broadcast(receivedPacket.getUsername(), receivedPacket.getUsername() + ": "
                            + receivedPacket.getMessage());
                    if (fl.get(0).equals("false")) {
                        Packet errorPacket = new Packet(auth, "broadcastError", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(errorPacket));
                    }
                    continue;
                }
                
                // Check the other users except for the self
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
                    continue;
                }
                
                // Check the other users except for the self after a period of time
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
                    continue;
                }
                
                // Add the user to the blackList
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
                
                 // Remove the user from the blackList
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
                    }
                    continue;
                }
                
                // User logout the system, clean up the socket and record the time
                if (auth.equals("true") && receivedPacket.getRequest().toString().equals("logout") == true) {
                    for (User user : users) {
                        if (user.getUsername().equals(receivedPacket.getUsername())) {
                            user.setOnline(false);
                            user.setSocket(null);
                            user.setTime(LocalDateTime.now());
                        }
                    }
                    Packet logoutPacket = new Packet(auth, "logout", "0", "0", "You have successfully logged out!");
                    for (User user : users) {
                        if (user.getUsername().equals(receivedPacket.getUsername())) {
                            oos = user.getOos();
                            break;
                        }
                    }
                    oos.writeObject(Packet.buildString(logoutPacket));
                    for (User user : users) {
                        if (user.isOnline() == true) {
                            String notification = receivedPacket.getUsername() + " logged out";
                            Packet notificationPacket = new Packet(auth, "logout", "0", "0", notification);
                            oos = user.getOos();
                            oos.writeObject(Packet.buildString(notificationPacket));
                        }
                    }
                    auth = "false";
                    // Server would ask the user to relogin after the timeout duration
                    connectionSocket.setSoTimeout(timeout);
                    continue;
                }
                
                // Handle the startprivate between two clients
                if (receivedPacket.getRequest().equals("startprivate")) {
                    ArrayList<String> fl = startprivate(receivedPacket.getUsername(), receivedPacket.getMessage());
                    if (fl.get(0).equals("true")) {
                        Packet privatePacket = new Packet("true", "startprivate_connect", "0", "0", "Start private messaging with " + receivedPacket.getMessage());
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(privatePacket));
                    } else {
                        Packet privateErrorPacket = new Packet("true", "startprivate_error", "0", "0", fl.get(1));
                        for (User user : users) {
                            if (user.getUsername().equals(receivedPacket.getUsername())) {
                                oos = user.getOos();
                                break;
                            }
                        }
                        oos.writeObject(Packet.buildString(privateErrorPacket));
                    }
                    continue;
                }
                // Print error for the unknown command
                if (commandList.contains(receivedPacket) == false) {
                    Packet unknownPacket = new Packet(auth, "error", "0", "0", "Unknown Command");
                    oos.writeObject(Packet.buildString(unknownPacket));
                    continue;
                }
            
            // Automatically logged out the user after the timeout
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
        }
    }
}

class Task extends TimerTask{
    private User user;

    public Task(User user) {
        this.user = user;
    }
    
    @Override
    public void run() {
        // Set the block times to zero after the block_duration
        user.setBlock(0);
    }
    
}

class User {

    private String username;
    private String password;
    private ArrayList<String> blackList;
    // stored the offline message
    private ArrayList<String> pending;
    // blocked numbers during the login
    private int block;
    private int port;
    private boolean online;
    private LocalDateTime time;
    // Each client has individual socket and IOStream
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public User(String username, String password, int block, ArrayList<String> blackList, boolean online, ArrayList<String> pending, Socket socket, LocalDateTime time, int port) {
        this.username = username;
        this.password = password;
        this.block = block;
        this.blackList = blackList;
        this.online = online;
        this.pending = pending;
        this.socket = socket;
        this.time = time;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public ArrayList<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(ArrayList<String> blackList) {
        this.blackList = blackList;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public ArrayList<String> getPending() {
        return pending;
    }

    public void setPending(ArrayList<String> pending) {
        this.pending = pending;
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ObjectInputStream getOis() throws IOException {  
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public ObjectOutputStream getOos() throws IOException {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }
}

class Packet implements Serializable {

    private String auth;
    private String request;
    private String username;
    private String password;
    private String message;

    public Packet() {

    }

    public Packet(String auth, String request, String username, String password, String message) {
        this.auth = auth;
        this.request = request;
        this.username = username;
        this.password = password;
        this.message = message;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    // Tear down the packet into Strings
    public static Packet fromString(String string) {
        Packet p = new Packet();
        String strArray[] = string.split("\n");
        p.setAuth(strArray[0]);
        p.setRequest(strArray[1]);
        p.setUsername(strArray[2]);
        p.setPassword(strArray[3]);
        p.setMessage(strArray[4]);

        return p;
    }
    
    // Composite the packet from string
    public static String buildString(Packet p) {
        String string = p.getAuth() + "\n" + p.getRequest() + "\n" + p.getUsername() + "\n" + p.getPassword() + "\n"
                + p.getMessage();

        return string;
    }

}
