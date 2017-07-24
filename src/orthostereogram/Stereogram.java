/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JPanel;


/**
 *
 * @author Fred
 */
public class Stereogram extends JPanel {
    static public BufferedImage OD, OG, ana ;
    static private Anaglyph anaglyph ;
    public int clue ;
    static private int deltaX ;
    
    public Stereogram (int size) {
        //Convergence
        deltaX = 10 ;
        //Create the image
        OD = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        OG = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        ana = new BufferedImage(size + deltaX, size, BufferedImage.TYPE_INT_RGB);
        this.setSize(size + deltaX, size);
        
        anaglyph = new Anaglyph () ;
    }
    
    public void incConvergence (int delta) {
        deltaX = deltaX + delta ;
        resize (OD.getWidth(), false) ;
    }
    
    public void resize (int newSize, boolean keepClue) {
        OD.flush(); OD = null ;
        OG.flush(); OG = null ;
        ana.flush(); ana = null ;
        OD = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_RGB);
        OG = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_RGB);
        ana = new BufferedImage(newSize + deltaX, newSize, BufferedImage.TYPE_INT_RGB);
        resetImg (keepClue) ;
        this.setSize(newSize + deltaX, newSize);
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
        anaglyph.createStereoscopicCombinedImage (OG, OD, ana, deltaX);
    }
    
    public void paint(Graphics g) {
      
      g.drawImage(ana, 0,0,this);
      
   }
}
