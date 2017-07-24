/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_ADD;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SUBTRACT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import javax.swing.JFrame;
import org.newdawn.easyogg.OggClip;

/**
 *
 * @author Fred
 */
public class DisplayStereogram extends JFrame implements WindowListener, KeyListener, MouseMotionListener {
    
    static private Stereogram bimage  ;
    static private int imgSize = 400 ;
    OggClip audioOK = null ;
    OggClip audioBAD = null ;
    
    public DisplayStereogram () {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Stéréogramme: ") ;
        setLayout(null);
        this.setSize(1000, 700);
        getContentPane().setBackground( Color.WHITE );

        
        //On crée un stéréogramme
        bimage = new Stereogram (imgSize) ;
        bimage.resetImg (false) ; 
        getContentPane().add(bimage);
        
        //On crée les fichiers sons
        try { audioOK = new OggClip(this.getClass().getResourceAsStream("correct.ogg")); }
        catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}
        try { audioBAD = new OggClip(this.getClass().getResourceAsStream("incorrect.ogg")); }
        catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}
    }
    
    public void setSizes () {
        
        bimage.setLocation((this.getWidth()-bimage.getWidth()) / 2, (this.getHeight()-bimage.getHeight())/2);
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        bimage.setLocation((this.getWidth()-bimage.getWidth()) / 2, (this.getHeight()-bimage.getHeight())/2);
        addComponentListener(new ComponentAdapter() { 
            public void componentResized(ComponentEvent e) {
                setSizes () ;
            
            }
        }); 
        //Position
        setLocationRelativeTo(null);
    }
    
    public void hideCursor () {
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        setCursor(transparentCursor);
    }

    @Override
    public void windowOpened(WindowEvent we) {
        
    }

    @Override
    public void windowClosing(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosed(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowIconified(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        
         
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        
        if (keyCode == bimage.clue) {
            bimage.incConvergence (5) ;
            repaint () ;
            hideCursor () ;
            if (audioOK != null) audioOK.play() ;
        }
        else if (keyCode == VK_UP | keyCode == VK_DOWN | keyCode == VK_LEFT | keyCode == VK_RIGHT) {
            
            if (audioBAD != null) audioBAD.play() ;
        }
        
        if (keyCode == VK_ADD &  !ke.isControlDown()) System.out.println("Plus");
        else if ((keyCode == VK_SUBTRACT | keyCode == VK_6) & ke.isControlDown() & ! ke.isShiftDown()) {
            imgSize = (int) (imgSize * 0.9 ) ;
            bimage.resize(imgSize, true);
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            imgSize = (int) (imgSize * 1.1 ) ;
            bimage.resize(imgSize, true);
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            imgSize = (int) (imgSize * 1.1 ) ;
            bimage.resize(imgSize, true);
        }
        setSizes () ;
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        setCursor(Cursor.getDefaultCursor());
    }
 
    
}
