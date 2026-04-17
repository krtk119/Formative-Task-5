package fanice;
import swiftbot.*;

/**
 * This interface is used so that each traffic light colour has its own class
 * for handling the response. Instead of having one big if-else chain in the
 * navigation loop, we use the Strategy pattern - the right response class gets
 * picked at runtime depending on what colour the camera detected.
 *
 * Supports requirements: 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
 * 
 * @author 2532744
 * @version 1.0
 */

public interface LightResponseStrategy {

	/**
	 * Runs whatever the SwiftBot needs to do for this particular traffic light
	 * colour - stopping, changing underlights, moving, etc.
	 * 
	 * @param swiftBot         the SwiftBotAPI used to control the SwiftBot
	 * @param movement         handles wheel speeds and turning
	 * @param distanceToObject how far away the traffic light is, in cm
	 * @param logger           records what happened during the run
	 * @param display          prints messages to the console
	 * @throws InterruptedException if the SwiftBot gets interrupted mid-movement
	 */
	void respond(SwiftBotAPI swiftBot, MovementController movement, double distanceToObject, ExecutionLogger logger,
			UIDisplay display) throws InterruptedException;

	/**
	 * Returns the name of this response strategy for logging.
	 * 
	 * @return the strategy name, e.g. "Red Light Response"
	 */
	String getStrategyName();
}