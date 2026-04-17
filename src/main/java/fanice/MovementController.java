package fanice;
import swiftbot.*;

/**
 * This class controls the SwiftBot's motors and tries to make it drive in a
 * straight line by adjusting the left and right wheel speeds.
 *
 * Since the two wheels don't always spin at the same rate, there are two ways
 * to handle it:
 *
 * 1. Calibration Table (default) - looks up what the left and right wheels need
 * to be set to, based on testing I did on lab carpet at speeds from 10 to 100.
 *
 * 2. Custom Per-Wheel Mode - if the user set separate left and right speeds in
 * Speed Adjustment Mode, those get used directly instead. This is useful for
 * tuning on different surfaces.
 *
 * Speeds are always kept between -100 and 100 and the SwiftBot pauses briefly
 * before switching direction so the motors don't get damaged.
 *
 * Covers requirements: 6, 25, 27, 28, 29, 36, 45, 46, 47, 52
 *
 * @author 2532744
 * @version 3.0
 */
public class MovementController {

	/** Minimum speed the SwiftBot can move at reliably. */
	public static final int MINIMUM_MOVING_SPEED = 40;

	/** Maximum allowed speed. */
	public static final int MAXIMUM_SPEED = 100;

	/** Default navigation speed. */
	public static final int DEFAULT_SPEED = 30;

	/*
	 * Calibration table for the left wheel. Each value is the actual speed the left
	 * wheel needs to be set to for a given base speed. Index 0 is base speed 10,
	 * index 1 is base speed 20, up to index 9 which is base speed 100. A value of
	 * -1 means the SwiftBot cannot move reliably at that speed.
	 *
	 * The left wheel is the dominant wheel on this SwiftBot. These values were
	 * figured out by testing on lab carpet.
	 */
	private static final int[] LEFT_WHEEL_SPEEDS = { -1, // Base speed 10: no movement
			-1, // Base speed 20: no movement
			30, // Base speed 30: left = 30, right = 40
			40, // Base speed 40: left = 40, right = 50
			50, // Base speed 50: left = 50, right = 60
			60, // Base speed 60: left = 60, right = 70
			70, // Base speed 70: left = 70, right = 85
			80, // Base speed 80: left = 80, right = 100
			90, // Base speed 90: left = 90, right = 100
			100 // Base speed 100: left = 100, right = 100
	};

	/*
	 * Same thing but for the right wheel. The right wheel needs higher values to
	 * compensate for the left wheel being dominant.
	 */
	private static final int[] RIGHT_WHEEL_SPEEDS = { -1, // Base speed 10: no movement
			-1, // Base speed 20: no movement
			40, // Base speed 30
			50, // Base speed 40
			60, // Base speed 50
			70, // Base speed 60
			85, // Base speed 70
			100, // Base speed 80
			100, // Base speed 90
			100 // Base speed 100
	};

	private final SwiftBotAPI swiftBot;
	private boolean isMoving;
	private int currentDirection; // 1 = forward, -1 = backward, 0 = stopped
	private boolean customWheelSpeedsActive;
	private int customLeftSpeed;
	private int customRightSpeed;

	/*
	 * Sets up the MovementController with the given SwiftBotAPI. Starts off using
	 * the calibration table - custom mode is off by default.
	 */
	public MovementController(SwiftBotAPI swiftBot) {
		this.swiftBot = swiftBot;
		this.isMoving = false;
		this.currentDirection = 0;
		this.customWheelSpeedsActive = false;
		this.customLeftSpeed = DEFAULT_SPEED;
		this.customRightSpeed = DEFAULT_SPEED;
	}

	/*
	 * This method saves the user's custom left and right wheel speeds and switches
	 * over to custom mode. From this point on, {@code moveForward()} and {@code
	 * moveForwardTimed()} will use these values instead of looking up the
	 * calibration table.
	 */
	public void setCustomWheelSpeeds(int leftWheelVelocity, int rightWheelVelocity) {
		this.customLeftSpeed = clampSpeed(leftWheelVelocity);
		this.customRightSpeed = clampSpeed(rightWheelVelocity);
		this.customWheelSpeedsActive = true;
	}

	/** @return true if custom per-wheel speeds are being used */
	public boolean isCustomWheelSpeedsActive() {
		return customWheelSpeedsActive;
	}

	/** @return the custom left wheel speed */
	public int getCustomLeftSpeed() {
		return customLeftSpeed;
	}

