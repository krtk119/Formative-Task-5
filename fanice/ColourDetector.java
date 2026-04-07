import swiftbot.*;

import java.awt.image.BufferedImage;

/**
 * This class takes photos using the SwiftBot's camera and works out what colour
 * traffic light is in front of it.
 *
 * It grabs a 720x720 image, looks at a 100x100 pixel square right in the centre
 * and adds up all the red, green and blue values across those pixels. Whichever
 * colour channel has the highest average - and beats the other two by more than
 * the threshold (default 30) - is the one that gets returned. If nothing stands
 * out enough, it returns NONE.
 *
 * Covers requirements: 7, 8, 9, 10, 11, 12, 13, 14, 52, 53
 *
 * @author 2532744
 * @version 3.0
 */
public class ColourDetector {

	/** The gap a colour needs to beat the others by. */
	public static final int DEFAULT_THRESHOLD = 30;

	private final SwiftBotAPI swiftBot;
	private int colourThreshold;
	private double lastAvgRed;
	private double lastAvgGreen;
	private double lastAvgBlue;
	private boolean lastCaptureSuccessful;

	/* Sets up a ColourDetector with the default threshold of 30. */
	public ColourDetector(SwiftBotAPI swiftBot) {
		this.swiftBot = swiftBot;
		this.colourThreshold = DEFAULT_THRESHOLD;
		this.lastAvgRed = 0;
		this.lastAvgGreen = 0;
		this.lastAvgBlue = 0;
		this.lastCaptureSuccessful = true;
	}

	/*
	 * Sets up a ColourDetector with a custom threshold. If the value is outside the
	 * valid range (10 to 100), it falls back to the default of 30 and prints a
	 * warning.
	 */
	public ColourDetector(SwiftBotAPI swiftBot, int threshold) {
		this(swiftBot);
		if (threshold >= 10 && threshold <= 100) {
			this.colourThreshold = threshold;
		} else {
			System.out.println("[WARN] Threshold " + threshold + " outside valid range (10-100). Using default ("
					+ DEFAULT_THRESHOLD + ").");
			this.colourThreshold = DEFAULT_THRESHOLD;
		}
	}

	/*
	 * Takes a photo and figures out what colour is in the middle of it. If the
	 * camera fails, it marks the capture as unsuccessful and returns NONE so the
	 * rest of the program can keep going.
	 */
	public TrafficLight detectColour() {
		BufferedImage image = captureImage();

		if (image == null) {
			lastCaptureSuccessful = false;
			return TrafficLight.NONE;
		}

		lastCaptureSuccessful = true;
		return analyseImage(image);
	}

	/*
	 * Tries to take a 720x720 photo with the camera. If something goes wrong it
	 * prints the error and returns null rather than crashing the whole program (Req
	 * 7).
	 */
	private BufferedImage captureImage() {
		try {
			return swiftBot.takeStill(ImageSize.SQUARE_720x720);
		} catch (Exception e) {
			System.out.println("ERROR: Camera capture failed.");
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * Looks at the 100x100 pixel region in the centre of the image, totals up the
	 * RGB values and works out the averages. Then it decides which colour (if any)
	 * is dominant.
	 */
	private TrafficLight analyseImage(BufferedImage image) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		// Work out where the centre 100x100 region starts
		int startX = (imageWidth - 100) / 2;
		int startY = (imageHeight - 100) / 2;

		// Make sure the image is big enough
		if (startX < 0 || startY < 0 || startX + 100 > imageWidth || startY + 100 > imageHeight) {
			System.out.println("[WARN] Image too small for sample region.");
			return TrafficLight.NONE;
		}

		long totalRed = 0;
		long totalGreen = 0;
		long totalBlue = 0;
		int pixelCount = 0;

		// Loop through every pixel in the sample region
		for (int y = startY; y < startY + 100; y++) {
			for (int x = startX; x < startX + 100; x++) {
				int rgb = image.getRGB(x, y);

				// Pull out the individual colour channels
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;

				totalRed += red;
				totalGreen += green;
				totalBlue += blue;
				pixelCount++;
			}
		}

		// Work out the averages
		lastAvgRed = (double) totalRed / pixelCount;
		lastAvgGreen = (double) totalGreen / pixelCount;
		lastAvgBlue = (double) totalBlue / pixelCount;

		return classifyColour(lastAvgRed, lastAvgGreen, lastAvgBlue);
	}

	/*
	 * Decides which colour wins. A colour only counts as dominant if it beats BOTH
	 * of the other two by more than the threshold. If nothing stands out enough, it
	 * returns NONE.
	 */
	private TrafficLight classifyColour(double avgRed, double avgGreen, double avgBlue) {
		// Red wins if it beats both green and blue by the threshold
		if (avgRed > avgGreen + colourThreshold && avgRed > avgBlue + colourThreshold) {
			return TrafficLight.RED;
		}

		// Green wins if it beats both red and blue by the threshold
		if (avgGreen > avgRed + colourThreshold && avgGreen > avgBlue + colourThreshold) {
			return TrafficLight.GREEN;
		}

		// Blue wins if it beats both red and green by the threshold
		if (avgBlue > avgRed + colourThreshold && avgBlue > avgGreen + colourThreshold) {
			return TrafficLight.BLUE;
		}

		// Nothing stood out enough
		return TrafficLight.NONE;
	}

	/** @return the average red from the last photo */
	public double getLastAvgRed() {
		return lastAvgRed;
	}

	/** @return the average green from the last photo */
	public double getLastAvgGreen() {
		return lastAvgGreen;
	}

	/** @return the average blue from the last photo */
	public double getLastAvgBlue() {
		return lastAvgBlue;
	}

	/** @return the current colour detection threshold */
	public int getColourThreshold() {
		return colourThreshold;
	}

	/*
	 * Lets you check if the last camera capture worked. The navigation manager uses
	 * this to decide whether to log a camera error.
	 */
	public boolean wasLastCaptureSuccessful() {
		return lastCaptureSuccessful;
	}

	/*
	 * Changes the colour detection threshold. If the new value is outside the valid
	 * range (10 to 100), it keeps the old one and prints a warning instead (Req
	 * 52).
	 */
	public void setColourThreshold(int threshold) {
		if (threshold >= 10 && threshold <= 100) {
			this.colourThreshold = threshold;
		} else {
			System.out.println("[WARN] Threshold " + threshold + " outside valid range. Keeping current value ("
					+ this.colourThreshold + ").");
		}
	}
}