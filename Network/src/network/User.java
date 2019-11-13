 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author ryan
 */
public class User {

    private String username;
    private String password;
    private int block;
    private ArrayList<String> blackList;
    private boolean online;
    private ArrayList<String> pending;
    private Socket socket;
    private LocalDateTime time;
    private int port;

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
    
    
    
    
    

}
