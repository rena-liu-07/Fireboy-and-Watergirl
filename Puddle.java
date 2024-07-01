/* Puddle.java
 * Rena Liu
 * 
 * Controls fire and water puddles.
 */
import java.awt.*;

public class Puddle {
    private int x, y, width, type, length, frame; // coordinates, dimensions, water or fire, size of puddle, animation frame
    private boolean sloshing; // if water moving sound effect is playing (plays if player is walking in puddle)
    public static final int HEIGHT = 10, FIRE = 0, WATER = 1; // constants for 'type'
    public static final int SHORT = 0, MEDIUM = 1, LONG = 2; // puddle size constants

    // animation images
    private Image[][] puddleShort = {Util.loadImages("FBPuddle/Short", 1, 5), Util.loadImages("WGPuddle/Short", 1, 5)};
    private Image[][] puddleMedium = {Util.loadImages("FBPuddle/Medium", 1, 5), Util.loadImages("WGPuddle/Medium", 1, 5)};
    private Image[][] puddleLong = {Util.loadImages("FBPuddle/Long", 1, 7), Util.loadImages("WGPuddle/Long", 1, 7)};
    
    private Sound puddleSoundEffect = new Sound("SOUNDS/PuddleWalkSoundEffect.wav");

    // constructor
    public Puddle(int x, int y, int type, int len) { // width will be different depending on 'len'
        this.x = x;
        this.y = y;
        this.type = type;
        length = len;
        frame = 0;
        sloshing = false;

        if (length == SHORT) {
            width = 45;
        }
        else if (length == MEDIUM) {
            width = 90;
        }
        else {
            width = 145; 
        }
    }

    // get puddle rectangle
    public Rectangle getRect() {
        return new Rectangle(x, y, width, HEIGHT);
    }

    // gets puddle type
    public int getType(){
        return type;
    }

    // plays sound effect
    public void slosh() {
        if (!sloshing) {
            sloshing = true;
            puddleSoundEffect.play();
        }
    }

    // stops sounds effect
    public void unslosh() {
        sloshing = false; // changes 'sloshing' back to false when player is out of puddle for next time
        puddleSoundEffect.stop();
    }

    // display puddle based on size and type
    public void draw(Graphics g) {
        if (length == SHORT){
            g.drawImage(puddleShort[type][frame/10%5], x, y-5, null);
        }
        else if (length == MEDIUM) {
            g.drawImage(puddleMedium[type][frame/10%5], x, y-5, null);
        }
        else {
            g.drawImage(puddleLong[type][frame/10%7], x, y-5, null);
        }
        
        frame++;
    }

}
