/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.util.TimerTask;

/**
 *
 * @author ryan
 */
public class Task extends TimerTask{
    private User user;

    public Task(User user) {
        this.user = user;
    }
    
    @Override
    public void run() {
        user.setBlock(0);
    }
    
}
