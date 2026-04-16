import java.util.ArrayList;
import java.util.List;
import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class DanceProgram {

   
    public static void main(String[] args) {

        // STEP 1: CREATE ONE INSTANCE OF EVERY SUB-PROCESS CLASS
        // Sub-process 1: Validates hex tokens (length and characters)
        HexValidator validator = new HexValidator();

        // Sub-process 2: Converts hex->decimal, decimal->octal, decimal->binary
        NumberConverter converter = new NumberConverter();

        // Sub-process 3: Calculates speed (from octal) and RGB colour (from decimal)
        DanceCalculator calculator = new DanceCalculator();

        // Sub-process 4: All 7 console screens - centralised display
        DisplayManager display = new DisplayManager();

        // Sub-process 5: Bubble sort + saves dance_log.txt
        SessionLogger logger = new SessionLogger();

        // Sub-process 6: All SwiftBot hardware - QR scan, LEDs, movement, buttons
        DanceExecutor executor;
        try {
            executor = new DanceExecutor();
        } catch (Exception e) {
            System.out.println("========================================");
            System.out.println("  ERROR: Could not connect to SwiftBot  ");
            System.out.println("========================================");
            System.out.println("Cause: " + e.getMessage());
            System.out.println("Ensure the SwiftBot is connected and");
            System.out.println("this program is running on the Pi.");
            return; 
        }



        // STEP 2: SESSION STORAGE
        List<String> sessionHexValues = new ArrayList<>();



        // STEP 3: WELCOME SCREEN + WAIT FOR Y TO START
        display.showWelcomeScreen();

        boolean startProgram = executor.waitForButton();

        if (!startProgram) {
            display.showFileSaved("(no session - exited at welcome screen)", new ArrayList<>());
            return;
        }



        // STEP 4: MAIN LOOP
        boolean keepRunning = true;

        while (keepRunning) {


            // STEP 4A: SHOW SCAN PROMPT AND SCAN QR CODE
            display.showScanPrompt();
            String scannedInput = executor.scanQRCode();

            if (scannedInput == null || scannedInput.trim().isEmpty()) {
                display.showNothingScanned();
                continue; 
            }



            // STEP 4B: SPLIT SCANNED INPUT BY '&'
            String[] rawTokens = scannedInput.split("&");

            List<String> validHexList   = new ArrayList<>(); 
            List<String> invalidHexList = new ArrayList<>(); 



            // STEP 4C: VALIDATE EACH TOKEN - MAX 5 PER SCAN
            int limit = rawTokens.length;
            if (limit > 5) {
                limit = 5; 
            }

            for (int i = 0; i < limit; i++) {

                String token = rawTokens[i];


                if (validator.validate(token)) {

                    validHexList.add(validator.normalise(token));
                } else {
                    if (!token.trim().isEmpty()) {
                        invalidHexList.add(token);
                    }
                }
            }



            // STEP 4D: SHOW INVALID VALUES - SCREEN 4
            display.showInvalidValues(invalidHexList);

            if (validHexList.isEmpty()) {
                display.showContinuePrompt(sessionHexValues);
                keepRunning = executor.waitForButton();
                continue;
            }



            // STEP 4E: PROCESS EACH VALID HEX VALUE
            for (String hex : validHexList) {
               
                int decimal = converter.hexToDecimal(hex);
               
                int octal = converter.decimalToOctal(decimal);
              
                String binary = converter.decimalToBinary(decimal);
                
                int hexLength = validator.getLength(hex);
               
                int speed = calculator.calculateSpeed(octal);
               
                RGBColour colour = calculator.calculateRGB(decimal);
               
                display.showDanceInfo(hex, octal, decimal, binary, speed, colour);
              
                executor.executeDance(binary, hexLength, speed, colour);
               
                display.showDanceComplete(hex);  
                
                sessionHexValues.add(hex);

            } 


            
            // STEP 4F: CONTINUE/QUIT PROMPT - SCREEN 6
            display.showContinuePrompt(sessionHexValues);
            keepRunning = executor.waitForButton();
        } 


       
        // STEP 5: TERMINATION - SORT, SAVE, DISPLAY, EXIT
        List<String> sortedValues = logger.getSortedValues(sessionHexValues);
        String filePath = logger.saveToFile(sessionHexValues);
        display.showFileSaved(filePath, sortedValues);

    } 

} 
