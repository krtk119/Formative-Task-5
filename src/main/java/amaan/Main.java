import swiftbot.*;
import java.util.ArrayList;
import java.util.Scanner;

// this is where the program starts
// it keeps asking for QR codes and drawing shapes until the user presses X
public class Main {

    // detects if we are running on a Raspberry Pi
    private static boolean isRunningOnPi() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return os.contains("linux") && (arch.contains("arm") || arch.contains("aarch"));
    }

    public static void main(String[] args) {

        SwiftBotController controller;
        QRCodeParser parser = new QRCodeParser();
        Logger logger = new Logger();

        if (isRunningOnPi()) {
            // running on the actual SwiftBot robot
            SwiftBotAPI swiftBot;
            try {
                swiftBot = SwiftBotAPI.INSTANCE;
            } catch (Exception e) {
                System.out.println("Error: could not connect to SwiftBot. " + e.getMessage());
                return;
            }
            controller = new SwiftBotController(swiftBot);
        } else {
            // running in Eclipse on a PC - use mock controller
            // passing null avoids touching SwiftBotAPI or Button at all
            System.out.println("[INFO] Not running on Raspberry Pi - using mock mode.");
            System.out.println("[INFO] Type QR codes in the console to simulate scanning.");
            System.out.println("[INFO] Type 'x' and press Enter to simulate the X button.");
            System.out.println("");

            controller = new SwiftBotController(null) {

                private volatile boolean xPressed = false;
                private final Scanner mockScanner = new Scanner(System.in);

                {
                    // background thread listens for 'x' typed in Eclipse console
                    Thread t = new Thread(() -> {
                        while (mockScanner.hasNextLine()) {
                            if (mockScanner.nextLine().trim().equalsIgnoreCase("x")) {
                                xPressed = true;
                                System.out.println("\n=========================");
                                System.out.println("X pressed - stopping...");
                                System.out.println("=========================");
                                break;
                            }
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }

                @Override
                public void moveForward(double cm) {
                    System.out.println("[MOCK] Move forward " + cm + " cm");
                }

                @Override
                public void moveBackward(double cm) {
                    System.out.println("[MOCK] Move backward " + cm + " cm");
                }

                @Override
                public void turnRight(double degrees) {
                    System.out.println("[MOCK] Turn right " + degrees + " degrees");
                }

                @Override
                public void turnLeft(double degrees) {
                    System.out.println("[MOCK] Turn left " + degrees + " degrees");
                }

                @Override
                public void repositionForNextShape() {
                    System.out.println("[MOCK] Repositioning for next shape");
                }

                @Override
                public void blinkBlue() {
                    System.out.println("[MOCK] Blink blue");
                }

                @Override
                public void blinkGreen() {
                    System.out.println("[MOCK] Blink green");
                }

                @Override
                public void blinkRed() {
                    System.out.println("[MOCK] Blink red");
                }

                @Override
                public void setLightsGreen() {
                    System.out.println("[MOCK] Lights green");
                }

                @Override
                public void setLightsYellow() {
                    System.out.println("[MOCK] Lights yellow");
                }

                @Override
                public void lightsOff() {
                    System.out.println("[MOCK] Lights off");
                }

                @Override
                public String scanQRCode() {
                    System.out.print("[MOCK] Enter QR code (e.g. S:30 or T:16:30:24): ");
                    return mockScanner.nextLine().trim();
                }

                @Override
                public boolean isXPressed() {
                    return xPressed;
                }
            };
        }

        // ASCII art header
        System.out.println(" ____  _____    ___   _    _     ____  _   _  _   _  ____  _____  ___ ");
        System.out.println("|  _ \\|  __ \\  / _ \\ | |  | |   / ___|| | | || | | ||  _ \\|_   _||__ \\");
        System.out.println("| | | | |__) || |_| || |  | |   \\___ \\| |_| || |_| || |_) | | |    ) |");
        System.out.println("| |_| |  _  / |  _  || |/\\| |    ___) ||  _  ||  _  ||  __/  | |   / / ");
        System.out.println("|____/|_| \\_\\ |_| |_||__/\\__|   |____/ |_| |_||_| |_||_|     |_|  /_/  ");
        System.out.println("");
        System.out.println("  Task 6: Draw Shape  |  Press X at any time to quit");
        System.out.println("=========================================================================");
        System.out.println("");

        // main loop - keeps going until user presses X
        while (!controller.isXPressed()) {

            System.out.println("Scan a QR code to draw a shape (or press X to quit).");

            pause(1000);

            // check in case they pressed X while we were waiting
            if (controller.isXPressed()) {
                break;
            }

            // scan the qr code
            String qrText = controller.scanQRCode();

            // user may have pressed X while waiting for a QR code
            if (controller.isXPressed()) {
                break;
            }

            System.out.println("QR code read: " + qrText);

            // flash blue 3 times to confirm the QR code has been scanned
            controller.blinkBlue();

            // parse it into a list of shapes
            ArrayList<Shape> shapes = parser.parse(qrText);

            // if parse returned null the input was invalid, flash red and try again
            if (shapes == null) {
                System.out.println("Invalid QR code - please try again.");
                controller.blinkRed();
                continue;
            }

            // countdown 10 seconds before drawing - yellow light during wait
            System.out.println("\nGet ready! Drawing starts in 10 seconds...");
            System.out.println("(Press X to cancel)");
            controller.setLightsYellow();
            for (int sec = 10; sec > 0; sec--) {
                if (controller.isXPressed()) {
                    break;
                }
                System.out.println(sec + "...");
                pause(1000);
            }
            controller.lightsOff();

            // check X was not pressed during countdown
            if (controller.isXPressed()) {
                break;
            }

            System.out.println("Go!");

            // draw each shape one by one
            for (int i = 0; i < shapes.size(); i++) {
                Shape shape = shapes.get(i);

                // check X before starting each shape so user can cancel mid-sequence
                if (controller.isXPressed()) {
                    System.out.println("\nX button pressed - cancelling remaining shapes.");
                    break;
                }

                System.out.println("\nPreparing to draw: " + shape.getName());

                // green light on before drawing
                controller.setLightsGreen();

                // draw the shape - calls Square.draw() or Triangle.draw() (polymorphism)
                shape.draw(controller);

                // blink green 3 times to show its done
                controller.blinkGreen();
                controller.lightsOff();

                // add to log
                logger.addShape(shape);

                System.out.println("Finished: " + shape.getLogEntry());

                // if theres another shape coming, move back 15cm to make room
                if (i < shapes.size() - 1) {
                    System.out.println("Moving back 15cm for next shape...");
                    controller.repositionForNextShape();
                }
            }

            // only show this if X wasnt pressed during drawing
            if (!controller.isXPressed()) {
                System.out.println("\nAll shapes done! Scan another QR code or press X to quit.");
            }
        }

        // program is ending - write the log file
        System.out.println("\n=========================");
        System.out.println("X pressed - program stopped.");
        System.out.println("=========================");

        controller.lightsOff();

        System.out.println("Saving log file...");
        String logPath = logger.writeLogFile();
        if (logPath != null) {
            System.out.println("Log saved to: " + logPath);
        }

        System.out.println("\nGoodbye! See you next time.");
        System.out.println("=========================");

        // pause 3 seconds so the user has time to read the final messages
        pause(3000);
    }

    // helper so I dont have to write try/catch every time I want to wait
    private static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
