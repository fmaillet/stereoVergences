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
import javax.swing.ImageIcon;
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
    SecureRandom securRand = new SecureRandom ();
    
    //Type de jeu :
    private static int game ;
    final static public int GAME_SIMPLE = 0  ;
    final static public int GAME_DOUBLE = 1  ;
    final static public int GAME_2PLAYERS = 2  ;
    
    //Infos display
    JLabel info, infosMax ;
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
    //private int initVergence ;
    private double stepC, stepD, step ;
    private double maxRequired, minRequired ;
    private double currentVergenceValue ;
    private static int disparity ;
    //Remember min and max obtained values
    private double obtainedMax = 0 ;
    private double obtainedMin = 0 ;
    
    //Constants
    final static public int CONVERGENCE_UP = 2  ;
    final static public int CONVERGENCE_DOWN = 1  ;
    final static public int DIVERGENCE_UP  = -1 ;
    final static public int DIVERGENCE_DOWN  = -2 ;
    static private int currentDirectionOfWork ;
    
    //XBOX
    static JoystickEvents joystickEvents ;
    
    //Trophés
    JLabel trophy[] ;
    final int NB_TROPHY = 6 ;
    int trophyNumber = 0 ;
    
    
    public DoubleStereogram (int stereogramSize, int workingDistance, int initVergence, int verticality, int stepC, double stepD) {
        this.size = stereogramSize ;
        this.workingDistance = workingDistance ;
        this.verticality = 0.25 * verticality ; //résultat en dioptries
        this.currentVergenceValue = initVergence ;
        step = this.stepC = (double) stepC ;
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
    
    public void setAppearence (int game, int max, int min, int timeOut, int typeExercice, int disparity, boolean alternate, boolean jump) {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addWindowListener(this);
        
        this.game = game ;
        this.maxRequired = max;
        this.minRequired = min ;
        currentDirectionOfWork = CONVERGENCE_UP ;
        this.disparity = disparity ;
        
        //Boudaries
        this.maxRequired = Math.floor(max / stepC ) * stepC ;
        this.minRequired = Math.floor(min / stepD ) * stepD ;
        
        int deltaX = calcPixelsForVergence (currentVergenceValue) ;
        int deltaY = calcPixelsForVergence (verticality) ;
        
        //Create trophy
        if (NewController.jTrophy.isSelected()) {
            trophy = new JLabel[NB_TROPHY] ;
            for (int i=0; i<NB_TROPHY; i++) {
                trophy[i] = new JLabel() ;
                trophy[i].setIcon(new ImageIcon(NewController.tinyTrophy));
                trophy[i].setBounds(20, 160 + (i * 85), 64, 64);
                this.getContentPane().add(trophy[i]) ;
                trophy[i].setEnabled(false);
            }
        }
        
        //On écoute la xbox
        if (NewController.glfwInit & NewController.xboxConnected) {
            joystickEvents = new JoystickEvents (this) ;
            joystickEvents.start();
        }
        
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
        int p = securRand.nextInt(4) ;
        
        //On lance la mise à jour du stéréogram
        ResetStereogram rs = new ResetStereogram (OD.img, OG.img, p, disparity, game ) ; rs.run();
        //On se souvient de la clue
        switch (p) {
            case 0 : clue = KeyEvent.VK_UP ; break ;    //up
            case 1 : clue = KeyEvent.VK_LEFT ; break ;    //left
            case 2 : clue = KeyEvent.VK_RIGHT ; break ;    //right
            default : clue = KeyEvent.VK_DOWN ; break ;   //down
        }
        //On corrige l'indice en fct des lunettes
        /*if (OrthoStereogram.BR_glasses) {
            if (clue == KeyEvent.VK_RIGHT) clue = KeyEvent.VK_LEFT ;
            else if (clue == KeyEvent.VK_LEFT) clue = KeyEvent.VK_RIGHT ;
        }*/
        //On positionne les "yeux" :
        int deltaX = calcPixelsForVergence (currentVergenceValue) ;
        int deltaY = calcPixelsForVergence (verticality) ;
        if (true) {
            OD.setLocation ((this.getWidth()-OD.size) / 2 - deltaX, (this.getHeight()-OD.size)/2 - deltaY) ; OD.setVisible(true);
            OG.setLocation ((this.getWidth()-OG.size) / 2 + deltaX, (this.getHeight()-OG.size)/2 + deltaY)  ; OG.setVisible(true);
        }
        else {
            OG.setLocation ((this.getWidth()-OD.size) / 2 - deltaX, (this.getHeight()-OD.size)/2 - deltaY) ; OG.setVisible(true);
            OD.setLocation ((this.getWidth()-OG.size) / 2 + deltaX, (this.getHeight()-OG.size)/2 + deltaY)  ; OD.setVisible(true);
        }
            
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
        JLabel label_1 = new JLabel ("En cours :") ;
        label_1.setBounds(10, 10, 100, 30);
        this.getContentPane().add(label_1) ;
        info = new JLabel ("--") ;
        info.setBounds(20, 35, 300, 30);
        info.setForeground(Color.GRAY);
        this.getContentPane().add(info) ;
        //Valeurs max
        JLabel label_2 = new JLabel ("Max score :") ;
        label_2.setBounds(10, 60, 100, 30);
        this.getContentPane().add(label_2) ;
        infosMax = new JLabel ("--") ;
        infosMax.setForeground(Color.GRAY);
        infosMax.setBounds(20, 85, 300, 30);
        this.getContentPane().add(infosMax) ;
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
        String str = new String() ;
        //On arrête le Time out
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //On joue de la musique
        sndGood.run();
        
        //On sauvegarde la valeur max atteinte
        boolean alternate = false ;
        if (alternate) {
            if (currentVergenceValue > obtainedMax) obtainedMax  = currentVergenceValue ;
            else if (currentVergenceValue < obtainedMin) obtainedMin  = currentVergenceValue ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_UP & currentVergenceValue > obtainedMax) {
            obtainedMax  = currentVergenceValue ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & currentVergenceValue < obtainedMin)
            obtainedMin  = currentVergenceValue ;
        
        //Si l'on est au max, on change de direction de travail
        if (currentDirectionOfWork == CONVERGENCE_UP & currentVergenceValue+step > maxRequired  ) {
            step = -stepC ;
            currentDirectionOfWork = CONVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_DOWN & currentVergenceValue+step < 0  ) {
            step = -stepD ;
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & currentVergenceValue+step < minRequired  ) {
            step = stepD ;
            currentDirectionOfWork = DIVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_DOWN & currentVergenceValue+step > 0  ) {
            step = stepC ;
            currentDirectionOfWork = CONVERGENCE_UP ;
        }
        
        //nouvelle valeur de vergence
        currentVergenceValue = currentVergenceValue + step ;
        
        //On affiche un nouveau stéréogramme
        resetStereogram () ;
        repaint () ;
        //Mise à jour valeur courante
        if (currentDirectionOfWork == CONVERGENCE_UP) str = "C\u2191 " ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) str = "C\u2193 " ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) str = "D\u2191 " ;
        else str = "D\u2193 " ;
        info.setText(str+String.valueOf(currentVergenceValue)+" \u0394");
        infosMax.setText("C" + String.valueOf(obtainedMax) + "  D" + String.valueOf(Math.abs(obtainedMin))) ;
        
        //A-t-on fait un cycle ? oui, on affiche un trophé
        if (trophy != null) {
            if (currentVergenceValue == 0 && obtainedMax == maxRequired && obtainedMin == minRequired) {
                trophy[trophyNumber].setEnabled(true);
                if (trophyNumber<NB_TROPHY-1) trophyNumber++ ;
            }
        }
        
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void badAnswer () {
        //On arrête le Time out
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //On joue de la musique
        sndBad.run();
        
        //Si on est à zéro, on restera à zéro
        if (currentVergenceValue != 0) {
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP   : if (currentVergenceValue > 0) currentVergenceValue = currentVergenceValue - step ; break ;
                case CONVERGENCE_DOWN : if (currentVergenceValue > 0) currentVergenceValue = currentVergenceValue + step ; break ;
                case DIVERGENCE_UP    : if (currentVergenceValue < 0) currentVergenceValue = currentVergenceValue - step ; break ;
                case DIVERGENCE_DOWN  : if (currentVergenceValue < 0) currentVergenceValue = currentVergenceValue + step ; break ;
            }
        }
        
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
        g.drawImage(img, 0,0,this);
        /*if ( !isRight ) g.drawImage(img, 0,0,this);
        else g.drawImage(img, 0 + size, 0, -size, size, this);
            
        /*if (isRight) {
            if (!OrthoStereogram.BR_glasses)
                g.drawImage(img, 0,0,this);
            else
                g.drawImage(img, 0 + size, 0, -size, size, this);
        }
        else {
            if (!OrthoStereogram.BR_glasses)
                g.drawImage(img, 0,0,this);
            else
                
            g.drawImage(img, 0 + size, 0, -size, size, this);
        }*/
    }
    
}

