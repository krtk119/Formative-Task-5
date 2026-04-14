public class Main {
    public static void main(String[] args) {
        InputHandler inputHandler = new InputHandler();
        SwiftBotController swiftBotController = new SwiftBotController();
        MastermindLogic mastermindLogic = new MastermindLogic();
        ScoreBoard scoreBoard = new ScoreBoard();
        FileLogger fileLogger = new FileLogger();

        GameManager gameManager = new GameManager(
                inputHandler,
                swiftBotController,
                mastermindLogic,
                scoreBoard,
                fileLogger
        );

        try {
            gameManager.startGame();
        } catch (Exception exception) {
            System.err.println("The program encountered an unexpected error: " + exception.getMessage());
            exception.printStackTrace();
            swiftBotController.shutdownHardware();
            fileLogger.close();
            inputHandler.close();
        }
    }
}
