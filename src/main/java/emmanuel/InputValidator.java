package emmanuel;
import swiftbot.*;
import java.awt.image.BufferedImage;

// Handles QR code scanning, parsing and input validation
public class InputValidator {

    private SwiftBotAPI swiftBot;

    public InputValidator(SwiftBotAPI swiftBot) {
        this.swiftBot = swiftBot;
    }

    // Scans QR code using the camera, keeps trying until it gets one
    public String getQRCode() {
        System.out.println("  ...Scanning for QR code...");
        System.out.println();

        String qrData = "";

        while (qrData.isEmpty()) {
            try {
                BufferedImage qrImage = swiftBot.getQRImage();
                qrData = swiftBot.decodeQRImage(qrImage);

                if (qrData == null || qrData.isEmpty()) {
                    qrData = "";
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                try { Thread.sleep(500); } catch (InterruptedException ie) {}
            }
        }

        System.out.println("  [SUCCESS] QR Code scanned successfully!");
        System.out.println("  Data received: " + qrData);
        System.out.println();
        return qrData;
    }

    // Splits the QR data by "-"
    public String[] parseQRData(String qrData) {
        return qrData.split("-");
    }

    // Checks if the input is valid, returns error message or empty string
    // result[] stores length and sections, colours[] stores colour names
    public String validateInput(String[] parts, int[] result, String[] colours) {

        // Must be 2 parts (20-6) or 4 parts (20-6-RED-YELLOW)
        if (parts.length != 2 && parts.length != 4) {
            return "Invalid format. Expected: Value1-Value2 or Value1-Value2-Colour1-Colour2";
        }

        int length;
        int sections;

        // Check both values are numbers
        try {
            length = Integer.parseInt(parts[0].trim());
            sections = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return "Length and Sections must be numbers.";
        }

        // Check length is 15-85
        if (length < 15 || length > 85) {
            return "Section length must be between 15 and 85 cm. You entered: " + length;
        }

        // Check sections is 2-12
        if (sections < 2 || sections > 12) {
            return "Number of sections must be between 2 and 12. You entered: " + sections;
        }

        // Check sections is even
        if (sections % 2 != 0) {
            return "Number of sections must be EVEN. You entered: " + sections;
        }

        // If 4 parts, check custom colours are valid
        if (parts.length == 4) {
            String[] validColours = {"RED", "GREEN", "BLUE", "YELLOW", "WHITE", "CYAN", "MAGENTA"};
            String colour1 = parts[2].trim().toUpperCase();
            String colour2 = parts[3].trim().toUpperCase();

            boolean colour1Valid = false;
            for (String c : validColours) {
                if (c.equals(colour1)) { colour1Valid = true; break; }
            }
            if (!colour1Valid) {
                return "Invalid colour: " + colour1 + ". Valid: RED, GREEN, BLUE, YELLOW, WHITE, CYAN, MAGENTA";
            }

            boolean colour2Valid = false;
            for (String c : validColours) {
                if (c.equals(colour2)) { colour2Valid = true; break; }
            }
            if (!colour2Valid) {
                return "Invalid colour: " + colour2 + ". Valid: RED, GREEN, BLUE, YELLOW, WHITE, CYAN, MAGENTA";
            }

            colours[0] = colour1;
            colours[1] = colour2;
        }

        result[0] = length;
        result[1] = sections;
        return "";
    }

    // Shows the error message when input is invalid
    public void displayError(String errorMessage, String inputReceived) {
        System.out.println();
        System.out.println("                    INPUT VALIDATION");
        System.out.println("  ****************************************************************");
        System.out.println("  [ERROR] INVALID INPUT DETECTED!");
        System.out.println("  ****************************************************************");
        System.out.println("                      ERROR DETAILS");
        System.out.println("  ****************************************************************");
        System.out.println("  INPUT RECEIVED: " + inputReceived);
        System.out.println();
        System.out.println("  [ERROR] " + errorMessage);
        System.out.println("  ****************************************************************");
        System.out.println();
        System.out.println("  >>> Please scan a valid QR code...");
        System.out.println();
    }
}
