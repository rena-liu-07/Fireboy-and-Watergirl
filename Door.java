/* Door.java
 * Rena Liu
 * 
 * Controls end door for each level.
 */

import java.awt.*;

public class Door {
    private int x, y; // coordinates of door
    private int frame; // animation frame
    private boolean opening, opened; // if the door is in process of opening, if done opening and level is complete 
    public static final int WIDTH = 60, HEIGHT = 70; // dimensions of door

    private Image doorImage = Util.loadImage("Door/Closed.png");
    private Image[] openedDoor = Util.loadImages("Door/OpenedDoor", 0, 3);
    private Sound openSoundEffect = new Sound("SOUNDS/DoorSoundEffect.wav");

    // constructor
    public Door(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // return door rectangle
    public Rectangle getRect() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // start opening of the door if the door isn't already in the process opening
    public void openDoor() {
        if (!opening) { 
            opening = true;
            openSoundEffect.play();
            frame = 0;
        }
    }

    // checks if door is opened (aka level is done) to signal when to change screens
    public boolean checkLevelComplete() {
        return opened;
    }

    // displaying door
    public void draw(Graphics g) {
        if (opening) { 
            if (frame/5 > 3) { // if done opening
                opened = true;
                openSoundEffect.stop(); // stop sound effect if it is still playing when door is done opening
            }
            else {
                g.drawImage(openedDoor[frame/10], x, y, null);
            }  
        }
        else {
            g.drawImage(doorImage, x, y, null); // normal door
        }

        frame++;
    }

}
