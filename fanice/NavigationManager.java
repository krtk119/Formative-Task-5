import swiftbot.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the main navigation class - it runs the loop that keeps the SwiftBot
 * moving, scanning for traffic lights and reacting to whatever it finds.
 *
 * Each cycle of the loop takes a photo with the camera, reads the ultrasound
 * sensor and if there's a traffic light within 30cm it hands off to the right
 * response class (red, green or blue).
 *
 * Every 3 traffic lights the SwiftBot pauses and asks the user whether they
 * want to keep going or stop. The user can also press Button X at any time to
 * terminate.
 *
 * Covers requirements: 3, 6, 7, 15, 16, 17, 31, 32, 33, 34, 35, 42, 43, 44
 *
 * @author 2532744
 * @version 3.0
 */
public class NavigationManager {

	private final SwiftBotAPI swiftBot;
	private final MovementController movement;
	private final ColourDetector colourDetector;
	private final ExecutionLogger logger;
	private final UIDisplay display;

	/*
	 * Maps each traffic light colour to the class that handles it. Gets set up once
	 * in the constructor so we're not making new objects every time a light is
	 * spotted.
	 */
	private final Map<TrafficLight, LightResponseStrategy> responseStrategies;

	private int navigationSpeed;
	private int leftWheelVelocity;
	private int rightWheelVelocity;
	private volatile boolean navigationActive;
	private volatile boolean terminationRequested;
	private int scanInterval;
	private volatile String pendingButton;

	/*
	 * Sets everything up and pre-loads the strategy map with a response object for
	 * each colour so they are ready when needed.
	 */
	public NavigationManager(SwiftBotAPI swiftBot, MovementController movement, ColourDetector colourDetector,
			ExecutionLogger logger, UIDisplay display, int speed) {
		this.swiftBot = swiftBot;
		this.movement = movement;
		this.colourDetector = colourDetector;
		this.logger = logger;
		this.display = display;
		this.navigationSpeed = speed;
		this.leftWheelVelocity = speed;
		this.rightWheelVelocity = speed;
		this.navigationActive = false;
		this.terminationRequested = false;
		this.scanInterval = 500; // default scan interval in ms
		this.pendingButton = "";

		if (movement.isCustomWheelSpeedsActive()) {
			this.leftWheelVelocity = movement.getCustomLeftSpeed();
			this.rightWheelVelocity = movement.getCustomRightSpeed();
		}

		this.responseStrategies = new HashMap<>();
		this.responseStrategies.put(TrafficLight.RED, new RedLightResponse());
		this.responseStrategies.put(TrafficLight.GREEN, new GreenLightResponse());
		this.responseStrategies.put(TrafficLight.BLUE, new BlueLightResponse());
	}

	/*
	 * Starts the navigation loop and keeps it going until the user presses Button X
	 * or decides to stop at a checkpoint. Each pass takes a photo, checks the
	 * ultrasound and responds to any traffic light that's close enough. If
	 * something goes wrong mid-cycle it gets logged and the loop carries on.
	 */
	public void startNavigation() throws InterruptedException {
		navigationActive = true;

		if (movement.isCustomWheelSpeedsActive()) {
			display.showNavigationStartPerWheel(leftWheelVelocity, rightWheelVelocity);
			logger.logEvent("Navigation started with per-wheel speeds: Left=" + leftWheelVelocity + " Right="
					+ rightWheelVelocity);
		} else {
			display.showNavigationStart(navigationSpeed);
			logger.logEvent("Navigation started at speed " + navigationSpeed);
		}

		// Yellow underlights mean navigation is active
		int[] yellow = new int[] { 255, 255, 0 };
		swiftBot.fillUnderlights(yellow);

		// Let the user terminate at any point with Button X
		swiftBot.enableButton(Button.X, () -> {
			terminationRequested = true;
			logger.logEvent("Termination requested by user (Button X)");
		});

		movement.moveForward(navigationSpeed);

		while (navigationActive && !terminationRequested) {
			try {
				performNavigationCycle();
			} catch (Exception e) {
				logger.logEvent("Error during navigation: " + e.getMessage());
				e.printStackTrace();
			}
		}

		movement.stop();
		navigationActive = false;
		logger.logEvent("Navigation loop ended");
	}

	/*
	 * One pass through the loop: take a photo, check the distance and if there's a
	 * traffic light close enough, deal with it. Then wait for the scan interval
	 * before going again.
	 */
	private void performNavigationCycle() throws InterruptedException {
		TrafficLight detectedColour = colourDetector.detectColour();

		if (!colourDetector.wasLastCaptureSuccessful()) {
			logger.logEvent("Image capture failed - skipping this cycle");
			display.showCameraError();
		}

		double distanceToObject = readUltrasoundSafely();

		if (terminationRequested) {
			return;
		}

		// Only respond if the light is within 30cm
		if (detectedColour != TrafficLight.NONE && distanceToObject <= 30.0) {
			handleTrafficLightDetection(detectedColour, distanceToObject);
		}

		Thread.sleep(scanInterval);
	}

