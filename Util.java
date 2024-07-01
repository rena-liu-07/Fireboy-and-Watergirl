/* Util.java
* Rena Liu
* 
* Contains helper methods.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.imageio.*;
import java.io.*;

public class Util {
    // loading an image
    public static Image loadImage(String img){
        return new ImageIcon("IMAGES/" + img).getImage();
    }

    // loading a set of images
    public static Image[] loadImages(String img, int start, int end) {
        Image[] imgs = new Image[end-start+1]; // array of images

        for (int i=0; i<=end-start; i++) { // load each image
            imgs[i] = Util.loadImage(img + (i+start) + ".png");
        }

        return imgs; 
    }

    // loading buffered image (code from Mr. Mckenzie)
    public static BufferedImage loadBufferedImg(String img) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File("IMAGES/" + img));
            return image;
        } 
        catch (IOException e) {
            return null;
        }
    }

    // random number (code from Mr. Mckenzie)
    public static int randint(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    // getting min of three values
    public static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    // loading fonts from a file (code from Mr. Mckenzie)
    public static Font loadFont(String name, int size){
        Font font=null;
        try{
            File fntFile = new File(name);
            font = Font.createFont(Font.TRUETYPE_FONT, fntFile).deriveFont((float)size);
        }
        catch(IOException ex){
            System.out.println(ex);	
        }
        catch(FontFormatException ex){
            System.out.println(ex);	
        }
        return font;
    }

    // returning number with leading zeros if needed
    public static String drawTime(int time) {
        if (time < 10) {
            return "0" + time;
        }
        else {
            return "" + time;
        }
    }

    // converting string to integer (did this because it takes less characters)
    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    // display text nicely with shadows
    public static void drawString(Graphics g, String s, int x, int y, Color shadow, Color color) {
        g.setColor(shadow);
        g.drawString(s, x-1, y-1);
        g.drawString(s, x+1, y-1);
        g.drawString(s, x+1, y+1);
        g.drawString(s, x+1, y-1);
        
        g.setColor(color);
        g.drawString(s, x, y);
    }
}
 