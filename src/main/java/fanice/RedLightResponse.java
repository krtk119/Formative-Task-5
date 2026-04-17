import swiftbot.*;

/**
 * This class deals with the red traffic light response. When the SwiftBot
 * detects a red light within range, it stops moving, sets the underlights to
 * red, waits for one second and then switches back to yellow before resuming
 * navigation.
 *
 * Covers requirements: 18, 19
 *
 * @author 2532744
 * @version 2.0
 */
public class RedLightResponse implements LightResponseStrategy {

	/*
	 * Steps for the red light response: 1. Set the underlights to red 2. Stop the
	 * SwiftBot 3. Wait 1 second 4. Set underlights back to yellow 5. Log that we're
	 * resuming
	 */
	@Override
	public void respond(SwiftBotAPI swiftBot, MovementController movement, double distanceToObject,
			ExecutionLogger logger, UIDisplay display) throws InterruptedException {

		// Set underlights to red and stop
		int[] red = new int[] { 255, 0, 0 };
		swiftBot.fillUnderlights(red);
		movement.stop();
		logger.logEvent("RED light - stopped at distance " + String.format("%.1f", distanceToObject) + " cm");

		// Wait for 1 second before resuming
		Thread.sleep(1000);

		// Switch back to yellow and carry on
		int[] yellow = new int[] { 255, 255, 0 };
		swiftBot.fillUnderlights(yellow);
		logger.logEvent("RED light - resuming navigation");
		display.showInfo("Red light stop complete - resuming navigation.");
	}

	/** @return the name of this strategy for logging */
	@Override
	public String getStrategyName() {
		return "Red Light Response";
	}
}