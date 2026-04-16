import java.util.List;


public class DisplayManager {

   
    public void showWelcomeScreen() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("      SWIFTBOT DANCE PROGRAM v1.0       ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("This program converts hexadecimal values");
        System.out.println("into dance movements for your SwiftBot.");
        System.out.println();
        System.out.println("What this program does:");
        System.out.println("  - Scans hex values from a QR code");
        System.out.println("  - Converts hex to octal, decimal, binary");
        System.out.println("  - Sets robot speed from octal value");
        System.out.println("  - Sets LED colour from decimal value");
        System.out.println("  - Executes dance routine from binary");
        System.out.println("  - Saves all session values to a log file");
        System.out.println();
        System.out.println("Press Y button to start...");
        System.out.println("Press X button to exit without running.");
        System.out.println("========================================");
        System.out.println();
    }

    
    public void showScanPrompt() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("             READY TO SCAN              ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Expected QR code format:");
        System.out.println("  Single value:    F   or  1F  or  A3");
        System.out.println("  Multiple values: 1F&2D  or  F&A3&5B");
        System.out.println();
        System.out.println("Rules:");
        System.out.println("  - Maximum 5 values per scan");
        System.out.println("  - Each value: 1 or 2 hex digits");
        System.out.println("  - Valid characters: 0-9 and A-F");
        System.out.println();
        System.out.println("Hold QR code to the camera now...");
        System.out.println("========================================");
        System.out.println();
    }

    
    public void showDanceInfo(String hex, int octal, int decimal,
                              String binary, int speed, RGBColour colour) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("  PROCESSING: " + hex);
        System.out.println("========================================");
        System.out.println();
        System.out.println("Conversions:");
        System.out.println("  Hexadecimal : " + hex);
        System.out.println("  Octal       : " + octal);
        System.out.println("  Decimal     : " + decimal);
        System.out.println("  Binary      : " + binary);
        System.out.println();
        System.out.println("Robot settings:");
        System.out.println("  Speed       : " + speed);
        System.out.println("  LED Red     : " + colour.getRed());
        System.out.println("  LED Green   : " + colour.getGreen());
        System.out.println("  LED Blue    : " + colour.getBlue());
        System.out.println();

        
        System.out.println("Dance pattern (binary read right to left):");
        for (int i = binary.length() - 1; i >= 0; i--) {
            char bit = binary.charAt(i);
            if (bit == '1') {
                System.out.println("  bit 1 -> Move Forward");
            } else {
                System.out.println("  bit 0 -> Spin");
            }
        }

        System.out.println();
        System.out.println("Starting dance routine now...");
        System.out.println("========================================");
        System.out.println();
    }


    public void showInvalidValues(List<String> invalidList) {

        if (invalidList == null || invalidList.isEmpty()) {
            return; 
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("          VALIDATION WARNING            ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("The following values were INVALID");
        System.out.println("and have been ignored:");
        System.out.println();

        for (String val : invalidList) {
            String reason = describeInvalidReason(val);
            System.out.println("  \"" + val + "\"  ->  " + reason);
        }

        System.out.println();
        System.out.println("Valid: 1-2 hex digits (0-9 and A-F only)");
        System.out.println("========================================");
        System.out.println();
    }


    public void showDanceComplete(String hex) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("         ROUTINE COMPLETE               ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("  Hex value \"" + hex + "\" processed successfully.");
        System.out.println("  Robot stopped. Underlights turned off.");
        System.out.println("========================================");
        System.out.println();
    }


    public void showContinuePrompt(List<String> sessionValues) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("         ALL ROUTINES FINISHED          ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Session summary:");
        System.out.println("  Values processed: " + sessionValues.size());
        System.out.println("  Values: " + sessionValues);
        System.out.println();
        System.out.println("  Press Y  ->  Scan another QR code");
        System.out.println("  Press X  ->  Save log and exit");
        System.out.println();
        System.out.println("Waiting for button press...");
        System.out.println("========================================");
        System.out.println();
    }


    public void showFileSaved(String filePath, List<String> sortedValues) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          SESSION LOG SAVED             ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Hex values sorted ascending:");
        System.out.println("  " + sortedValues);
        System.out.println();
        System.out.println("Log file saved to:");
        System.out.println("  " + filePath);
        System.out.println();
        System.out.println("Thank you for using SwiftBot Dance.");
        System.out.println("Program ended.");
        System.out.println("========================================");
        System.out.println();
    }


    public void showNothingScanned() {
        System.out.println();
        System.out.println("  [!] Nothing scanned. Please try again.");
        System.out.println();
    }


    private String describeInvalidReason(String val) {
        String trimmed = val.trim();
        if (trimmed.isEmpty()) {
            return "REJECTED - empty value";
        }
        if (trimmed.length() > 2) {
            return "REJECTED - too long (" + trimmed.length() + " digits, max is 2)";
        }
        return "REJECTED - invalid characters (use 0-9 and A-F only)";
    }

}
