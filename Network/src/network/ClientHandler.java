/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryan
 */
public class ClientHandler extends Thread{
    User user;
    Packet packet;
    
    public ClientHandler(User user, Packet packet){
        this.user = user;
        this.packet = packet;
    }
    
    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(user.getSocket().getOutputStream());
            oos.writeObject(Packet.buildString(packet));
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }

}
