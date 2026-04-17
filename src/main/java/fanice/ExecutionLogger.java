import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class keeps track of everything that happens while the program is
 * running - every traffic light detected, every event logged and how long the
 * whole session lasted.
 *
 * It uses the Singleton pattern, so there's only ever one instance of this
 * class in the entire program. Every class that needs to log something grabs
 * the same instance through {@code getInstance()}, which means all the data
 * ends up in one place.
 *
 * It counts how many red, green and blue lights were detected, stores a
 * timestamped list of events in order and records when the program started and
 * stopped so it can calculate the total running time.
 *
 * Covers requirements: 31, 38, 39, 40, 41, 42, 43, 44, 50
 *
 * @author 2532744
 * @version 2.0
 */
public class ExecutionLogger {

	/** The file name where the log gets saved. */
	public static final String LOG_FILE_NAME = "traffic_log.txt";

	private static ExecutionLogger instance;
	private int redCount;
	private int greenCount;
	private int blueCount;
	private int totalLightCount;
	private final List<String> eventLog;
	private long startTime;
	private long endTime;
	private String apiVersion;

	/*
	 * Private constructor - nobody outside this class can create a new one. You
	 * have to go through {@code getInstance()} instead, that's the whole point of
	 * the Singleton pattern.
	 */
	private ExecutionLogger() {
		this.redCount = 0;
		this.greenCount = 0;
		this.blueCount = 0;
		this.totalLightCount = 0;
		this.eventLog = new ArrayList<>();
		this.startTime = 0;
		this.endTime = 0;
		this.apiVersion = "Unknown";
	}

	/*
	 * This method returns the one shared logger instance. If it doesn't exist yet,
	 * it creates one. The synchronized keyword stops two threads from accidentally
	 * making two instances at the same time.
	 */
	public static synchronized ExecutionLogger getInstance() {
		if (instance == null) {
			instance = new ExecutionLogger();
		}
		return instance;
	}

	/*
	 * This method stores the SwiftBotAPI version so it can be included in the
	 * execution log summary. Called once at the start of the program after the API
	 * has been initialised.
	 */
	public void setApiVersion(String version) {
		this.apiVersion = version;
	}

	/*
	 * Records the current time as when the program started. Called right before
	 * navigation begins.
	 */
	public void recordStartTime() {
		this.startTime = System.currentTimeMillis();
		logEvent("Program started");
	}

	/*
	 * Records the current time as when the program ended. Called when the user
	 * triggers termination.
	 */
	public void recordEndTime() {
		this.endTime = System.currentTimeMillis();
		logEvent("Program terminated");
	}

	/*
	 * This method works out how long the program ran for, in seconds. Returns 0 if
	 * the start or end time wasn't recorded.
	 */
	public long getDurationSeconds() {
		if (endTime == 0 || startTime == 0) {
			return 0;
		}
		return (endTime - startTime) / 1000;
	}

