/* GamePanel.java
 * Rena Liu
 * 
 * Controls main game.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
 
class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener{
    Timer timer;
    Player boy, girl;
    BufferedImage mask;
    Image levelBackground;
    ArrayList<Gem> gems;
    ArrayList<Puddle> puddles;
    ArrayList<Button> buttons;
    ArrayList<Wall> walls;
    ArrayList<Trapdoor> trapdoors;
    ArrayList<Blob> blobs;
    Door door;
    Button pressedButton;
    LevelTimer gameTimer;

    String screen = "intro";

    // current level player is on (1-4), how many levels are unclocked
    int level = 1, openLevels = 1; 
    int gemCount; // how many total gems collected by both players
    boolean firstFrame; // for drawing semi-transparent layer only once (for pause, game over, and win screen)
    
    boolean[] keys;
    int[][] menuGemPos = {{467, 504}, {428, 214}, {702, 159}, {161, 124}}; // positions for displaying images on the map
    int[] levelStatus = {0, 0, 0, 0}; // which color gem to display (0 -> grey, 1 -> yellow, etc)
    int[] numberOfGems = {14, 12, 14, 9}; // number of total gems each level includes
    int[] levelTimes = {50, 60, 45, 70}; // set time for each level that determines whether user gets a star for time or not

    static final int BORDER = 20, WIDTH = 940, HEIGHT = 700; // margin size, screen dimensions
    static final int HORIZONTAL = 0, VERTICAL = 1; // orientation for walls
    
    // Rectangles for each level on the map
    Rectangle[] levelButtons = {new Rectangle(457, 494, 70, 80), new Rectangle(418, 204, 70, 80), new Rectangle(692, 149, 70, 80), new Rectangle(151, 114, 70, 80)};
    
    // Different buttons user can click on
    Rectangle playButton = new Rectangle(373, 375, 195, 55), backButton = new Rectangle(70, 575, 170, 40);
    Rectangle menuButton = new Rectangle(313, 380, 140, 55), GORetryButton = new Rectangle(486, 380, 140, 55);
    Rectangle PRetryButton = new Rectangle(493, 331, 140, 55), endButton = new Rectangle(301, 330, 140, 55);
    Rectangle resumeButton = new Rectangle(393, 407, 150, 55), continueButton = new Rectangle(370, 400, 200, 55);
    Rectangle pauseButton = new Rectangle(895, 15, 30, 30);

    BufferedImage[] masks = {Util.loadBufferedImg("Masks/MaskLevel1.png"), Util.loadBufferedImg("Masks/MaskLevel2.png"), Util.loadBufferedImg("Masks/MaskLevel3.png"), Util.loadBufferedImg("Masks/MaskLevel4.png")};
    Image[] backgrounds = Util.loadImages("Levels/Level", 1, 4); // level map (walls)
    Image[] menuImgs = Util.loadImages("MenuScreens/", 1, 4); // different menu images with different number of levels open
    Image[] menuGemImages = {Util.loadImage("GreyLevelGem.png"), Util.loadImage("YellowLevelGem.png"), Util.loadImage("RedLevelGem.png"), Util.loadImage("PurpleLevelGem.png")};
    Image brickBackground = Util.loadImage("Background.png"), menuBackground = Util.loadImage("MenuBackground.png"), homeScreen = Util.loadImage("HomeScreen.png");
    Image pausedImg = Util.loadImage("PausedScreen.png"), gameOverImage = Util.loadImage("GameOverScreen.png"), wonImage = Util.loadImage("WinScreen.png");
    Image menuHover = Util.loadImage("Hover.png"), winGemImg = Util.loadImage("OrangeWinGem.png");

    // Button images (displays when mouse is hovered over the button)
    Image continueImg = Util.loadImage("Buttons/ContinueButton.png"), retryImg = Util.loadImage("Buttons/RetryButton.png");
    Image menuButtonImg = Util.loadImage("Buttons/MenuButton.png"), endImg = Util.loadImage("Buttons/EndButton.png"); 
    Image resumeImg = Util.loadImage("Buttons/ResumeButton.png"), pauseButtonImage = Util.loadImage("PauseButton.png"); 

    // Audio
    Sound clickSoundEffect = new Sound("SOUNDS/ClickSound.wav"), startSoundEffect = new Sound("SOUNDS/StartLevel.wav"); 
    Sound dieMusic = new Sound("SOUNDS/DieMusic.wav"), backgroundMusic = new Sound("SOUNDS/BackgroundMusic.wav"), musicEnd = new Sound("SOUNDS/FinishLevel.wav");    
    
    Color semitransparentBlack = new Color(0, 0, 0, 140);
    Color gold = new Color(255, 201, 12);
    Color darkGold = new Color(156, 123, 9);
    Color evenDarkerGold = new Color(77, 65, 6);

    Font smallerFont = Util.loadFont("trajanpro-bold.ttf", 40);
    Font largerFont = Util.loadFont("trajanpro-bold.ttf", 70);

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));	
        
        keys = new boolean[1000];

        timer = new Timer(20, this);
        timer.start();

        setFocusable(true);
        requestFocus();
        addKeyListener(this);
        addMouseListener(this);
    }

    // moves all components in the game, checks collisions
    public void move(){
        boy.move(keys, walls, trapdoors, mask);
        girl.move(keys, walls, trapdoors, mask);
       
        for (Wall wall : walls) {
            wall.move();
        }

        for (Trapdoor trapdoor : trapdoors) {
            trapdoor.move();
        }

        for (Blob blob : blobs) {
            blob.move();
        }

        collectGem();
        checkTrapdoor();
        checkButton();
        checkReachEnd();
        checkOwnPuddle();
        checkBlobs(girl);
        checkBlobs(boy);
        checkWrongPuddle(girl);
        checkWrongPuddle(boy);

        gameTimer.update();
    }

    // creating a new level, loads information from text file
    /* File format
    * level background image
    * level mask
    * boy x pos
    * boy y pos
    * girl x pos
    * girl y pos
    * door x pos 
    * door y pos
    * # of fireboy gems 
    * 	gem x positon  
    * 	gem y positon
    * # of watergirl gems 
    * 	gem x positon  
    * 	gem y positon
    * # of fire puddles
    * 	puddle x position  
    * 	puddle y position
    *	puddle type (0 = short, 1 = medium, 2 = long)
    * # of water puddles
    * 	puddle x position  
    * 	puddle y position
    *	puddle type
    * # of walls/buttons
    * 	wall starting x pos  
    * 	wall ending y pos
    * 	wall starting x pos  
    * 	wall ending y pos
    * 	button x pos  
    * 	button y pos
    *	wall type (0 = horizontal, 1 = vertical)
    * # of trapdoors
    * 	trapdoor x pos  
    * 	trapdoor y pos
    */
    public void loadLevel(int lvl) { 
        try{
    		Scanner inFile = new Scanner(new File("LEVEL INFO/Level" + lvl + ".txt")); 

            // loading images
            levelBackground = Util.loadImage(inFile.nextLine());
            mask = Util.loadBufferedImg(inFile.nextLine());

            // making the components
            boy = new Player(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Player.BOY);
            girl = new Player(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Player.GIRL);
            door = new Door(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()));
            gems = makeGems(inFile);
            puddles = makePuddles(inFile, lvl);
            walls = makeWalls(inFile, lvl);
            buttons = makeButtons(inFile);
            trapdoors = makeTrapdoors(inFile);
            blobs = makeBlobs(inFile);  
    	}

    	catch (IOException ex){
    		System.out.println(ex);
    	}	
        
        gameTimer = new LevelTimer();
        gemCount = 0;

        backgroundMusic.loop();
    }

    // creating all the gems (storing in ArrayList)
    public ArrayList<Gem> makeGems(Scanner inFile) {
        ArrayList<Gem> g = new ArrayList<Gem>(); 

        int n = Util.toInt(inFile.nextLine()); // # of fireboy gems
        for (int i=0; i<n; i++) {
            g.add(new Gem(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Gem.BOY));
        }   

        n = Util.toInt(inFile.nextLine()); // watergirl gems
        for (int i=0; i<n; i++) {
            g.add(new Gem(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Gem.GIRL));
        }
        
        return g; 
    }

    // creating each puddle (storing in ArrayList)
    public ArrayList<Puddle> makePuddles(Scanner inFile, int lvl) {
        ArrayList<Puddle> p = new ArrayList<Puddle>();

        int n = Util.toInt(inFile.nextLine()); // # of fire puddles
        for (int i=0; i<n; i++) { // fire puddles
            p.add(new Puddle(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Puddle.FIRE, Util.toInt(inFile.nextLine())));
        }
        
        n = Util.toInt(inFile.nextLine());
        for (int i=0; i<n; i++) { // water puddles
            p.add(new Puddle(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Puddle.WATER, Util.toInt(inFile.nextLine())));
        }

        return p;
    }

    // creating all the buttons (storing in ArrayList)
    public ArrayList<Button> makeButtons(Scanner inFile) {
        ArrayList<Button> b = new ArrayList<Button>(); 

        int n = Util.toInt(inFile.nextLine()); // # of buttons
        for (int i=0; i<n; i++) {                                                                                 // set color to same as corresponding wall
            Button button = new Button(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), walls.get(i), walls.get(i).getColorForButton());
            b.add(button); 
        }

        return b;
    }

    // creating all the walls (storing in ArrayList)
    public ArrayList<Wall> makeWalls(Scanner inFile, int lvl) {
        ArrayList<Wall> w = new ArrayList<Wall>(); 

        int n = Util.toInt(inFile.nextLine()); // # of walls
        for (int i=0; i<n; i++) { 
            int width, height;
            
            // getting wall's dimensions based on orientation
            if (Util.toInt(inFile.nextLine()) == HORIZONTAL) { 
                width = 80;
                height = 20;
            }
            else {
                height = 80;
                width = 20;
            }
        
            w.add(new Wall(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), width, height, i));
        }

        return w;
    }

    // creating all the trapdoors (storing in ArrayList)
    public ArrayList<Trapdoor> makeTrapdoors(Scanner inFile) {
        ArrayList<Trapdoor> t = new ArrayList<Trapdoor>();

        int n = Util.toInt(inFile.nextLine()); // # of trapdoors
        for (int i=0; i<n; i++) {
            t.add(new Trapdoor(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine())));
        }

        return t;
    }

    // creating all the enemies (storing in ArrayList) 
    public ArrayList<Blob> makeBlobs(Scanner inFile) {
        ArrayList<Blob> b = new ArrayList<Blob>();

        int n = Util.toInt(inFile.nextLine()); // # of blobs
        for (int i=0; i<n; i++) {
            b.add(new Blob(Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine()), Util.toInt(inFile.nextLine())));
        }

        return b;
    }

    // removes gem if player collects their own gem, increases total count
    public void collectGem() {
        for (int i=gems.size()-1; i>=0; i--) { 
            Gem gem = gems.get(i); 

            if (girl.getRect().intersects(gem.getRect()) && gem.getType() == Gem.GIRL) { // if watergirl collected blue gem
                gem.collect(); // plays sound effect
                gems.remove(gem);
                gemCount++;
            }

            if (boy.getRect().intersects(gem.getRect()) && gem.getType() == Gem.BOY) { // if fireboy collected red gem
                gem.collect(); // plays sound effect
                gems.remove(gem);
                gemCount++; 
            }
        }
    }

    // player dies if player touches other player's puddle
    public void checkWrongPuddle(Player player) {
        for (Puddle puddle : puddles) {
            if (player.getRect().intersects(puddle.getRect()) && player.getType() != puddle.getType()) {
                player.die();
            }
        }
    }

    // checks if either player is walking in own puddle (for playing puddle sound)
    public void checkOwnPuddle() {
        for (Puddle puddle : puddles) {
                // player is in puddle and player is walking (not just standing)
            if ((boy.getRect().intersects(puddle.getRect()) && boy.isWalking()) || (girl.getRect().intersects(puddle.getRect()) && girl.isWalking())) {
                puddle.slosh();
            }
            else {
                puddle.unslosh(); 
            }
        }
    }

    // checks if player pushed a button
    public void checkButton() {
        for (Button button : buttons) {
            if (boy.getRect().intersects(button.getRect()) || girl.getRect().intersects(button.getRect())) {
                button.push();
            }
            else {
                button.unpush();
            }
        }
    }

    // checks if player touches or gets near blob
    public void checkBlobs(Player player) {
        for (Blob blob : blobs) {
            if (blob.getRect().intersects(player.getRect())) { // touching enemy
                blob.explode(); 
                player.exploded(); // stops player movement and animation
            }
    
            else if (blob.getRangeRect().intersects(player.getRect())) { // in blob's range
                blob.chasePlayer(player.getMiddleX()); 
            }
        }
    }

    // activates trapdoor's opening if player stands on trapdoor
    public void checkTrapdoor() {
        for (Trapdoor trapdoor : trapdoors) {
            if (trapdoor.getPolygon().contains(boy.getMiddleX(), boy.getBottomY()) || trapdoor.getPolygon().contains(girl.getMiddleX(), girl.getBottomY())) {
                trapdoor.activate();
            }
        }
    }

    // opens door and ends level if both players made it to the door
    public void checkReachEnd() {
        if (boy.getRect().intersects(door.getRect()) && girl.getRect().intersects(door.getRect())) {
            door.openDoor();
        }
    }

    // if mouse is hovering over a rectangle (code from Mr. Mckenzie)
    public boolean hover(Rectangle button) {
        Point mouse = MouseInfo.getPointerInfo().getLocation();
		Point offset = getLocationOnScreen();

        // get location of mouse in the window
		int mx = mouse.x-offset.x;
		int my = mouse.y-offset.y;

        // if mouse is on button, return true
        if (button.contains(mx, my)) {
            return true;
        }
        return false;
    } 
    
    @Override
    public void actionPerformed(ActionEvent e){
        if (screen == "game") {
            move();

            // game over if player touches wrong puddle
            if (boy.checkDead() || girl.checkDead()) { 
                backgroundMusic.stop();
                dieMusic.play();
                screen = "game over";
                firstFrame = true;
            }
            
            // if game is completed
            if (door.checkLevelComplete()) {
                backgroundMusic.stop();
                musicEnd.play(); // ending music
                levelStatus[level-1] = 1; // 1/3 star for completing

                // 1/3 star for collecting all gems
                if (gemCount == numberOfGems[level-1]) {
                    levelStatus[level-1]++;
                }

                // 1/3 star for completing in time
                if (gameTimer.getTime() <= levelTimes[level-1]) {
                    levelStatus[level-1]++;
                }

                // open another level 
                openLevels = openLevels < 4 ? openLevels + 1 : 4;
                screen = "win";
                firstFrame = true;
            }

            // game over if a blob killed a player
            for (Blob blob : blobs) {
                if (blob.killedPlayer()) {
                    backgroundMusic.stop();
                    screen = "game over";
                    firstFrame = true;
                }
            }
        }

        repaint();
    }
    
    @Override
    public void	keyPressed(KeyEvent e){
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void	keyReleased(KeyEvent e){
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void	keyTyped(KeyEvent e){}

    @Override
    public void	mouseClicked(MouseEvent e){ // clicking buttons to change screens
        int x = e.getX(), y = e.getY(); // mouse location

        if (screen == "intro") {
            if (playButton.contains(x, y)) {
                clickSoundEffect.play();
                screen = "menu";
            }
        }

        if (screen == "menu") {
            // checking each level's button on the map and if level is unlocked
            for (int i=0; i<4; i++) {
                if (levelButtons[i].contains(x, y) && i<openLevels) { 
                    startSoundEffect.play();
                    level = i+1;
                    loadLevel(level);
                    screen = "game";
                }
            }

            if (backButton.contains(x, y)) {
                clickSoundEffect.play();
                screen = "intro";
            }
        }

        if (screen == "game over") {
            if (GORetryButton.contains(x, y)) {
                clickSoundEffect.play();
                loadLevel(level);
                screen = "game";
            }

            if (menuButton.contains(x, y)) {
                clickSoundEffect.play();
                screen = "menu";
            }
        }

        if (screen == "paused") {
            if (PRetryButton.contains(x, y)) {
                clickSoundEffect.play();
                loadLevel(level);
                screen = "game";
            }

            if (endButton.contains(x, y)) {
                clickSoundEffect.play();
                screen = "menu";
            }

            if (resumeButton.contains(x, y)) {
                clickSoundEffect.play();
                backgroundMusic.play();
                gameTimer.unpause(); 
                screen = "game";
            }
        }

        if (screen == "win") {
            if (continueButton.contains(x, y)) {
                clickSoundEffect.play();
                screen = "menu";
            }
        }

        if (screen == "game") {
            if (pauseButton.contains(x, y)) {
                backgroundMusic.stop();
                clickSoundEffect.play();
                gameTimer.pause();
                screen = "paused";
                firstFrame = true;
            }
        }
    }

    @Override
    public void	mouseEntered(MouseEvent e){}

    @Override
    public void	mouseExited(MouseEvent e){}
    
    @Override
    public void	mousePressed(MouseEvent e){}
    
    @Override
    public void	mouseReleased(MouseEvent e){}

    @Override
    public void paint(Graphics g){ // displaying different screens
        if (screen == "intro") {
            g.drawImage(homeScreen, 0, 0, null);

            g.setFont(largerFont);
            Util.drawString(g, "PLAY", 375, 430, darkGold, gold);

            if (hover(playButton)) {
                Util.drawString(g, "PLAY", 375, 430, evenDarkerGold, darkGold);
            }
        }

        if (screen == "menu") {
            g.drawImage(menuBackground, 0, 0, null);
            g.drawImage(menuImgs[openLevels-1], 0, 0, null); // game map image

            for (int i=0; i<4; i++) {
                if (hover(levelButtons[i]) && i<openLevels) { // hovering on unlocked levels
                    g.drawImage(menuHover, levelButtons[i].x, levelButtons[i].y, null);
                }

                g.drawImage(menuGemImages[levelStatus[i]], menuGemPos[i][0], menuGemPos[i][1], null); // gem on map for each level
            }

            g.setFont(smallerFont);
            Util.drawString(g, "< BACK", 70, 610, darkGold, gold);

            if (hover(backButton)) {
                g.setColor(darkGold);
                Util.drawString(g, "< BACK", 70, 610, evenDarkerGold, darkGold);
            }
        }

        if (screen == "win") {
            if (firstFrame) { // fill screen with semi-transparent black color once (make shadow)
                g.setColor(semitransparentBlack);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                firstFrame = false;
            }

            g.drawImage(wonImage, 115, 125, null);

            if (hover(continueButton)) {
                g.drawImage(continueImg, continueButton.x, continueButton.y, null);
            }

            for (int i=0; i<levelStatus[level-1]; i++) { // earned gems (out of 3)
                g.drawImage(winGemImg, 321+i*122, 271, null);
            }
        }

        if (screen == "game over") {
            if (firstFrame) { // fill screen with semi-transparent black color once (make shadow)
                g.setColor(semitransparentBlack);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                firstFrame = false;
            }

            g.drawImage(gameOverImage, 115, 125, null);

            if (hover(GORetryButton)) {
                g.drawImage(retryImg, GORetryButton.x, GORetryButton.y, null);
            }

            if (hover(menuButton)) {
                g.drawImage(menuButtonImg, menuButton.x, menuButton.y, null);
            }
        }

        if (screen == "paused") {
            if (firstFrame) { // fill screen with semi-transparent black color once (make shadow)
                g.setColor(semitransparentBlack);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                firstFrame = false;
            }

            g.drawImage(pausedImg, 115, 125, null);

            if (hover(endButton)) {
                g.drawImage(endImg, endButton.x, endButton.y, null);
            }

            if (hover(PRetryButton)) {
                g.drawImage(retryImg, PRetryButton.x, PRetryButton.y, null);
            }

            if (hover(resumeButton)) {
                g.drawImage(resumeImg, resumeButton.x, resumeButton.y, null);
            }

        }

        // drawing all components of game
        if (screen == "game") {
            g.drawImage(brickBackground, 0, 0, null);

            door.draw(g);
            
            for (Button button : buttons) {
                button.draw(g); 
            }

            for (Wall wall : walls) {
                wall.draw(g);
            }

            for (Gem gem : gems) {
                gem.draw(g);
            }

            for (Trapdoor trapdoor : trapdoors) {
                trapdoor.draw(g);
            }

            g.drawImage(levelBackground, 0, 0, null);
            
            boy.draw(g);
            girl.draw(g);

            for (Puddle puddle : puddles) {
                puddle.draw(g);
            }

            for (Blob blob : blobs) {
                blob.draw(g);
            }
            
            g.setFont(smallerFont);
            g.setColor(gold);
            gameTimer.draw(g);

            g.drawImage(pauseButtonImage, 895, 15, null);
        }
    }
}