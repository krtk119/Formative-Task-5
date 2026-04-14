import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
 
// Tracks journey data, writes the log file, and shows the summary
public class JourneyLogger {
 
    private int journeyCount;
    private ArrayList<double[]> journeyHistory;
 
    public JourneyLogger() {
        this.journeyCount = 0;
        this.journeyHistory = new ArrayList<>();
    }
 
    // Pythagorean theorem to get straight line distance
    public double calculateStraightLine(int length, int sections) {
        double horizontal = length * (sections / 2.0);
        double vertical = length * (sections / 2.0);
        return Math.sqrt((horizontal * horizontal) + (vertical * vertical));
    }
 
    // Saves journey data to the ArrayList using actual measured duration
    public void saveJourneyData(int sectionLength, int sectionCount, int wheelSpeed, double actualDuration) {
        journeyCount++;
        int totalDistance = sectionLength * sectionCount;
        double straightLine = calculateStraightLine(sectionLength, sectionCount);
        double[] journey = new double[]{sectionLength, sectionCount, wheelSpeed, totalDistance, straightLine, actualDuration};
        journeyHistory.add(journey);
    }
 
    // Writes all journey data to zigzag_log.txt
    public void writeLogFile() {
        try {
            FileWriter writer = new FileWriter("zigzag_log.txt");
 
            writer.write("SWIFTBOT ZIGZAG LOG FILE\n");
            writer.write("========================\n\n");
            writer.write("Total Journeys: " + journeyCount + "\n\n");
 
            for (int i = 0; i < journeyHistory.size(); i++) {
                double[] journey = journeyHistory.get(i);
                writer.write("Journey " + (i + 1) + ":\n");
                writer.write("  Section Length: " + (int)journey[0] + " cm\n");
                writer.write("  Sections: " + (int)journey[1] + "\n");
                writer.write("  Wheel Speed: " + (int)journey[2] + "\n");
                writer.write("  Total Path Length (Start to End): " + (int)journey[3] + " cm\n");
                writer.write("  Duration (Start to End): " + String.format("%.2f", journey[5]) + " seconds\n");
                writer.write("  Straight Line Distance: " + String.format("%.2f", journey[4]) + " cm\n\n");
            }
 
            writer.close();
            System.out.println("  [SUCCESS] Log saved to zigzag_log.txt");
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not write log file.");
        }
    }
 
    // Shows stats after each journey
    public void displayJourneyComplete(int sectionLength, int sectionCount, int wheelSpeed,
            String oddColour, String evenColour, double actualDuration) {
 
        int totalDistance = sectionLength * sectionCount;
        double straightLine = calculateStraightLine(sectionLength, sectionCount);
 
        System.out.println();
        System.out.println("                    JOURNEY COMPLETE");
        System.out.println("  ****************************************************************");
        System.out.println("  [SUCCESS] SWIFTBOT HAS RETURNED TO THE STARTING POSITION!");
        System.out.println("  [SUCCESS] LEDs TURNED OFF.");
        System.out.println("  [SUCCESS] JOURNEY DATA SAVED TO LOG FILE.");
        System.out.println();
        System.out.println("  ****************************************************************");
        System.out.println("                    JOURNEY STATISTICS");
        System.out.println("  ****************************************************************");
        System.out.println("  TOTAL PATH LENGTH:        " + totalDistance + " CM");
        System.out.println("  STRAIGHT LINE DISTANCE:   " + String.format("%.2f", straightLine) + " CM");
        System.out.println("  FORWARD DURATION:         " + String.format("%.2f", actualDuration) + " SECONDS");
        System.out.println("  ****************************************************************");
        System.out.println();
        System.out.println("  >>> PRESS Y TO SCAN ANOTHER QR CODE");
        System.out.println("  >>> PRESS X TO EXIT PROGRAM");
        System.out.println();
    }
 
    // Shows the final summary when the program ends
    public void displaySummary() {
        System.out.println();
        System.out.println("  ****************************************************************");
        System.out.println("                      PROGRAM SUMMARY");
        System.out.println("  ****************************************************************");
        System.out.println("                    OVERALL STATISTICS");
        System.out.println("  ****************************************************************");
        System.out.println("  TOTAL JOURNEYS COMPLETED: " + journeyCount);
        System.out.println("  ****************************************************************");
        System.out.println();
 
        if (journeyHistory.size() > 0) {
 
            System.out.println("                    ALL JOURNEY DETAILS");
            System.out.println("  ****************************************************************");
            System.out.println("  JOURNEY | LENGTH  | SECTIONS | SPEED | STRAIGHT DISTANCE");
            System.out.println("  ****************************************************************");
 
            for (int i = 0; i < journeyHistory.size(); i++) {
                double[] journey = journeyHistory.get(i);
                System.out.println("     " + (i + 1) + "    |  " + (int)journey[0] + " CM  |    " + (int)journey[1] + "     |   " + (int)journey[2] + "  |  " + String.format("%.2f", journey[4]) + " CM");
            }
            System.out.println("  ****************************************************************");
            System.out.println();
 
            // Find longest and shortest straight line distance
            double longestStraight = 0;
            double shortestStraight = Double.MAX_VALUE;
            int longestJourney = 0;
            int shortestJourney = 0;
 
            for (int i = 0; i < journeyHistory.size(); i++) {
                double[] journey = journeyHistory.get(i);
                double straightLine = journey[4];
 
                if (straightLine > longestStraight) {
                    longestStraight = straightLine;
                    longestJourney = i + 1;
                }
                if (straightLine < shortestStraight) {
                    shortestStraight = straightLine;
                    shortestJourney = i + 1;
                }
            }
 
            double[] longest = journeyHistory.get(longestJourney - 1);
            double[] shortest = journeyHistory.get(shortestJourney - 1);
 
            System.out.println("  [SUCCESS] LONGEST STRAIGHT-LINE DISTANCE");
            System.out.println("  JOURNEY " + longestJourney + ": " + (int)longest[0] + "CM X " + (int)longest[1] + " SECTIONS = " + String.format("%.2f", longestStraight) + " CM");
            System.out.println("  ****************************************************************");
            System.out.println("  [WARNING] SHORTEST STRAIGHT-LINE DISTANCE");
            System.out.println("  JOURNEY " + shortestJourney + ": " + (int)shortest[0] + "CM X " + (int)shortest[1] + " SECTIONS = " + String.format("%.2f", shortestStraight) + " CM");
            System.out.println("  ****************************************************************");
        }
 
        System.out.println();
        System.out.println("  LOG FILE SAVED AT: ZIGZAG_LOG.TXT");
        System.out.println("  THANK YOU FOR USING THE SWIFTBOT ZIGZAG PROGRAM!");
        System.out.println("  GOODBYE!");
        System.out.println();
    }
 
    public int getJourneyCount() {
        return journeyCount;
    }
}
 