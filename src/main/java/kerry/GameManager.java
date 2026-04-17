package kerry;
import java.util.List;

public class GameManager {
    private static final int DEFAULT_CODE_LENGTH = 4;
    private static final int DEFAULT_MAX_ATTEMPTS = 6;
    private static final int MIN_CUSTOM_CODE_LENGTH = 3;
    private static final int MAX_CUSTOM_CODE_LENGTH = 6;
    private static final int MIN_CUSTOM_ATTEMPTS = 1;
    private static final int MAX_CUSTOM_ATTEMPTS = 12;

    private final InputHandler inputHandler;
    private final SwiftBotController swiftBotController;
    private final MastermindLogic mastermindLogic;
    private final ScoreBoard scoreBoard;
    private final FileLogger fileLogger;

    public GameManager(InputHandler inputHandler,
                       SwiftBotController swiftBotController,
                       MastermindLogic mastermindLogic,
                       ScoreBoard scoreBoard,
                       FileLogger fileLogger) {
        this.inputHandler = inputHandler;
        this.swiftBotController = swiftBotController;
        this.mastermindLogic = mastermindLogic;
        this.scoreBoard = scoreBoard;
        this.fileLogger = fileLogger;
    }

    public void startGame() {
        boolean running = true;
        int roundNumber = 1;

        fileLogger.open();
        swiftBotController.showStartupStatus();

        while (running) {
            int mainMenuChoice = showMainMenuAndReadChoice();
            switch (mainMenuChoice) {
                case 1:
                    runRound(roundNumber);
                    roundNumber++;
                    running = "Y".equals(swiftBotController.waitForContinueOrQuitButton());
                    break;
                case 2:
                    showInstructions();
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    swiftBotController.showError("Unexpected main menu choice.");
            }
        }

        shutdown();
    }

    private int showMainMenuAndReadChoice() {
        Utils.printTitle("SwiftBot Mastermind");
        System.out.println(scoreBoard.toDisplayString());
        System.out.println("1. Start Game");
        System.out.println("2. Instructions");
        System.out.println("3. Quit");
        if (swiftBotController.isSimulationMode()) {
            System.out.println("Simulation mode is active for local testing.");
        }
        Utils.printDivider();
        System.out.print("Enter menu option: ");
        return inputHandler.readMenuChoice(1, 3);
    }

    private void showInstructions() {
        Utils.printTitle("Instructions");
        System.out.println("1. Choose Default mode with button A or Customised mode with button B.");
        System.out.println("2. In Default mode the code length is 4 and you have 6 attempts.");
        System.out.println("3. In Customised mode you enter the code length and maximum attempts in the CLI.");
        System.out.println("4. Scan one colour card at a time with the SwiftBot camera.");
        System.out.println("5. '+' means correct colour in the correct position.");
        System.out.println("6. '-' means correct colour in the wrong position.");
        System.out.println("7. All '+' symbols are shown before '-'.");
        System.out.println("8. Press Y after a round to play again or X to quit.");
        Utils.printDivider();
    }

    private void runRound(int roundNumber) {
        String modeButton = swiftBotController.waitForModeSelectionButton();
        boolean defaultMode = "A".equals(modeButton);
        String modeName = defaultMode ? "Default" : "Customised";

        int codeLength = defaultMode
                ? DEFAULT_CODE_LENGTH
                : inputHandler.readIntInRange("Enter code length (3 - 6): ", MIN_CUSTOM_CODE_LENGTH, MAX_CUSTOM_CODE_LENGTH);

        int maxAttempts = defaultMode
                ? DEFAULT_MAX_ATTEMPTS
                : inputHandler.readIntInRange("Enter max attempts (1 - 12): ", MIN_CUSTOM_ATTEMPTS, MAX_CUSTOM_ATTEMPTS);

        boolean allowRepeats = false;
        List<String> secretCode = mastermindLogic.generateSecretCode(codeLength, allowRepeats);

        fileLogger.logRoundStart(roundNumber, modeName, codeLength, maxAttempts);
        fileLogger.logSecretCode(roundNumber, secretCode);

        int attemptsUsed = 0;
        boolean playerWon = false;
        boolean playerQuitRound = false;

        while (attemptsUsed < maxAttempts && !playerWon && !playerQuitRound) {
            int remainingAttempts = maxAttempts - attemptsUsed;
            Utils.printTitle("Round " + roundNumber);
            System.out.println("Mode: " + modeName);
            System.out.println("Attempts Remaining: " + remainingAttempts);
            System.out.println(scoreBoard.toDisplayString());
            Utils.printDivider();

            try {
                List<String> guess = swiftBotController.scanGuess(codeLength, allowRepeats);
                attemptsUsed++;

                String feedback = mastermindLogic.evaluateGuess(secretCode, guess);
                playerWon = mastermindLogic.isWinningGuess(secretCode, guess);
                int guessesLeft = maxAttempts - attemptsUsed;

                Utils.printTitle("Feedback");
                System.out.println("Guess: " + Utils.formatColourList(guess));
                System.out.println("Result: " + (feedback.isEmpty() ? "No matches" : feedback));
                System.out.println("Attempts Remaining: " + guessesLeft);
                Utils.printDivider();

                fileLogger.logAttempt(roundNumber, attemptsUsed, guess, feedback, guessesLeft);
            } catch (SwiftBotController.RoundQuitException roundQuitException) {
                playerQuitRound = true;
                System.out.println("Round ended early by the player.");
            }
        }

        if (playerWon) {
            scoreBoard.recordPlayerWin();
            swiftBotController.celebrateWin();
            showWinScreen(secretCode);
        } else {
            scoreBoard.recordComputerWin();
            swiftBotController.indicateLoss();
            showLossScreen(secretCode, playerQuitRound);
        }

        fileLogger.logRoundResult(roundNumber, playerWon, secretCode, attemptsUsed, scoreBoard);
    }

    private void showWinScreen(List<String> secretCode) {
        Utils.printTitle("You Win!");
        System.out.println("Secret Code: " + Utils.formatColourList(secretCode));
        System.out.println(scoreBoard.toDisplayString());
        System.out.println("Y - Play Again");
        System.out.println("X - Quit");
        Utils.printDivider();
    }

    private void showLossScreen(List<String> secretCode, boolean playerQuitRound) {
        Utils.printTitle(playerQuitRound ? "Round Ended" : "Game Over");
        System.out.println(playerQuitRound ? "The round was quit before completion." : "Attempts used up.");
        System.out.println("Secret Code: " + Utils.formatColourList(secretCode));
        System.out.println(scoreBoard.toDisplayString());
        System.out.println("Y - Play Again");
        System.out.println("X - Quit");
        Utils.printDivider();
    }

    private void shutdown() {
        fileLogger.logSessionEnd(scoreBoard);
        fileLogger.close();
        swiftBotController.shutdownHardware();
        Utils.printTitle("Session Ended");
        System.out.println("Final Scoreboard: " + scoreBoard.toDisplayString());
        System.out.println("Log file saved to: " + fileLogger.getLogFilePath());
        Utils.printDivider();
        inputHandler.close();
    }
}
