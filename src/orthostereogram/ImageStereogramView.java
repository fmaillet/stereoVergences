/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;



/**
 *
 * @author Fred
 */
public class ImageStereogramView extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    Image imgStereo ;
    static public BufferedImage od, og ;
    static public BufferedImage bimage, image ;
    Cursor transparentCursor ;
    
    private Point center ;
    private int deltaX = 0 ;
    private OneEyeBis xpanelOD, xpanelOG ;
    private JLabel info ;
    
    //Boundaries
    private int minVergence, maxVergence ;
    private int workingDistance = 70 ;
    private int minPixels = -200 ;
    private int maxPixels = +400 ;
    private int timeout = 150 ;
    private int dirVergence = KeyEvent.VK_LEFT ;
    private int dirMove = KeyEvent.VK_LEFT ;
    //private Point maxPixelsBoundaries ;
    //every tics
    final ScheduledThreadPoolExecutor executorVergence, executorMove ;
    
    public ImageStereogramView (String file, int speed, int minVergence, int maxVergence, int workingDistance) {
        this.minVergence = minVergence ;
        this.maxVergence = maxVergence ;
        this.minPixels = calcPixelsForVergence (minVergence) ;
        this.maxPixels = calcPixelsForVergence (maxVergence) ;
        this.workingDistance = workingDistance ;
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "invisibleCursor");
        
        //Image ?
        URL url = ImageStereogramView.class.getResource("/Ressources/"+file);
        try {bimage = ImageIO.read(url);} catch (IOException io) {System.out.println ("IO !!") ;}
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Slider") ;
        setLayout(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        getContentPane().setBackground( Color.WHITE );
        
        //On initialise les timeout
        executorVergence = new ScheduledThreadPoolExecutor(1);
        executorMove = new ScheduledThreadPoolExecutor(1);
        switch (speed) {
            case 0 : timeout = 150 ; break ;
            case 1 : timeout = 100 ; break ;
            case 2 : timeout = 50 ; break ;
            case 3 : timeout = 25 ; break ;
        }
    }
    
    public int calcPixelsForVergence (int vergence) {
        //System.out.println (vergence + " " + workingDistance + " " + OrthoStereogram.screenResolution) ;
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = ((double) ((double)vergence * (double) workingDistance / 100) /2.54f ) * (double) OrthoStereogram.screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels) ;
    }
    
    public double calcVergenceForPixels (int pixels) {
        double vergence = (double) (pixels * 254 * 2) / (double) (OrthoStereogram.screenResolution * workingDistance) ;
        return (vergence) ;
    }
    
    private boolean isBoundariesOK () {
        int w = xpanelOD.getWidth() ;
        //Envergure du stéréogramme
        int c, d ;
        d = Math.abs(deltaX) ;
        if (d > w) c = 2 * w + d ;
        else c = w + d ;
        //rapporté au centre
        if (dirMove == KeyEvent.VK_LEFT ) {
            //d = center.x + c / 2;
            return ( (center.x + c / 2) < this.getWidth()) ;
        }
        else {
            //d = center.x - c / 2;
            return ( (center.x - c / 2) > 0) ;
        }
        //Fin
        //info.setText("panel= " + String.valueOf(w) + " Center=" + String.valueOf(center.x)+ " deltaX=" + String.valueOf(deltaX) + " c=" + String.valueOf(c) + " max=" + String.valueOf(this.getWidth()));
        //if (dirMove == KeyEvent.VK_LEFT ) return (d < this.getWidth()) ;
        //else return (d > 0) ;
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.setIgnoreRepaint(true);
        
        
            
        /*//Test animated gif
        URL url = TestImageStereogram.class.getResource("/ressources/animated.gif");
        ImageIcon imageIcon = new ImageIcon(url);
        JLabel tt = new JLabel (imageIcon) ; tt.setText("");
        tt.setBounds (10, 10, 600, 300) ;
        this.getContentPane().add(tt);*/
        
        info = new JLabel ("Initializing...") ;
        info.setBounds(10, 10, 300, 30);
        this.getContentPane().add(info) ;
                
        //on crée l'OD (moitié gauche ?)
        BufferedImage sub = bimage.getSubimage(0, 0, bimage.getWidth(), bimage.getHeight()) ;
        // Create empty compatible image
        od = new BufferedImage(bimage.getWidth() / 2, bimage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = od.createGraphics();
        try {
            g.drawImage(sub, 0, 0, null);
        }
        finally {
            g.dispose();
        }
        //on crée l'OG (moitié droite ?)
        sub = bimage.getSubimage(bimage.getWidth() / 2, 0, bimage.getWidth() / 2, bimage.getHeight()) ;
        og = new BufferedImage(bimage.getWidth() / 2, bimage.getHeight(), BufferedImage.TYPE_INT_RGB);
        g = og.createGraphics();
        try {
            g.drawImage(sub, 0, 0, null);
        }
        finally {
            g.dispose();
        }
        
        //On anaglyphe les images
        Anaglyph anaglyph = new Anaglyph () ;
        anaglyph.createStereoscopicRedImage (od) ;
        anaglyph.createStereoscopicBlueImage (og) ;
        
        //Un panel pour afficher ça
        //deltaX = bimage.getWidth() /2  ;
        xpanelOD = new OneEyeBis (od) ;
        xpanelOG = new OneEyeBis (og) ;
        //On met ça de part et d'autres du milieu
        center = new Point (this.getWidth() / 2, (this.getHeight()-xpanelOD.getHeight())/2) ;
        xpanelOD.setLocation(center.x - deltaX/2 - xpanelOD.getWidth()/2, center.y);
        xpanelOG.setLocation(center.x + deltaX/2 - xpanelOG.getWidth()/2, center.y);
        //xpanel.setLocation(100, 100);
        //System.out.println (xpanel.getWidth()) ;
        this.getContentPane().add(xpanelOD) ;
        this.getContentPane().add(xpanelOG) ;
               
        //On lance les timer
        executorVergence.scheduleAtFixedRate(() -> timeOutVergence(),6000, timeout, TimeUnit.MILLISECONDS);
        executorMove.scheduleAtFixedRate(() -> timeOutMove(),6000, timeout / 2, TimeUnit.MILLISECONDS);
    }
    
    public void setPositions () {
        xpanelOD.setLocation(center.x - deltaX/2 - xpanelOD.getWidth()/2, center.y);
        xpanelOG.setLocation(center.x + deltaX/2 - xpanelOG.getWidth()/2, center.y);
    }
    
    //code from : https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
    public BufferedImage scaleImg (BufferedImage before, double scale) {
        int w = (int) (before.getWidth() * scale) ;
        int h = (int) (before.getHeight() * scale) ;
        //On vérifie els bornes !
        if (h > this.getContentPane().getHeight() | h < 200 ) return before ;
        if (w > this.getContentPane().getWidth() | w < 150 ) return before ;
        //c'est bon on peut y aller
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        AffineTransformOp scaleOp = 
           new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        
        return after ;
    }

    @Override
    public void windowOpened(WindowEvent we) {
        //repaint() ;
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
        setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void hideCursor () {
        
        setCursor(transparentCursor);
    }
    
    private void timeOutMove () {
        //Check to move the image
        if ( dirMove == KeyEvent.VK_LEFT ) {
            if (isBoundariesOK() ) center.x++ ;
            else dirMove = KeyEvent.VK_RIGHT ;
        }
        else {
            if (isBoundariesOK() ) center.x-- ;
            else dirMove = KeyEvent.VK_LEFT ;
        }
    }

    private void timeOutVergence () {
        
        if (deltaX < minPixels) dirVergence = KeyEvent.VK_RIGHT ;
        else if (deltaX > maxPixels ) dirVergence = KeyEvent.VK_LEFT ;
        this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, dirVergence, 'A'));
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        else if (keyCode == VK_LEFT) { deltaX--; setPositions () ; }
        else if (keyCode == VK_RIGHT) { deltaX++; setPositions () ; }
        else if (keyCode == VK_SPACE) {
            center.x = this.getWidth() / 2;
            center.y = (this.getHeight()-xpanelOD.getHeight())/2 ;
            deltaX = 0;
            dirVergence = KeyEvent.VK_LEFT ;
        }
        
        //Dynamic resizing
        if ((keyCode == VK_SUBTRACT | keyCode == VK_6) & ke.isControlDown() & ! ke.isShiftDown()) {
            od = scaleImg (od, 0.9); og = scaleImg (og, 0.9);
            xpanelOD.resize(od); 
            xpanelOG.resize(og);
            center.y = (this.getHeight()-xpanelOD.getHeight())/2 ;
            setPositions () ;
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            od = scaleImg (od, 1.1); og = scaleImg (og, 1.1);
            xpanelOD.resize(od); 
            xpanelOG.resize(og);
            center.y = (this.getHeight()-xpanelOD.getHeight())/2 ;
            setPositions () ;
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            od = scaleImg (od, 1.1); og = scaleImg (og, 1.1);
            xpanelOD.resize(od); 
            xpanelOG.resize(og);
            center.y = (this.getHeight()-xpanelOD.getHeight())/2 ;
            setPositions () ;
        }
        
        hideCursor () ;
        info.setText(String.format("%+2.1f",calcVergenceForPixels(deltaX)));
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}


class OneEyeBis extends JPanel {
    
    private BufferedImage oo ;
    
    public OneEyeBis (BufferedImage oo) {
        this.oo = oo ;
        
        //System.out.println("xpanel: " +oo.getWidth());
        this.setSize(oo.getWidth(null), oo.getHeight(null));
        this.setOpaque(false);
        this.setIgnoreRepaint(true);
    }
    
    public void resize (BufferedImage oo) {
        this.oo = oo ;
        this.setSize(oo.getWidth(null), oo.getHeight(null));
    }
    
    public void paint(Graphics g) {
        super.paintComponent(g);
        g.setXORMode(Color.WHITE);
        if (OrthoStereogram.BR_glasses)
            g.drawImage(oo, 0,0,this);
        else
                g.drawImage(oo, 0 + oo.getWidth(), 0, -oo.getWidth(), oo.getHeight(), this);
    }
    
}