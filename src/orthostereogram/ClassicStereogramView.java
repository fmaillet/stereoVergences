/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import ch.aplu.xboxcontroller.XboxControllerListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
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
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_SUBTRACT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.newdawn.easyogg.OggClip;

/**
 *
 * @author Fred
 */
public class ClassicStereogramView extends JFrame implements WindowListener, KeyListener, MouseMotionListener, XboxControllerListener {
    
    static private Stereogram bimage  ;
    //OggClip audioOK = null ;
    //OggClip audioBAD = null ;
    SoundThread audioOK, audioBAD ;
    JLabel value ;
    
    //Constants
    final static public int CONVERGENCE_UP = 2  ;
    final static public int CONVERGENCE_DOWN = 1  ;
    final static public int DIVERGENCE_UP  = -1 ;
    final static public int DIVERGENCE_DOWN  = -2 ;
    
    //Parameters
    static private int stepC, stepD ;
    static private int step = 5 ;
    static private int max = 35 ;
    static private int min = -10 ;
    static private int timeOut = 20 ;
    static private int currentDirectionOfWork = CONVERGENCE_UP ;
    static private boolean alternate = false ;
    static private boolean jump = false ;
    private int workingDistance ;
    
    //pour l'alternance
    static int currentConvergenceValue ;
    static int currentDivergenceValue ;
    
    //Gestion du temps
    final ScheduledThreadPoolExecutor executor ;
    ScheduledFuture<?> scheduledFuture ;
    boolean keyPressedIsActive = false ;
    
