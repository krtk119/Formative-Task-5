package fanice;
/**
 * This class handles everything the user sees in the console. Every message,
 * screen and error goes through here.
 *
 * The text is colour-coded using ANSI escape codes so it's easier to read in
 * the command prompt: - Red for red light detections and errors - Green for
 * green light detections and success messages - Blue for blue light detections
 * - Yellow for warnings and state indicators - Cyan for general info messages -
 * Bold for headings and important values
 *
 * All the screens are formatted with fixed-width ASCII lines so they look the
 * same no matter which terminal you are using.
 *
 * Covers requirements: 1, 2, 4, 5, 17, 18, 19, 20, 23, 33, 37, 38, 41, 44, 49,
 * 50, 51
 *
 * @author 2532744
 * @version 3.0
 */
public class UIDisplay {

	// ANSI colour codes
	static final String RESET = "\u001B[0m";
	static final String BOLD = "\u001B[1m";
	static final String RED = "\u001B[31m";
	static final String GREEN = "\u001B[32m";
	static final String YELLOW = "\u001B[33m";
	static final String BLUE = "\u001B[34m";
	static final String MAGENTA = "\u001B[35m";
	static final String CYAN = "\u001B[36m";
	static final String WHITE = "\u001B[37m";

	// Bright ANSI colour codes
	static final String BRIGHT_RED = "\u001B[91m";
	static final String BRIGHT_GREEN = "\u001B[92m";
	static final String BRIGHT_YELLOW = "\u001B[93m";
	static final String BRIGHT_BLUE = "\u001B[94m";
	static final String BRIGHT_CYAN = "\u001B[96m";

	// Separator lines used across all screens
	static final String SEPARATOR = CYAN + "============================================================" + RESET;
	static final String SUB_SEPARATOR = CYAN + "------------------------------------------------------------" + RESET;

