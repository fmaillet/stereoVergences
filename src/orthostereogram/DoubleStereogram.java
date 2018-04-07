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
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    int size, clue ;
    SecureRandom rand = new SecureRandom ();
    
    //Infos display
    JLabel info ;
    //Stéréogramme anaglyphe
    static Eye OD, OG ;
    
    //Gestion Time Out réponse
    final ScheduledThreadPoolExecutor executor ;
    ScheduledFuture<?> scheduledFuture ;
    static private int timeOut = 20 ;
    
    //Sounds
    private static WavSoundThread sndGood = new WavSoundThread (1) ;
    private static WavSoundThread sndBad = new WavSoundThread (0) ;
    
    //Paramètres orthotiques
    private double verticality ;
    private int workingDistance ;
    private int initVergence ;
    private double stepC, stepD ;
    
    
    public DoubleStereogram (int stereogramSize, int workingDistance, int initVergence, int verticality, int stepC, double stepD) {
        this.size = stereogramSize ;
        this.workingDistance = workingDistance ;
        this.verticality = 0.25 * verticality ; //résultat en dioptries
        this.initVergence = initVergence ;
        this.stepC = (double) stepC ;
        this.stepD = stepD ;
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
        
        //On initialise les TimeOuts
        executor = new ScheduledThreadPoolExecutor(1);
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
        resetStereogram () ;
        this.getContentPane().add(OD) ; OD.repaint();
        this.getContentPane().add(OG) ; OG.repaint();
        repaint () ;
    }
    
    private void resetStereogram () {
        //On choisi une orientation
        int p = rand.nextInt(4) ;
        
        //On lance la mise à jour du stéréogram
        ResetStereogram rs = new ResetStereogram (OD.img, OG.img, p ) ; rs.run();
        //On se souvient de la clue
        switch (p) {
            case 0 : clue = KeyEvent.VK_UP ; break ;    //up
            case 1 : clue = KeyEvent.VK_LEFT ; break ;    //left
            case 2 : clue = KeyEvent.VK_RIGHT ; break ;    //right
            default : clue = KeyEvent.VK_DOWN ; break ;   //down
        }
        //On corrige l'indice en fct des lunettes
        if (OrthoStereogram.BR_glasses) {
            if (clue == KeyEvent.VK_RIGHT) clue = KeyEvent.VK_LEFT ;
            else if (clue == KeyEvent.VK_LEFT) clue = KeyEvent.VK_RIGHT ;
        }
        //On positionne les "yeux" :
        int deltaX = calcPixelsForVergence (initVergence) ;
        int deltaY = calcPixelsForVergence (verticality) ;
        OD.setLocation ((this.getWidth()-OD.size) / 2 - deltaX, (this.getHeight()-OD.size)/2 - deltaY) ; OD.setVisible(true);
        OG.setLocation ((this.getWidth()-OG.size) / 2 + deltaX, (this.getHeight()-OG.size)/2 + deltaY)  ; OG.setVisible(true);
        //Pour le prochain, on inverse la verticalité
        verticality = - verticality ;
    }
    
    public int calcPixelsForVergence (double vergence) {
        //System.out.println (vergence + " " + workingDistance + " " + OrthoStereogram.screenResolution) ;
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = (double) (vergence * (double) workingDistance / 254f ) * (double) OrthoStereogram.screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels/2) ;
    }
    
    public void hideCursor () {
        setCursor(transparentCursor);
    }
    
    @Override
    public void windowOpened(WindowEvent e) {
        OrthoStereogram.controller.setEnabled(false);
        hideCursor () ;
        
        //infos courantes
        JLabel label_1 = new JLabel ("Expérimental (marche avec verre rouge sur OG uniquement)") ;
        label_1.setBounds(10, 10, 100, 30);
        this.getContentPane().add(label_1) ;
        info = new JLabel ("Choisir le carré le plus proche de vous...") ;
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
                
        //Echap : on sort
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        //Flèches
        else if (keyCode == clue) {
            
            goodAnswer () ;
        }
        else if (keyCode == VK_UP | keyCode == VK_DOWN | keyCode == VK_LEFT | keyCode == VK_RIGHT | keyCode == VK_SPACE) {
            
            badAnswer () ;
        }
    }
    
    public void timeOut () {
        this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, 'A'));
    }
    
    public void goodAnswer () {
        //On arrête le Time out
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //On joue de la musique
        sndGood.run();
        
        //On affiche un nouveau stéréogramme
        resetStereogram () ;
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void badAnswer () {
        //On arrête le Time out
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //On joue de la musique
        sndBad.run();
        
        //On affiche un nouveau stéréogramme
        resetStereogram () ;
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
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
        
        //g.drawImage(img, 0,0,this);
        if (!OrthoStereogram.BR_glasses)
                g.drawImage(img, 0,0,this);
            else
                g.drawImage(img, 0 + size, 0, -size, size, this);
    }
    
}

class ResetStereogram implements Runnable {
    BufferedImage od, og ;
    Random rand = new Random ();
    //Thread t ;
    int r = -16711681 ;
    int c = -65536 ;
    int position = 0 ;
    
    public ResetStereogram (BufferedImage od, BufferedImage og, int position) {
        this.od = od;
        this.og = og ;
        this.position = position ;
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
        int depth = 5 ;   //disparité
        
        //Position du carré
        int dh, dc ;
        switch (position) {
            case 0 : dh = bord ; dc = size/2 - t/2 ; break ;    //up
            case 1 : dh = size/2 - t/2 ; dc = bord;  break ;    //left
            case 2 : dh = size/2 - t/2 ; dc = size - t - bord ; break ;    //right
            default : dh = size - t - bord ; dc = size/2 - t/2 ; break ;   //down
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
        //On en choisi un second
        switch (position) {
            case 0 : position = 3; break ;
            case 1 : position = 2 ; break ;
            case 2 : position = 1 ; break ;
            default : position = 0 ; break ;
        }
        switch (position) {
            case 0 : dh = bord ; dc = size/2 - t/2 ; break ;    //up
            case 1 : dh = size/2 - t/2 ; dc = bord;  break ;    //left
            case 2 : dh = size/2 - t/2 ; dc = size - t - bord ; break ;    //right
            default : dh = size - t - bord ; dc = size/2 - t/2 ; break ;   //down
        }
        //On crée le carré
        depth = depth - 1 ;
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