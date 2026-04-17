package fanice;
import swiftbot.*;

/**
 * This is the main class - where everything starts. It connects to the
 * SwiftBot, shows the welcome screen and runs the whole program from start to
 * finish.
 *
 * The program flow goes like this: first it connects to the SwiftBot hardware,
 * then shows the welcome screen with the button options. The user can press A
 * to start navigating, B to adjust wheel speeds or X to quit. After a
 * navigation session ends, it asks if the user wants to see the execution log,
 * saves the log to file and then offers the option to restart or exit.
 *
 * The whole thing runs in a loop so after one session ends the user can go back
 * to the main menu and do it all again.
 *
 * Covers requirements: 1, 2, 3, 35, 36, 37, 38, 39, 40, 41, 45, 48
 *
 * @author 2532744
 * @version 3.0
 */
public class TrafficLightMain {

	static SwiftBotAPI swiftBot;
	static int forwardSpeed = MovementController.DEFAULT_SPEED;
	static int leftWheelVelocity = MovementController.DEFAULT_SPEED;
	static int rightWheelVelocity = MovementController.DEFAULT_SPEED;
	static boolean perWheelSpeedSet = false;
	static UIDisplay display;
	static ExecutionLogger logger;
	static volatile String startupButton = "";
	static volatile String logChoiceButton = "";
	static volatile String restartChoiceButton = "";

	/*
	 * The entry point - this is what runs when you start the program. It sets up
	 * the SwiftBot connection once, then loops: show the menu, run a navigation
	 * session, handle termination and ask if the user wants to go again or quit.
	 */
	public static void main(String[] args) {
		display = new UIDisplay();

		try {
			swiftBot = SwiftBotAPI.INSTANCE;
		} catch (Exception e) {
			display.showInitialisationError();
			return;
		}

		String version = swiftBot.getVersion();

		boolean running = true;
		while (running) {

			resetSessionState();
			logger = ExecutionLogger.getInstance();

			// Store the API version so it shows up in the execution log
			logger.setApiVersion(version);

			display.showWelcomeScreen(version);

			setupStartupMenu();
			waitForStartupChoice();

			// If Button B was pressed, enter speed adjustment mode
			if ("B".equals(startupButton)) {
				SpeedAdjustmentMode speedMode = new SpeedAdjustmentMode(swiftBot, display, leftWheelVelocity,
						rightWheelVelocity);
				speedMode.run();

				leftWheelVelocity = validateSpeed(speedMode.getLeftSpeed());
				rightWheelVelocity = validateSpeed(speedMode.getRightSpeed());
				perWheelSpeedSet = true;

				forwardSpeed = (leftWheelVelocity + rightWheelVelocity) / 2;

				logger.logEvent("Per-wheel speeds set: Left=" + leftWheelVelocity + " Right=" + rightWheelVelocity);

				// Show welcome screen again after speed adjustment
				display.showWelcomeScreen(version);
				startupButton = "";
				setupStartupMenuAfterSpeed();
				waitForStartupChoice();
			}

			// If Button X was pressed, terminate
			if ("X".equals(startupButton)) {
				display.showInfo("Termination requested.");
				if (!promptRestart()) {
					running = false;
				}
				continue;
			}

			// If Button A was pressed, start navigation
			if ("A".equals(startupButton)) {
				logger.recordStartTime();

				MovementController movement = new MovementController(swiftBot);

				if (perWheelSpeedSet) {
					movement.setCustomWheelSpeeds(leftWheelVelocity, rightWheelVelocity);
					logger.logEvent("Custom per-wheel speeds applied: Left=" + leftWheelVelocity + " Right="
							+ rightWheelVelocity);

					if (!MovementController.isViableSpeed(leftWheelVelocity)) {
						display.showInfo("Warning: Left wheel speed " + leftWheelVelocity
								+ " is below minimum for reliable movement (" + MovementController.MINIMUM_MOVING_SPEED
								+ ").");
					}
					if (!MovementController.isViableSpeed(rightWheelVelocity)) {
						display.showInfo("Warning: Right wheel speed " + rightWheelVelocity
								+ " is below minimum for reliable movement (" + MovementController.MINIMUM_MOVING_SPEED
								+ ").");
					}
				} else {
					forwardSpeed = validateSpeed(forwardSpeed);
					if (!MovementController.isViableSpeed(forwardSpeed)) {
						display.showInfo("Speed " + forwardSpeed + " is below minimum for movement. Using "
								+ MovementController.MINIMUM_MOVING_SPEED + ".");
						forwardSpeed = MovementController.MINIMUM_MOVING_SPEED;
					}
				}

				ColourDetector colourDetector = new ColourDetector(swiftBot);

				NavigationManager navManager = new NavigationManager(swiftBot, movement, colourDetector, logger,
						display, forwardSpeed);

				try {
					navManager.startNavigation();
				} catch (Exception e) {
					logger.logEvent("Navigation interrupted");
					e.printStackTrace();
				}

				handleTermination();

				if (!promptRestart()) {
					running = false;
				}
			}
		}

		safeShutdown();
	}

