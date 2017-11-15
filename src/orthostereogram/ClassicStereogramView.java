/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import ch.aplu.xboxcontroller.XboxControllerListener;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Fred
 */
public class ClassicStereogramView extends JFrame implements WindowListener, MouseMotionListener, KeyListener, XboxControllerListener {
    
    //Constructor
    //SoundThread audioOK, audioBAD ;
    Anaglyph anaglyph ;
    static private Stereogram bimage  ;
    private OneEye od, og ;
    private int deltaX = 0 ;
    private int deltaY = 0 ;
    Cursor transparentCursor ;
    private JLabel info ;
    
    
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
    static private int max = 35 ;
    static private int min = -10 ;
    static private int currentDirectionOfWork = CONVERGENCE_UP ;
    static private boolean alternate = false ;
    static private boolean jump = false ;
    //pour l'alternance
    static double currentConvergenceValue ;
    static double currentDivergenceValue ;
    static double currentVergenceValue ;
    static private boolean previousBadAnswer = false ;
    
    //délai de réponse
    static private int timeOut = 20 ;
    final ScheduledThreadPoolExecutor executor ;
    ScheduledFuture<?> scheduledFuture ;
    boolean keyPressedIsActive = false ;
    
    //Delai xBox
    final ScheduledThreadPoolExecutor executorXBox ;
    private boolean xboxInhibit = false ;
    
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
    
    //Min and max are given in dioptries
    public ClassicStereogramView (int initialValue, int currentDirectionOfWork, int workingDistance) {
        //this.minPixels = calcPixelsForVergence (min) ;
        //this.maxPixels = calcPixelsForVergence (max) ;
        this.currentDirectionOfWork = currentDirectionOfWork ;
        this.workingDistance = workingDistance ;
        this.currentVergenceValue = initialValue ;
        this.deltaX = calcPixelsForVergence (initialValue) ;
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
        executorXBox = new ScheduledThreadPoolExecutor(1);
        
        //Si on a la xbox
        if (OrthoStereogram.xboxConnected)
            OrthoStereogram.xbox.addXboxControllerListener(this );
        
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
        
        //infos
        info = new JLabel ("Allez-y...") ;
        info.setBounds(10, 10, 300, 30);
        this.getContentPane().add(info) ;
        
        
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
        this.max = max ;
        this.min = min ;
        //time out to answer
        this.timeOut = timeOut ;
        
        //Game mode
        this.alternate = alternate ;
        this.jump = jump ;
        currentConvergenceValue = 0 ;
        currentDivergenceValue = 0 ;
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
        
        if (keyPressedIsActive) return ;
        
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
            keyPressedIsActive = true ;
            if (NewController.imgScale( 0.9 )) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            keyPressedIsActive = true ;
            if ( NewController.imgScale( 1.1 ) ) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            keyPressedIsActive = true ;
            if ( NewController.imgScale( 1.1 ) ) {
                bimage.resize(NewController.imgSize, true);
                od.resize(); anaglyph.createStereoscopicBlueImage (bimage.OD) ;
                og.resize(); anaglyph.createStereoscopicRedImage (bimage.OG) ;
                setPositions () ;
            }
        }
        
        hideCursor () ;
        keyPressedIsActive = false;
    }
    
    public void goodAnswer () {
        String tmp = new String() ;
        sndGood.run();
        previousBadAnswer = false ;        
        //Time out off
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        
        //On change le step aux bornes selon la direction de travail
        if (currentDirectionOfWork == CONVERGENCE_UP & currentVergenceValue+step > max) {
            step = - stepC ;
            currentDirectionOfWork = CONVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_DOWN & currentVergenceValue+step < 0) {
            step = - stepD ;
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & currentVergenceValue+step < min) {
            step = stepD ;
            currentDirectionOfWork = DIVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_DOWN & currentVergenceValue+step > 0) {
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
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void badAnswer () {
        String tmp = new String() ;
        double step = this.step ;
        sndBad.run();
        //Première mauvaise réponse ?
        if (previousBadAnswer) {
            step = 2 * step ;
        }
        else
            previousBadAnswer = true ;
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
        //System.out.println("Classic opened");
        //this.setExtendedState(ICONIFIED);
        OrthoStereogram.controller.setEnabled(false) ;
        hideCursor () ;
    }

    @Override
    public void windowClosing(WindowEvent we) {
        OrthoStereogram.controller.setEnabled(true) ;
        executor.shutdownNow() ;
    }

    @Override
    public void windowClosed(WindowEvent we) {
        OrthoStereogram.controller.setEnabled(true);
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

    @Override
    public void buttonA(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buttonB(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buttonX(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buttonY(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void back(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void leftShoulder(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rightShoulder(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void leftThumb(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rightThumb(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dpad(int i, boolean bln) {
        if (bln & !xboxInhibit) {
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
            xboxInhibit = true ;
            executorXBox.schedule(() -> xboxTimeOut(), 50, TimeUnit.MILLISECONDS) ;
        }
    }
    
    private void xboxTimeOut () {
        xboxInhibit = false ;
    }

    @Override
    public void leftTrigger(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rightTrigger(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void leftThumbMagnitude(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void leftThumbDirection(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rightThumbMagnitude(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rightThumbDirection(double d) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void isConnected(boolean bln) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

/*//Classe OD OG
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
                g.drawImage(Stereogram.OG, 0 + Stereogram.OG.getWidth(), 0, -Stereogram.OG.getWidth(), Stereogram.OG.getHeight(), this);
        }
        else {
            if (OrthoStereogram.BR_glasses)
                g.drawImage(Stereogram.OG, 0,0,this);
            else
                g.drawImage(Stereogram.OD, 0 + Stereogram.OD.getWidth(), 0, -Stereogram.OD.getWidth(), Stereogram.OD.getHeight(), this);
        }
    }
}*/