/* Main.java
 * Rena Liu
 */
import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {
    GamePanel game = new GamePanel();
    public Main() {
		super("Fireboy and Watergirl");
		
		// displaying icon
		Image icon = Util.loadImage("icon.png");
		setIconImage(icon);

		// opening window in middle of the screen
		setSize(GamePanel.WIDTH, GamePanel.HEIGHT);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(game);
		pack(); 
		setVisible(true);
    }    
    public static void main(String[] arguments) {
		new Main();		
    }

}