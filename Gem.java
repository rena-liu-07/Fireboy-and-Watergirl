/* Gem.java
 * Rena Liu
 * 
 * Controls player gems that players need to collect.
 */

import java.awt.*;

public class Gem {
    public int x, y, type; // coords of gem, which player this gem is for
    public static final int BOY = 0, GIRL = 1; // constants for 'type'
    public static final int WIDTH = 23, HEIGHT = 26; // gem dimensions
    private Image FBGem = Util.loadImage("FBGem.png");
    private Image WGGem = Util.loadImage("WGGem.png");
    private Sound collectGemSoundEffect = new Sound("SOUNDS/GemSoundEffect.wav");

    // constructor
    public Gem(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    // return gem rectangle
    public Rectangle getRect() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // return which player gem is for
    public int getType() {
        return type;
    }

    // play sound effect when player collects gem
    public void collect() {
        collectGemSoundEffect.play();
    }

    // display gem
    public void draw(Graphics g) {
        if (type == BOY) {
            g.drawImage(FBGem, x, y, null);
        }
        else {
            g.drawImage(WGGem, x, y, null);
        }
    }
}
