/* Player.java
 * Rena Liu
 * 
 * Controls two main players (fireboy and watergirl).
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Player {
    private int x, y; // coordinates of player
    private int type, frame, leftKey, rightKey, upKey; // girl/boy, animation frame, keys that control player
    private int vy; // vertical velocity

    // what the player is currently doing
    private boolean jumping, falling, runLeft, runRight, sliding, dying, dead, exploded; 

    // offset distance for different sets of images
    private int[][] xDists = {{10, 25, 12, 10, 12}, {12, 36, 12, 12, 11}};
    private int[][] yDists = {{10, 10, 27, 5, 30}, {12, 13, 19, 14, 44}};

    public static final int BOY = 0, GIRL = 1; // player type
    public static final int UP = -1, DOWN = 1, LEFT = -1, RIGHT = 1; // direction for moving
    public static final int L = 0, R = 1, N = 2, U = 3, D = 4; // indexes for offset distances for each direction
    public static final int WIDTH = 11, HEIGHT = 39; // player dimensions
    public static final int XSPEED = 7, YSPEED = 12, SLOPESPEED = 2; // movement speed
    public static final int WALL = 0xFFFF0000, CLEAR = 0x00000000; // color codes for mask

    private Image[] Left, Right, Rest, Up, Down; 
    private Image[] dyingImgs = Util.loadImages("DieCloud/DieCloud", 0, 6);
    
    private Sound dieSoundEffect = new Sound("SOUNDS/DeathSoundEffect.wav"), jumpSoundEffect = new Sound("SOUNDS/JumpSoundEffect.wav");
    
    // constructor
    public Player(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        falling = false;
        jumping = false;
        runLeft = false;
        runRight = false;
        sliding = false;
        dying = false;
        dead = false;
        exploded = false;
        
        // setting the control keys and images based on which type of player it is
        if (type == BOY) {
            leftKey = KeyEvent.VK_LEFT;
            rightKey = KeyEvent.VK_RIGHT;
            upKey = KeyEvent.VK_UP;
            Left = Util.loadImages("FBLeft/FBLeft", 1, 8);
            Right = Util.loadImages("FBRight/FBRight", 1, 8);
            Up = Util.loadImages("FBUp/FBUp", 1, 5);
            Down = Util.loadImages("FBDown/FBDown", 1, 5);
            Rest = Util.loadImages("FBFront/FBFront", 1, 5);
        }
        else {
            leftKey = KeyEvent.VK_A;
            rightKey = KeyEvent.VK_D;
            upKey = KeyEvent.VK_W;
            Left = Util.loadImages("WGLeft/WGLeft", 1, 8);
            Right = Util.loadImages("WGRight/WGRight", 1, 8);
            Up = Util.loadImages("WGUp/WGUp", 1, 5);
            Down = Util.loadImages("WGDown/WGDown", 1, 5);
            Rest = Util.loadImages("WGFront/WGFront", 1, 5);
        }
    }

    // get player rectangle
    public Rectangle getRect() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // return x position of player center
    public int getMiddleX() {
        return x + WIDTH/2;
    }

    // return bottom y posiiton of player
    public int getBottomY() {
        return y + HEIGHT + 1;
    }

    // return type (girl or boy)
    public int getType(){
        return type;
    }

    // checks if player is currently walking (for puddle sound effect)
    public boolean isWalking() {
        return runLeft || runRight;
    }

    // moving player
    public void move(boolean[] keys, ArrayList<Wall> walls, ArrayList<Trapdoor> trapdoors, BufferedImage mask) {
        if (!dying && !exploded) {
            /* player can jump if there's nothing above player (checkWall), and if player is not already in the process of 
             * jumping or falling
             */
            if (keys[upKey] && !checkWall(walls, trapdoors, x, y) && !checkWall(walls, trapdoors, x+WIDTH, y)) {
                if (sliding || (!falling && !jumping)) {
                    jumping = true; // start jumping  
                    jumpSoundEffect.play();
                    frame = 0;
                    vy = YSPEED; 
                }
            }

            // if the player's walking left
            if (keys[leftKey]) { 
                runLeft = true; 

                /* if the player isn't running into a wall, move player left the max distance before 
                 * it hits something (wall or sliding platforn). if the player is walking left up a slope, 
                 * move player left and however far up it needs to go so that the player isn't in the slope.
                */
                if (!checkWall(walls, trapdoors, x, y)) {
                    x -= Math.min(distHorizontal(x, LEFT, mask), distHorizontalWall(x, LEFT, walls));
    
                    if (checkGoingUpSlope(x-1, LEFT, mask) && clear(x-SLOPESPEED, y, mask)) {
                        x -= SLOPESPEED;
                        y -= distUp(x, y+HEIGHT, mask); // gets distance until player's no longer in the platform
                    }
                }
            }
            else {
                runLeft = false;
            }
            
            // if the player's walking right
            if (keys[rightKey]) {
                runRight = true;

                /* if the player isn't running into a wall, move player right the max distance before 
                 * it hits something (wall or sliding platforn). if the player is walking right up a slope, 
                 * move player right and however far up it needs to go so that the player isn't in the slope.
                */
                if (!checkWall(walls, trapdoors, x+WIDTH, y)) {
                    x += Math.min(distHorizontal(x+WIDTH, RIGHT, mask), distHorizontalWall(x+WIDTH, RIGHT, walls));
                    
                    if (checkGoingUpSlope(x+WIDTH, RIGHT, mask) && clear(x+WIDTH+SLOPESPEED, y, mask)) { 
                        x += SLOPESPEED;
                        y -= distUp(x+WIDTH, y+HEIGHT, mask); // gets distance until player's no longer in the platform
                    }
                }
            }
            else {
                runRight = false;
            }
            
            /* if the player is at rest, checks if player is sliding down a slope. if player is sliding down a slope, 
             * this just needs to move player horizontally, and gravity/falling will do the rest in moving the player down
             * until it lands on the slope.
             */
            if (!runRight && !runLeft) {
                if (checkGoingDownSlope(x, LEFT, mask)) {
                    sliding = true;
                    x += SLOPESPEED;
                }
    
                else if (checkGoingDownSlope(x+WIDTH, RIGHT, mask)) { 
                    sliding = true;
                    x -= SLOPESPEED;
                }
    
                else {
                    sliding = false;
                }
            }
            
            if (!jumping) {
                // if the player is on a platform, it's not moving down in any way
                if (checkRow(y+HEIGHT+1, mask) || checkWall(walls, trapdoors, x, y+HEIGHT+1) || checkWall(walls, trapdoors, x+WIDTH, y+HEIGHT+1)) {
                    sliding = false;
                    vy = 0;               
                    falling = false;
                }
                else { // if the player is falling, accelerate until it reaches the max speed
                    if (vy < YSPEED) {
                        vy++;
                    }
                    
                    // move max distance the player can move at one time without hitting a wall or platform
                    y += Util.min(vy, distVertical(y+HEIGHT, DOWN, mask), distVerticalWall(y+HEIGHT, DOWN, walls));
    
                    falling = true;
                }
            }
            
            if (jumping) { 
                // move max distance the player can move at one time without hitting a wall or platform
                y -= Util.min(vy, distVertical(y, UP, mask), distVerticalWall(y, UP, walls));
                vy--; // deaccelerate up
    
                // if max height is reached or player hits something above it, player starts falling
                if (vy <= 0 || checkRow(y-1, mask) || checkWall(walls, trapdoors, x, y) || checkWall(walls, trapdoors, x+WIDTH, y)) {
                    sliding = false;
                    falling = true;
                    jumping = false;
                    vy = 0;
                    frame = 0;
                }
            }
        }
    }
    
    // code from Mr. Mckenzie
    private boolean clear(int x, int y, BufferedImage mask){
		if (x < 0 || x >= mask.getWidth(null) || y < 0 || y >= mask.getHeight(null)) {
			return false;
		}

		int color = mask.getRGB(x, y);
		return color == CLEAR;
	}

    // check if given row is touching a platform (checks if any pixel in the row isn't clear on the mask)
    private boolean checkRow(int y, BufferedImage mask) {
        for (int i=x; i<=x+WIDTH; i++) { 
            if (!clear(i, y, mask)) {
                return true;
            }
        }

        return false;
    }

    // finds most distance player can move vertically without hitting anything
    private int distVertical(int y, int direct, BufferedImage mask) {
        // for each row from furthest distance player can go to just touching player, find farthest row that isn't hitting anything
        for (int i=YSPEED; i>=0; i--) { 
            if (!checkRow(y+direct*i, mask)) {
                return i;
            }
        }

        return 0;
    }

    // check if given column is touching platform (if any pixel in column isn't clear on the mask)
    private boolean checkColumn(int x, BufferedImage mask) {
        for (int i=y; i<=y+HEIGHT; i++) { 
            if (!clear(x, i, mask)) { 
                return true;
            }
        }

        return false;
    }

    // finds most distance player can move horizontally without hitting anything
    private int distHorizontal(int x, int direct, BufferedImage mask) {
        // for each column from furthest distance player can go to just touching player, find farthest column that isn't hitting anything
        for (int i=XSPEED; i>=0; i--) {
            if (!checkColumn(x+direct*i, mask)) {
                return i;
            }
        }

        return 0;
    }

    /* finds distance player can move vertically without hitting a sliding wall. 
     * starts with a big distance, moves back until the farthest clear pixel from the player. returns the 
     * minimum out of all the distances for each wall.
    */
    private int distVerticalWall(int y, int direct, ArrayList<Wall> walls) {
        int dist = 100; 
        for (Wall wall : walls) {
            for (int i=YSPEED; i>=0; i--) {
                if (!wall.getRect().contains(x, y+direct*i) && !wall.getRect().contains(x+WIDTH, y+direct*i)) {
                    dist = Math.min(dist, i);
                    break;
                }
            }
        }

        return dist;
    }

    /* finds distance player can move horizontally without hitting a sliding wall.  
     * starts with a big distance, moves back until the farthest clear pixel from the player. returns the 
     * minimum out of all the distances for each wall.
    */
    public int distHorizontalWall(int x, int direct, ArrayList<Wall> walls) {
        int dist = 100;
        for (Wall wall : walls) {
            for (int i=XSPEED; i>=0; i--) {
                if (!wall.getRect().contains(x+direct*i, y) && !wall.getRect().contains(x+direct*i, y+HEIGHT)) {
                    dist = Math.min(dist, i);
                    break;
                }
            }
        }

        return dist;
    }

    /* checks if player is on a slope 
     * if pixels diagonally down from given corner aren't clear but pixels touching the player's same side higher up are clear, 
     * player will be going up a slope
    */
    private boolean checkGoingUpSlope(int x, int direct, BufferedImage mask) { 
        if (!clear(x, y+HEIGHT+SLOPESPEED, mask) && !clear(x+direct*SLOPESPEED, y+HEIGHT, mask)) {
            if (clear(x+direct*SLOPESPEED, y+HEIGHT-5*SLOPESPEED, mask)) {
                return true;
            }
        }
        return false;
    }

    /* checks if player is going down a slope 
     * if pixels diagonally down from given corner aren't clear but pixels under player's opposite side are clear, 
     * player will be going down a slope
    */
    private boolean checkGoingDownSlope(int x, int direct, BufferedImage mask) { 
        if (!clear(x, y+HEIGHT+SLOPESPEED, mask) && !clear(x+direct*SLOPESPEED, y+HEIGHT, mask)) {
            if (clear(x-direct*(WIDTH + SLOPESPEED), y+HEIGHT, mask)) {
                return true;
            }
        }
        return false;
    }

    // checks if player is hitting a sliding wall or trapdoor 
    public boolean checkWall(ArrayList<Wall> walls, ArrayList<Trapdoor> trapdoors, int x, int y) {
        for (Wall wall : walls) {
            if (wall.getRect().contains(x, y)) {
                return true;
            }
        }

        for (Trapdoor trapdoor : trapdoors) {
            if (trapdoor.getPolygon().contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    // finds distance from current point to empty pixel (moving out of slopes)
    private int distUp(int x, int y, BufferedImage mask) {
        int dist = 0;

        while (!clear(x, y, mask)) { // keep going up from current point until clear pixel is reached
            if (y < 0) { // out of bounds
                return -1;
            }
            y -= 1;
            dist++; 
        }

        return dist;
    }

    // finds x position to display image at. t is the index for the offset distance for the set of images being displayed
    public int drawX(int t) {
        return x-(xDists[type][t]); 
    }

    // finds y position to display image at. t is the index for the offset distance for the set of images being displayed
    public int drawY(int t) {
        return y-yDists[type][t]; 
    }

    // if player died, start dying animation
    public void die() {
        if (!dying) {
            dying = true;
            frame = 0;
            dieSoundEffect.play();
        }
    }

    // checks if player got exploded by a blob. if true, the player won't move or animate
    public void exploded() {
        exploded = true; 
    }

    // checks if player is dead or not
    public boolean checkDead(){ 
        return dead;
    }

    // drawing player based on what the player is currently doing
    public void draw(Graphics g) {  
        if (!exploded) {
            if (dying) {
                if (frame/3 > 6) {
                    dead = true;
                }
                g.drawImage(dyingImgs[frame/3%7], x-12, y-25, null);
            }  
            else if (runLeft) {
                g.drawImage(Left[frame/3%8], drawX(L), drawY(L), null);
            }
            else if (runRight) {
                g.drawImage(Right[frame/3%8], drawX(R), drawY(R), null);
            }
            else if (sliding) {
                g.drawImage(Rest[frame/3%5], drawX(N), drawY(N), null);
            }
            else if (jumping) {
                g.drawImage(Up[frame/3%5], drawX(U), drawY(U), null);
            }
            else if (falling) {
                g.drawImage(Down[frame/3%5], drawX(D), drawY(D), null);
            }
            else {
                g.drawImage(Rest[frame/3%5], drawX(N), drawY(N), null);
            }
    
            frame++;
        }
    }
}
