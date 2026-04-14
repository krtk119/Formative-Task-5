import swiftbot.*;
import java.awt.image.BufferedImage;

// this class handles all the swiftbot movement so I dont have to
// repeat swiftbot api calls everywhere in my code
public class SwiftBotController {

    // calibration values - update these after testing with the robot
    private static final int SPEED = 50;
    private static final double MS_PER_CM = 60.0;
    private static final int TURN_90_MS = 300;

    private SwiftBotAPI swiftBot;

    // track X button press via callback instead of polling isPressed()
    private volatile boolean xPressed = false;

    public SwiftBotController(SwiftBotAPI swiftBot) {
        this.swiftBot = swiftBot;

        // only register the button if we have a real SwiftBot (null = mock/Eclipse mode)
        if (swiftBot != null) {
            swiftBot.enableButton(Button.X, () -> {
                xPressed = true;
                System.out.println("\n=========================");
                System.out.println("X pressed - stopping...");
                System.out.println("=========================");
            });
        }
    }

    // moves forward a given distance in cm
    public void moveForward(double cm) {
        int ms = (int)(cm * MS_PER_CM);
        swiftBot.startMove(SPEED, SPEED);
        sleep(ms);
        swiftBot.stopMove();
    }

    // moves backward a given distance in cm
    public void moveBackward(double cm) {
        int ms = (int)(cm * MS_PER_CM);
        swiftBot.startMove(-SPEED, -SPEED);
        sleep(ms);
        swiftBot.stopMove();
    }

    // turns right by a given number of degrees
    public void turnRight(double degrees) {
        int ms = (int)((degrees / 90.0) * TURN_90_MS);
        swiftBot.startMove(SPEED, -SPEED);
        sleep(ms);
        swiftBot.stopMove();
    }

    // turns left by a given number of degrees
    public void turnLeft(double degrees) {
        int ms = (int)((degrees / 90.0) * TURN_90_MS);
        swiftBot.startMove(-SPEED, SPEED);
        sleep(ms);
        swiftBot.stopMove();
    }

    // moves back 15cm to get ready for the next shape
    public void repositionForNextShape() {
        moveBackward(15);
        sleep(300);
    }

    // blinks blue 3 times to confirm a QR code has been scanned
    public void blinkBlue() {
        for (int i = 0; i < 3; i++) {
            swiftBot.fillUnderlights(new int[]{0, 0, 200});
            sleep(300);
            swiftBot.disableUnderlights();
            sleep(300);
        }
    }

    // blinks green 3 times to show shape is done
    public void blinkGreen() {
        for (int i = 0; i < 3; i++) {
            swiftBot.fillUnderlights(new int[]{0, 200, 0});
            sleep(300);
            swiftBot.disableUnderlights();
            sleep(300);
        }
    }

    // blinks red 3 times to show invalid QR code
    public void blinkRed() {
        for (int i = 0; i < 3; i++) {
            swiftBot.fillUnderlights(new int[]{200, 0, 0});
            sleep(300);
            swiftBot.disableUnderlights();
            sleep(300);
        }
    }

    public void setLightsGreen() {
        swiftBot.fillUnderlights(new int[]{0, 200, 0});
    }

    // steady yellow light used during countdown
    public void setLightsYellow() {
        swiftBot.fillUnderlights(new int[]{200, 150, 0});
    }

    public void lightsOff() {
        swiftBot.disableUnderlights();
    }

    // waits until the swiftbot camera sees a valid QR code then returns the text
    // returns null immediately if X is pressed while waiting
    public String scanQRCode() {
        System.out.println("Hold a QR code in front of the camera...");

        String result = null;
        while (result == null || result.isBlank()) {

            // stop waiting if X is pressed
            if (xPressed) {
                return null;
            }

            try {
                // use the built-in SwiftBot API to get image and decode QR
                BufferedImage img = swiftBot.getQRImage();
                result = swiftBot.decodeQRImage(img);
            } catch (Exception e) {
                // no QR found yet, keep trying
            }

            if (result == null || result.isBlank()) {
                sleep(500);
            }
        }

        return result.trim();
    }

    // checks if button X has been pressed
    public boolean isXPressed() {
        return xPressed;
    }

    // helper to make Thread.sleep less annoying to write
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
