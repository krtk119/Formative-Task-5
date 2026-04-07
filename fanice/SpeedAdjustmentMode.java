import swiftbot.*;

/**
 * This class lets the user adjust the left and right wheel speeds separately
 * before starting navigation.
 *
 * Button X cycles through the modes: LEFT (just the left wheel), RIGHT (just
 * the right), BOTH (both at once) and RESET (puts both back to the default of
 * 30). Button A increases by 1, Button B decreases by 1 and Button Y confirms
 * the speeds and goes back to the main menu.
 *
 * The underlights get brighter or dimmer as you change the speed so you can see
 * roughly how fast you've set things without looking at the console.
 *
 * Each wheel can go from 10 to 100, but anything below 40 probably will not
 * make the SwiftBot move properly.
 *
 * Covers requirements: 54, 55, 56, 57, 58, 59, 60, 61
 *
 * @author 2532744
 * @version 4.0
 */
public class SpeedAdjustmentMode {

	/*
	 * This enum keeps track of which wheel(s) the user is currently adjusting.
	 * Button X cycles through them in order: LEFT -> RIGHT -> BOTH -> RESET -> back
	 * to LEFT. In RESET mode, pressing A or B resets both wheels to default.
	 */
	private enum AdjustMode {
		LEFT, RIGHT, BOTH, RESET;

		/* Returns the next mode in the cycle. */
		public AdjustMode next() {
			switch (this) {
			case LEFT:
				return RIGHT;
			case RIGHT:
				return BOTH;
			case BOTH:
				return RESET;
			case RESET:
				return LEFT;
			default:
				return BOTH;
			}
		}
	}

	private final SwiftBotAPI swiftBot;
	private final UIDisplay display;
	private int leftSpeed;
	private int rightSpeed;
	private AdjustMode currentMode;
	private volatile String buttonPressed;
	private volatile boolean exitMode;

	/*
	 * Sets up speed adjustment mode with both wheels starting at the same speed.
	 */
	public SpeedAdjustmentMode(SwiftBotAPI swiftBot, UIDisplay display, int initialSpeed) {
		this.swiftBot = swiftBot;
		this.display = display;
		this.leftSpeed = initialSpeed;
		this.rightSpeed = initialSpeed;
		this.currentMode = AdjustMode.BOTH;
		this.buttonPressed = "";
		this.exitMode = false;
	}

	/*
	 * Sets up speed adjustment mode with different starting speeds for each wheel -
	 * useful if the user already adjusted them before.
	 */
	public SpeedAdjustmentMode(SwiftBotAPI swiftBot, UIDisplay display, int initialLeft, int initialRight) {
		this.swiftBot = swiftBot;
		this.display = display;
		this.leftSpeed = initialLeft;
		this.rightSpeed = initialRight;
		this.currentMode = AdjustMode.BOTH;
		this.buttonPressed = "";
		this.exitMode = false;
	}

	/*
	 * This method runs the speed adjustment screen and waits for the user to press
	 * Button Y to confirm. It blocks until that happens. Once it returns, you can
	 * use {@code getLeftSpeed()} and {@code getRightSpeed()} to grab the final
	 * values.
	 */
	public int run() {
		display.showWheelSpeedAdjustmentScreen(leftSpeed, rightSpeed, currentMode.name());
		updateUnderlightBrightness();

		setupButtonHandlers();

		while (!exitMode) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}