    public ClassicStereogramView (int initialDelta, int currentDirectionOfWork, int workingDistance) {
        this.setVisible(false);
        //On travaille dans quel sens : C ou D ?
        this.currentDirectionOfWork = currentDirectionOfWork ;
        this.workingDistance = workingDistance ;
        //jolie fenêtre
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle ("Stéréogramme: ") ;
        setLayout(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        getContentPane().setBackground( Color.WHITE );
        
        //Si on a la xbox
        if (OrthoStereogram.xbox.isConnected())
            OrthoStereogram.xbox.addXboxControllerListener(this );

        
        //On crée un stéréogramme
        bimage = new Stereogram (NewController.imgSize, workingDistance, initialDelta) ;
        bimage.resetImg (false) ; 
        getContentPane().add(bimage);
        
        //On crée les fichiers sons
        /*try { audioOK = new OggClip(this.getClass().getResourceAsStream("correct.ogg")); }
        catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}
        try { audioBAD = new OggClip(this.getClass().getResourceAsStream("incorrect.ogg")); }
        catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}*/
        
        //New audio try
        audioOK = new SoundThread (true) ;
        audioBAD = new SoundThread (false) ;
        
        //On initialise le timeout
        executor = new ScheduledThreadPoolExecutor(1);        
    }
    
    public void setMode (int stepC, int stepD, int max, int min, int timeOut, boolean alternate, boolean jump) {
        
        this.stepC = stepC ; this.stepD = stepD ;
        //Step increment
        if (currentDirectionOfWork == CONVERGENCE_UP) this.step = stepC ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) this.step = -stepC ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) this.step = -stepD ;
        else this.step = stepD ;
        this.max = max ;
        this.min = min ;
        this.timeOut = timeOut ;
        this.alternate = alternate ;
        this.jump = jump ;
        currentConvergenceValue = 0 ;
        currentDivergenceValue = 0 ;
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
        //Current deltaX value (in pixels)
        value = new JLabel (String.valueOf(Stereogram.deltaPixelsX));
        value.setFont(new Font(value.getFont().getName(), Font.BOLD, 16));
        value.setBounds(20, 20, 150, 35);
        this.getContentPane().add(value) ;
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
        OrthoStereogram.controller.setEnabled(false);
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        OrthoStereogram.controller.setEnabled(true);
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
            audioOK.run() ;
            hideCursor () ;
            goodAnswer () ;
        }
        else if (keyCode == VK_UP | keyCode == VK_DOWN | keyCode == VK_LEFT | keyCode == VK_RIGHT | keyCode == VK_SPACE) {
            audioBAD.run() ;
            hideCursor () ;
            badAnswer () ;
        }
        
        
        //Dynamic resizing
        keyPressedIsActive = true ;
        if ((keyCode == VK_SUBTRACT | keyCode == VK_6) & ke.isControlDown() & ! ke.isShiftDown()) {
            if ( NewController.imgScale( 0.9 ) )
                bimage.resize(NewController.imgSize, true);
        }
        else if (keyCode == VK_ADD & ke.isControlDown() & ! ke.isShiftDown()) {
            if ( NewController.imgScale( 1.1 ) )
                bimage.resize(NewController.imgSize, true);
        }
        else if (keyCode == VK_EQUALS & ke.isControlDown() & ke.isShiftDown()) {
            if ( NewController.imgScale( 1.1 ) )
                bimage.resize(NewController.imgSize, true);
        }
        //On recentre le stéréogramme
        setSizes () ;
        //On a fini
        keyPressedIsActive = false ;
    }
    
    public void goodAnswer () {
        String tmp = new String() ;
                
        //Time out off
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        
        //On change le step aux bornes selon la direction de travail
        if (currentDirectionOfWork == CONVERGENCE_UP & Stereogram.currentVergenceValue+step > max) {
            step = - stepC ;
            currentDirectionOfWork = CONVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == CONVERGENCE_DOWN & Stereogram.currentVergenceValue+step < 0) {
            step = - stepD ;
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_UP & Stereogram.currentVergenceValue+step < min) {
            step = stepD ;
            currentDirectionOfWork = DIVERGENCE_DOWN ;
        }
        else if (currentDirectionOfWork == DIVERGENCE_DOWN & Stereogram.currentVergenceValue+step > 0) {
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
        //avant de modifier la direcetion courante
        if (currentDirectionOfWork == CONVERGENCE_UP) tmp = "C\u2191 " ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) tmp = "C\u2193 " ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) tmp = "D\u2191 " ;
        else tmp = "D\u2193 " ;
        
        //Step on
        if (jump)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : 
                    bimage.goToVergence(max);
                    currentDirectionOfWork = DIVERGENCE_UP ;
                    break ;
                case DIVERGENCE_UP  :
                    bimage.goToVergence(min);
                    currentDirectionOfWork = CONVERGENCE_UP ;
                    break ;
            }
        else if (alternate & currentDirectionOfWork == CONVERGENCE_UP) {
            bimage.goToVergence(currentConvergenceValue);
            currentDirectionOfWork = DIVERGENCE_UP ;
        }
        else if (alternate & currentDirectionOfWork == DIVERGENCE_UP) {
            bimage.goToVergence(currentDivergenceValue);
            currentDirectionOfWork = CONVERGENCE_UP ;
        }
        else    bimage.stepVergence (step) ;
        
        //Mise à jour valeur courante
        value.setText(tmp+String.valueOf(Stereogram.currentVergenceValue)+" \u0394");
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void badAnswer () {
        String tmp = new String() ;
        //Time out off
        if (scheduledFuture != null) scheduledFuture.cancel (true) ;
        executor.remove(() -> timeOut());
        //Si on alterne
        if (jump)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : 
                    bimage.goToVergence(max);
                    currentDirectionOfWork = DIVERGENCE_UP ;
                    break ;
                case DIVERGENCE_UP  :
                    bimage.goToVergence(min);
                    currentDirectionOfWork = CONVERGENCE_UP ;
                    break ;
            }
        else if (alternate)
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : bimage.goToVergence(currentDivergenceValue); currentDirectionOfWork = DIVERGENCE_UP ; break ;
                case DIVERGENCE_UP  : bimage.goToVergence(currentConvergenceValue); currentDirectionOfWork = CONVERGENCE_UP ; break ;
            }
        //et si on alterne pas
        else if (Stereogram.currentVergenceValue == 0) bimage.goToVergence(0);
        else
            switch (currentDirectionOfWork) {
                case CONVERGENCE_UP : if (Stereogram.currentVergenceValue > 0) bimage.stepVergence(-step); break ;
                case CONVERGENCE_DOWN : if (Stereogram.currentVergenceValue > 0) bimage.stepVergence(step); break ;
                case DIVERGENCE_UP : if (Stereogram.currentVergenceValue < 0) bimage.stepVergence(-step); break ;
                case DIVERGENCE_DOWN : if (Stereogram.currentVergenceValue < 0) bimage.stepVergence(step); break ;
            }
        
        //Affichage
        if (currentDirectionOfWork == CONVERGENCE_UP) tmp = "C\u2191 " ;
        else if (currentDirectionOfWork == CONVERGENCE_DOWN) tmp = "C\u2193 " ;
        else if (currentDirectionOfWork == DIVERGENCE_UP) tmp = "D\u2191 " ;
        else tmp = "D\u2193 " ;
        value.setText(tmp+String.valueOf(Stereogram.currentVergenceValue)+" \u0394");
        repaint () ;
        //On relance le timer
        scheduledFuture = executor.schedule(() -> timeOut(), timeOut, TimeUnit.SECONDS);
    }
    
    public void timeOut () {
        this.dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, 'A'));
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
        if (bln)
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