	/*
	 * This method sets up the three buttons on the startup menu: A starts
	 * navigation, B opens speed adjustment, X quits.
	 */
	static void setupStartupMenu() {
		startupButton = "";

		swiftBot.enableButton(Button.A, () -> {
			startupButton = "A";
		});

		swiftBot.enableButton(Button.B, () -> {
			startupButton = "B";
		});

		swiftBot.enableButton(Button.X, () -> {
			startupButton = "X";
		});
	}

	/*
	 * This method sets up the startup menu after coming back from speed adjustment.
	 * Only A (start) and X (quit) are available since there's no point going back
	 * into speed mode straight away.
	 */
	static void setupStartupMenuAfterSpeed() {
		startupButton = "";

		swiftBot.enableButton(Button.A, () -> {
			startupButton = "A";
		});

		swiftBot.enableButton(Button.X, () -> {
			startupButton = "X";
		});
	}

	/*
	 * This method waits until the user presses one of the startup buttons. It
	 * checks every 100ms so it's not hammering the CPU.
	 */
	static void waitForStartupChoice() {
		while (startupButton.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		swiftBot.disableAllButtons();
	}

	/*
	 * This method runs the shutdown sequence after navigation ends. It stops the
	 * motors, turns off all the lights, asks if the user wants to see the log and
	 * then saves the log to a file no matter what.
	 */
	static void handleTermination() {
		logger.recordEndTime();

		swiftBot.stopMove();
		swiftBot.disableUnderlights();
		swiftBot.disableButtonLights();
		swiftBot.disableAllButtons();

		display.showTerminationScreen();

		logChoiceButton = "";

		swiftBot.enableButton(Button.Y, () -> {
			logChoiceButton = "Y";
		});

		swiftBot.enableButton(Button.X, () -> {
			logChoiceButton = "X";
		});

		// Wait up to 30 seconds for the user to decide
		long timeout = System.currentTimeMillis() + 30000;
		while (logChoiceButton.isEmpty() && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		swiftBot.disableAllButtons();

		if ("Y".equals(logChoiceButton)) {
			display.showExecutionLog(logger);
		} else {
			display.showInfo("Skipping log display.");
		}

		// Save the log to file regardless
		boolean saved = logger.saveToFile();
		if (saved) {
			String filePath = System.getProperty("user.dir") + System.getProperty("file.separator")
					+ ExecutionLogger.LOG_FILE_NAME;
			display.showLogSaved(filePath);
		} else {
			display.showLogSaveError();
		}
	}

	/*
	 * This method shuts down all the SwiftBot hardware safely - stops the motors,
	 * turns off all the lights and disables all the buttons. It's wrapped in a
	 * try-catch so even if something goes wrong during cleanup, the program still
	 * exits cleanly.
	 */
	static void safeShutdown() {
		try {
			if (swiftBot != null) {
				swiftBot.stopMove();
				swiftBot.disableUnderlights();
				swiftBot.disableButtonLights();
				swiftBot.disableAllButtons();
			}
		} catch (Exception e) {
			System.out.println("ERROR: Error during shutdown.");
			e.printStackTrace();
		}
		display.showProgramTerminated();
	}

	/*
	 * This method makes sure the hardware is in a safe state before showing the
	 * restart prompt. Stops motors, turns off lights, disables buttons.
	 */
	static void promptRestart_cleanup() {
		try {
			swiftBot.stopMove();
			swiftBot.disableUnderlights();
			swiftBot.disableButtonLights();
			swiftBot.disableAllButtons();
		} catch (Exception e) {
			// If cleanup fails, carry on
		}
	}

	/*
	 * This method shows the "SESSION COMPLETE" screen and asks the user what they
	 * want to do next. Button X goes back to the main menu for another run, Button
	 * Y exits the program completely.
	 */
	static boolean promptRestart() {
		promptRestart_cleanup();

		display.showRestartPrompt();

		restartChoiceButton = "";

		swiftBot.enableButton(Button.X, () -> {
			restartChoiceButton = "X";
		});

		swiftBot.enableButton(Button.Y, () -> {
			restartChoiceButton = "Y";
		});

		while (restartChoiceButton.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		swiftBot.disableAllButtons();

		if ("X".equals(restartChoiceButton)) {
			display.showInfo("Returning to main menu...");
			return true;
		} else {
			display.showInfo("Exiting program...");
			return false;
		}
	}

	/*
	 * This method resets everything for a fresh session. It puts all the speeds
	 * back to default, clears the button flags and wipes the old logger so a new
	 * one gets created with zero counts.
	 */
	static void resetSessionState() {
		forwardSpeed = MovementController.DEFAULT_SPEED;
		leftWheelVelocity = MovementController.DEFAULT_SPEED;
		rightWheelVelocity = MovementController.DEFAULT_SPEED;
		perWheelSpeedSet = false;
		startupButton = "";
		logChoiceButton = "";
		restartChoiceButton = "";

		ExecutionLogger.resetInstance();
	}

	/*
	 * This method checks that a speed is within the valid range of 10 to 100. If
	 * it's not, it prints a warning and returns the default speed instead.
	 */
	static int validateSpeed(int speed) {
		if (speed < 10 || speed > 100) {
			System.out.println("[WARN] Speed " + speed + " is outside valid range (10-100). Resetting to default ("
					+ MovementController.DEFAULT_SPEED + ").");
			return MovementController.DEFAULT_SPEED;
		}
		return speed;
	}
}