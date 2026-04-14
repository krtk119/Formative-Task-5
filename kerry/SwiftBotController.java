import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import swiftbot.Button;
import swiftbot.ImageSize;
import swiftbot.SwiftBotAPI;

public class SwiftBotController {
    public static final class RoundQuitException extends RuntimeException {
        public RoundQuitException(String message) {
            super(message);
        }
    }

    private static final int[] LED_RED = new int[]{255, 0, 0};
    private static final int[] LED_GREEN = new int[]{0, 255, 0};
    private static final int[] LED_BLUE = new int[]{0, 0, 255};
    private static final int[] LED_YELLOW = new int[]{255, 255, 0};
    private static final int[] LED_ORANGE = new int[]{255, 165, 0};
    private static final int[] LED_PINK = new int[]{255, 105, 180};
    private static final int[] LED_WHITE = new int[]{255, 255, 255};
    private static final int[] LED_OFF = new int[]{0, 0, 0};

    private final boolean simulationMode;
    private final SwiftBotAPI api;
    private final Scanner simulationScanner;

    public SwiftBotController() {
        this(Boolean.parseBoolean(System.getProperty("swiftbot.simulation", "false")));
    }

    public SwiftBotController(boolean simulationMode) {
        this.simulationMode = simulationMode;
        this.api = simulationMode ? null : SwiftBotAPI.INSTANCE;
        this.simulationScanner = simulationMode ? Utils.SHARED_SCANNER : null;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    public void showStartupStatus() {
        if (simulationMode) {
            System.out.println("Running in simulation mode. Keyboard input will be used instead of SwiftBot hardware.");
            return;
        }

        System.out.println("SwiftBot API version: " + api.getVersion());
        pulseUnderlights(LED_WHITE, 250);
    }

    public String waitForModeSelectionButton() {
        Utils.printTitle("Select Game Mode");
        System.out.println("A - Default Mode");
        System.out.println("B - Customised Mode");
        System.out.println("Press SwiftBot button to continue.");
        Utils.printDivider();

        if (simulationMode) {
            return waitForSimulationChoice("Enter A for Default or B for Customised: ", Set.of("A", "B"));
        }

        setButtonLights(true, false, false, false);
        Button button = waitForHardwareButton(Set.of(Button.A, Button.B));
        clearButtonLights();
        return button.name();
    }

    public String waitForContinueOrQuitButton() {
        Utils.printTitle("Next Action");
        System.out.println("Y - Play Again");
        System.out.println("X - Quit");
        System.out.println("Press SwiftBot button to continue.");
        Utils.printDivider();

        if (simulationMode) {
            return waitForSimulationChoice("Enter Y to play again or X to quit: ", Set.of("Y", "X"));
        }

        setButtonLights(false, false, true, true);
        Button button = waitForHardwareButton(Set.of(Button.X, Button.Y));
        clearButtonLights();
        return button.name();
    }

    public List<String> scanGuess(int codeLength, boolean allowRepeats) {
        List<String> guess = new ArrayList<>();
        Set<String> seenColours = new HashSet<>();

        for (int position = 1; position <= codeLength; position++) {
            boolean scanned = false;
            while (!scanned) {
                Utils.printTitle("Scan Guess");
                System.out.println("Scan colour card " + position + " of " + codeLength);
                if (!guess.isEmpty()) {
                    System.out.println("Current Guess: " + Utils.formatColourList(guess));
                }
                System.out.println("Press A when the card is ready to scan.");
                System.out.println("Press X to quit the current round.");
                Utils.printDivider();

                if (simulationMode) {
                    String action = waitForSimulationChoice("Type A to scan or X to quit the round: ", Set.of("A", "X"));
                    if ("X".equals(action)) {
                        throw new RoundQuitException("The player chose to quit during guess input.");
                    }

                    String colour = readSimulationColour(position);
                    if (!allowRepeats && seenColours.contains(colour)) {
                        showError("Repeated colours are not allowed. Scan a different colour.");
                        continue;
                    }
                    guess.add(colour);
                    seenColours.add(colour);
                    showDetectedColour(colour);
                    scanned = true;
                    continue;
                }

                setButtonLights(true, false, true, false);
                Button button = waitForHardwareButton(Set.of(Button.A, Button.X));
                clearButtonLights();
                if (button == Button.X) {
                    throw new RoundQuitException("The player chose to quit during guess input.");
                }

                BufferedImage image = safelyTakeStill();
                String detectedColour = detectColour(image);
                if (detectedColour == null) {
                    showError("Scan failed. Please adjust the card and try again.");
                    continue;
                }
                if (!allowRepeats && seenColours.contains(detectedColour)) {
                    showError("Repeated colours are not allowed. Scan a different colour.");
                    continue;
                }

                guess.add(detectedColour);
                seenColours.add(detectedColour);
                showDetectedColour(detectedColour);
                scanned = true;
            }
        }

        disableUnderlights();
        return guess;
    }

    public void showDetectedColour(String colour) {
        System.out.println("Detected Colour: " + colour);
        int[] rgb = colourToRgb(colour);
        if (rgb != null) {
            pulseUnderlights(rgb, 400);
        }
    }

    public void showError(String message) {
        Utils.printTitle("Error");
        System.out.println(message);
        Utils.printDivider();
        pulseUnderlights(LED_RED, 700);
    }

    public void celebrateWin() {
        pulseUnderlights(LED_GREEN, 250);
        pulseUnderlights(LED_BLUE, 250);
        pulseUnderlights(LED_GREEN, 250);
    }

    public void indicateLoss() {
        pulseUnderlights(LED_RED, 300);
        pulseUnderlights(LED_OFF, 100);
        pulseUnderlights(LED_RED, 300);
    }

    public void shutdownHardware() {
        if (simulationMode) {
            return;
        }
        try {
            clearButtonLights();
            api.disableAllButtons();
            api.disableUnderlights();
            api.disableButtonLights();
            api.stopMove();
        } catch (Exception exception) {
            System.err.println("Warning: hardware cleanup did not complete cleanly.");
        }
    }

    private BufferedImage safelyTakeStill() {
        try {
            Utils.sleepSilently(1200);
            return api.takeStill(ImageSize.SQUARE_240x240);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to capture an image from the SwiftBot camera.", exception);
        }
    }

    private String readSimulationColour(int position) {
        while (true) {
            System.out.print("Enter simulated colour for card " + position + " (RED/GREEN/BLUE/YELLOW/ORANGE/PINK): ");
            String value = Utils.normaliseColour(simulationScanner.nextLine());
            if (value != null) {
                return value;
            }
            System.out.println("Invalid colour. Please try again.");
        }
    }

    private String waitForSimulationChoice(String prompt, Set<String> allowedChoices) {
        while (true) {
            System.out.print(prompt);
            String value = simulationScanner.nextLine().trim().toUpperCase(Locale.ROOT);
            if (allowedChoices.contains(value)) {
                return value;
            }
            System.out.println("Invalid choice. Allowed options are: " + allowedChoices);
        }
    }

    private Button waitForHardwareButton(Set<Button> allowedButtons) {
        AtomicReference<Button> pressedButton = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            api.disableAllButtons();
            for (Button button : allowedButtons) {
                api.enableButton(button, () -> {
                    if (pressedButton.compareAndSet(null, button)) {
                        latch.countDown();
                    }
                });
            }

            while (true) {
                if (latch.await(100, TimeUnit.MILLISECONDS)) {
                    return pressedButton.get();
                }
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Button wait was interrupted.", interruptedException);
        } finally {
            try {
                api.disableAllButtons();
            } catch (Exception ignored) {
                // Safe cleanup only.
            }
        }
    }

    private void setButtonLights(boolean a, boolean b, boolean x, boolean y) {
        if (simulationMode) {
            return;
        }
        api.setButtonLight(Button.A, a);
        api.setButtonLight(Button.B, b);
        api.setButtonLight(Button.X, x);
        api.setButtonLight(Button.Y, y);
    }

    private void clearButtonLights() {
        if (simulationMode) {
            return;
        }
        setButtonLights(false, false, false, false);
    }

    private void pulseUnderlights(int[] rgb, long milliseconds) {
        if (simulationMode) {
            return;
        }
        try {
            api.fillUnderlights(rgb);
            Utils.sleepSilently(milliseconds);
            api.disableUnderlights();
        } catch (Exception ignored) {
            // LED feedback should never crash the game.
        }
    }

    private void disableUnderlights() {
        if (simulationMode) {
            return;
        }
        try {
            api.disableUnderlights();
        } catch (Exception ignored) {
            // Safe cleanup only.
        }
    }

    private int[] colourToRgb(String colour) {
        switch (colour) {
            case Utils.RED:
                return LED_RED;
            case Utils.GREEN:
                return LED_GREEN;
            case Utils.BLUE:
                return LED_BLUE;
            case Utils.YELLOW:
                return LED_YELLOW;
            case Utils.ORANGE:
                return LED_ORANGE;
            case Utils.PINK:
                return LED_PINK;
            default:
                return null;
        }
    }

    private String detectColour(BufferedImage image) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        int xStart = width / 4;
        int xEnd = width - xStart;
        int yStart = height / 4;
        int yEnd = height - yStart;

        long redTotal = 0L;
        long greenTotal = 0L;
        long blueTotal = 0L;
        long pixels = 0L;

        for (int y = yStart; y < yEnd; y++) {
            for (int x = xStart; x < xEnd; x++) {
                int rgb = image.getRGB(x, y);
                redTotal += (rgb >> 16) & 0xFF;
                greenTotal += (rgb >> 8) & 0xFF;
                blueTotal += rgb & 0xFF;
                pixels++;
            }
        }

        if (pixels == 0) {
            return null;
        }

        int averageRed = (int) (redTotal / pixels);
        int averageGreen = (int) (greenTotal / pixels);
        int averageBlue = (int) (blueTotal / pixels);

        float[] hsb = java.awt.Color.RGBtoHSB(averageRed, averageGreen, averageBlue, null);
        float hueDegrees = hsb[0] * 360.0f;
        float saturation = hsb[1];
        float brightness = hsb[2];

        if (brightness < 0.18f) {
            return null;
        }

        if (averageRed > 170 && averageBlue > 120 && averageGreen < 180 && saturation >= 0.18f) {
            return Utils.PINK;
        }

        if (saturation < 0.20f) {
            return null;
        }

        if (hueDegrees < 15 || hueDegrees >= 345) {
            return Utils.RED;
        }
        if (hueDegrees >= 15 && hueDegrees < 40) {
            return brightness > 0.90f && averageGreen > 150 ? Utils.YELLOW : Utils.ORANGE;
        }
        if (hueDegrees >= 40 && hueDegrees < 75) {
            return Utils.YELLOW;
        }
        if (hueDegrees >= 75 && hueDegrees < 165) {
            return Utils.GREEN;
        }
        if (hueDegrees >= 165 && hueDegrees < 270) {
            return Utils.BLUE;
        }
        if (hueDegrees >= 270 && hueDegrees < 345) {
            return averageRed > averageGreen ? Utils.PINK : Utils.BLUE;
        }

        return null;
    }
}
 