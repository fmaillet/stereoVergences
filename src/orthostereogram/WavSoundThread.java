package orthostereogram;


import java.io.BufferedInputStream;
import java.io.IOException;
import javafx.scene.media.AudioClip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fred
 */
public class WavSoundThread extends Thread {
    public WavSoundThread (int t) {
        son = t ;
    }
    
    @Override
    public void run () {
        
        /*if (! OrthoVS.fen.sndMenu.isSelected())
                return ;*/
        switch (son) {
            case 0 :
                fichier = "/Ressources/incorrect.wav" ;
                break ;
            case 1 :
                fichier = "/Ressources/correct.wav" ;
                break ;
        }
        try {
            
            clip = AudioSystem.getClip();
                       
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                double gain = 0.25;
                float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
            clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream(fichier))));
            clip.start();
            /*try {
                Thread.sleep(6000);
            } catch (Exception e) {}
            if (clip.isOpen()) {
             clip.close();
            }*/
            
            clip.addLineListener( new LineListener() {
                @Override
                public void update(LineEvent evt) {
                    if (evt.getType() == LineEvent.Type.STOP) {
                        evt.getLine().close();
                    }
                }

                
            });
            
        }    catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
        {
            //System.out.println (fichier) ;    
            e.printStackTrace(System.out);
        }
        
    }
    
    int son ;
    String fichier ;
    Clip clip ;
}
