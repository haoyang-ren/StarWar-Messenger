/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.Serializable;

/**
 *
 * @author ryan
 */
public class Packet implements Serializable {

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
