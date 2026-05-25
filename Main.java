import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for Haunted Mansion Explorer
 * Initializes the game and starts the JavaFX application
 * The main method launches the JavaFX application, which then calls the start method to display the initial StartScreen
 * The StartScreen allows the player to start a new game, which creates a new GameWorld and transitions to the GameScreen where the main gameplay occurs
 * This class serves as the central hub for launching the game and can be expanded in the future to include additional setup or configuration if needed
 */
public class Main extends Application
{
    // The main method launches the JavaFX application, which then calls the start method to display the initial StartScreen.
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * The start method is called after the JavaFX application is launched.
     * It initializes the StartScreen, which is the first screen the player sees when they launch the game
     * 
     * @param primaryStage the primary stage for this application, onto which the StartScreen will be set
     */
    @Override
    public void start(Stage primaryStage)
    {
        StartScreen start = new StartScreen(primaryStage);
        start.show();
    }
}