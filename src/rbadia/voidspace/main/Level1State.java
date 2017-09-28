package rbadia.voidspace.main;
import java.awt.Color;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

import rbadia.voidspace.graphics.GraphicsManager;
import rbadia.voidspace.model.Asteroid;
import rbadia.voidspace.model.BigAsteroid;
import rbadia.voidspace.model.BigBullet;
import rbadia.voidspace.model.Boss;
import rbadia.voidspace.model.Bullet;
import rbadia.voidspace.model.BulletBoss;
import rbadia.voidspace.model.BulletBoss2;
import rbadia.voidspace.model.Floor;
import rbadia.voidspace.model.MegaMan;
import rbadia.voidspace.model.Platform;
import rbadia.voidspace.sounds.SoundManager;

/**
 * Main game screen. Handles all game graphics updates and some of the game logic.
 */
public class Level1State extends LevelState {

	private static final long serialVersionUID = 1L;

	protected GraphicsManager graphicsMan;

	protected BufferedImage backBuffer;
	//protected Graphics2D g2d;

	protected MegaMan megaMan;
	protected Boss boss;
	protected Boss boss2;
	protected Asteroid asteroid;
	protected Asteroid asteroid2;
	protected BigAsteroid bigAsteroid;
	protected List<Bullet> bullets;
	protected List<BulletBoss> bulletsBoss;
	protected List<BulletBoss2> bulletsBoss2;
	protected List<BigBullet> bigBullets;
	protected Floor[] floor;	// END Moved from GameLogic class
	protected int numPlatforms=8;
	protected Platform[] platforms;

	protected int damage=0;
	protected static final int NEW_SHIP_DELAY = 500;
	protected static final int NEW_ASTEROID_DELAY = 500;

	protected long lastAsteroidTime;
	protected long lastShipTime;

	protected Rectangle asteroidExplosion;

	protected Random rand;

	protected Font originalFont;
	protected Font bigFont;
	protected Font biggestFont;

	protected int boom=0;
	protected int levelAsteroidsDestroyed = 0;

	// Constructors
	public Level1State(int level, MainFrame frame, GameStatus status, LevelLogic gameLogic, InputHandler inputHandler) {
		super();
		this.setLevel(level);
		this.setMainFrame(frame);
		this.setGameStatus(status);
		this.setGameLogic(gameLogic);
		this.setInputHandler(inputHandler);
		
		this.setSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(500, 400));
		this.setBackground(Color.BLACK);

		// initialize random number generator
		rand = new Random();

		graphicsMan = new GraphicsManager();

		// init back buffer image
		backBuffer = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
		this.setGraphics2D(backBuffer.createGraphics());

		//this.setGameStatus(new GameStatus());
		this.setSoundManager(new SoundManager());

