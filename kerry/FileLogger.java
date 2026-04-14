import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileLogger {
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private BufferedWriter writer;
    private Path logFilePath;

    public void open() {
        if (writer != null) {
            return;
        }

        try {
            Path logDirectory = Paths.get(System.getProperty("user.dir"), "logs");
            Files.createDirectories(logDirectory);
            String filename = "mastermind_log_" + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".txt";
            logFilePath = logDirectory.resolve(filename);
            writer = Files.newBufferedWriter(
                    logFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE
            );
            writeLine("SwiftBot Mastermind Session Started: " + LocalDateTime.now());
            writeLine("");
            flush();
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to open log file.", ioException);
        }
    }

    public void logRoundStart(int roundNumber, String modeName, int codeLength, int maxAttempts) {
        writeLine("ROUND " + roundNumber + " START");
        writeLine("Mode: " + modeName);
        writeLine("Code Length: " + codeLength);
        writeLine("Maximum Attempts: " + maxAttempts);
        writeLine("");
        flush();
    }

    public void logSecretCode(int roundNumber, List<String> secretCode) {
        writeLine("Round " + roundNumber + " Secret Code: " + Utils.formatColourList(secretCode));
        flush();
    }

    public void logAttempt(int roundNumber, int attemptNumber, List<String> guess, String feedback, int guessesLeft) {
        writeLine("Round " + roundNumber + " Attempt " + attemptNumber + ":");
        writeLine("Guess: " + Utils.formatColourList(guess));
        writeLine("Feedback: " + (feedback.isEmpty() ? "No matches" : feedback));
        writeLine("Guesses Left: " + guessesLeft);
        writeLine("");
        flush();
    }

    public void logRoundResult(int roundNumber, boolean playerWon, List<String> secretCode, int attemptsUsed, ScoreBoard scoreBoard) {
        writeLine("Round " + roundNumber + " Result: " + (playerWon ? "PLAYER WIN" : "COMPUTER WIN"));
        writeLine("Round " + roundNumber + " Secret Code: " + Utils.formatColourList(secretCode));
        writeLine("Total Guesses Used: " + attemptsUsed);
        writeLine("Score: " + scoreBoard.getPlayerScore() + "-" + scoreBoard.getComputerScore());
        writeLine("----------------------------------");
        writeLine("");
        flush();
    }

    public void logSessionEnd(ScoreBoard scoreBoard) {
        writeLine("Session Ended: " + LocalDateTime.now());
        writeLine("Final Score: " + scoreBoard.getPlayerScore() + "-" + scoreBoard.getComputerScore());
        flush();
    }

    public String getLogFilePath() {
        return logFilePath == null ? "No log file created" : logFilePath.toAbsolutePath().toString();
    }

    public void close() {
        if (writer == null) {
            return;
        }

        try {
            writer.close();
        } catch (IOException ioException) {
            System.err.println("Warning: failed to close the log file cleanly.");
        } finally {
            writer = null;
        }
    }

    private void writeLine(String line) {
        if (writer == null) {
            return;
        }

        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to write to the log file.", ioException);
        }
    }

    private void flush() {
        if (writer == null) {
            return;
        }

        try {
            writer.flush();
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to flush the log file.", ioException);
        }
    }
}
