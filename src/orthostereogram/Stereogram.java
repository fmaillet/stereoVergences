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
    static public int currentVergenceValue;
    
    //needed to calculate vergence
    static Dimension screenSize ;
    static int screenResolution;
    static double workingDistance = 70 ;
    
    public Stereogram (int stereogramSize, int initialDelta) {
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
    
    public int calcPixelsForVergence (int vergence) {
        
        //double pixels = (((double)vergence * workingDistance /100) / 2.54) / (double) screenResolution ;
        double pixels = ((double) ((double)vergence * (double) workingDistance / 100) /2.54f ) * (double) screenResolution ;
        //System.out.println (pixels) ;
        return (int) Math.round(pixels) ;
    }
    
    //step a vergence
    public void stepVergence (int delta) {
        currentVergenceValue = currentVergenceValue + delta ;
        deltaPixelsX = calcPixelsForVergence ( currentVergenceValue ) ;
        resize (OD.getWidth(), false) ;
    }
    
    //Just to go to a fixed vergence value
    public void goToVergence (int value) {
        currentVergenceValue = value ;
        deltaPixelsX = calcPixelsForVergence (value) ;
        resize (OD.getWidth(), false) ;
    }
    
    public void slideToVergence (DisplayStereogram display, int value) {
        int step ;
        if (currentVergenceValue == value) return ;
        int debut = calcPixelsForVergence (currentVergenceValue ) ;
        int fin   = calcPixelsForVergence (value ) ;
        //Calcul d'un step en pixels
        step = (fin - debut) / 10 ;
        System.out.println (step) ;
        for (int i = debut; i>fin; i=i+step) {
            System.out.println (i) ;
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
    public void resetImg (boolean keepClue) {
        Random rand = new Random() ;
        Color colour ;
        
        //On rempli de valeurs aléatoires identiques OD=OG
        for (int i= 0; i<OD.getWidth(); i++)
            for (int j=0; j<OD.getHeight(); j++) {
                if (rand.nextBoolean())  colour = Color.BLACK ;
                else colour = Color.WHITE ;
                OD.setRGB(i, j, colour.getRGB());
                OG.setRGB(i, j, colour.getRGB());
            }
        //Paramètres
        int taille = OD.getHeight() ;
        int t = taille / 3 ;   //taille de la matrice
        int bord = 30 ;                  //bord
        int depth = 15 ;                    //disparité
        //Position aléatoire du carré : haut-bas-gauche-droite
        int p ;
        if (keepClue) switch (clue) {
            case KeyEvent.VK_UP : p = 0 ; break ;
            case KeyEvent.VK_LEFT : p = 1; break ;
            case KeyEvent.VK_RIGHT : p = 2 ; break ;
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
        //On crée le carré en relief
        for (int i=0; i<t; i++)
            for (int j=0; j<t; j++) {
                if (rand.nextBoolean())  colour = Color.BLACK ;
                else colour = Color.WHITE ;
                OG.setRGB(dc+i - depth, j+dh, colour.getRGB());
                OD.setRGB(dc+i + depth, j+dh, colour.getRGB());
            }
        //On crée l'anaglyphe
        anaglyph.createStereoscopicCombinedImage (OG, OD, ana, deltaPixelsX);
        if (deltaPixelsX < 0 & clue == KeyEvent.VK_LEFT)  clue = KeyEvent.VK_RIGHT ;
        else if (deltaPixelsX < 0 & (clue == KeyEvent.VK_RIGHT)) clue = KeyEvent.VK_LEFT ;
    }
    
    public void paint(Graphics g) {
      
        if (deltaPixelsX >= 0)
            g.drawImage(ana, 0,0,this);
        else
            g.drawImage(ana, 0 + ana.getWidth(), 0, -ana.getWidth(), ana.getHeight(), this);
      
   }
}
