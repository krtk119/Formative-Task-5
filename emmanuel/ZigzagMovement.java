import swiftbot.*;

/**
 * ZigzagMovement: Parent class for zigzag section movements.
 * Contains all shared methods: moveForward, turn90, setLEDColour,
 * turnOffLEDs, getRGB, and calculateMoveTime.
 * OddSection and EvenSection extend this class.
 */
public class ZigzagMovement {

    protected SwiftBotAPI swiftBot;

    public ZigzagMovement(SwiftBotAPI swiftBot) {
        this.swiftBot = swiftBot;
    }

  

    public int calculateMoveTime(int length, int speed) {
        double time = (length / 50.0) * 1000.0 * (100.0 / speed);
        return (int) time;
    }

    public void moveForward(int length, int speed) {
        int time = calculateMoveTime(length, speed);
        swiftBot.move(speed, speed, time);
    }

    public void turn90(String direction, int speed) {
        int turnTime = 475;

        if (direction.equals("RIGHT")) {
            swiftBot.move(speed, -speed, turnTime);
        } else {
            swiftBot.move(-speed, speed, turnTime);
        }
    }

    public void turn180(int speed) {
        System.out.println(">>> Turning 180 degrees...");
        int turnTime = 950;
        swiftBot.move(speed, -speed, turnTime);
    }



    public int[] getRGB(String colourName) {
        switch (colourName.toUpperCase()) {
            case "RED":     return new int[]{255, 0, 0};
            case "GREEN":   return new int[]{0, 255, 0};
            case "BLUE":    return new int[]{0, 0, 255};
            case "YELLOW":  return new int[]{255, 255, 0};
            case "WHITE":   return new int[]{255, 255, 255};
            case "CYAN":    return new int[]{0, 255, 255};
            case "MAGENTA": return new int[]{255, 0, 255};
            default:        return new int[]{0, 255, 0};
        }
    }

    public void setLEDColour(String colourName) {
        int[] rgb = getRGB(colourName);
        swiftBot.fillUnderlights(rgb);
    }

    public void turnOffLEDs() {
        swiftBot.disableUnderlights();
    }
}