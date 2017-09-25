package rbadia.voidspace.main;

/**
 * Implements the main game loop, i.e. what actions should be taken on each frame update.
 */
public class GameLoop implements Runnable{
	private GameState gameScreen;
	private GameLogic gameLogic;
	private InputHandler inputHandler;

	/**
	 * Creates a new game loop.
	 * @param gameScreen the game screen
	 * @param gameLogic the game logic handler
	 * @param inputHandler the user input handler
	 */
	public GameLoop(GameState gameScreen){
		this.gameScreen = gameScreen;
		this.gameLogic = gameScreen.getGameLogic();
		this.inputHandler = gameScreen.getInputHandler();
	}

	/**
	 * Implements the run interface method. Should be called by the running thread.
	 */
	public void run() {
		while(true){
			// main game loop
			try{
				// sleep/wait for 1/60th of a second,
				// for a resulting refresh rate of 60 frames per second (fps) 
				Thread.sleep(1000/60);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			// check game or level ending conditions
			gameLogic.checkConditions();
			
			gameLogic.handleInput(inputHandler, gameScreen);
			
			// update the game graphics
			gameScreen.updateScreen();
			
			// handle input
			gameLogic.handleInput(inputHandler, gameScreen);
			
			// repaint the graphics unto screen
			gameScreen.repaint();
		}
	}

}