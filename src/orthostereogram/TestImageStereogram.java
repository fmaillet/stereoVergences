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
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static orthostereogram.SlideStereogramView.workingDistance;


/**
 *
 * @author Fred
 */
public class TestImageStereogram extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    Image imgStereo ;
    static public BufferedImage od, og ;
    static public BufferedImage bimage, image ;
    Cursor transparentCursor ;
    
    private Point center ;
    private int deltaX = 200 ;
    private OneEyeBis xpanelOD, xpanelOG ;
    private JLabel info ;
    
    //Boundaries
    private int minVergence, maxVergence ;
    private int minPixels = -200 ;
    private int maxPixels = +400 ;
    private int timeout = 150 ;
    private int dirVergence = KeyEvent.VK_LEFT ;
    private int dirMove = KeyEvent.VK_LEFT ;
    //private Point maxPixelsBoundaries ;
    //every tics
    final ScheduledThreadPoolExecutor executorVergence, executorMove ;
    
    public TestImageStereogram (int speed, int minVergence, int maxVergence) {
        this.minVergence = minVergence ;
        this.maxVergence = maxVergence ;
        this.minPixels = calcPixelsForVergence (minVergence) ;
        this.maxPixels = calcPixelsForVergence (maxVergence) ;
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        
        //Image ?
        URL url = TestImageStereogram.class.getResource("/Ressources/3d-practise.png");
        try {bimage = ImageIO.read(url);} catch (IOException io) {System.out.println ("IO !!") ;}
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Stéréogramme: ") ;
        setLayout(null);
        this.setSize(1000, 700);
        getContentPane().setBackground( Color.WHITE );
        
        //On initialise le timeout
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
    
    public boolean isBoundariesOK () {
        int w = xpanelOD.getWidth() ;
        //Envergure du stéréogramme
        int c, d ;
        if (Math.abs(deltaX) > w) c = 2 * w + Math.abs(deltaX) ;
        else c = w + Math.abs(deltaX) ;
        //rapporté au centre
        if (dirMove == KeyEvent.VK_LEFT ) d = center.x + c / 2;
        else d = center.x - c / 2;
        //Fin
        info.setText("panel= " + String.valueOf(w) + " Center=" + String.valueOf(center.x)+ " deltaX=" + String.valueOf(deltaX) + " c=" + String.valueOf(c) + " max=" + String.valueOf(this.getWidth()));
        if (dirMove == KeyEvent.VK_LEFT ) return (d < this.getWidth()) ;
        else return (d > 0) ;
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        
            
        /*//Test animated gif
        URL url = TestImageStereogram.class.getResource("/ressources/animated.gif");
        ImageIcon imageIcon = new ImageIcon(url);
        JLabel tt = new JLabel (imageIcon) ; tt.setText("");
        tt.setBounds (10, 10, 600, 300) ;
        this.getContentPane().add(tt);*/
        
        info = new JLabel ("info") ;
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
    
    public void hideCursor () {
        
        setCursor(transparentCursor);
    }
    
    public void timeOutMove () {
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

    public void timeOutVergence () {
        
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
        else if (keyCode == VK_SPACE) {deltaX = 0; dirVergence = KeyEvent.VK_LEFT ;}
        
        hideCursor () ;
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
        this.setSize(oo.getWidth(null), oo.getHeight(null));
        this.setOpaque(false);
    }
    
    public void paint(Graphics g) {
        //super.paintComponent(g);
        g.setXORMode(Color.WHITE);
        g.drawImage(oo, 0,0,this);
    }
    
}