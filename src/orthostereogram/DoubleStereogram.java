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
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Fred
 */
public class DoubleStereogram extends JFrame implements WindowListener, MouseMotionListener, KeyListener {

    //General
    Cursor transparentCursor ;
    int size ;
    
    //Infos display
    JLabel info ;
    //Stéréogramme anaglyphe
    static Eye OD, OG ;
    
    
    public DoubleStereogram (int stereogramSize) {
        this.size = size ;
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        
        //Create the eyes images
        OD = new Eye(stereogramSize, true);
        OG = new Eye(stereogramSize, false);
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        this.setUndecorated(true);
        getContentPane().setBackground( Color.WHITE );
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }
    
    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addWindowListener(this);
        
        int deltaX = 50 ;
        int deltaY = 10 ;
        
        //On ajoute les yeux
        OD.setLocation ((this.getWidth()-OD.size) / 2 - deltaX, (this.getHeight()-OD.size)/2 - deltaY) ; OD.setVisible(true);
        OG.setLocation ((this.getWidth()-OG.size) / 2 + deltaX, (this.getHeight()-OG.size)/2 + deltaY)  ; OG.setVisible(true);
        ResetStereogram rs = new ResetStereogram (OD.img, OG.img ) ; rs.run();
        this.getContentPane().add(OD) ; OD.repaint();
        this.getContentPane().add(OG) ; OG.repaint();
        repaint () ;
    }
    
    
    public void hideCursor () {
        setCursor(transparentCursor);
    }
    
    @Override
    public void windowOpened(WindowEvent e) {
        OrthoStereogram.controller.setEnabled(false);
        hideCursor () ;
        
        //infos courantes
        JLabel label_1 = new JLabel ("Running :") ;
        label_1.setBounds(10, 10, 100, 30);
        this.getContentPane().add(label_1) ;
        info = new JLabel ("--") ;
        info.setBounds(20, 35, 300, 30);
        info.setForeground(Color.GRAY);
        this.getContentPane().add(info) ;
    }
    
    

    @Override
    public void windowClosing(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosed(WindowEvent e) {
        OrthoStereogram.controller.setEnabled(true);
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
        ResetStereogram rs ;
        
        //Echap : on sort
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        //Flèches
        if (keyCode == VK_UP) {
            
            rs = new ResetStereogram (OD.img, OG.img ) ;
            rs.run();
            repaint () ;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

class Eye extends JPanel {
    static boolean isRight ;
    public BufferedImage img ;
    static int size ;
    
    
    
    
    public Eye (int size, boolean isRight) {
        //Constantes
        this.size = size ;
        this.isRight = isRight ;
        this.setSize(size, size);
        //Création du buffer image
        img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        this.setOpaque(false);
        
    }
    
    /*public void resetStereogram () {
        int couleurRGB = 0 ;
        //On rempli de valeurs aléatoires identiques OD=OG
        for (int i= 0; i<size; i++)
            for (int j=0; j<size; j++) {
                if (rand.nextBoolean())  couleurRGB = (isRight ? c : r ); 
                else couleurRGB = Color.WHITE.getRGB() ;
                img.setRGB(i, j, couleurRGB);
            }
    }*/
    
    public void paint(Graphics g) {
        super.paintComponent(g);
        g.setXORMode(Color.WHITE);
        g.drawImage(img, 0,0,this);
        
    }
    
}

class ResetStereogram implements Runnable {
    BufferedImage od, og ;
    Random rand = new Random ();
    Thread t ;
    int r = -16711681 ;
    int c = -65536 ;
    
    public ResetStereogram (BufferedImage od, BufferedImage og) {
        this.od = od;
        this.og = og ;
        rand.setSeed(System.currentTimeMillis());
        //t = new Thread (this, "resetStereogram") ;
        //t.start ( ) ;
    }

    @Override
    public void run() {
        
        int size = od.getHeight() ;
        
        boolean b ;
        //On rempli de valeurs aléatoires identiques OD=OG
        for (int i= 0; i<size; i++)
            for (int j=0; j<size; j++) {
                b = rand.nextBoolean() ;
                /*if (rand.nextBoolean())  couleurRGB = (isRight ? c : r ); 
                else couleurRGB = Color.WHITE.getRGB() ;*/
                od.setRGB(i, j, (b ? Color.WHITE.getRGB() : c));
                og.setRGB(i, j, (b ? Color.WHITE.getRGB() : r));
            }
        //paramètres du carré
        int t = size / 3 ; // taille du carré
        int bord = 30 ;    //distance du bord
        int depth = 20 ;   //disparité
        int p = rand.nextInt(4) ;
        //Position du carré
        int dh, dc, clue ;
        switch (p) {
            case 0 : dh = bord ; dc = size/2 - t/2 ; clue = KeyEvent.VK_UP ; break ;    //up
            case 1 : dh = size/2 - t/2 ; dc = bord;  clue = KeyEvent.VK_LEFT ; break ;    //left
            case 2 : dh = size/2 - t/2 ; dc = size - t - bord ; clue = KeyEvent.VK_RIGHT ; break ;    //right
            default : dh = size - t - bord ; dc = size/2 - t/2 ; clue = KeyEvent.VK_DOWN ; break ;   //down
        }
        //On crée le carré
        for (int i=0; i<t; i++)
            for (int j=0; j<t; j++) {
                b = rand.nextBoolean() ;
                //if (rand.nextBoolean())  couleurRGB = Color.BLACK.getRGB() ;
                //else couleurRGB = Color.WHITE.getRGB() ;
                od.setRGB(dc+i - depth, j+dh, (b ? Color.WHITE.getRGB() : c));
                og.setRGB(dc+i + depth, j+dh, (b ? Color.WHITE.getRGB() : r));
            }
    }
}