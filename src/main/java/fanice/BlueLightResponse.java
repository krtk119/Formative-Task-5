package fanice;
import swiftbot.*;

/**
 * This class deals with the blue traffic light response. Blue means yield - so
 * the SwiftBot has to pull aside and then come back.
 *
 * The full sequence is: stop and wait 1 second, blink the underlights blue 3
 * times, turn left 90 degrees, check for obstacles, move forward for 1 second,
 * stop and wait, reverse back, then turn right 90 degrees to face the original
 * direction again.
 *
 * If an obstacle is detected after turning left, the whole thing gets cancelled
 * and the SwiftBot turns back to where it was facing before.
 *
 * Covers requirements: 23, 24, 25, 26, 27, 28, 29, 30, 53
 *
 * @author 2532744
 * @version 2.0
 */
public class BlueLightResponse implements LightResponseStrategy {

	/*
	 * Runs the full yield manoeuvre as described in requirements 23 to 30. If
	 * there's an obstacle after turning left, the forward/reverse bit gets skipped
	 * and the SwiftBot turns back.
	 */
	@Override
	public void respond(SwiftBotAPI swiftBot, MovementController movement, double distanceToObject,
			ExecutionLogger logger, UIDisplay display) throws InterruptedException {

		logger.logEvent("BLUE light - beginning yield manoeuvre");

		// Stop and wait for 1 second
		movement.stop();
		Thread.sleep(1000);

		// Blink blue three times
		blinkBlue(swiftBot);
		logger.logEvent("BLUE light - blink sequence complete");

		// Turn left 90 degrees
		movement.turnLeft90();
		logger.logEvent("BLUE light - turned left 90 degrees");

		// Check for obstacles after turning
		double obstacleDistance = readUltrasoundSafely(swiftBot);

		if (obstacleDistance <= 30.0 && obstacleDistance > 0) {
			display.showYieldAborted();
			logger.logEvent(
					"BLUE light - obstacle at " + String.format("%.1f", obstacleDistance) + " cm. Yield aborted.");

			// Turn back to original heading and resume
			movement.turnRight90();
			logger.logEvent("BLUE light - returned to original heading after abort");

			int[] yellow = new int[] { 255, 255, 0 };
			swiftBot.fillUnderlights(yellow);
			display.showInfo("Yield aborted - resuming navigation.");
			return;
		}

		// Move forward for 1 second at speed 40
		movement.moveForwardTimed(40, 1000);
		logger.logEvent("BLUE light - moved forward");

		// Stop and wait for 1 second
		movement.stop();
		Thread.sleep(1000);

		// Reverse for 1 second to retrace the path
		movement.moveBackwardTimed(40, 1000);
		logger.logEvent("BLUE light - retraced path");

		// Turn right 90 degrees to return to original heading
		movement.turnRight90();
		logger.logEvent("BLUE light - returned to original heading");

		// All done - stop, go back to yellow, let the user know
		movement.stop();
		int[] yellow = new int[] { 255, 255, 0 };
		swiftBot.fillUnderlights(yellow);
		display.showYieldComplete();
		logger.logEvent("BLUE light - yield manoeuvre complete");
	}

	/*
	 * Flashes the underlights blue 3 times. Each blink is 300ms on then 300ms off.
	 * After all the blinks are done, the lights stay on solid blue.
	 */
	private void blinkBlue(SwiftBotAPI swiftBot) throws InterruptedException {
		int[] blue = new int[] { 0, 0, 255 };
		int[] off = new int[] { 0, 0, 0 };

		for (int i = 0; i < 3; i++) {
			swiftBot.fillUnderlights(blue);
			Thread.sleep(300);
			swiftBot.fillUnderlights(off);
			Thread.sleep(300);
		}
		// Leave the lights on solid blue after blinking
		swiftBot.fillUnderlights(blue);
	}

	/*
	 * Tries to read the ultrasound sensor. If it fails for whatever reason, it
	 * returns 9999.0 so the yield keeps going rather than aborting because of a bad
	 * sensor reading.
	 */
	private double readUltrasoundSafely(SwiftBotAPI swiftBot) {
		try {
			return swiftBot.useUltrasound();
		} catch (Exception e) {
			System.out.println("ERROR: Ultrasound read failed during yield.");
			e.printStackTrace();
			return 9999.0;
		}
	}

	/** @return the name of this strategy for logging */
	@Override
	public String getStrategyName() {
		return "Blue Light Yield Response";
	}
}