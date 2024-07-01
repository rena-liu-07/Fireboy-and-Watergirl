/* Trapdoor.java
 * Rena Liu
 * 
 * Controls rotating trapdoors.
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class Trapdoor {
    private int currPos = 0, frame; // index for which stage of turning it's currently at, animation frame
    private int x, y; // coordinates of the corner the trapdoor rotates around
    private boolean opening = false; // if trapdoor is rotating
    private int[][] xPoints = new int[9][4]; // coordinates for each stage of turning
    private int[][] yPoints = new int[9][4];
    public static final int WIDTH = 40, HEIGHT = 25; // trapdoor dimensions

    // for rotating the image, code from Mr. Mckenzie
    private AffineTransform rotate;
    private AffineTransformOp rotateOp;
    private BufferedImage img = Util.loadBufferedImg("Trapdoor.png");
    private Graphics2D g2D;

    private Sound swingSound = new Sound("SOUNDS/TrapdoorSoundEffect.wav");

    // constructor
    public Trapdoor(int x, int y) { 
        this.x = x;
        this.y = y;

        for (int i=0; i<=8; i++) { // calculating coordinates for Polygon based on angle
            double angle = (Math.PI/12)*i;
            xPoints[i][0] = x;
            xPoints[i][1] = x + (int)(WIDTH * Math.cos(angle));
            xPoints[i][2] = x + (int)(WIDTH * Math.cos(angle) - HEIGHT * Math.sin(angle));
            xPoints[i][3] = x - (int)(HEIGHT * Math.sin(angle));

            yPoints[i][0] = y;
            yPoints[i][1] = y + (int)(WIDTH * Math.sin(angle));
            yPoints[i][2] = y + (int)(WIDTH * Math.sin(angle) + HEIGHT * Math.cos(angle));
            yPoints[i][3] = y + (int)(HEIGHT * Math.cos(angle));
        }
    }

    // get trapdoor Polygon shape
    public Polygon getPolygon() {
        return new Polygon(xPoints[currPos], yPoints[currPos], 4);
    }

    // start opening trapdoor
    public void activate() {
        if (!opening) {
            opening = true;
            swingSound.play();
            frame = 0;
        }
    }

    // rotates the trapdoor
    public void move() {
        if (opening) {
            if (frame < 8) { // swinging open when frame is betwen 0 and 8
                currPos++;
            }
            else {
                if (frame == 16) { // finished swinging (back to start position)
                    opening = false;
                }
                else { // swinging closed when frame is between 8 and 16
                    currPos--;
                }
            }         

            frame++;
        }
    }

    // drawing trapdoor, rotating the image
    // code from Mr. Mckenzie
    public void draw(Graphics g) {
        rotate = new AffineTransform();
        rotate.rotate((Math.PI/12)*currPos, 0, 0);
        rotateOp = new AffineTransformOp(rotate, AffineTransformOp.TYPE_BILINEAR);
        g2D = (Graphics2D)g;
        g2D.drawImage(img, rotateOp, x, y);
    }
}