			if (!buttonPressed.isEmpty()) {
				processButtonPress(buttonPressed);
				buttonPressed = "";
			}
		}

		swiftBot.disableAllButtons();
		swiftBot.disableUnderlights();

		return leftSpeed;
	}

	/*
	 * This method wires up all four buttons on the SwiftBot so each one sets a flag
	 * when pressed. The main loop in {@code run()} picks up the flag and acts on
	 * it.
	 */
	private void setupButtonHandlers() {
		swiftBot.enableButton(Button.A, () -> {
			buttonPressed = "A";
		});

		swiftBot.enableButton(Button.B, () -> {
			buttonPressed = "B";
		});

		swiftBot.enableButton(Button.X, () -> {
			buttonPressed = "X";
		});

		swiftBot.enableButton(Button.Y, () -> {
			buttonPressed = "Y";
		});
	}

	/*
	 * This method decides what to do based on which button was pressed. In RESET
	 * mode, A and B both reset the speeds to default. In any other mode, A
	 * increases and B decreases the selected wheel(s). X cycles through the modes,
	 * and Y confirms and exits.
	 */
	private void processButtonPress(String button) {
		switch (button) {
		case "A":
			if (currentMode == AdjustMode.RESET) {
				leftSpeed = MovementController.DEFAULT_SPEED;
				rightSpeed = MovementController.DEFAULT_SPEED;
				currentMode = AdjustMode.BOTH;
				display.showInfo("Both wheel speeds reset to default (" + MovementController.DEFAULT_SPEED + ").");
				display.showWheelSpeedUpdate(leftSpeed, rightSpeed, currentMode.name());
				updateUnderlightBrightness();
			} else {
				increaseSpeed();
			}
			break;

		case "B":
			if (currentMode == AdjustMode.RESET) {
				leftSpeed = MovementController.DEFAULT_SPEED;
				rightSpeed = MovementController.DEFAULT_SPEED;
				currentMode = AdjustMode.BOTH;
				display.showInfo("Both wheel speeds reset to default (" + MovementController.DEFAULT_SPEED + ").");
				display.showWheelSpeedUpdate(leftSpeed, rightSpeed, currentMode.name());
				updateUnderlightBrightness();
			} else {
				decreaseSpeed();
			}
			break;

		case "X":
			currentMode = currentMode.next();
			if (currentMode == AdjustMode.RESET) {
				display.showModeToggle(currentMode.name());
				display.showInfo("Press A or B to reset both wheels to default (" + MovementController.DEFAULT_SPEED
						+ ") or X to continue cycling.");
			} else {
				display.showModeToggle(currentMode.name());
				display.showWheelSpeedUpdate(leftSpeed, rightSpeed, currentMode.name());
			}
			break;

		case "Y":
			display.showWheelSpeedConfirmed(leftSpeed, rightSpeed);
			exitMode = true;
			break;

		default:
			break;
		}
	}

	/*
	 * This method increases the speed of whichever wheel(s) are currently selected
	 * by 1. It will not go above 100.
	 */
	private void increaseSpeed() {
		boolean changed = false;

		if (currentMode == AdjustMode.LEFT || currentMode == AdjustMode.BOTH) {
			if (leftSpeed < 100) {
				leftSpeed += 1;
				changed = true;
			}
		}

		if (currentMode == AdjustMode.RIGHT || currentMode == AdjustMode.BOTH) {
			if (rightSpeed < 100) {
				rightSpeed += 1;
				changed = true;
			}
		}

		if (changed) {
			display.showWheelSpeedUpdate(leftSpeed, rightSpeed, currentMode.name());
			updateUnderlightBrightness();
		} else {
			display.showInfo("Speed is already at maximum (100) for the selected wheel(s).");
		}
	}

	/*
	 * This method decreases the speed of whichever wheel(s) are currently selected
	 * by 1. It will not go below 10. If either wheel ends up below 40 it shows a
	 * warning since that's the minimum for the SwiftBot to move properly.
	 */
	private void decreaseSpeed() {
		boolean changed = false;

		if (currentMode == AdjustMode.LEFT || currentMode == AdjustMode.BOTH) {
			if (leftSpeed > 10) {
				leftSpeed -= 1;
				changed = true;
			}
		}

		if (currentMode == AdjustMode.RIGHT || currentMode == AdjustMode.BOTH) {
			if (rightSpeed > 10) {
				rightSpeed -= 1;
				changed = true;
			}
		}

		if (changed) {
			display.showWheelSpeedUpdate(leftSpeed, rightSpeed, currentMode.name());

			if (leftSpeed < MovementController.MINIMUM_MOVING_SPEED) {
				display.showInfo(
						"Warning: Left wheel speed " + leftSpeed + " is below the minimum for reliable movement ("
								+ MovementController.MINIMUM_MOVING_SPEED + ").");
			}
			if (rightSpeed < MovementController.MINIMUM_MOVING_SPEED) {
				display.showInfo(
						"Warning: Right wheel speed " + rightSpeed + " is below the minimum for reliable movement ("
								+ MovementController.MINIMUM_MOVING_SPEED + ").");
			}
			updateUnderlightBrightness();
		} else {
			display.showInfo("Speed is already at minimum (10) for the selected wheel(s).");
		}
	}

	/*
	 * This method adjusts the underlight brightness based on the average of both
	 * wheel speeds using {@code fillUnderlights()}. At speed 10 they are barely
	 * glowing, at speed 100 they are at full brightness.
	 */
	private void updateUnderlightBrightness() {
		int avgSpeed = (leftSpeed + rightSpeed) / 2;

		// Scale brightness from 25 (at speed 10) to 255 (at speed 100)
		int brightness = (int) (25 + (avgSpeed - 10) * (230.0 / (100 - 10)));
		brightness = Math.max(0, Math.min(255, brightness));

		int[] colour = new int[] { brightness, brightness, brightness };
		swiftBot.fillUnderlights(colour);
	}

	/** @return the left wheel speed the user chose */
	public int getLeftSpeed() {
		return leftSpeed;
	}

	/** @return the right wheel speed the user chose */
	public int getRightSpeed() {
		return rightSpeed;
	}
}