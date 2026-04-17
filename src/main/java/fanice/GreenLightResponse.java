package fanice;
import swiftbot.*;

/**
 * This class deals with the green traffic light response. When a green light is
 * spotted within range, the SwiftBot works out how fast it needs to go to pass
 * the light within 2 seconds by dividing the distance by 2. If the speed comes
 * out below 30 it gets set to 30 and if it's above 100 it gets capped at 100.
 * Then the SwiftBot drives forward for 2 seconds at that speed, stops for 1
 * second and goes back to normal navigation.
 *
 * Covers requirements: 20, 21, 22
 *
 * @author 2532744
 * @version 2.0
 */
public class GreenLightResponse implements LightResponseStrategy {

	/*
	 * Steps for the green light response: 1. Set underlights to green 2. Work out
	 * the pass speed: distance / 2, clamped between 30 and 100 3. Drive forward at
	 * that speed for 2 seconds 4. Stop and wait for 1 second 5. Set underlights
	 * back to yellow
	 */
	@Override
	public void respond(SwiftBotAPI swiftBot, MovementController movement, double distanceToObject,
			ExecutionLogger logger, UIDisplay display) throws InterruptedException {

		// Set underlights to green
		int[] green = new int[] { 0, 255, 0 };
		swiftBot.fillUnderlights(green);

		// Calculate the speed needed to pass the light in 2 seconds
		int passSpeed = (int) Math.round(distanceToObject / 2.0);

		if (passSpeed < 30) {
			passSpeed = 30;
		}
		if (passSpeed > 100) {
			passSpeed = 100;
		}

		logger.logEvent("GREEN light - passing at speed " + passSpeed + " (distance: "
				+ String.format("%.1f", distanceToObject) + " cm)");

		// Move forward for 2 seconds at the calculated speed
		movement.moveForwardTimed(passSpeed, 2000);

		// Stop and wait for 1 second
		movement.stop();
		Thread.sleep(1000);

		// Switch back to yellow and carry on
		int[] yellow = new int[] { 255, 255, 0 };
		swiftBot.fillUnderlights(yellow);
		logger.logEvent("GREEN light - passed. Resuming navigation.");
		display.showInfo("Passed traffic light - resuming.");
	}

	/** @return the name of this strategy for logging */
	@Override
	public String getStrategyName() {
		return "Green Light Response";
	}
}