/* SoundEffect.java
 * Code from Mr. Mckenzie + my loop() method
 * 
 * Controls all sound (music and sound effects).
 */

import java.io.File;
import javax.sound.sampled.*;

public class Sound {
    private Clip clip; 

    public Sound(String file) {
        setClip(file);
    }

    public void setClip(String filename){
        try {
            File file = new File(filename);
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
        } 
        catch(Exception e) { 
            System.out.println("ERROR"); 
        }
    }

    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    // looping the clip until stop() is called
    public void loop() {
        clip.setLoopPoints(0, -1); // set points to beginning and end of clip
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }
}