	/*
	 * Called when a traffic light is spotted within range. Stops the SwiftBot,
	 * shows the detection on screen, logs it, runs the right response strategy,
	 * checks if it's checkpoint time and then starts the SwiftBot moving again.
	 */
	private void handleTrafficLightDetection(TrafficLight detectedColour, double distanceToObject)
			throws InterruptedException {

		movement.stop();

		displayDetection(detectedColour, distanceToObject);

		logger.recordDetection(detectedColour);

		LightResponseStrategy strategy = responseStrategies.get(detectedColour);
		if (strategy != null) {
			strategy.respond(swiftBot, movement, distanceToObject, logger, display);
		}

		// Check if we've hit a checkpoint (every 3 lights)
		int totalLights = logger.getTotalLightCount();
		if (totalLights > 0 && totalLights % 3 == 0) {
			boolean continueNav = handleCheckpoint();
			if (!continueNav) {
				terminationRequested = true;
				return;
			}
		}

		if (!terminationRequested) {
			int[] yellow = new int[] { 255, 255, 0 };
			swiftBot.fillUnderlights(yellow);
			movement.moveForward(navigationSpeed);
		}
	}

	/*
	 * Shows the right detection screen depending on the colour. For green lights it
	 * also works out the pass speed to display.
	 */
	private void displayDetection(TrafficLight colour, double distanceToObject) {
		double avgR = colourDetector.getLastAvgRed();
		double avgG = colourDetector.getLastAvgGreen();
		double avgB = colourDetector.getLastAvgBlue();

		System.out.println("Traffic light detected at " + String.format("%.1f", distanceToObject) + " cm");
		System.out.println("Detected colour: " + colour.name());

		switch (colour) {
		case RED:
			display.showRedLightDetected(distanceToObject, avgR, avgG, avgB);
			break;
		case GREEN:
			int passSpeed = (int) Math.round(distanceToObject / 2.0);
			passSpeed = Math.max(30, Math.min(100, passSpeed));
			display.showGreenLightDetected(distanceToObject, passSpeed, avgR, avgG, avgB);
			break;
		case BLUE:
			display.showBlueLightDetected(distanceToObject, avgR, avgG, avgB);
			break;
		default:
			break;
		}
	}

	/*
	 * Pauses navigation and shows the checkpoint screen after every 3 traffic
	 * lights. The user presses Button A to keep going or Button X to stop.
	 */
	private boolean handleCheckpoint() throws InterruptedException {
		movement.stop();
		pendingButton = "";

		display.showCheckpointPrompt(logger.getTotalLightCount(), logger.getRedCount(), logger.getGreenCount(),
				logger.getBlueCount());
		logger.logEvent("Checkpoint reached - " + logger.getTotalLightCount() + " lights");

		swiftBot.disableButton(Button.X);

		swiftBot.enableButton(Button.A, () -> {
			pendingButton = "A";
		});

		swiftBot.enableButton(Button.X, () -> {
			pendingButton = "X";
		});

		// Wait for the user to pick a button
		while (pendingButton.isEmpty()) {
			Thread.sleep(100);
		}

		swiftBot.disableButton(Button.A);
		swiftBot.disableButton(Button.X);

		if ("A".equals(pendingButton)) {
			logger.logEvent("User chose to continue navigation");
			display.showInfo("Continuing navigation...");

			// Re-enable termination button
			swiftBot.enableButton(Button.X, () -> {
				terminationRequested = true;
				logger.logEvent("Termination requested by user (Button X)");
			});

			return true;
		} else {
			logger.logEvent("User chose to terminate at checkpoint");
			display.showInfo("Termination requested...");
			return false;
		}
	}

	/*
	 * Tries to get a distance reading from the ultrasound sensor. If it fails or
	 * the reading is over 1200 (which usually means the sensor couldn't bounce off
	 * anything), it returns 9999.0 instead of crashing.
	 */
	private double readUltrasoundSafely() {
		double distanceToObject = 0.0;
		try {
			distanceToObject = swiftBot.useUltrasound();
			if (distanceToObject > 1200) {
				return 9999.0;
			}
			return distanceToObject;
		} catch (Exception e) {
			display.showUltrasoundError();
			logger.logEvent("Ultrasound read failed");
			e.printStackTrace();
			return 9999.0;
		}
	}

	/** @return true if navigation is currently running */
	public boolean isNavigationActive() {
		return navigationActive;
	}

	/*
	 * Changes how often the SwiftBot scans for traffic lights. Must be between
	 * 100ms and 2000ms.
	 */
	public void setScanInterval(int intervalMs) {
		if (intervalMs >= 100 && intervalMs <= 2000) {
			this.scanInterval = intervalMs;
		} else {
			System.out.println("[WARN] Scan interval " + intervalMs
					+ " ms outside valid range (100-2000 ms). Using default (500 ms).");
			this.scanInterval = 500;
		}
	}
}