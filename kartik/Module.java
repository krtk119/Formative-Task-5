import swiftbot.SwiftBotAPI;
import java.util.Scanner;

    // Abstract base class — all 8 functional modules extend this
// Provides shared constants, LED helpers, and sleep utility
    public abstract class Module {

        // Hardware API reference — passed in via constructor (dependency injection)
    protected final SwiftBotAPI bot;
    protected final Scanner keyboard;

        // Morse LED timing constants — dot=500ms, dash=1500ms
    protected static final int DOT_MS        = 500;
    protected static final int DASH_MS       = 1500;
    protected static final int END_CHAR_MS   = 1000;
    protected static final int END_WORD_MS   = 2000;
    protected static final int END_MSG_MS    = 3000;
    protected static final int LED_GAP_MS    = 200;
    protected static final int FEEDBACK_MS   = 150;
        // Obstacle threshold — stop if object within 15cm
    protected static final int OBSTACLE_CM   = 15;
    protected static final int OBSTACLE_WAIT_S = 10;
        // Calibrated for carpet: speed=84, trim=16 compensates stronger wheel
    protected static final int WHEEL_SPEED   = 84;
    protected static final int DRIVE_50CM_MS = 3500;
    protected static final int TURN_120_MS   = 770;
    protected static final int WHEEL_TRIM    = 16;
    public    static final int DWELL_S       = 10;
        // Max 3 QR attempts before IMPOSTER protocol triggers
    protected static final int QR_ATTEMPTS   = 3;
    protected static final int AUTH_LOCKOUT_S = 5;

    // ── Shared LED colour arrays — available to every subclass ─────────
        // LED colour arrays — used by setLED() and pulseLED()
    protected static final int[] WHITE  = {255, 255, 255};
    protected static final int[] BLUE   = {  0,   0, 255};
    protected static final int[] AMBER  = {255, 191,   0};
    protected static final int[] RED    = {255,   0,   0};
    protected static final int[] GREEN  = {  0, 255,   0};
    protected static final int[] YELLOW = {255, 255,   0};
    protected static final int[] OFF    = {  0,   0,   0};

    // ── Shared file names — available to every subclass ────────────────
        // File paths shared across all modules
    protected static final String MORSE_DICT = "morse_dictionary.txt";
    protected static final String COMM_LOG   = "spybot_communications.txt";
    protected static final String CYCLE_LOG  = "cycle_analysis_log.txt";
    protected static final String EMRG_LOG   = "emergency_log.txt";

  
    protected Module(SwiftBotAPI bot, Scanner keyboard) {
        this.bot      = bot;
        this.keyboard = keyboard;
    }
        // Every subclass must implement run() — polymorphism in action
    public abstract void run();

    
        // Set all underlights to given RGB colour
    protected void setLED(int[] rgb) {
        try { bot.fillUnderlights(rgb); } catch (Exception ignored) {}
    }

    protected void clearLED() {
        try { bot.disableUnderlights(); } catch (Exception ignored) {}
    }

        // Flash LED for given duration then turn off
    protected void pulseLED(int[] rgb, int ms) {
        setLED(rgb);
        sleep(ms);
        clearLED();
    }

    
        // Safe sleep — ignores InterruptedException
    protected void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

        // Returns ultrasound distance in cm, -1 if sensor fails
    protected double getDistance() {
        try { return bot.useUltrasound(); } catch (Exception e) { return -1; }
    }
}
