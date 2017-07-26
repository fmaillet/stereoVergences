/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.image.BufferedImage;

/**
 *
 * @author Fred
 */
public class Anaglyph {
    public static void createStereoscopicCombinedImage(
            BufferedImage left, BufferedImage right, BufferedImage combined) {
        
        int width = left.getWidth();
        int height = left.getHeight();
        
        int[] leftRGB = new int[3];
        int[] rightRGB = new int[3];
        int[] combinedRGB = new int[3];
        
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                left.getRaster().getPixel(x, y, leftRGB);
                right.getRaster().getPixel(x, y, rightRGB);
                

                convertToGrayscale(leftRGB);
                convertToGrayscale(rightRGB);
                
                combinedRGB[0] = leftRGB[0];
                combinedRGB[1] = rightRGB[1];
                combinedRGB[2] = rightRGB[2];
                combined.getRaster().setPixel(x, y, combinedRGB);
            }
        }
    }
    
    public static void createStereoscopicCombinedImage(
            BufferedImage left, BufferedImage right, BufferedImage combined, int deltaX) {
        
        if (deltaX < 0 ) {
            BufferedImage tmp = left ;
            left = right ;
            right = tmp ;
            deltaX = - deltaX ;
        }
        
        
        int width = left.getWidth();
        int height = left.getHeight();
        
        int[] leftRGB = new int[3];
        int[] rightRGB = new int[3];
        int[] combinedRGB = new int[3];
        
        //Bande gauche
        rightRGB[0] = rightRGB[1] = rightRGB[2] = 255;
        convertToGrayscale(rightRGB);
        for (int x=0; x<deltaX; x++) {
            for (int y=0; y<height; y++) {
                if (x>=width) combined.getRaster().setPixel(x, y, rightRGB);
                else {
                    left.getRaster().getPixel(x, y, leftRGB);
                    convertToGrayscale(leftRGB);

                    combinedRGB[0] = leftRGB[0];
                    combinedRGB[1] = rightRGB[1];
                    combinedRGB[2] = rightRGB[2];
                    combined.getRaster().setPixel(x, y, combinedRGB);
                }
            }
        }
        //Bande droite
        leftRGB[0] = leftRGB[1] = leftRGB[2] = 255;
        convertToGrayscale(leftRGB);
        for (int x=0; x<deltaX; x++) {
            for (int y=0; y<height; y++) {
                if (x>=width) combined.getRaster().setPixel(width+deltaX - x - 1, y, leftRGB);
                else {
                    right.getRaster().getPixel(width-x-1, y, rightRGB);

                    convertToGrayscale(rightRGB);
                    combinedRGB[0] = leftRGB[0];
                    combinedRGB[1] = rightRGB[1];
                    combinedRGB[2] = rightRGB[2];
                    combined.getRaster().setPixel(width+deltaX - x - 1, y, combinedRGB);
                }
            }
        }
        //Bande centrale
        for (int x=0; x<width-deltaX; x++) {
            for (int y=0; y<height; y++) {
                left.getRaster().getPixel(x+deltaX, y, leftRGB);
                right.getRaster().getPixel(x, y, rightRGB);
                convertToGrayscale(leftRGB);
                convertToGrayscale(rightRGB);
                combinedRGB[0] = leftRGB[0];
                combinedRGB[1] = rightRGB[1];
                combinedRGB[2] = rightRGB[2];
                combined.getRaster().setPixel(deltaX+x, y, combinedRGB);
            }
        }
    }
    
    private static void convertToGrayscale(int[] rgb) {
        int m = (rgb[0] + rgb[1] + rgb[2]) / 3;
        rgb[0] = rgb[1] = rgb[2] = m;
    }
}
