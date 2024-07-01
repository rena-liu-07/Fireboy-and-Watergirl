/* LevelTimer.java
 * Rena Liu
 * 
 * Controls game's timer for each level. 
 */

import java.awt.*;

public class LevelTimer {
    private long startTime, current; // start of current elapsed time in milliseconds, current time in milliseconds 
    // total time in current level (seconds), remaining seconds after taking out minutes, elapsed minutes, total time before current elapsed period (if player paused game)
    private int time, seconds, minutes, beforePause; 
    private Image timerImage = Util.loadImage("Timer.png");

    // constructor
    // start timer when timer is created (when loading the level)
    public LevelTimer() {
        startTime = System.currentTimeMillis();
        beforePause = 0; // game hasn't been paused so no previous time
    }

    // updating the timer
    // converts time between 'current' and 'startTime' to seconds and updates time
    public void update() {
        current = System.currentTimeMillis();
        //current period (always updating) + previous times in current level (only changes when player pauses game)
        time = (int)(current-startTime)/1000 + beforePause;
        seconds = time % 60;
        minutes = time/60;
    }

    // pausing timer
    // adding current elapsed total to beforePause
    public void pause() {
        current = System.currentTimeMillis();
        beforePause += (current-startTime)/1000;
    }

    // restarting timer 
    // new elapsed period starting from the moment the timer was unpaused
    public void unpause() {
        startTime = System.currentTimeMillis();
    }

    // returns total time in current level in seconds
    public int getTime() {
        return time;
    }

    // drawing timer
    public void draw(Graphics g) {
        g.drawImage(timerImage, 359, 0, null);
        g.drawString(Util.drawTime(minutes) + ":" + Util.drawTime(seconds), 418, 37);
    }
}
