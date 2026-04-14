import swiftbot.*;

// Main class - runs the whole program
public class Main {

    static SwiftBotAPI swiftBot = SwiftBotAPI.INSTANCE;

    // Journey settings
    static int SectionLength;
    static int SectionCount;
    static int wheelSpeed;
    static String oddColour = "GREEN";
    static String evenColour = "BLUE";

    // Objects from other classes
    static InputValidator validator;
    static OddSection oddSection;
    static EvenSection evenSection;
    static JourneyLogger logger;

    // For button control
    static boolean continueProgram = true;
    static boolean buttonPressed = false;

    public static void main(String[] args) {

        // Create the objects
        validator = new InputValidator(swiftBot);
        oddSection = new OddSection(swiftBot);
        evenSection = new EvenSection(swiftBot);
        logger = new JourneyLogger();

        // Set up the Y and X buttons
        setupButtons();

        // Main loop - keeps going until user presses X
        while (continueProgram) {

            // Reset colours each journey
            oddColour = "GREEN";
            evenColour = "BLUE";

            displayWelcome();

            // Keep scanning until we get valid input
            while (true) {
                String qrData = validator.getQRCode();
                String[] parts = validator.parseQRData(qrData);

                int[] result = new int[2];
                String[] colours = new String[]{oddColour, evenColour};

                String error = validator.validateInput(parts, result, colours);

                if (!error.isEmpty()) {
                    validator.displayError(error, qrData);
                    continue;
                }

                // Store the valid values
                SectionLength = result[0];
                SectionCount = result[1];
                oddColour = colours[0];
                evenColour = colours[1];

                oddSection.setColour(oddColour);
                evenSection.setColour(evenColour);
                break;
            }

            // Random speed between 55 and 66 
            wheelSpeed = (int) (Math.random() * 11) + 55;

            int moveTime = oddSection.calculateMoveTime(SectionLength, wheelSpeed);

            displayValidInput(moveTime);

            // Countdown
            System.out.println("  >>> START ZIGZAG MOVEMENT IN 3 SECONDS...");
            for (int i = 3; i >= 1; i--) {
                System.out.println("      " + i + "...");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            System.out.println();

            // Start timer - measures actual time from START to END
            long startTime = System.currentTimeMillis();

            // Do the zigzag
            executeZigzag(wheelSpeed);

            // Stop  timer
            long endTime = System.currentTimeMillis();
            double actualDuration = (endTime - startTime) / 1000.0;

            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            // Retrace
            executeRetrace(wheelSpeed);

            // Save and log using actual measured duration
            logger.saveJourneyData(SectionLength, SectionCount, wheelSpeed, actualDuration);
            logger.writeLogFile();
            logger.displayJourneyComplete(SectionLength, SectionCount, wheelSpeed, oddColour, evenColour, actualDuration);

           
            waitForButton();
        }

        // User pressed X so show summary and exit
        logger.displaySummary();

        try {
            swiftBot.disableButton(Button.Y);
            swiftBot.disableButton(Button.X);
        } catch (Exception e) {}
    }

    // Welcome screen
    public static void displayWelcome() {
        System.out.println();
        System.out.println("        WELCOME TO THE SWIFTBOT ZIGZAG PROGRAM");
        System.out.println("                                                                                      ,----,                                                            \r\n"
        		+ "                                                                                    ,/   .`|                 ,----,                                     \r\n"
        		+ "  .--.--.                      .---.           ,---,            ,---,.            ,`   .'  :               .'   .`|           ,---,          ,----..    \r\n"
        		+ " /  /    '.                   /. ./|        ,`--.' |          ,'  .' |          ;    ;     /            .'   .'   ;        ,`--.' |         /   /   \\   \r\n"
        		+ "|  :  /`. /               .--'.  ' ;        |   :  :        ,---.'   |        .'___,/    ,'           ,---, '    .'        |   :  :        |   :     :  \r\n"
        		+ ";  |  |--`               /__./ \\ : |        :   |  '        |   |   .'        |    :     |            |   :     ./         :   |  '        .   |  ;. /  \r\n"
        		+ "|  :  ;_             .--'.  '   \\' .        |   :  |        :   :  :          ;    |.';  ;            ;   | .'  /          |   :  |        .   ; /--`   \r\n"
        		+ " \\  \\    `.         /___/ \\ |    ' '        '   '  ;        :   |  |-,        `----'  |  |            `---' /  ;           '   '  ;        ;   | ;  __  \r\n"
        		+ "  `----.   \\        ;   \\  \\;      :        |   |  |        |   :  ;/|            '   :  ;              /  ;  /            |   |  |        |   : |.' .' \r\n"
        		+ "  __ \\  \\  |         \\   ;  `      |        '   :  ;        |   |   .'            |   |  '             ;  /  /--,          '   :  ;        .   | '_.' : \r\n"
        		+ " /  /`--'  /          .   \\    .\\  ;        |   |  '        '   :  '              '   :  |            /  /  / .`|          |   |  '        '   ; : \\  | \r\n"
        		+ "'--'.     /            \\   \\   ' \\ |        '   :  |        |   |  |              ;   |.'           ./__;       :          '   :  |        '   | '/  .' \r\n"
        		+ "  `--'---'              :   '  |--\"         ;   |.'         |   :  \\              '---'             |   :     .'           ;   |.'         |   :    /   \r\n"
        		+ "                         \\   \\ ;            '---'           |   | ,'                                ;   |  .'              '---'            \\   \\ .'    \r\n"
        		+ "                          '---\"                             `----'                                  `---'                                    `---`      \r\n"
        		+ "                                                                                                                                                        ");
        System.out.println("  ****************************************************************");
        System.out.println();
        System.out.println("                         INSTRUCTIONS:");
        System.out.println("    1. PREPARE A QR CODE WITH FORMAT: LENGTH - SECTIONS");
        System.out.println("       EXAMPLE: 20-6 (20CM SECTIONS, 6 ZIGZAGS)");
        System.out.println();
        System.out.println("    2. FOR CUSTOM COLOURS: LENGTH - SECTIONS - COLOUR1 - COLOUR2");
        System.out.println("       EXAMPLE: 25-8-RED-YELLOW");
        System.out.println();
        System.out.println("                         VALID RANGES:");
        System.out.println("       - SECTION LENGTH: 15 - 85 CM");
        System.out.println("       - NUMBER OF SECTIONS: 2 - 12 (MUST BE AN EVEN NUMBER)");
        System.out.println("       - COLOURS: RED, GREEN, BLUE, YELLOW, WHITE, CYAN, MAGENTA");
        System.out.println();
        System.out.println("  ****************************************************************");
        System.out.println();
        System.out.println("  >>> PLEASE SCAN A QR CODE TO BEGIN...");
        System.out.println();
    }

    // Shows the validated input before movement starts
    public static void displayValidInput(int moveTime) {
        double moveTimeSec = moveTime / 1000.0;
        boolean isCustom = !oddColour.equals("GREEN") || !evenColour.equals("BLUE");

        System.out.println();
        System.out.println("                      INPUT VALIDATION");
        System.out.println("  ****************************************************************");
        System.out.println("  [SUCCESS] QR CODE SCANNED SUCCESSFULLY!");
       
        
        if (isCustom) {
            System.out.println("  [SUCCESS] CUSTOM COLOURS DETECTED!");
        }
        System.out.println("  ****************************************************************");
        System.out.println("                     JOURNEY PARAMETERS");
        System.out.println("  ****************************************************************");
        System.out.println("    SECTION LENGTH:      " + SectionLength + " CM");
        System.out.println("    NUMBER OF SECTIONS:  " + SectionCount);
        System.out.println("    WHEEL SPEED:         " + wheelSpeed + " (RANDOMLY GENERATED)");
        
        if (isCustom) {
            System.out.println("    LED COLOURS:         " + oddColour + "(ODD) / " + evenColour + "(EVEN)  [CUSTOM]");
        } else {
            System.out.println("    LED COLOURS:         GREEN(ODD) / BLUE(EVEN)");
        }
        System.out.println("    MOVEMENT TIME:       " + String.format("%.2f", moveTimeSec) + " SECONDS PER SECTION");
        System.out.println("  ****************************************************************");
        System.out.println();
    }

    // Runs the forward zigzag using OddSection and EvenSection objects
    public static void executeZigzag(int speed) {
        
    	System.out.println("                   ZIGZAG MOVEMENT - FORWARD");
        System.out.println("  ****************************************************************");

        for (int section = 1; section <= SectionCount; section++) {
            if (section % 2 == 1) {
                oddSection.executeForward(section, SectionCount, SectionLength, speed);
            } else {
                evenSection.executeForward(section, SectionCount, SectionLength, speed);
            }
        }

        System.out.println("  ****************************************************************");
        System.out.println("  [SUCCESS] FORWARD JOURNEY COMPLETE!");
        System.out.println("  ****************************************************************");
        System.out.println();

        oddSection.turnOffLEDs();
        System.out.println("  >>> TURNING 180 DEGREES TO RETRACE PATH...");
        System.out.println();
    }

    // Retraces the path backwards
    public static void executeRetrace(int speed) {
        System.out.println("                   ZIGZAG MOVEMENT - RETRACE");
        System.out.println("  ****************************************************************");

        oddSection.turn180(speed);
        System.out.println();

        // Loop backwards through sections
        for (int section = SectionCount; section >= 1; section--) {
            if (section % 2 == 1) {
                oddSection.executeRetrace(section, SectionLength, speed);
            } else {
                evenSection.executeRetrace(section, SectionLength, speed);
            }
        }

        oddSection.turnOffLEDs();

        System.out.println("  ****************************************************************");
        System.out.println("  [SUCCESS] RETRACE COMPLETE! BACK AT STARTING POSITION.");
        System.out.println("  ****************************************************************");
        System.out.println();
    }

    // Y = scan again, X = exit
    public static void setupButtons() {
        swiftBot.enableButton(Button.Y, () -> {
            synchronized (Main.class) {
                continueProgram = true;
                buttonPressed = true;
                Main.class.notifyAll();
            }
        });

        swiftBot.enableButton(Button.X, () -> {
            synchronized (Main.class) {
                continueProgram = false;
                buttonPressed = true;
                Main.class.notifyAll();
            }
        });
    }

    // Waits until the user presses Y or X
    public static void waitForButton() {
        buttonPressed = false;
        synchronized (Main.class) {
            while (!buttonPressed) {
                try { Main.class.wait(); } catch (InterruptedException e) {}
            }
        }
    }
}