class ResetStereogram implements Runnable {
    BufferedImage od, og ;
    Random rand = new Random ();
    //Thread t ;
    int r = -16711681 ;
    int c = -65536 ;
    int position = 0 ;
    int size ;
    int disparity ; // depht from back
    int game ;      //game type
    
    
    public ResetStereogram (BufferedImage od, BufferedImage og, int position, int disparity, int game) { //position = position du petit carré
        this.od = od;
        this.og = og ;
        this.position = position ;
        this.disparity = disparity ;
        this.game = game ;
        //Quel type de lunettes ?
        if (!OrthoStereogram.BR_glasses) {
            int t = c ;
            c = r ;
            r = t ;
        }
                
        size = od.getHeight() ;
    }
    
    @Override
    public void run() {
        //long begin = System.currentTimeMillis() ;
        
        
        boolean b ;
        //On rempli de valeurs aléatoires identiques OD=OG
        for (int i= 0; i<size; i++)
            for (int j=0; j<size; j++) {
                b = rand.nextBoolean() ;
                od.setRGB(i, j, (b ? Color.WHITE.getRGB() : r));
                og.setRGB(i, j, (b ? Color.WHITE.getRGB() : c));
            }
        
        //paramètres du carré
        int t = size / 3 ; // taille du carré
        int bord = 30 ;    //distance du bord
        
        
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
                od.setRGB(dc+i - disparity, j+dh, (b ? Color.WHITE.getRGB() : r));
                og.setRGB(dc+i + disparity, j+dh, (b ? Color.WHITE.getRGB() : c));
            }
        //s'il n'y a qu'un carré on sort
        if (game == 0) return ;
        //sinon on dessine un deuxième carré
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
        disparity = disparity - (Integer) NewController.jDeltaDisparity.getValue() ;
        for (int i=0; i<t; i++)
            for (int j=0; j<t; j++) {
                b = rand.nextBoolean() ;
                //if (rand.nextBoolean())  couleurRGB = Color.BLACK.getRGB() ;
                //else couleurRGB = Color.WHITE.getRGB() ;
                od.setRGB(dc+i - disparity, j+dh, (b ? Color.WHITE.getRGB() : r));
                og.setRGB(dc+i + disparity, j+dh, (b ? Color.WHITE.getRGB() : c));
            }
        //On a fini
        //System.out.println ( System.currentTimeMillis() - begin ) ;
    }
    
    
}