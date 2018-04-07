/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;


/**
 *
 * @author Fred
 */
public class Stereogram extends JPanel {
    static public BufferedImage OD, OG, ana ;
    static private Anaglyph anaglyph ;
    public int clue ;
    static public int deltaPixelsX ;
    static public double currentVergenceValue;
    SecureRandom rand = new SecureRandom() ;
    
    //Used for slider
    private boolean centered = false ;
    
    //needed to calculate vergence
    static Dimension screenSize ;
    static int screenResolution;
    static int workingDistance = 70 ;
    
    public Stereogram (int stereogramSize, int workingDistance, int initialDelta, boolean centered) {
        
        this.centered = centered ;
        this.workingDistance = workingDistance ;
        
        //Check for odd size
        if ( (stereogramSize & 1) != 0 )  {
            stereogramSize-- ;
            //System.out.println ("Beware !! stereogram even size !! (corrected...)") ;
        } 
        
        //Just to be able to calculate pixels vs vergence demand
        screenResolution = Toolkit.getDefaultToolkit().getScreenResolution() ;
        
        //Vergence de base
        currentVergenceValue = initialDelta ;
        deltaPixelsX = calcPixelsForVergence(initialDelta) ;
        //Create the image
        OD = new BufferedImage(stereogramSize, stereogramSize, BufferedImage.TYPE_INT_RGB);
        OG = new BufferedImage(stereogramSize, stereogramSize, BufferedImage.TYPE_INT_RGB);
        ana = new BufferedImage(stereogramSize + Math.abs(deltaPixelsX), stereogramSize, BufferedImage.TYPE_INT_RGB);
        this.setSize(stereogramSize + Math.abs(deltaPixelsX), stereogramSize);
        //Render anaglyph
        anaglyph = new Anaglyph () ;
        
    }
    
