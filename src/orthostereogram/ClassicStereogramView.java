/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_ADD;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_SUBTRACT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;

/**
 *
 * @author Fred
 */
public class ClassicStereogramView extends JFrame implements WindowListener, MouseMotionListener, KeyListener {
    
    //Constructor
    //SoundThread audioOK, audioBAD ;
    Anaglyph anaglyph ;
    static private Stereogram bimage  ;
    private OneEye od, og ;
    private int deltaX = 0 ;
    private int deltaY = 0 ;
    Cursor transparentCursor ;
    private JLabel info, infosMax ;
    
    //Remember min and max obtained values
    private double obtainedMax = 0 ;
    private double obtainedMin = 0 ;
    
    //Constants
    final static public int CONVERGENCE_UP = 2  ;
    final static public int CONVERGENCE_DOWN = 1  ;
    final static public int DIVERGENCE_UP  = -1 ;
    final static public int DIVERGENCE_DOWN  = -2 ;
    
    //Parameters
    static private double verticality ;
    static private double stepC ;
    static private double stepD ;
    static private double step = 1 ;
    static private double max = 35 ;
    static private double min = -10 ;
    static private int currentDirectionOfWork = CONVERGENCE_UP ;
    static private boolean alternate = false ;
    static private boolean jump = false ;
    //pour l'alternance
    static double currentConvergenceValue ;
    static double currentDivergenceValue ;
    static double currentVergenceValue ;
    static private int previousBadAnswer = 0 ;
    
    //délai de réponse
    static private int timeOut = 20 ;
    final ScheduledThreadPoolExecutor executor ;
    ScheduledFuture<?> scheduledFuture ;
    boolean resizingIsActive = false ;
    boolean keypressedIsActive = false ;
       
    //Boundaries
    //private int minPixels = -200 ;
    //private int maxPixels = +400 ;
    private int direction = KeyEvent.VK_LEFT ;
    
    //needed to calculate vergence
    static Dimension screenSize ;
    static int workingDistance = 70 ;
    
    //Sounds
    private static WavSoundThread sndGood = new WavSoundThread (1) ;
    private static WavSoundThread sndBad = new WavSoundThread (0) ;
    
    //XBOX
    static JoystickEvents joystickEvents ;
    
    //Trphés
    JLabel trophy[] ;
    final int NB_TROPHY = 6 ;
    int trophyNumber = 0 ;
    
    //Min and max are given in dioptries
    public ClassicStereogramView (int initialValue, int currentDirectionOfWork, int workingDistance) {
        //this.minPixels = calcPixelsForVergence (min) ;
        //this.maxPixels = calcPixelsForVergence (max) ;
        this.currentDirectionOfWork = currentDirectionOfWork ;
        this.workingDistance = workingDistance ;
        this.currentVergenceValue = initialValue ;
        this.deltaX = calcPixelsForVergence (initialValue) ;
        
        this.setAlwaysOnTop(true);
        
        //System.out.println ("min : " + min + " max : " + max) ;
        //System.out.println ("min : " + this.minPixels + " max : " + this.maxPixels) ;
        
        //Trasnparent cursor
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //setTitle ("Stéréogramme: ") ;
        setLayout(null);
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        getContentPane().setBackground( Color.WHITE );
        
        //On initialise les TimeOuts
        executor = new ScheduledThreadPoolExecutor(1);        
               
    }

    public void setAppearence () {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addWindowListener(this);
        
        //Create stereogram
        bimage = new Stereogram (NewController.imgSize, workingDistance, 0, false) ; //initial delta set to zero
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
        
        //infos courantes
        JLabel label_1 = new JLabel ("Running :") ;
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
        
        //Create trophy
        trophy = new JLabel[NB_TROPHY] ;
        for (int i=0; i<NB_TROPHY; i++) {
            trophy[i] = new JLabel() ;
            trophy[i].setIcon(new ImageIcon(NewController.tinyTrophy));
            trophy[i].setBounds(20, 160 + (i * 85), 64, 64);
            this.getContentPane().add(trophy[i]) ;
            trophy[i].setEnabled(false);
        }
        
        //On écoute la xbox
        if (NewController.glfwInit & NewController.xboxConnected) {
            joystickEvents = new JoystickEvents (this) ;
            joystickEvents.start();
        }
    }
    
