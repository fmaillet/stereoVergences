/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

/**
 *
 * @author Fred
 */
public class OrthoStereogram {
    
    public static final String VERSION = "0.1.0" ;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // no flickering on resize
        System.setProperty("sun.awt.noerasebackground", "true");
        
        //Main Frame
        NewController controller = new NewController () ;
        controller.setSize(400, 650);
        controller.setLocationRelativeTo(null);
        controller.setVisible(true);
    }
    
}