	/*
	 * This method shows the welcome screen - the first thing the user sees when the
	 * program starts. It displays an ASCII art banner, the API version and a list
	 * of what each button does.
	 */
	public void showWelcomeScreen(String apiVersion) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_CYAN + "    SWIFTBOT TRAFFIC LIGHT NAVIGATION SYSTEM" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BRIGHT_GREEN + " ____          _  __ _   ____        _   " + RESET);
		System.out.println(BRIGHT_GREEN + "/ ___|_      _(_)/ _| |_| __ )  ___ | |_ " + RESET);
		System.out.println(BRIGHT_GREEN + "\\___ \\ \\ /\\ / / | |_| __| _ \\ / _ \\| __|" + RESET);
		System.out.println(BRIGHT_GREEN + " ___) \\ V  V /| |  _| |_| |_) | (_) | |_ " + RESET);
		System.out.println(BRIGHT_GREEN + "|____/ \\_/\\_/ |_|_|  \\__|____/ \\___/ \\__|" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "IDLE - Awaiting user input");
		System.out.println("SwiftBotAPI version: " + BOLD + apiVersion + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "Controls (physical buttons on SwiftBot):" + RESET);
		System.out.println(GREEN + "  Button A" + RESET + " : Start navigation");
		System.out.println(BLUE + "  Button B" + RESET + " : Enter per-wheel speed adjustment mode");
		System.out.println(RED + "  Button X" + RESET + " : Terminate program");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "Note:" + RESET + " All movement is controlled by the SwiftBot");
		System.out.println("      hardware. This console shows status, logs");
		System.out.println("      and prompts.");
		System.out.println(BRIGHT_YELLOW + "Waiting for button press..." + RESET);
	}

	/*
	 * This method shows the per-wheel speed adjustment screen. It displays both
	 * wheel speeds with arrows next to whichever one(s) the user is currently
	 * adjusting, along with the button controls.
	 */
	public void showWheelSpeedAdjustmentScreen(int leftSpeed, int rightSpeed, String mode) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + MAGENTA + "        PER-WHEEL SPEED ADJUSTMENT MODE" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "SPEED MODE");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Current wheel speeds:" + RESET);

		// Show arrows next to the wheel(s) being adjusted
		String leftIndicator = mode.equals("LEFT") || mode.equals("BOTH") ? BRIGHT_YELLOW + " <<" + RESET : "";
		String rightIndicator = mode.equals("RIGHT") || mode.equals("BOTH") ? BRIGHT_YELLOW + " <<" + RESET : "";

		System.out.println(
				"  Left  wheel speed : " + BOLD + leftSpeed + RESET + " (valid range: 10 to 100)" + leftIndicator);
		System.out.println(
				"  Right wheel speed : " + BOLD + rightSpeed + RESET + " (valid range: 10 to 100)" + rightIndicator);
		System.out.println(SUB_SEPARATOR);
		System.out.println("Currently adjusting : " + BOLD + BRIGHT_YELLOW + mode + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Speed controls (physical buttons):" + RESET);

		// In RESET mode, both A and B reset to default instead of adjusting
		if (mode.equals("RESET")) {
			System.out.println(GREEN + "  Button A" + RESET + " : Reset both wheels to default ("
					+ MovementController.DEFAULT_SPEED + ")");
			System.out.println(RED + "  Button B" + RESET + " : Reset both wheels to default ("
					+ MovementController.DEFAULT_SPEED + ")");
		} else {
			System.out.println(GREEN + "  Button A" + RESET + " : Increase selected wheel(s) by +1");
			System.out.println(RED + "  Button B" + RESET + " : Decrease selected wheel(s) by -1");
		}

		System.out.println(BLUE + "  Button X" + RESET + " : Toggle mode (LEFT -> RIGHT -> BOTH -> RESET)");
		System.out.println(BRIGHT_GREEN + "  Button Y" + RESET + " : Confirm speeds and exit");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " Underlights brightness reflects current speed.");
		System.out.println(CYAN + "[INFO]" + RESET + " Recommended: use 40-70 for reliable movement.");
		System.out.println(CYAN + "[INFO]" + RESET + " Toggle with X to fine-tune each wheel separately.");
	}

	/* Wrapper for single-speed - shows both wheels at the same value. */
	public void showSpeedAdjustmentScreen(int currentSpeed) {
		showWheelSpeedAdjustmentScreen(currentSpeed, currentSpeed, "BOTH");
	}

	/* Confirms that the per-wheel speeds were updated. */
	public void showWheelSpeedUpdate(int leftSpeed, int rightSpeed, String mode) {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Speed updated. Left: " + BOLD + leftSpeed + RESET
				+ "  Right: " + BOLD + rightSpeed + RESET + "  (Mode: " + BRIGHT_YELLOW + mode + RESET + ")");
	}

	/* Confirms that a single speed value was updated. */
	public void showSpeedUpdate(int newSpeed) {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Speed updated. Current speed: " + BOLD + newSpeed + RESET);
	}

	/* Shows the final confirmed per-wheel speeds when leaving speed mode. */
	public void showWheelSpeedConfirmed(int leftSpeed, int rightSpeed) {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Speeds confirmed: Left=" + BOLD + leftSpeed + RESET
				+ " Right=" + BOLD + rightSpeed + RESET);
		System.out.println("Returning to main menu...");
	}

	/* Shows the confirmed speed when leaving speed mode (single value). */
	public void showSpeedConfirmed(int confirmedSpeed) {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Speed confirmed: " + BOLD + confirmedSpeed + RESET);
		System.out.println("Returning to main menu...");
	}

	/* Tells the user which adjustment mode they switched to. */
	public void showModeToggle(String newMode) {
		System.out.println(
				BRIGHT_YELLOW + "[MODE]" + RESET + " Now adjusting: " + BOLD + BRIGHT_YELLOW + newMode + RESET);
	}

	/*
	 * This method shows that navigation has started. It displays the left and right
	 * wheel speeds along with the ultrasound threshold and scan interval so the
	 * user knows what settings are active.
	 */
	public void showNavigationStartPerWheel(int leftSpeed, int rightSpeed) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_GREEN + "         NAVIGATION MODE - ACTIVE" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "NAVIGATION");
		System.out.println("Left wheel speed  : " + BOLD + leftSpeed + RESET);
		System.out.println("Right wheel speed : " + BOLD + rightSpeed + RESET);
		System.out.println("Ultrasound threshold: 30 cm");
		System.out.println("Scan interval: 500 ms");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " Navigation started. Scanning for traffic lights...");
		System.out.println("  Press " + RED + "Button X" + RESET + " to request termination.");
	}

	/* Wrapper for single-speed navigation display. */
	public void showNavigationStart(int initialSpeed) {
		showNavigationStartPerWheel(initialSpeed, initialSpeed);
	}

	/*
	 * This method shows the red light detection screen. The header and action steps
	 * are shown in red, with the sensor readings below.
	 */
	public void showRedLightDetected(double distanceToObject, double avgR, double avgG, double avgB) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "[DETECTION] RED light detected" + RESET);
		System.out.println(RED + "[STATE] NAVIGATION - RED STOP" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "Action:" + RESET);
		System.out.println(RED + "  1. Stopping SwiftBot for 1 second." + RESET);
		System.out.println(RED + "  2. Underlights set to RED." + RESET);
		System.out.println("  3. Navigation resumes after stop period.");
		System.out.println(BOLD + "Sensor details:" + RESET);
		System.out.printf("  Distance to obstacle : " + YELLOW + "%.1f cm%n" + RESET, distanceToObject);
		System.out.printf("  Average RGB : " + RED + "R=%.0f" + RESET + " G=%.0f B=%.0f%n", avgR, avgG, avgB);
		System.out.println(CYAN + "[INFO]" + RESET + " Navigation will resume after the stop period.");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows the green light detection screen. It also displays the
	 * calculated pass speed alongside the sensor readings.
	 */
	public void showGreenLightDetected(double distanceToObject, int calculatedSpd, double avgR, double avgG,
			double avgB) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_GREEN + "[DETECTION] GREEN light detected" + RESET);
		System.out.println(GREEN + "[STATE] NAVIGATION - GREEN PROCEED" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "Action:" + RESET);
		System.out.println(GREEN + "  1. Calculating speed to pass in 2 seconds." + RESET);
		System.out.println(GREEN + "  2. Moving forward for 2 seconds." + RESET);
		System.out.println("  3. Stopping for 1 second, then resuming.");
		System.out.println(BOLD + "Sensor details:" + RESET);
		System.out.printf("  Distance to obstacle : " + YELLOW + "%.1f cm%n" + RESET, distanceToObject);
		System.out.printf("  Average RGB : R=%.0f " + GREEN + "G=%.0f" + RESET + " B=%.0f%n", avgR, avgG, avgB);
		System.out.println("  Calculated speed     : " + BOLD + calculatedSpd + RESET);
		System.out.println(CYAN + "[INFO]" + RESET + " Underlights set to GREEN during movement.");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows the blue light detection screen and lists the full yield
	 * manoeuvre sequence. The first three steps are shown in blue.
	 */
	public void showBlueLightDetected(double distanceToObject, double avgR, double avgG, double avgB) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_BLUE + "[DETECTION] BLUE light detected" + RESET);
		System.out.println(BLUE + "[STATE] NAVIGATION - YIELD MANOEUVRE" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "Action sequence:" + RESET);
		System.out.println(BLUE + "  1. Stop SwiftBot." + RESET);
		System.out.println(BLUE + "  2. Blink underlights BLUE 3 times." + RESET);
		System.out.println(BLUE + "  3. Turn left 90 degrees." + RESET);
		System.out.println("  4. Move forward for 1 second.");
		System.out.println("  5. Wait 1 second.");
		System.out.println("  6. Retrace path to original position.");
		System.out.println(BOLD + "Sensor details:" + RESET);
		System.out.printf("  Distance to obstacle : " + YELLOW + "%.1f cm%n" + RESET, distanceToObject);
		System.out.printf("  Average RGB : R=%.0f G=%.0f " + BLUE + "B=%.0f" + RESET + "%n", avgR, avgG, avgB);
		System.out.println(CYAN + "[INFO]" + RESET + " Ultrasound is monitored during the manoeuvre.");
		System.out.println("       If an obstacle is within 30 cm, the manoeuvre");
		System.out.println("       will be interrupted.");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows the checkpoint prompt that appears after every 3 traffic
	 * lights. It displays the colour counts and asks the user whether to continue
	 * or stop.
	 */
	public void showCheckpointPrompt(int totalCount, int redCount, int greenCount, int blueCount) {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_YELLOW + "         3-LIGHT CHECKPOINT REACHED" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "CHECKPOINT");
		System.out.println(BOLD + "Summary so far:" + RESET);
		System.out.println("  " + RED + "Red" + RESET + "   : " + redCount);
		System.out.println("  " + GREEN + "Green" + RESET + " : " + greenCount);
		System.out.println("  " + BLUE + "Blue" + RESET + "  : " + blueCount);
		System.out.println("  Total : " + BOLD + totalCount + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BRIGHT_YELLOW + "[QUESTION]" + RESET + " Continue navigation or terminate?");
		System.out.println(BOLD + "Controls (physical buttons):" + RESET);
		System.out.println(GREEN + "  Button A" + RESET + " : Continue navigation");
		System.out.println(RED + "  Button X" + RESET + " : Terminate and proceed to shutdown");
		System.out.println(BRIGHT_YELLOW + "Waiting for your choice..." + RESET);
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows the termination screen. It lists what the program is doing
	 * (stopping motors, turning off lights) and asks if the user wants to see the
	 * execution log before it gets saved.
	 */
	public void showTerminationScreen() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + RED + "           TERMINATION REQUESTED" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "TERMINATING");
		System.out.println(BOLD + "Action:" + RESET);
		System.out.println("  1. Stopping all motors.");
		System.out.println("  2. Disabling underlights and button lights.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " You may now choose to display the log.");
		System.out.println(BOLD + "Controls (physical buttons):" + RESET);
		System.out.println(GREEN + "  Button Y" + RESET + " : Display execution log, then save");
		System.out.println(RED + "  Button X" + RESET + " : Skip display, save log only");
		System.out.println(BRIGHT_YELLOW + "Waiting for button press..." + RESET);
		System.out.println(SEPARATOR);
	}

	/* Prints the full execution log in bright cyan so it stands out. */
	public void showExecutionLog(ExecutionLogger logger) {
		System.out.println(BOLD + BRIGHT_CYAN);
		System.out.println(logger.generateLogString());
		System.out.print(RESET);
	}

	/* Confirms the log file was saved and shows where to find it. */
	public void showLogSaved(String filePath) {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Log saved to: " + BOLD + filePath + RESET);
	}

	/*
	 * This method shows an error when saving the log file fails. It explains what
	 * went wrong and suggests things the user can check.
	 */
	public void showLogSaveError() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "[ERROR] FILE WRITE - Failed to save execution log." + RESET);
		System.out.println(SEPARATOR);
		System.out.println(RED + "What went wrong:" + RESET);
		System.out.println("  Target file: traffic_log.txt");
		System.out.println(RED + "What happens next:" + RESET);
		System.out.println("  Log was NOT saved to file.");
		System.out.println("  Console log remains available for review.");
		System.out.println(CYAN + "[INFO]" + RESET + " Please check: disk space, file permissions");
		System.out.println("       and directory existence.");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows the initialisation error screen. It usually means I2C is
	 * disabled on the Raspberry Pi, so it tells the user exactly what commands to
	 * run to fix it.
	 */
	public void showInitialisationError() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "[ERROR] INITIALISATION - SwiftBotAPI failed to start." + RESET);
		System.out.println(SEPARATOR);
		System.out.println(RED + "What went wrong:" + RESET);
		System.out.println("  The SwiftBotAPI could not be initialised.");
		System.out.println("  Most likely cause: I2C is disabled.");
		System.out.println(BRIGHT_YELLOW + "How to fix it:" + RESET);
		System.out.println("  Run these commands on the Raspberry Pi:");
		System.out.println("  1. sudo raspi-config nonint do_i2c 0");
		System.out.println("  2. sudo reboot");
		System.out.println("  Then run this program again.");
		System.out.println(RED + "What happens next:" + RESET);
		System.out.println("  The program will now exit.");
		System.out.println(YELLOW + "[STATE] " + RESET + "FAILED TO START");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows an error when the camera cannot take a photo. It lists the
	 * probable causes and lets the user know this cycle gets skipped but navigation
	 * keeps going.
	 */
	public void showCameraError() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "[ERROR] CAMERA - Failed to capture image." + RESET);
		System.out.println(SEPARATOR);
		System.out.println(RED + "What went wrong:" + RESET);
		System.out.println("  Method: takeStill(ImageSize.SQUARE_720x720)");
		System.out.println("  Probable causes: camera not connected, in use");
		System.out.println("  by another process or hardware fault.");
		System.out.println(RED + "What happens next:" + RESET);
		System.out.println("  This detection cycle is skipped.");
		System.out.println("  SwiftBot movement state: UNCHANGED.");
		System.out.println(CYAN + "[INFO]" + RESET + " If this error repeats, restart the program");
		System.out.println("       or check the camera connection.");
		System.out.println(SEPARATOR);
	}

	/*
	 * This method shows an error when the ultrasound sensor cannot get a reading.
	 * The SwiftBot keeps moving since we cannot tell how far away anything is.
	 */
	public void showUltrasoundError() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "[ERROR] ULTRASOUND - Failed to read distance." + RESET);
		System.out.println(SEPARATOR);
		System.out.println(RED + "What went wrong:" + RESET);
		System.out.println("  Method: useUltrasound()");
		System.out.println(RED + "What happens next:" + RESET);
		System.out.println("  Distance treated as unknown for this cycle.");
		System.out.println("  No obstacle-based stop triggered.");
		System.out.println("  SwiftBot movement state: UNCHANGED.");
		System.out.println(BRIGHT_YELLOW + "[WARN]" + RESET + " Repeated failures may reduce safety.");
		System.out.println("       Consider stopping and checking hardware.");
		System.out.println(SEPARATOR);
	}

	/* Prints a general info message with a cyan [INFO] tag. */
	public void showInfo(String message) {
		System.out.println(CYAN + "[INFO]" + RESET + " " + message);
	}

	/* Shows that the program has shut down safely. */
	public void showProgramTerminated() {
		System.out.println(SEPARATOR);
		System.out.println(BRIGHT_GREEN + "[STATE]" + RESET + " Program terminated safely.");
		System.out.println(SEPARATOR);
	}

	/* Warns that the yield manoeuvre was aborted due to an obstacle. */
	public void showYieldAborted() {
		System.out.println(BRIGHT_YELLOW + "[WARN]" + RESET + " Obstacle detected during yield manoeuvre.");
		System.out.println("  Yield aborted - returning to original heading.");
	}

	/* Confirms the yield manoeuvre finished successfully. */
	public void showYieldComplete() {
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Yield manoeuvre complete. Resuming navigation.");
	}

	/*
	 * This method shows up after a session ends. It gives the user a choice: press
	 * X to go back to the main menu for another run or press Y to quit the program
	 * completely.
	 */
	public void showRestartPrompt() {
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_YELLOW + "            SESSION COMPLETE" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "What would you like to do?" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Controls (physical buttons):" + RESET);
		System.out.println(BLUE + "  Button X" + RESET + " : Return to main menu (start a new session)");
		System.out.println(RED + "  Button Y" + RESET + " : Exit program completely");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BRIGHT_YELLOW + "Waiting for your choice..." + RESET);
		System.out.println(SEPARATOR);
	}
}