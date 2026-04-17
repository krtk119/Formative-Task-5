package kerry;
public class ScoreBoard {
    private int playerScore;
    private int computerScore;

    public void recordPlayerWin() {
        playerScore++;
    }

    public void recordComputerWin() {
        computerScore++;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getComputerScore() {
        return computerScore;
    }

    public String toDisplayString() {
        return "Player Score: " + playerScore + "   Computer Score: " + computerScore;
    }
}
