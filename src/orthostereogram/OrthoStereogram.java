/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import ch.aplu.xboxcontroller.XboxController;
import javax.swing.JOptionPane;

/**
 *
 * @author Fred
 */
public class OrthoStereogram {
    
    public static final String VERSION = "0.3.3" ;

    /**
     * @param args the command line arguments
     */
    
    public static XboxController xbox ;
    static public boolean xboxConnected = false ;
    
    public static void main(String[] args) {
        // no flickering on resize
        System.setProperty("sun.awt.noerasebackground", "true");
        
        //On initialise la xbox
        try {
            xbox = new XboxController((is64bit()? "xboxcontroller64" : "xboxcontroller"), 1, 50, 50);
        } catch (Exception e) {JOptionPane.showMessageDialog(null, "Erreur init xbox (dll)!!"); }
        xboxConnected = xbox.isConnected() ;
                
        //Main Frame
        NewController controller = new NewController (xboxConnected) ;
        //controller.setSize(450, 650);
        controller.setLocationRelativeTo(null);
        controller.setVisible(true);
    }
    
    static boolean is64bit() {
        return System.getProperty("sun.arch.data.model").equals("64");
    }
    
}
