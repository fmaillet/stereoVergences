/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_ADD;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_SUBTRACT;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Fred
 */
public class SlideStereogramView extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    //Constructor
    Anaglyph anaglyph ;
    static private Stereogram bimage  ;
    private OneEye od, og ;
    private int deltaX = 0 ;
    Cursor transparentCursor ;
    
    //every tics
    final ScheduledThreadPoolExecutor executor ;
    
    //Boundaries
    private int minPixels = -200 ;
    private int maxPixels = +400 ;
    private int timeout = 150 ;
    private int direction = KeyEvent.VK_LEFT ;
    
    //needed to calculate vergence
    static Dimension screenSize ;
    static double workingDistance = 70 ;
    
    //Min and max are given in dioptries
    public SlideStereogramView (int speed, int min, int max) {
        this.minPixels = calcPixelsForVergence (min) ;
        this.maxPixels = calcPixelsForVergence (max) ;
        System.out.println ("min : " + min + " max : " + max) ;
        System.out.println ("min : " + this.minPixels + " max : " + this.maxPixels) ;
        
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Stéréogramme: ") ;
        setLayout(null);
        this.setSize(1000, 700);
        getContentPane().setBackground( Color.WHITE );
        
        //On initialise le timeout
        executor = new ScheduledThreadPoolExecutor(1);
        switch (speed) {
            case 0 : timeout = 200 ; break ;
            case 1 : timeout = 150 ; break ;
            case 2 : timeout = 100 ; break ;
        }
    }

    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        
        //Create stereogram
        bimage = new Stereogram (NewController.imgSize, 0) ; //initial delta set to zero
        bimage.resetImg (false) ;
        //Anaglyph
        //BufferedImage ana = new BufferedImage(bimage.OD.getWidth(), bimage.OD.getHeight(), BufferedImage.TYPE_INT_RGB);
        anaglyph = new Anaglyph () ;
        anaglyph.createStereoscopicBlueImage (bimage.OD) ;
        og = new OneEye (0) ;
        anaglyph.createStereoscopicRedImage (bimage.OG) ;
        od = new OneEye (1);

        //Position
        od.setLocation((this.getWidth()-od.getWidth()) / 2 - deltaX, (this.getHeight()-od.getHeight())/2);
        og.setLocation((this.getWidth()-og.getWidth()) / 2 + deltaX, (this.getHeight()-og.getHeight())/2);
        this.getContentPane().add (od) ;
        this.getContentPane().add (og) ;
        od.setVisible(true);
        og.setVisible(true);
        
        //On lance le timer
        executor.scheduleAtFixedRate(() -> timeOut(),6000, timeout, TimeUnit.MILLISECONDS);
    }
    
    public int calcPixelsForVergence (int vergence) {
        System.out.println (vergence + " " + workingDistance + " " + OrthoStereogram.screenResolution) ;
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = ((double) ((double)vergence * (double) workingDistance / 100) /2.54f ) * (double) OrthoStereogram.screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels) ;
    }
    
    public void setPositions () {
        od.setLocation((this.getWidth()-od.getWidth()) / 2 - deltaX/2, (this.getHeight()-od.getHeight())/2);
        og.setLocation((this.getWidth()-og.getWidth()) / 2 + deltaX/2, (this.getHeight()-og.getHeight())/2);
    }
    
    public void timeOut () {
        
        if (deltaX < minPixels) direction = KeyEvent.VK_RIGHT ;
        else if (deltaX > maxPixels ) direction = KeyEvent.VK_LEFT ;
        this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, direction, 'A'));
    }
    
    public void hideCursor () {
        
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
        else if (keyCode == VK_SPACE) {deltaX = 0; direction = KeyEvent.VK_LEFT ;}
         
        //Dynamic resizing
        if ((keyCode == VK_SUBTRACT | keyCode == VK_6) & ke.isControlDown() & ! ke.isShiftDown()) {
            NewController.imgSize = (int) (NewController.imgSize * 0.9 ) ;
            bimage.resize(NewController.imgSize, true);
            od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
            og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
            setPositions () ;
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            NewController.imgSize = (int) (NewController.imgSize * 1.1 ) ;
            bimage.resize(NewController.imgSize, true);
            od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
            og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
            setPositions () ;
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            NewController.imgSize = (int) (NewController.imgSize * 1.1 ) ;
            bimage.resize(NewController.imgSize, true);
            od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
            og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
            setPositions () ;
        }
        
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
        executor.shutdownNow() ;
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
    
    private int eye ;

    public OneEye (int eye) {
        this.eye = eye ;
        setVisible(false);
        //Copie de l'image
        //ColorModel cm = SlideStereogramView.eyes[eye].getColorModel();
        //boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        //WritableRaster raster = SlideStereogramView.eyes[eye].copyData(null);
        //this.vue = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        //Taille du panel
        if (eye == 0)
            this.setSize(Stereogram.OD.getWidth(), Stereogram.OD.getHeight());
        else
            this.setSize(Stereogram.OG.getWidth(), Stereogram.OG.getHeight());
        setOpaque(false) ;
        
    }
    
    public void resize () {
         if (eye == 0)
            this.setSize(Stereogram.OD.getWidth(), Stereogram.OD.getHeight());
        else
            this.setSize(Stereogram.OG.getWidth(), Stereogram.OG.getHeight());
    }
    
    public void paint(Graphics g) {
        //super.paintComponent(g);
        
        //float alpha = 0.5f ;
        //AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
        g.setXORMode(Color.WHITE);
        if (eye == 0) {
            if (OrthoStereogram.BR_glasses)
                g.drawImage(Stereogram.OD, 0,0,this);
            else
                g.drawImage(Stereogram.OD, 0 + Stereogram.OD.getWidth(), 0, -Stereogram.OD.getWidth(), Stereogram.OD.getHeight(), this);
        }
        else {
            if (OrthoStereogram.BR_glasses)
                g.drawImage(Stereogram.OG, 0,0,this);
            else
                g.drawImage(Stereogram.OG, 0 + Stereogram.OG.getWidth(), 0, -Stereogram.OG.getWidth(), Stereogram.OG.getHeight(), this);
        }
    }
}