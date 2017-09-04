/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.WritableRaster;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static orthostereogram.Stereogram.ana;

/**
 *
 * @author Fred
 */
public class SlideStereogramView extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    //Constructor
    
    static private Stereogram bimage  ;
    static private int imgSize = 400 ;
    private OneEye od, og ;
    private int deltaX = 300 ;
    
    public SlideStereogramView () {
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
        //setLocationRelativeTo(null);
        
        
        //Create stereogram
        bimage = new Stereogram (imgSize, 0) ; //initial delta set to zero
        bimage.resetImg (false) ;
        //Anaglyph
        BufferedImage ana = new BufferedImage(bimage.OD.getWidth(), bimage.OD.getHeight(), BufferedImage.TYPE_INT_RGB);
        Anaglyph anaglyph = new Anaglyph () ;
        anaglyph.createStereoscopicBlueImage (bimage.OD, ana) ;
        og = new OneEye (ana) ;
        anaglyph.createStereoscopicRedImage (bimage.OG, ana) ;
        od = new OneEye (ana);
        
        //Position
        od.setLocation((this.getWidth()-od.getWidth()) / 2 - deltaX, (this.getHeight()-od.getHeight())/2);
        og.setLocation((this.getWidth()-og.getWidth()) / 2 + deltaX, (this.getHeight()-og.getHeight())/2);
        this.getContentPane().add (od) ;
        this.getContentPane().add (og) ;
        
    }
    
    public void setPositions () {
        od.setLocation((this.getWidth()-od.getWidth()) / 2 - deltaX, (this.getHeight()-od.getHeight())/2);
        og.setLocation((this.getWidth()-og.getWidth()) / 2 + deltaX, (this.getHeight()-og.getHeight())/2);
    }
    
    public void hideCursor () {
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        setCursor(transparentCursor);
    }
    
    

    @Override
    public void keyTyped(KeyEvent ke) {
        
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        else if (keyCode == VK_LEFT) { deltaX--; setPositions () ; }
        else if (keyCode == VK_RIGHT) { deltaX++; setPositions () ; }
        
        hideCursor () ;
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

    @Override
    public void windowOpened(WindowEvent we) {
        hideCursor () ;
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
        hideCursor () ;
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

//Classe OD OG
class OneEye extends JPanel {
    
    private BufferedImage vue ;

    public OneEye (BufferedImage vue) {
        //Copie de l'image
        ColorModel cm = vue.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = vue.copyData(null);
        this.vue = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        //Taille du panel
        this.setSize(vue.getWidth(), vue.getHeight());
    }
    
    public void paint(Graphics g) {
        super.paintComponent(g);
        g.drawImage(vue, 0,0,this);
    }
}