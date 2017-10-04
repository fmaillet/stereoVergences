/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.easyogg.OggClip;

/**
 *
 * @author Fred
 */
public class SoundThread extends Thread {
    private OggClip audio = null ;
    private boolean ok ;
    
    
    public SoundThread (boolean ok) {
        this.ok = ok ;
        if (ok) {
            try { audio = new OggClip(this.getClass().getResourceAsStream("correct.ogg")); }
            catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}
            this.setName("audioOK");
        }
        else {
            try { audio = new OggClip(this.getClass().getResourceAsStream("incorrect.ogg")); }
            catch (final IOException e) {System.out.println ("Sound loading pb: " + e.toString()) ;}
            this.setName("audioBAD");
        }
        
    }
    public void run() {
        
        audio.play() ;
        while ( !audio.stopped()) {}
        try {
            Thread.sleep(150) ;
            } catch (InterruptedException ex) {
                Logger.getLogger(SoundThread.class.getName()).log(Level.SEVERE, null, ex);
            }

    }
}
