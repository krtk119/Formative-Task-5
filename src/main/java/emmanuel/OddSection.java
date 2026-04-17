import swiftbot.*;

// Handles odd-numbered sections (1, 3, 5...)
// GREEN LEDs by default, turns RIGHT after moving forward
public class OddSection extends ZigzagMovement {

    private String colour;

    public OddSection(SwiftBotAPI swiftBot) {
        super(swiftBot);
        this.colour = "GREEN";
    }

    public OddSection(SwiftBotAPI swiftBot, String customColour) {
        super(swiftBot);
        this.colour = customColour;
    }

    // Forward: LED on, move, pause 1s, turn right
    public void executeForward(int sectionNum, int totalSections, int length, int speed) {
        setLEDColour(colour);
        System.out.println("  Section " + sectionNum + " of " + totalSections + "  [" + colour + "]  Moving forward...");
        moveForward(length, speed);
        System.out.println("  [SUCCESS] Section " + sectionNum + " complete.");

        if (sectionNum < totalSections) {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            System.out.println("  Turning 90 degrees RIGHT...");
            turn90("RIGHT", speed);
        }
        System.out.println();
    }

    // Retrace: opposite direction - turn RIGHT  instead
    public void executeRetrace(int sectionNum, int length, int speed) {
        setLEDColour(colour);
        System.out.println("  Retracing Section " + sectionNum + "  [" + colour + "]  Moving...");
        moveForward(length, speed);
        System.out.println("  [SUCCESS] Section " + sectionNum + " retraced.");

        if (sectionNum > 1) {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            System.out.println("  Turning 90 degrees RIGHT...");
            turn90("RIGHT", speed);
        }
        System.out.println();
    }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
}