	/** @return the custom right wheel speed */
	public int getCustomRightSpeed() {
		return customRightSpeed;
	}

	/*
	 * This method starts the SwiftBot moving forward using {@code startMove()} and
	 * keeps it going until something tells it to stop. If custom speeds are set
	 * those are used, otherwise it looks up the calibration table. If the SwiftBot
	 * was going backward, it pauses briefly before switching direction so the
	 * motors don't get damaged.
	 */
	public void moveForward(int baseSpeed) throws InterruptedException {
		int leftWheelVelocity = 0;
		int rightWheelVelocity = 0;

		if (customWheelSpeedsActive) {
			leftWheelVelocity = customLeftSpeed;
			rightWheelVelocity = customRightSpeed;
		} else {
			leftWheelVelocity = getCalibratedLeftSpeed(baseSpeed);
			rightWheelVelocity = getCalibratedRightSpeed(baseSpeed);
		}

		leftWheelVelocity = clampSpeed(leftWheelVelocity);
		rightWheelVelocity = clampSpeed(rightWheelVelocity);

		// Pause before switching from backward to forward
		if (isMoving && currentDirection < 0) {
			swiftBot.stopMove();
			Thread.sleep(200);
		}

		swiftBot.startMove(leftWheelVelocity, rightWheelVelocity);
		isMoving = true;
		currentDirection = 1;
	}

	/*
	 * This method moves the SwiftBot forward for a set amount of time using {@code
	 * move()}, then stops automatically. If custom speeds are on, they get scaled
	 * proportionally so the left/right ratio stays the same but the overall speed
	 * matches what was requested. Used for passing green lights and the yield
	 * forward step.
	 */
	public void moveForwardTimed(int baseSpeed, int movementTime) throws InterruptedException {
		int leftWheelVelocity = 0;
		int rightWheelVelocity = 0;

		if (customWheelSpeedsActive) {
			leftWheelVelocity = scaleCustomSpeed(customLeftSpeed, baseSpeed);
			rightWheelVelocity = scaleCustomSpeed(customRightSpeed, baseSpeed);
		} else {
			leftWheelVelocity = getCalibratedLeftSpeed(baseSpeed);
			rightWheelVelocity = getCalibratedRightSpeed(baseSpeed);
		}

		leftWheelVelocity = clampSpeed(leftWheelVelocity);
		rightWheelVelocity = clampSpeed(rightWheelVelocity);

		if (isMoving && currentDirection < 0) {
			swiftBot.stopMove();
			Thread.sleep(200);
		}

		swiftBot.move(leftWheelVelocity, rightWheelVelocity, movementTime);
		isMoving = false;
		currentDirection = 0;
	}

	/*
	 * This method moves the SwiftBot backward for a set amount of time using {@code
	 * move()} with negative velocities. Same scaling logic as {@code
	 * moveForwardTimed()} if custom speeds are on. Used when retracing the path
	 * during the blue yield manoeuvre.
	 */
	public void moveBackwardTimed(int baseSpeed, int movementTime) throws InterruptedException {
		int leftWheelVelocity = 0;
		int rightWheelVelocity = 0;

		if (customWheelSpeedsActive) {
			leftWheelVelocity = scaleCustomSpeed(customLeftSpeed, baseSpeed);
			rightWheelVelocity = scaleCustomSpeed(customRightSpeed, baseSpeed);
		} else {
			leftWheelVelocity = getCalibratedLeftSpeed(baseSpeed);
			rightWheelVelocity = getCalibratedRightSpeed(baseSpeed);
		}

		leftWheelVelocity = clampSpeed(leftWheelVelocity);
		rightWheelVelocity = clampSpeed(rightWheelVelocity);

		// Pause before switching from forward to backward
		if (isMoving && currentDirection > 0) {
			swiftBot.stopMove();
			Thread.sleep(200);
		}

		swiftBot.move(-leftWheelVelocity, -rightWheelVelocity, movementTime);
		isMoving = false;
		currentDirection = 0;
	}

	/*
	 * This method spins the SwiftBot left (anticlockwise) by roughly 90 degrees
	 * using {@code move()} with opposite wheel directions. If the SwiftBot was
	 * already moving it stops first and waits a moment. Anticlockwise needs speed
	 * 57 based on calibration testing.
	 */
	public void turnLeft90() throws InterruptedException {
		if (isMoving) {
			swiftBot.stopMove();
			Thread.sleep(200);
		}
		swiftBot.move(-57, 57, 650);
		isMoving = false;
		currentDirection = 0;
	}