    public int calcPixelsForVergence (double vergence) {
        
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = ((double) (vergence * (double) workingDistance / 100) /2.54f ) * (double) screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels) ;
    }
    
    //step a vergence
    public void stepVergence (double delta) {
        currentVergenceValue = currentVergenceValue + delta ;
        deltaPixelsX = calcPixelsForVergence ( currentVergenceValue ) ;
        resize (OD.getWidth(), false) ;
    }
    
    //Just to go to a fixed vergence value
    public void goToVergence (double value) {
        currentVergenceValue = value ;
        deltaPixelsX = calcPixelsForVergence (value) ;
        resize (OD.getWidth(), false) ;
    }
    
    public void slideToVergence (OldClassicStereogramView display, int value) {
        int step ;
        if (currentVergenceValue == value) return ;
        int debut = calcPixelsForVergence (currentVergenceValue ) ;
        int fin   = calcPixelsForVergence (value ) ;
        //Calcul d'un step en pixels
        step = (fin - debut) / 10 ;
        //System.out.println (step) ;
        for (int i = debut; i>fin; i=i+step) {
            //System.out.println (i) ;
            ana.flush(); ana = null ;
            ana = new BufferedImage(OD.getWidth() + Math.abs(i), OD.getWidth(), BufferedImage.TYPE_INT_RGB);
            anaglyph.createStereoscopicCombinedImage (OG, OD, ana, i);
            display.repaint () ;
            try {TimeUnit.MILLISECONDS.sleep(250);} catch (InterruptedException e) {}
        }
        currentVergenceValue = value ;
        deltaPixelsX = calcPixelsForVergence (value) ;
        resize (OD.getWidth(), false) ;
    }
    
    public void resize (int newSize, boolean keepClue) {
        OD.flush(); OD = null ;
        OG.flush(); OG = null ;
        ana.flush(); ana = null ;
        OD = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_RGB);
        OG = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_RGB);
        ana = new BufferedImage(newSize + Math.abs(deltaPixelsX), newSize, BufferedImage.TYPE_INT_RGB);
        resetImg (keepClue) ;
        this.setSize(newSize + Math.abs(deltaPixelsX), newSize);
        repaint() ;
    }
    
    //Reset the stereogram content
    //if keepclue= true then same clue
    public void resetImg (boolean keepClue) {
        
        int couleurRGB = 0 ;
        
        //On rempli de valeurs aléatoires identiques OD=OG
        int taille = OD.getHeight() ;
        for (int i= 0; i<taille; i++)
            for (int j=0; j<taille; j++) {
                if (rand.nextBoolean())  couleurRGB = Color.BLACK.getRGB() ;
                else couleurRGB = Color.WHITE.getRGB() ;
                OD.setRGB(i, j, couleurRGB);
                OG.setRGB(i, j, couleurRGB);
            }
        //Paramètres
        int t = taille / 3 ;   //taille de la matrice
        int bord = 30 ;                  //bord
        int depth = 20 ;                    //disparité
        //Position aléatoire du carré : haut-bas-gauche-droite
        int p ;
        if (keepClue) switch (clue) {
            case KeyEvent.VK_UP : p = 0 ; break ;
            case KeyEvent.VK_LEFT : if (OrthoStereogram.BR_glasses) p = 1; else p = 2; break ;
            case KeyEvent.VK_RIGHT : if (OrthoStereogram.BR_glasses) p = 2 ; else p = 1 ; break ;
            default : p = 3 ; break ;
        }
        else p = rand.nextInt(4) ;
        int dh, dc ;
        switch (p) {
            case 0 : dh = bord ; dc = taille/2 - t/2 ; clue = KeyEvent.VK_UP ; break ;    //up
            case 1 : dh = taille/2 - t/2 ; dc = bord;  clue = KeyEvent.VK_LEFT ; break ;    //left
            case 2 : dh = taille/2 - t/2 ; dc = taille - t - bord ; clue = KeyEvent.VK_RIGHT ; break ;    //right
            default : dh = taille - t - bord ; dc = taille/2 - t/2 ; clue = KeyEvent.VK_DOWN ; break ;   //down
        }
        //Si slider alors diamond
        if (centered) {
            t = taille / 2 ;
            dh = dc =  (taille - t) / 2 ;
            int c = 0 ;
            //Draw a diamond
            for (int i=0; i<t/2; i++)
                for (int j=0; j<i; j++) {
                    if (rand.nextBoolean()) couleurRGB = Color.BLACK.getRGB() ; else couleurRGB = Color.WHITE.getRGB() ;
                    //Quart inf gauche
                    OG.setRGB(taille/4 + i - depth, taille / 2 + j, couleurRGB);
                    OD.setRGB(taille/4 + i + depth, taille / 2 + j, couleurRGB);
                    //Quart ?
                    OG.setRGB(taille/4 + i - depth, taille / 2 - j, couleurRGB);
                    OD.setRGB(taille/4 + i + depth, taille / 2 - j, couleurRGB);
                    //Quart
                    OG.setRGB((3*taille/4) - i - depth, taille / 2 + j, couleurRGB);
                    OD.setRGB((3*taille/4) - i + depth, taille / 2 + j, couleurRGB);
                    //Quart
                    OG.setRGB((3*taille/4) - i - depth, taille / 2 - j, couleurRGB);
                    OD.setRGB((3*taille/4) - i + depth, taille / 2 - j, couleurRGB);
                }
        }
        //On crée le carré en relief
        else for (int i=0; i<t; i++)
            for (int j=0; j<t; j++) {
                if (rand.nextBoolean())  couleurRGB = Color.BLACK.getRGB() ;
                else couleurRGB = Color.WHITE.getRGB() ;
                OG.setRGB(dc+i - depth, j+dh, couleurRGB);
                OD.setRGB(dc+i + depth, j+dh, couleurRGB);
            }
        
        
        //On crée l'anaglyphe
        anaglyph.createStereoscopicCombinedImage (OG, OD, ana, deltaPixelsX);
        if (OrthoStereogram.BR_glasses) {
            if (deltaPixelsX < 0 & clue == KeyEvent.VK_LEFT)  clue = KeyEvent.VK_RIGHT ;
            else if (deltaPixelsX < 0 & clue == KeyEvent.VK_RIGHT) clue = KeyEvent.VK_LEFT ;
        }
        else {
            if (deltaPixelsX >= 0 & clue == KeyEvent.VK_LEFT)  clue = KeyEvent.VK_RIGHT ;
            else if (deltaPixelsX >= 0 & clue == KeyEvent.VK_RIGHT) clue = KeyEvent.VK_LEFT ;
        }
    }
    
    public void paint(Graphics g) {
        super.paintComponent(g);
        if (OrthoStereogram.BR_glasses) {
            if (deltaPixelsX >= 0)
                g.drawImage(ana, 0,0,this);
            else
                g.drawImage(ana, 0 + ana.getWidth(), 0, -ana.getWidth(), ana.getHeight(), this);
        }
        else {
            if (deltaPixelsX >= 0)
                g.drawImage(ana, 0 + ana.getWidth(), 0, -ana.getWidth(), ana.getHeight(), this);
            else
                g.drawImage(ana, 0,0,this);
        }
   }
}
