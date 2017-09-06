/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 *
 * @author Fred
 */
public class TestImageStereogram extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    Image imgStereo ;
    static public BufferedImage od, og ;
    static public BufferedImage bimage, image ;
    
    public TestImageStereogram () {
        
        //Image ?
        imgStereo = getToolkit().getImage(getClass().getResource("/Ressources/polytope.png"));
        //Try to load directly an bufferedImage
        URL url = TestImageStereogram.class.getResource("/Ressources/polytope.png");
     
        try {bimage = ImageIO.read(url);} catch (IOException io) {System.out.println ("IO !!") ;}
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Stéréogramme: ") ;
        setLayout(null);
        this.setSize(1000, 700);
        getContentPane().setBackground( Color.WHITE );
        
        
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        
        //Some internal stuff in Image
        //ImageIcon temporary = new ImageIcon(imgStereo);
        
        
        /*JLabel tt = new JLabel (new ImageIcon(imgStereo)) ; tt.setText("");
        tt.setBounds (10, 10, 600, 300) ;
        this.getContentPane().add(tt);
        System.out.println (imgStereo.getWidth(null)) ;*/
        /*// Create a buffered image with transparency
        int n = 0 ;
        do { n = temporary.getWidth(null);}
        while (n == -1) ;
        bimage = new BufferedImage(temporary.getWidth(null), temporary.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(temporary, 0, 0, null);
        bGr.dispose();*/
        
        /*//Autre façon de copier
        bimage = new BufferedImage(temporary.getImage().getWidth(null), temporary.getImage().getHeight(null), BufferedImage.TYPE_INT_RGB);
        bimage.getGraphics().drawImage(temporary.getImage(), 0, 0, null);*/
        
        /*//On coupe en deux
        BufferedImage sub = bimage.getSubimage(0, 0, 300, 300) ;
        // Create empty compatible image
        od = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = od.createGraphics();
        try {
            g.drawImage(sub, 0, 0, null);
        }
        finally {
            g.dispose();
        }*/
        
        //Un panel pour afficher ça
        OneEyeBis xpanel = new OneEyeBis (bimage) ;
        xpanel.setLocation((this.getWidth()-xpanel.getWidth()) / 2, (this.getHeight()-xpanel.getHeight())/2);
        //xpanel.setLocation(100, 100);
        //System.out.println (xpanel.getWidth()) ;
        this.getContentPane().add(xpanel) ;
        
    }

    @Override
    public void windowOpened(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosing(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosed(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowIconified(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}


class OneEyeBis extends JPanel {
    
    BufferedImage oo ;
    
    public OneEyeBis (BufferedImage oo) {
        this.oo = oo ;
        
        System.out.println("xpanel: " +oo.getWidth());
        this.setSize(TestImageStereogram.bimage.getWidth(null), TestImageStereogram.bimage.getHeight(null));
        this.setOpaque(false);
    }
    
    public void paint(Graphics g) {
        //super.paintComponent(g);
        g.drawImage(TestImageStereogram.bimage, 0,0,this);
    }
    
}