		// init some variables
		bullets = new ArrayList<Bullet>();
		bulletsBoss = new ArrayList<BulletBoss>();
		bulletsBoss2 = new ArrayList<BulletBoss2>();
		bigBullets = new ArrayList<BigBullet>();
	}

	// Getters
	public int getBoom()							{ return boom; 			}
	public MegaMan getMegaMan() 					{ return megaMan; 		}
	public Floor[] getFloor()					{ return floor; 			}
	public int getNumPlatforms()					{ return numPlatforms; 	}
	public Platform[] getPlatforms()				{ return platforms; 		}
	public Boss getBoss() 						{ return boss; 			}
	public Boss getBoss2() 						{ return boss2; 			}
	public Asteroid getAsteroid() 				{ return asteroid; 		}
	public Asteroid getAsteroid2() 				{ return asteroid2; 		}
	public BigAsteroid getBigAsteroid() 			{ return bigAsteroid; 	}
	public List<Bullet> getBullets() 			{ return bullets; 		}
	public List<BulletBoss> getBulletBoss() 		{ return bulletsBoss;	}
	public List<BulletBoss2> getBulletBoss2()	{ return bulletsBoss2; 	}
	public List<BigBullet> getBigBullets()		{ return bigBullets;   	}
	
	// Level state methods
	// The method associated with the current state will be called 
	// repeatedly during each LevelLoop iteration until the next a state 
	// transition occurs
	
	public void doStart() {	
		
		setStartState(START_STATE);
		setCurrentState(getStartState());
		// init game variables
		bullets = new ArrayList<Bullet>();
		bulletsBoss = new ArrayList<BulletBoss>();
		bulletsBoss2 = new ArrayList<BulletBoss2>();
		bigBullets = new ArrayList<BigBullet>();
		//numPlatforms = new Platform[5];

		GameStatus status = this.getGameStatus();

		//status.setGameStarting(true);
		status.setShipsLeft(3);
		//status.setLevel(1);
		status.setGameOver(false);
		status.setAsteroidsDestroyed(0);
		status.setNewAsteroid(false);
		status.setNewAsteroid2(false);
		status.setNewBigAsteroid(false);
		//status.setNewFloor(false);

		// init the ship and the asteroid
		newMegaMan(this);
		newFloor(this, 9);
		newPlatforms(getNumPlatforms());
		newBoss(this);
		newBoss2(this);
		newAsteroid(this);
		newAsteroid2(this);
		newBigAsteroid(this);

		lastAsteroidTime = -NEW_ASTEROID_DELAY;
		//lastBigAsteroidTime = -NEW_BIG_ASTEROID_DELAY;
		lastShipTime = -NEW_SHIP_DELAY;

		bigFont = originalFont;
		biggestFont = null;

		// Display initial values for scores
		getMainFrame().getDestroyedValueLabel().setForeground(Color.BLACK);
		getMainFrame().getDestroyedValueLabel().setText(Integer.toString(status.getShipsLeft()));
		getMainFrame().getDestroyedValueLabel().setText(Long.toString(status.getAsteroidsDestroyed()));
		getMainFrame().getDestroyedValueLabel().setText(Long.toString(status.getLevel()));
		
	}
	
	public void doInitialScreen() {
		setCurrentState(INITIAL_SCREEN);
		//updateScreen();
		// erase screen
		Graphics2D g2d = getGraphics2D();
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, getSize().width, getSize().height);
		getGameLogic().drawInitialMessage();
	};
	
	public void doGettingReady() {
		setCurrentState(GETTING_READY);
		getGameLogic().drawGetReady();
		repaint();
		LevelLogic.delay(2000);
		//Changes music from "menu music" to "ingame music"
		MegaManMain.audioClip.close();
		MegaManMain.audioFile = new File("audio/mainGame.wav");
		try {
			MegaManMain.audioStream = AudioSystem.getAudioInputStream(MegaManMain.audioFile);
			MegaManMain.audioClip.open(MegaManMain.audioStream);
			MegaManMain.audioClip.start();
			MegaManMain.audioClip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
	};
	
	public void doPlaying() {
		setCurrentState(PLAYING);
		updateScreen();
	};
	
	public void doNewMegaman() {
		setCurrentState(NEW_MEGAMAN);
	};
	
	public void doLevelWon(){
		setCurrentState(LEVEL_WON);
	}

	public void doGameOverScreen(){
		getGameLogic().drawGameOver();
		getMainFrame().getDestroyedValueLabel().setForeground(new Color(128, 0, 0));
		if (getCurrentState() == GAME_OVER_SCREEN) return;
		setCurrentState(GAME_OVER_SCREEN);	
		Timer timer = new Timer(1500, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setCurrentState(GAME_OVER);
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	public void doGameOver(){
		this.getGameStatus().setGameOver(true);
	}

	/**
	 * Update the game screen.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// draw current backbuffer to the actual game screen
		g.drawImage(backBuffer, 0, 0, this);
	}

	/**
	 * Update the game screen's backbuffer image.
	 */
	@Override
	public void updateScreen(){
		MegaMan megaMan = this.getMegaMan();
		//Floor[] floor = this.getFloor();
		Platform[] numPlatforms = this.getPlatforms();
		List<Bullet> bullets = this.getBullets();
		Asteroid asteroid = this.getAsteroid();
		List<BigBullet> bigBullets = this.getBigBullets();
		Graphics2D g2d = getGraphics2D();
		//		Asteroid asteroid2 = gameLogic.getAsteroid2();
		//		BigAsteroid bigAsteroid = gameLogic.getBigAsteroid();
		//		List<BulletBoss> bulletsBoss = gameLogic.getBulletBoss();
		//		List<BulletBoss2> bulletsBoss2 = gameLogic.getBulletBoss2();		
		//		Boss boss = gameLogic.getBoss();
		//		Boss boss2 = gameLogic.getBoss2();

		GameStatus status = this.getGameStatus();
		// set orignal font - for later use
		if(this.originalFont == null){
			this.originalFont = g2d.getFont();
			this.bigFont = originalFont;
		}

		// erase screen
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, getSize().width, getSize().height);

		// draw 50 random stars
		drawStars(50);


		//draw Floor
		for(int i=0; i<9; i++){
			graphicsMan.drawFloor(floor[i], g2d, this, i);	
		}


		//		if(level==1){
		//draw Platform LV. 1
		for(int i=0; i<getNumPlatforms(); i++){
			graphicsMan.drawPlatform(numPlatforms[i], g2d, this, i);
			//			}
		}
		//		//draw Platform LV. 2
		//		else if(level==2){
		//			for(int i=0; i<8; i++){
		//			
		//				graphicsMan.drawPlatform2(numPlatforms[i], g2d, this, i);
		//			}	
		//		}

		//draw MegaMan
		if(!status.isNewMegaMan()){
			if((Gravity() == true) || ((Gravity() == true) && (Fire() == true || Fire2() == true))){
				graphicsMan.drawMegaFallR(megaMan, g2d, this);
			}
		}

		if((Fire() == true || Fire2()== true) && (Gravity()==false)){
			graphicsMan.drawMegaFireR(megaMan, g2d, this);
		}

		if((Gravity()==false) && (Fire()==false) && (Fire2()==false)){
			graphicsMan.drawMegaMan(megaMan, g2d, this);
		}

		// draw first asteroid
		if(!status.isNewAsteroid() && boom <= 2){
			// draw the asteroid until it reaches the bottom of the screen

			//LEVEL 1
			if((asteroid.getX() + asteroid.getAsteroidWidth() >  0) && (boom <= 5 || boom == 15)){
				asteroid.translate(-asteroid.getSpeed(), 0);
				graphicsMan.drawAsteroid(asteroid, g2d, this);	
			}
			else if (boom <= 5){
				asteroid.setLocation(this.getWidth() - asteroid.getAsteroidWidth(),
						rand.nextInt(this.getHeight() - asteroid.getAsteroidHeight() - 32));
			}	
		}

		else if(!status.isNewAsteroid() && boom > 2){
			// draw the asteroid until it reaches the bottom of the screen
			//LEVEL 2
			if((asteroid.getX() + asteroid.getAsteroidWidth() >  0)){
				asteroid.translate(-asteroid.getSpeed(), asteroid.getSpeed()/2);
				graphicsMan.drawAsteroid(asteroid, g2d, this);	
			}
			else if (boom <= 5){
				asteroid.setLocation(this.getWidth() - asteroid.getAsteroidWidth(),
						rand.nextInt(this.getHeight() - asteroid.getAsteroidHeight() - 32));
			}	
		}

		else{
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new asteroid
				lastAsteroidTime = currentTime;
				status.setNewAsteroid(false);
				asteroid.setLocation(this.getWidth() - asteroid.getAsteroidWidth(),
						rand.nextInt(this.getHeight() - asteroid.getAsteroidHeight() - 32));
			}

			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}

		// draw bullets   
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			graphicsMan.drawBullet(bullet, g2d, this);

			boolean remove =   this.moveBullet(bullet);
			if(remove){
				bullets.remove(i);
				i--;
			}
		}

		// draw big bullets
		for(int i=0; i<bigBullets.size(); i++){
			BigBullet bigBullet = bigBullets.get(i);
			graphicsMan.drawBigBullet(bigBullet, g2d, this);

			boolean remove = this.moveBigBullet(bigBullet);
			if(remove){
				bigBullets.remove(i);
				i--;
			}
		}

		// check bullet-asteroid collisions
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(asteroid.intersects(bullet)){
				// increase asteroids destroyed count
				status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 100);

				removeAsteroid(asteroid);

				levelAsteroidsDestroyed++;

				if(boom != 5 && boom != 15){
					boom=boom + 1;
				}
				damage=0;
				// remove bullet
				bullets.remove(i);
				break;
			}
		}

		// check big bullet-asteroid collisions
		for(int i=0; i<bigBullets.size(); i++){
			BigBullet bigBullet = bigBullets.get(i);
			if(asteroid.intersects(bigBullet)){
				// increase asteroids destroyed count
				status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 100);

				removeAsteroid(asteroid);



				if(boom != 5 && boom != 15){
					boom=boom + 1;
				}
				damage=0;
			}
		}

		//MM-Asteroid collision
		if(asteroid.intersects(megaMan)){
			status.setShipsLeft(status.getShipsLeft() - 1);
			removeAsteroid(asteroid);
		}

		//Asteroid-Floor collision
		for(int i=0; i<9; i++){
			if(asteroid.intersects(floor[i])){
				removeAsteroid(asteroid);

			}
		}
		//