    public void setMode (double stepC, double stepD, int max, int min, int timeOut, boolean alternate, boolean jump, int verticality) {
        
        //Veticalité
        this.verticality = 0.25 * verticality ;
        //steps
        this.stepC = stepC ; this.stepD = stepD ;
        //Step increment selon le démarrage
        if (currentDirectionOfWork == CONVERGENCE_UP) this.step = stepC ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) this.step = -stepC ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) this.step = -stepD ;
        else this.step = stepD ;
        
        //Boudaries
        
        this.max = Math.floor(max / stepC ) * stepC ;
        this.min = Math.floor(min / stepD ) * stepD ;
        //time out to answer
        this.timeOut = timeOut ;
        
        //Game mode
        this.alternate = alternate ;
        this.jump = jump ;
        currentConvergenceValue = 0 ;
        currentDivergenceValue = 0 ;
        obtainedMin = obtainedMax = 0 ;
    }
    
    public int calcPixelsForVergence (double vergence) {
        //System.out.println (vergence + " " + workingDistance + " " + OrthoStereogram.screenResolution) ;
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = (double) (vergence * (double) workingDistance / 254f ) * (double) OrthoStereogram.screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels/2) ;
    }
    
    public double calcVergenceForPixels (int pixels) {
        double vergence = (double) (pixels * 254 * 2) / (double) (OrthoStereogram.screenResolution * workingDistance) ;
        return (vergence) ;
    }
    
    public void goToVergence (double value) {
        //Horizontal
        currentVergenceValue = value ;
        deltaX = calcPixelsForVergence (value) ;
        //Vertical
        deltaY = calcPixelsForVergence (verticality) ;
        verticality = - verticality ;
        //On recalcule un stéréogramme
        resetStereogram () ;
        //On le positionne
        setPositions () ;
    }
    
    public void stepVergence (double delta) {
        currentVergenceValue = currentVergenceValue + delta ;
        deltaX = calcPixelsForVergence (currentVergenceValue) ;
        //Vertical
        deltaY = calcPixelsForVergence (verticality) ;
        verticality = - verticality ;
        resetStereogram () ;
        setPositions () ;
    }
    
    public void setPositions () {
        od.setLocation((this.getWidth()-od.getWidth()) / 2 - deltaX, (this.getHeight()-od.getHeight())/2 - deltaY);
        og.setLocation((this.getWidth()-og.getWidth()) / 2 + deltaX, (this.getHeight()-og.getHeight())/2 + deltaY);
    }
    
    public void resetStereogram () {
        bimage.resetImg (false) ;
        anaglyph.createStereoscopicBlueImage (bimage.OD) ;
        anaglyph.createStereoscopicRedImage (bimage.OG) ;
    }
    
    public void timeOut () {
        this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, 'A'));
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
        char c  = ke.getKeyChar() ;
        if (resizingIsActive | keypressedIsActive) return ;
        
        if (c != 'A' & !ke.isControlDown() & ! ke.isShiftDown()) keypressedIsActive = true ;
        
        if (keyCode == VK_ESCAPE) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        
        if (keyCode == bimage.clue) {
            //audioOK.run() ;
            hideCursor () ;
            goodAnswer () ;
        }
        else if (keyCode == VK_UP | keyCode == VK_DOWN | keyCode == VK_LEFT | keyCode == VK_RIGHT | keyCode == VK_SPACE) {
            //audioBAD.run() ;
            hideCursor () ;
            badAnswer () ;
        }
         
        //Dynamic resizing
        if ((keyCode == VK_SUBTRACT | keyCode == VK_6) & ke.isControlDown() & ! ke.isShiftDown()) {
            resizingIsActive = true ;
            if (NewController.imgScale( 0.9 )) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            resizingIsActive = true ;
            if ( NewController.imgScale( 1.1 ) ) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            resizingIsActive = true ;
            if ( NewController.imgScale( 1.1 ) ) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        
        hideCursor () ;
        resizingIsActive = false;
    }
    
    public void goodAnswer () {
        
        //Pour l'affichage
        String tmp = new String() ;
        //Son bonne réponse
        sndGood.run();
        //On se souvient que c'est une bonne réponse
        previousBadAnswer = 0 ; 
        
        //On sauvegarde la valeur max atteinte
        if (alternate) {
            if (currentVergenceValue > obtainedMax) obtainedMax  = currentVergenceValue ;
            else if (currentVergenceValue < obtainedMin) obtainedMin  = currentVergenceValue ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_UP & currentVergenceValue > obtainedMax) {
            obtainedMax  = currentVergenceValue ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & currentVergenceValue < obtainedMin)
            obtainedMin  = currentVergenceValue ;
        
        
        
        
        //On arrête le Time out
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        
        //On change le step aux bornes selon la direction de travail
        if (currentDirectionOfWork == CONVERGENCE_UP & currentVergenceValue+step > max) {
            step = - stepC ;
            currentDirectionOfWork = CONVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_DOWN & currentVergenceValue+step < 0.0) {
            step = - stepD ;
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & currentVergenceValue+step < min) {
            step = stepD ;
            currentDirectionOfWork = DIVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_DOWN & currentVergenceValue+step > 0.0) {
            step = stepC ;
            currentDirectionOfWork = CONVERGENCE_UP ;
        }
        //Si on alterne :
        if (alternate)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP :
                    currentConvergenceValue = currentConvergenceValue + stepC ;
                    if (currentConvergenceValue > max) { currentConvergenceValue = max ; step = stepC = - stepC ; }
                    else if (currentConvergenceValue < 0) { currentConvergenceValue = 0 ; step = stepC = - stepC ;}
                    break;
                case DIVERGENCE_UP :
                    currentDivergenceValue = currentDivergenceValue - stepD ;
                    if (currentDivergenceValue < min) { currentDivergenceValue = min ; step = stepD = - stepD ; }
                    else if (currentDivergenceValue > 0) { currentDivergenceValue = 0 ; step = stepD = - stepD ;}
                    break ;
            }
        //avant de modifier la direction courante
        if (currentDirectionOfWork == CONVERGENCE_UP) tmp = "C\u2191 " ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) tmp = "C\u2193 " ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) tmp = "D\u2191 " ;
        else tmp = "D\u2193 " ;
        
        //Step on
        if (jump)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : 
                    goToVergence(max);
                    currentDirectionOfWork = DIVERGENCE_UP ;
                    break ;
                case DIVERGENCE_UP  :
                    goToVergence(min);
                    currentDirectionOfWork = CONVERGENCE_UP ;
                    break ;
            }
        else if (alternate & currentDirectionOfWork == CONVERGENCE_UP) {
            goToVergence(currentConvergenceValue);
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (alternate & currentDirectionOfWork == DIVERGENCE_UP) {
            goToVergence(currentDivergenceValue);
            currentDirectionOfWork = CONVERGENCE_UP ;
        }
        else    stepVergence (step) ;
        
        //Mise à jour valeur courante
        info.setText(tmp+String.valueOf(currentVergenceValue)+" \u0394");
        infosMax.setText("C" + String.valueOf(obtainedMax) + "  D" + String.valueOf(Math.abs(obtainedMin))) ;
        //Mise à jour du graphe
        OrthoStereogram.controller.addGraphMax (currentVergenceValue) ;
        repaint () ;
        
        //A-t-on fait un cycle ? oui, on affiche un trophé
        if (currentVergenceValue == 0 && obtainedMax == max && obtainedMin == min) {
            trophy[trophyNumber].setEnabled(true);
            if (trophyNumber<NB_TROPHY-1) trophyNumber++ ;
        }
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void badAnswer () {
        String tmp = new String() ;
        double step = this.step ;
        sndBad.run();
        //Première mauvaise réponse ?
        if (! alternate & ! jump & previousBadAnswer > 1) {
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : 
                    currentDirectionOfWork = CONVERGENCE_DOWN ;
                    this.step = - stepC ;
                    break ;
                case DIVERGENCE_UP  :
                    currentDirectionOfWork = DIVERGENCE_DOWN ;
                    this.step = stepD ;
                    break ;
            }
            //previousBadAnswer = 0 ;
        }
        else if (previousBadAnswer == 1) {
            step = 2 * step ;
        }
        
            previousBadAnswer++ ;
        //Time out off
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //Si on alterne
        if (jump)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : 
                    goToVergence(max);
                    currentDirectionOfWork = DIVERGENCE_UP ;
                    break ;
                case DIVERGENCE_UP  :
                    goToVergence(min);
                    currentDirectionOfWork = CONVERGENCE_UP ;
                    break ;
            }
        else if (alternate)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : goToVergence(currentDivergenceValue); currentDirectionOfWork = DIVERGENCE_UP ; break ;
                case DIVERGENCE_UP  : goToVergence(currentConvergenceValue); currentDirectionOfWork = CONVERGENCE_UP ; break ;
            }
        //et si on alterne pas
        else if (currentVergenceValue == 0) goToVergence(0);
        else
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : if (currentVergenceValue > 0) stepVergence(-step); break ;
                case CONVERGENCE_DOWN : if (currentVergenceValue > 0) stepVergence(step); break ;
                case DIVERGENCE_UP : if (currentVergenceValue < 0) stepVergence(-step); break ;
                case DIVERGENCE_DOWN : if (currentVergenceValue < 0) stepVergence(step); break ;
            }
        
        //Affichage
        if (currentDirectionOfWork == CONVERGENCE_UP) tmp = "C\u2191 " ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) tmp = "C\u2193 " ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) tmp = "D\u2191 " ;
        else tmp = "D\u2193 " ;
        info.setText(tmp+String.valueOf(currentVergenceValue)+" \u0394");
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        keypressedIsActive = false ;
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
        //System.out.println("Classic opened");
        //this.setExtendedState(ICONIFIED);
        OrthoStereogram.controller.setEnabled(false) ;
        hideCursor () ;
    }

    @Override
    public void windowClosing(WindowEvent we) {
        //On arrête le thread xbox
        if (joystickEvents != null ) joystickEvents.interrupt();
        //Add max value to graph
        //if (max != 0) OrthoStereogram.controller.addGraphMax (obtainedMax) ;
        //if (min != 0) OrthoStereogram.controller.addGraphMin (obtainedMin) ;
        
        //On réactive la fenêtre
        //OrthoStereogram.controller.setEnabled(true) ;
        executor.shutdownNow() ;
    }

    @Override
    public void windowClosed(WindowEvent we) {
        OrthoStereogram.controller.setEnabled(true);
        OrthoStereogram.controller.setVisible(true);
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
        OrthoStereogram.controller.setEnabled(false);
        hideCursor () ;
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        
    }

    
    public void dpad(int i, boolean bln) {
        if (bln) {
            switch (i) {
            case 0:  this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, 'A'));
                    break;
            case 1: break ;
            case 2:  this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, 'A'));
                     break;   
            case 3: break ;
            case 4:  this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, 'A'));
                     break;
            case 5: break ;
            case 6:  this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, 'A'));
                     break;
            default: break ;
            }
            
        }
    }
    
       
}

