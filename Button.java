/* Button.java
 * Rena Liu
 * 
 * Controls buttons that player steps on to activate sliding walls. 
 */

import java.awt.*;

public class Button {
    private int x, y; // coordinates of button
    private boolean pushed, activated; // if the player is currently standing on the button, if the button's on or off
    public static final int WIDTH = 47, HEIGHT = 16;   
    
    private Image button;
    private Wall wall; // corresponding wall that button controls
    private Sound pressSoundEffect = new Sound("SOUNDS/ButtonSoundEffect.wav");

    // constructor
    public Button(int x, int y, Wall wall, String img) {
        this.x = x;
        this.y = y;
        this.wall = wall;
        activated = false;
        pushed = false;
        button = Util.loadImage("Button" + img + ".png"); // loading button image that is the same color as the corresponding wall
    }

    // getting button rectangle
    public Rectangle getRect(){
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // pushes button and activates wall movement when player steps on button
    public void push() {
        if (!pushed) { // first time push() is called
            pushed = true;
            wall.activate();
            activated = !activated; // turn on/off
            pressSoundEffect.play();
        }
    }

    // stops pushing when player steps off button
    public void unpush() {
        pushed = false;
    }

    // draw button
    public void draw(Graphics g) {
        if (!pushed) { // show full button when not being pushed
            g.drawImage(button, x, y, null);
        }
    }

}