	/*
	 * This method spins the SwiftBot right (clockwise) by roughly 90 degrees.
	 * Clockwise needs speed 57 based on calibration testing.
	 */
	public void turnRight90() throws InterruptedException {
		if (isMoving) {
			swiftBot.stopMove();
			Thread.sleep(200);
		}
		swiftBot.move(57, -57, 650);
		isMoving = false;
		currentDirection = 0;
	}

	/*
	 * This method stops both motors straight away using {@code stopMove()}.
	 */
	public void stop() {
		swiftBot.stopMove();
		isMoving = false;
		currentDirection = 0;
	}

	/** @return true if the motors are currently running */
	public boolean isCurrentlyMoving() {
		return isMoving;
	}

	/*
	 * This method looks up what the left wheel speed should be for a given base
	 * speed using the calibration table. If the speed falls between two calibration
	 * points, it estimates using linear interpolation.
	 */
	private int getCalibratedLeftSpeed(int baseSpeed) {
		if (baseSpeed < MINIMUM_MOVING_SPEED) {
			return baseSpeed;
		}

		int index = (baseSpeed / 10) - 1;
		index = Math.max(0, Math.min(index, LEFT_WHEEL_SPEEDS.length - 1));

		if (baseSpeed % 10 == 0) {
			return LEFT_WHEEL_SPEEDS[index];
		}

		int upperIndex = Math.min(index + 1, LEFT_WHEEL_SPEEDS.length - 1);
		if (LEFT_WHEEL_SPEEDS[index] == -1) {
			return LEFT_WHEEL_SPEEDS[upperIndex];
		}
		double fraction = (baseSpeed % 10) / 10.0;
		return (int) Math.round(
				LEFT_WHEEL_SPEEDS[index] + fraction * (LEFT_WHEEL_SPEEDS[upperIndex] - LEFT_WHEEL_SPEEDS[index]));
	}

	/*
	 * Same as {@code getCalibratedLeftSpeed()} but uses the right wheel calibration
	 * table instead.
	 */
	private int getCalibratedRightSpeed(int baseSpeed) {
		if (baseSpeed < MINIMUM_MOVING_SPEED) {
			return baseSpeed;
		}

		int index = (baseSpeed / 10) - 1;
		index = Math.max(0, Math.min(index, RIGHT_WHEEL_SPEEDS.length - 1));

		if (baseSpeed % 10 == 0) {
			return RIGHT_WHEEL_SPEEDS[index];
		}

		int upperIndex = Math.min(index + 1, RIGHT_WHEEL_SPEEDS.length - 1);
		if (RIGHT_WHEEL_SPEEDS[index] == -1) {
			return RIGHT_WHEEL_SPEEDS[upperIndex];
		}
		double fraction = (baseSpeed % 10) / 10.0;
		return (int) Math.round(
				RIGHT_WHEEL_SPEEDS[index] + fraction * (RIGHT_WHEEL_SPEEDS[upperIndex] - RIGHT_WHEEL_SPEEDS[index]));
	}

	/*
	 * When custom per-wheel speeds are active but the system needs a different
	 * overall speed (like when passing a green light), this method scales one wheel
	 * proportionally so the left/right ratio stays the same.
	 */
	private int scaleCustomSpeed(int customSpeed, int baseSpeed) {
		int avgCustom = (customLeftSpeed + customRightSpeed) / 2;
		if (avgCustom == 0) {
			return baseSpeed;
		}
		double scaleFactor = (double) baseSpeed / avgCustom;
		int scaled = (int) Math.round(customSpeed * scaleFactor);
		if (scaled > 0 && scaled < MINIMUM_MOVING_SPEED && baseSpeed >= MINIMUM_MOVING_SPEED) {
			scaled = MINIMUM_MOVING_SPEED;
		}
		return scaled;
	}

	/*
	 * Checks if a speed will make the SwiftBot move. Anything below 40 will not
	 * work properly and anything above 100 is beyond the hardware limit.
	 */
	public static boolean isViableSpeed(int baseSpeed) {
		return baseSpeed >= MINIMUM_MOVING_SPEED && baseSpeed <= MAXIMUM_SPEED;
	}

	/*
	 * This method makes sure a speed value stays within -100 to 100 so we don't
	 * accidentally send a bad value to the motors.
	 */
	private int clampSpeed(int speed) {
		if (speed > 100) {
			return 100;
		}
		if (speed < -100) {
			return -100;
		}
		return speed;
	}
}