//Gestion de la xbox
class JoystickEvents extends Thread {
    private JFrame frame ;
    ByteBuffer buttons ;
    byte[] oldValues ={0,0,0,0,0,0,0,0,0,0,0,0,0,0,} ;
    //Constructor
    public JoystickEvents (JFrame frame) {
        this.frame = frame ;
        this.setName("xbox events") ;
        
        /*t = new Thread (this, "Joystick Events") ;
        t.start ( ) ;*/
    }
    
    public void run () {
        boolean stop = false ;
        do {
            glfwWaitEvents();
            if (glfwJoystickPresent(GLFW_JOYSTICK_1)) {
                buttons = GLFW.glfwGetJoystickButtons(GLFW_JOYSTICK_1);
                if (oldValues[10] == 0 & buttons.get(10) == 1) {
                    //System.out.println ("dpad up") ;
                    frame.dispatchEvent(new KeyEvent(frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, 'A'));
                }
                else if (oldValues[11] == 0 & buttons.get(11) == 1) {
                    //System.out.println ("dpad up") ;
                    frame.dispatchEvent(new KeyEvent(frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, 'A'));
                }
                else if (oldValues[12] == 0 & buttons.get(12) == 1) {
                    //System.out.println ("dpad up") ;
                    frame.dispatchEvent(new KeyEvent(frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, 'A'));
                }
                else if (oldValues[13] == 0 & buttons.get(13) == 1) {
                    //System.out.println ("dpad up") ;
                    frame.dispatchEvent(new KeyEvent(frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, 'A'));
                }
                oldValues[10] = buttons.get(10) ; // UP
                oldValues[11] = buttons.get(11) ; // RIGHT
                oldValues[12] = buttons.get(12) ; // DOWN
                oldValues[13] = buttons.get(13) ; // Left
            }
            else {
                //System.out.println(org.lwjgl.Version.getVersion());
                NewController.xboxConnected = glfwJoystickPresent (GLFW_JOYSTICK_1) ;
                NewController.jImgXBOX.setEnabled(NewController.xboxConnected);
                
                //System.out.println (GLFW.glfwGetJoystickName(GLFW_JOYSTICK_1) ) ;
            }
            
            try { Thread.sleep ( 100 ) ;} catch (InterruptedException  interruptedException) {stop = true ;}
            //System.out.println ("loop JoystickEventes :" + numButtons) ;
        } while (!stop) ;
        //System.out.println ("xbox events stopped") ;
    }
}