/* Wall.java
 * Rena Liu
 * 
 * Controls sliding walls.
 */

import java.awt.*;

public class Wall {
    private int x1, y1, x2, y2; // coordinates of the two positions it'll move between
    private int x, y, vx, vy; // curr coordinates of wall, horizontal and vertical velocities (one will equal 0)
    private int width, height, color; // dimensions, color of wall (same as corresponding button's color)
    private boolean moving, opened; // when wall is at (x1, y1), opened = false, (x2, y2) -> opened = true 
    public static final int SPEED = 20; 
    private Image activatedImg, unactivatedImage;

    // constructor
    public Wall(int x1, int y1, int x2, int y2, int width, int height, int color) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.width = width;
        this.height = height;
        this.color = color;

        // start at first position
        x = x1;
        y = y1;

        // calculate velocities based on distance between two points
        vx = (x2-x1)/SPEED;
        vy = (y2-y1)/SPEED;

        moving = false;
        opened = false;

        // load images based on color and orientation
        activatedImg = Util.loadImage("Walls/OpenWall" + color + width + ".png");
        unactivatedImage = Util.loadImage("Walls/Wall" + color + width + ".png");
    }

    // get button rectangle
    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    // returns string for corresponding button's image's file name 
    public String getColorForButton() {
        return "" + color + width;
    }

    // starts the wall's movement
    public void activate() {
        if (!moving) { 
            moving = true;
        }
    }    

    // move the wall
    public void move() {
        if (moving) {
            // stop moving if wall reached end of path
            if (((reachedEnd(x, x2, vx)|| reachedEnd(y, y2, vy)) && !opened) || ((reachedEnd(x, x1, vx) || reachedEnd(y, y1, vy)) && opened)) { 
                opened = !opened;
                moving = false;

                // flip velocites for next time wall is activated
                vx *= -1;
                vy *= -1;
            }
            else { 
                x += vx;
                y += vy;
            }
        }
    }

    // checking if wall reached end of path
    public boolean reachedEnd(int pos, int dest, int velocity) { 
        // if not moving in this direction, can't use these values to determine if destination is reached, so false
        if (velocity == 0) { 
            return false;
        }
        else {
            /* if velocity is negative, position will be getting smaller, so wall will have reached end when current position is 
            smaller than destination position. opposite for when velocity is positive. */
            if (velocity < 0) {
                return pos <= dest;
            }
            else {
                return pos >= dest;
            }
        }
    }

    // display wall
    public void draw(Graphics g) {
        if (moving || opened) {
            g.drawImage(activatedImg, x, y, null);
        }
        else {
            g.drawImage(unactivatedImage, x, y, null);
        }
    }
}
