/**
 * This enum holds the different traffic light colours that the SwiftBot can
 * pick up while navigating. Each value corresponds to a colour the camera
 * detects and the system uses it to decide what the SwiftBot should do next. If
 * no colour is strong enough, it returns NONE and the SwiftBot carries on as
 * normal.
 * 
 * Covers requirements: 11, 12, 13, 14
 * 
 * @author 2532744
 * @version 2.0
 */

public enum TrafficLight {

	/** Red traffic light - requires the SwiftBot to stop. */
	RED,

	/**
	 * Green traffic light - requires the SwiftBot to proceed at the calculated or
	 * chosen speed.
	 */
	GREEN,

	/**
	 * Blue traffic light - requires the SwiftBot to perform a yield manoeuvre -
	 * which makes the SwiftBot briefly pull aside and return to its original path.
	 */
	BLUE,

	/** No dominant colour detected - no action required. */
	NONE

}