//		if(boom == 2)
//			restructure();

//		status.getAsteroidsDestroyed();
//		status.getShipsLeft();
//		status.getLevel();

		// update asteroids destroyed label  
		getMainFrame().getDestroyedValueLabel().setText(Long.toString(status.getAsteroidsDestroyed()));

		// update ships left label
		getMainFrame().getDestroyedValueLabel().setText(Integer.toString(status.getShipsLeft()));

		//update level label

		getMainFrame().getDestroyedValueLabel().setText(Long.toString(status.getLevel()));
	}



	/**
	 * Draws the specified number of stars randomly on the game screen.
	 * @param numberOfStars the number of stars to draw
	 */
	protected void drawStars(int numberOfStars) {
		Graphics2D g2d = getGraphics2D();
		g2d.setColor(Color.WHITE);
		for(int i=0; i<numberOfStars; i++){
			int x = (int)(Math.random() * this.getWidth());
			int y = (int)(Math.random() * this.getHeight());
			g2d.drawLine(x, y, x, y);
		}
	}




	public boolean isLevelWon() {
		return boom >= 3; // TODO change to use asteroids destroyed in this level
	}

	protected boolean Gravity(){
		MegaMan megaMan = this.getMegaMan();
		Floor[] floor = this.getFloor();

		for(int i=0; i<9; i++){
			if((megaMan.getY() + megaMan.getMegaManHeight() -17 < this.getHeight() - floor[i].getFloorHeight()/2) 
					&& Fall() == true){

				megaMan.translate(0 , 2);
				return true;

			}
		}
		return false;
	}

	//Bullet fire pose
	protected boolean Fire(){
		MegaMan megaMan = this.getMegaMan();
		List<Bullet> bullets = this.getBullets();
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if((bullet.getX() > megaMan.getX() + megaMan.getMegaManWidth()) && 
					(bullet.getX() <= megaMan.getX() + megaMan.getMegaManWidth() + 60)){
				return true;
			}
		}
		return false;
	}

	//BigBullet fire pose
	protected boolean Fire2(){
		MegaMan megaMan = this.getMegaMan();
		List<BigBullet> bigBullets = this.getBigBullets();
		for(int i=0; i<bigBullets.size(); i++){
			BigBullet bigBullet = bigBullets.get(i);
			if((bigBullet.getX() > megaMan.getX() + megaMan.getMegaManWidth()) && 
					(bigBullet.getX() <= megaMan.getX() + megaMan.getMegaManWidth() + 60)){
				return true;
			}
		}
		return false;
	}

	//Platform Gravity
	public boolean Fall(){
		MegaMan megaMan = this.getMegaMan(); 
		Platform[] platforms = this.getPlatforms();
		for(int i=0; i<getNumPlatforms(); i++){
			if((((platforms[i].getX() < megaMan.getX()) && (megaMan.getX()< platforms[i].getX() + platforms[i].getPlatformWidth()))
					|| ((platforms[i].getX() < megaMan.getX() + megaMan.getMegaManWidth()) 
							&& (megaMan.getX() + megaMan.getMegaManWidth()< platforms[i].getX() + platforms[i].getPlatformWidth())))
					&& megaMan.getY() + megaMan.getMegaManHeight() == platforms[i].getY()
					){
				return false;
			}
		}
		return true;
	}

	public void removeAsteroid(Asteroid asteroid){
		// "remove" asteroid
		asteroidExplosion = new Rectangle(
				asteroid.x,
				asteroid.y,
				asteroid.width,
				asteroid.height);
		asteroid.setLocation(-asteroid.width, -asteroid.height);
		this.getGameStatus().setNewAsteroid(true);
		lastAsteroidTime = System.currentTimeMillis();

		// play asteroid explosion sound
		this.getSoundManager().playAsteroidExplosionSound();
	}

	// BEGIN Moved from GameLogic
	/**
	 * Fire a bullet from ship.
	 */
	public void fireBullet(){
		Bullet bullet = new Bullet(megaMan);
		bullets.add(bullet);
		this.getSoundManager().playBulletSound();
	}

	/**
	 * Fire the "Power Shot" bullet
	 */
	public void fireBigBullet(){
		BigBullet bigBullet = new BigBullet(megaMan);
		bigBullets.add(bigBullet);
		this.getSoundManager().playBulletSound();
	}

	/**
	 * Move a bullet once fired from the ship.
	 * @param bullet the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBullet(Bullet bullet){
		if(bullet.getY() - bullet.getSpeed() >= 0){
			bullet.translate(bullet.getSpeed(), 0);
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * Move a bullet once fired from the boss.
	 * @param bulletBoss the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBulletBoss(BulletBoss bulletBoss){
		if(bulletBoss.getY() - bulletBoss.getSpeed() >= 0){
			bulletBoss.translate(0, bulletBoss.getSpeed());
			return false;
		}
		else{
			return true;
		}
	}

	/** Move a bullet once fired from the second boss.
	 * @param bulletBoss2 the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBulletBoss2(BulletBoss2 bulletBoss2){
		if(bulletBoss2.getY() - bulletBoss2.getSpeed() >= 0){
			bulletBoss2.translate(0, bulletBoss2.getSpeed());
			return false;
		}
		else{
			return true;
		}
	}

	/** Move a "Power Shot" bullet once fired from the ship.
	 * @param bulletBoss2 the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBigBullet(BigBullet bigBullet){
		if(bigBullet.getY() - bigBullet.getBigSpeed() >= 0){
			bigBullet.translate(bigBullet.getBigSpeed(), 0);
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * Create a new ship (and replace current one).
	 */
	public MegaMan newMegaMan(Level1State screen){
		this.megaMan = new MegaMan(screen);
		return megaMan;
	}

	public Floor[] newFloor(Level1State screen, int n){
		floor = new Floor[n];
		for(int i=0; i<n; i++){
			this.floor[i] = new Floor(screen, i);
		}

		return floor;
	}

	public Platform[] newPlatforms(int n){
		platforms = new Platform[n];
		for(int i=0; i<n; i++){
			this.platforms[i] = new Platform(this, i);
		}
		return platforms;

	}


	/**
	 * Create the first boss.
	 */
	public Boss newBoss(Level1State screen){
		this.boss = new Boss(screen);
		return boss;
	}

	/**
	 * Create the second boss.
	 */
	public Boss newBoss2(Level1State screen){
		this.boss2 = new Boss(screen);
		return boss2;
	}

	/**
	 * Create a new asteroid.
	 */
	public Asteroid newAsteroid(Level1State screen){
		this.asteroid = new Asteroid(screen);
		return asteroid;
	}

	/**
	 * Create a second asteroid.
	 */
	public Asteroid newAsteroid2(Level1State screen){
		this.asteroid2 = new Asteroid(screen);
		return asteroid2;
	}

	/**
	 * Create a new big asteroid.
	 */
	public BigAsteroid newBigAsteroid(Level1State screen){
		this.bigAsteroid = new BigAsteroid(screen);
		return bigAsteroid;
	}



	/**
	 * Move the megaMan up
	 * @param megaMan the megaMan
	 */
	public void moveMegaManUp(){
		if(megaMan.getY() - megaMan.getSpeed() >= 0){
			megaMan.translate(0, -megaMan.getSpeed()*2);
		}
	}

	/**
	 * Move the megaMan down
	 * @param megaMan the megaMan
	 */
	public void moveMegaManDown(){
		for(int i=0; i<9; i++){
			if(megaMan.getY() + megaMan.getSpeed() + megaMan.height < getHeight() - floor[i].getFloorHeight()/2){
				megaMan.translate(0, 2);
			}
		}
	}

	/**
	 * Move the megaMan left
	 * @param megaMan the megaMan
	 */
	public void moveMegaManLeft(){
		if(megaMan.getX() - megaMan.getSpeed() >= 0){
			megaMan.translate(-megaMan.getSpeed(), 0);
		}
	}

	/**
	 * Move the megaMan right
	 * @param megaMan the megaMan
	 */
	public void moveMegaManRight(){
		if(megaMan.getX() + megaMan.getSpeed() + megaMan.width < getWidth()){
			megaMan.translate(megaMan.getSpeed(), 0);
		}
	}

	public void speedUpMegaMan() {
		megaMan.setSpeed(megaMan.getDefaultSpeed() * 2 +1);
	}

	public void slowDownMegaMan() {
		megaMan.setSpeed(megaMan.getDefaultSpeed());
	}
}
