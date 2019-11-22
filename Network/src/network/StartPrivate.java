/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

/**
 *
 * @author ryan
 */
public class StartPrivate implements Runnable{
    String source;
    int portNUmber;
    String status;

    public StartPrivate(String source, int portNUmber, String status) {
        this.source = source;
        this.portNUmber = portNUmber;
        this.status = status;
    }
    
    
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