	/*
	 * This method formats a time in milliseconds into a readable clock format like
	 * "14:30:00". Used for displaying the start and end times in the execution log.
	 */
	private String formatTime(long timeMillis) {
		if (timeMillis == 0) {
			return "N/A";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(new Date(timeMillis));
	}

	/*
	 * This method adds an event to the log with a timestamp showing how many
	 * seconds have passed since the program started. For example, an event 45
	 * seconds in would look like: [045s] RED light detected
	 */
	public void logEvent(String message) {
		long elapsed = 0;
		if (startTime > 0) {
			elapsed = (System.currentTimeMillis() - startTime) / 1000;
		}
		String entry = String.format("[%03ds] %s", elapsed, message);
		eventLog.add(entry);
	}

	/*
	 * This method records that a traffic light was detected. It bumps up the
	 * counter for whatever colour it was and logs the event. If the colour is NONE
	 * (nothing detected), it does nothing.
	 */
	public void recordDetection(TrafficLight colour) {
		switch (colour) {
		case RED:
			redCount++;
			logEvent("RED light detected - count: " + redCount);
			break;
		case GREEN:
			greenCount++;
			logEvent("GREEN light detected - count: " + greenCount);
			break;
		case BLUE:
			blueCount++;
			logEvent("BLUE light detected - count: " + blueCount);
			break;
		default:
			return; // NONE - do nothing
		}
		totalLightCount++;
	}

	/*
	 * This method figures out which colour was detected the most. If there's a tie,
	 * red wins over green and green wins over blue - so there's a consistent
	 * tiebreaker. Returns NONE if no lights were detected at all.
	 */
	public TrafficLight getMostFrequentColour() {
		if (totalLightCount == 0) {
			return TrafficLight.NONE;
		}

		if (redCount >= greenCount && redCount >= blueCount) {
			return TrafficLight.RED;
		}
		if (greenCount >= redCount && greenCount >= blueCount) {
			return TrafficLight.GREEN;
		}
		return TrafficLight.BLUE;
	}

	/** @return how many times the most frequent colour was seen */
	public int getMostFrequentCount() {
		return Math.max(redCount, Math.max(greenCount, blueCount));
	}

	/*
	 * This method builds the full execution log as a formatted string. It includes
	 * the start time, end time, duration, SwiftBotAPI version, how many of each
	 * colour were detected, which colour was most frequent and a complete list of
	 * every event in order.
	 */
	public String generateLogString() {
		StringBuilder sb = new StringBuilder();

		sb.append("============================================================\n");
		sb.append("            EXECUTION LOG SUMMARY\n");
		sb.append("============================================================\n");
		sb.append("Run information:\n");
		sb.append("  Start time       : ").append(formatTime(startTime)).append("\n");
		sb.append("  End time         : ").append(formatTime(endTime)).append("\n");
		sb.append("  Duration         : ").append(getDurationSeconds()).append(" seconds\n");
		sb.append("  SwiftBotAPI version: ").append(apiVersion).append("\n");
		sb.append("------------------------------------------------------------\n");
		sb.append("Traffic light statistics:\n");
		sb.append("  Red              : ").append(redCount).append("\n");
		sb.append("  Green            : ").append(greenCount).append("\n");
		sb.append("  Blue             : ").append(blueCount).append("\n");
		sb.append("  Total            : ").append(totalLightCount).append("\n");
		sb.append("------------------------------------------------------------\n");

		TrafficLight mostFrequent = getMostFrequentColour();
		sb.append("Most frequent colour: ").append(mostFrequent.name());
		sb.append(" (").append(getMostFrequentCount()).append(" times)\n");
		sb.append("============================================================\n");

		sb.append("\n--- EVENT LOG ---\n");
		for (String event : eventLog) {
			sb.append(event).append("\n");
		}
		sb.append("============================================================\n");

		return sb.toString();
	}

	/*
	 * This method writes the full log to a file called 'traffic_log.txt' in
	 * whatever folder the program is running from. Returns true if it worked, false
	 * if something went wrong.
	 */
	public boolean saveToFile() {
		String logContent = generateLogString();
		try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME))) {
			writer.print(logContent);
			writer.flush();
			return true;
		} catch (Exception e) {
			System.out.println("ERROR: Failed to save log file.");
			e.printStackTrace();
			return false;
		}
	}

	/** @return how many red lights were detected */
	public int getRedCount() {
		return redCount;
	}

	/** @return how many green lights were detected */
	public int getGreenCount() {
		return greenCount;
	}

	/** @return how many blue lights were detected */
	public int getBlueCount() {
		return blueCount;
	}

	/** @return total number of traffic lights detected */
	public int getTotalLightCount() {
		return totalLightCount;
	}

	/*
	 * Returns a copy of the event log. It's a copy so nothing outside this class
	 * can accidentally mess with the original list.
	 */
	public List<String> getEventLog() {
		return new ArrayList<>(eventLog);
	}

	/*
	 * Wipes the singleton instance so a fresh one gets created next time {@code
	 * getInstance()} is called. Used when restarting for a new session so the old
	 * counts don't carry over.
	 */
	public static synchronized void resetInstance() {
		instance = null;
	}
}