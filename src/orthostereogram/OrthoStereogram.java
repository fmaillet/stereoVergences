/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import ch.aplu.xboxcontroller.XboxController;
import java.awt.Toolkit;
import javax.swing.JOptionPane;

/**
 *
 * @author Fred
 */
public class OrthoStereogram {
    
    public static final String VERSION = "0.6.2" ;
    public static boolean BR_glasses = true ;
    
    public static int screenResolution ;
    public static NewController controller ;

    /**
     * @param args the command line arguments
     */
    
    public static XboxController xbox ;
    static public boolean xboxConnected = false ;
    
    public static void main(String[] args) {
        
        screenResolution = Toolkit.getDefaultToolkit().getScreenResolution() ;
        
        // no flickering on resize
        System.setProperty("sun.awt.noerasebackground", "true");
        
        //On initialise la xbox
        try {
            xbox = new XboxController((is64bit()? "xboxcontroller64" : "xboxcontroller"), 1, 50, 50);
            xboxConnected = xbox.isConnected() ;
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, "Pas de xBox 360 reconnue !!\n(dll absente)");
        }
        
        
        //Main Frame
        controller = new NewController (xboxConnected) ;
        //controller.setSize(450, 650);
        controller.setLocationRelativeTo(null);
        controller.setVisible(true);
    }
    
    static boolean is64bit() {
        return System.getProperty("sun.arch.data.model").equals("64");
    }
    
}
