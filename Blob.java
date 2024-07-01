/* Blob.java
 * Rena Liu
 * 
 * Controls black blobs (enemies).
 */

import java.awt.*;

public class Blob {
    private int x1, x2; // leftmost and rightmost x-positions
    private int x, y; // current coordinates
    private int vx, frame; // horizontal velocity (will alternate between negative and positive value), animation frame
    private boolean exploding, exploded, turning, movingLeft; // ,movingLeft => to keep track of direction to know which images to display
    public static final int WIDTH = 15, HEIGHT = 15;  // blob rectangle dimensions (for checking collisions)

    private Image[] explodeImages = Util.loadImages("Blob/Explode", 1, 10);
    private Image[] turningLeft = Util.loadImages("Blob/TurnLeft", 1, 4), turningRight = Util.loadImages("Blob/TurnRight", 1, 4);
    private Image left = Util.loadImage("Blob/Left.png"), right = Util.loadImage("Blob/Right.png");
    private Sound splatSound = new Sound("SOUNDS/SplatSoundEffect.wav");

    // constructor
    public Blob(int x1, int x2, int y) {
        this.x1 = x1;
        this.x2 = x2;
        this.y = y;
        vx = 2;
        x = x1; // start from left positon
        exploding = false;
    }

    // get blob rectangle for collisions (a bit smaller than actual blob)
    public Rectangle getRect() {
        return new Rectangle(x+10, y+15, WIDTH, HEIGHT);
    }

    // returns rectangle of the area the blob paces back and forth within
    // used to check if player is in range so blob can chase player
    public Rectangle getRangeRect() {
        return new Rectangle(x1, y, x2-x1+30, HEIGHT);
    }

    // move blob if its not exploding or turning
    public void move() {
        if (!exploding && !turning) { 
            x += vx;

            // switches directions if blob reaches end of path
            if (x <= x1 || x >= x2) {
                turn();
            }
        }
    }

    // changing blob's direction to opposite 
    private void turn() {
        vx *= -1;
        turning = true;
        movingLeft = !movingLeft;
        frame = 0;
    }

    // start blob explosion 
    public void explode() {
        if (!exploding) {
            exploding = true;
            frame = 0;
            splatSound.play();
        }
    }

    // returns if game over or not
    public boolean killedPlayer() {
        return exploded;
    }

    // if player is in range, move blob towards the player
    public void chasePlayer(int playerx) {
                    // moving in opposite direction means player will be closer, change directions
        if (!exploding && Math.abs(x+vx - playerx) > Math.abs(x-vx - playerx)) {
            turn();
        }
    }

    // draw blob
    public void draw(Graphics g) {  
        if (exploding) {
            g.drawImage(explodeImages[frame/3], x-25, y-22, null);
            
            // finished exploding after going through all 10 images
            if (frame/3 == 9) { 
                exploding = false;
                exploded = true;
            }
        }
        else if (turning) {
            if (movingLeft) {
                g.drawImage(turningLeft[frame/3], x, y, null);
            }
            else {
                g.drawImage(turningRight[frame/3], x, y, null);
            }

            // finshed turning after going through all 3 images
            if (frame/3 == 3) {
                turning = false;
            }
        }
        // regular moving
        else if (movingLeft) {
            g.drawImage(left, x, y, null);
        }
        else {
            g.drawImage(right, x, y, null);
        } 
        
        frame++;
    }
}
