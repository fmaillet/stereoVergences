/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.util.Random;
import javafx.scene.layout.Border;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Fred
 */
public class AccommodationJob extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    //Constructor
    Cursor transparentCursor ;
    CuedJLabel cue[] ;
    
    public AccommodationJob () {
        
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Accommodation: ") ;
        setLayout(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        getContentPane().setBackground( Color.WHITE );
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addWindowListener(this);
        
        //Add 5 JLabels
        cue = new CuedJLabel[5] ;
        for (int i= 0; i<5; i++) {
            cue[i] = new CuedJLabel () ;
            cue[i].setLocation(this.getContentPane().getWidth()/2 - cue[i].getWidth()*4 + cue[i].getWidth()*2*i, this.getContentPane().getHeight()/2 - cue[i].getHeight()/2);
            //cue[i].setBorder(new Border());
            this.getContentPane().add(cue[i]) ;
            cue[i].setVisible(true);
        }
            
        
    }
    
    public void hideCursor () {
        
        setCursor(transparentCursor);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        OrthoStereogram.controller.setEnabled(false) ;
        hideCursor () ;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        
    }

    @Override
    public void windowClosed(WindowEvent e) {
        OrthoStereogram.controller.setEnabled(true) ;
        OrthoStereogram.controller.setVisible(true);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

class CuedJLabel extends JLabel {
    
    //L'orientation de l'item
    private Random rand ;
    private int orientation  = 3 ;
    static int size = 4 ;

    //constructor
    public CuedJLabel () {
        rand = new Random() ;
        this.setText("");
        this.setSize(35,35);
        alea () ;
    }
    
    public void alea () {
        orientation = rand.nextInt(4) ;
    }
    
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke( 1.0f ));
        
        switch (orientation) {
            case 0 : g2.drawArc(6, 0, size, size, 30, 300); ; break ;
            case 1 : g2.drawArc(6, 0, size, size, 120, 300); ; break ;
            case 2 : g2.drawArc(6, 0, size, size, 210, 300); ; break ;
            default: g2.drawArc(6, 0, size, size, 300, 300); ; break ;
        }
        
    }
}