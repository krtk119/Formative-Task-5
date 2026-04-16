import swiftbot.*;
import swiftbot.Button;
import swiftbot.ButtonFunction;
import java.awt.image.BufferedImage;


public class DanceExecutor {

    
    private final SwiftBotAPI swiftBot;

    
    //  forces main threat to always read from main memory, could potentially catches a stale value in main thread
    private volatile String buttonPressed = "";

   
    public DanceExecutor() throws Exception {
       
        swiftBot = SwiftBotAPI.INSTANCE;

        swiftBot.enableButton(Button.Y, new ButtonFunction() {
            public void run() {
                yButtonPressed();
            }
        });

        swiftBot.enableButton(Button.X, new ButtonFunction() {
            public void run() {
                xButtonPressed();
            }
        });

        System.out.println("[SwiftBot] Connected. Buttons Y and X registered.");
    }


    // METHOD 1: scanQRCode()
    public String scanQRCode() {

        try {
            BufferedImage image = swiftBot.getQRImage();

            if (image == null) {
                return ""; 
            }

            String result = swiftBot.decodeQRImage(image);

            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            } else {
                return "";
            }

        } catch (Exception e) {
            System.out.println("[SwiftBot] QR scan error: " + e.getMessage());
            return "";
        }
    }

    // METHOD 2: executeDance(String binary, int hexLength, int speed, RGBColour colour)
    public void executeDance(String binary, int hexLength, int speed, RGBColour colour) {

        try {

            // STEP 1: SET ALL 6 UNDERLIGHTS TO THE RGB COLOUR
            int[] rgb = new int[3];
            rgb[0] = colour.getRed();    
            rgb[1] = colour.getGreen();  
            rgb[2] = colour.getBlue();   

            swiftBot.fillUnderlights(rgb);


            // STEP 2: DETERMINE FORWARD MOVEMENT DURATION
            int forwardDuration;
            if (hexLength == 1) {
                forwardDuration = 1000; // 1 second
            } else {
                forwardDuration = 500;  // 0.5 seconds
            }

            // STEP 3: READ BINARY RIGHT TO LEFT - EXECUTE EACH MOVEMENT
            for (int i = binary.length() - 1; i >= 0; i--) {

                char bit = binary.charAt(i);

                if (bit == '1') {
                    swiftBot.move(speed, speed, forwardDuration);
                } else {
                    swiftBot.move(speed, -speed, 1000);
                }

                Thread.sleep(300);
            }


            // STEP 4: TURN OFF UNDERLIGHTS AFTER LAST MOVEMENT
            swiftBot.disableUnderlights();

        } catch (InterruptedException e) {
            System.out.println("[SwiftBot] Dance interrupted: " + e.getMessage());
            swiftBot.disableUnderlights(); 
        } catch (Exception e) {
            System.out.println("[SwiftBot] Dance error: " + e.getMessage());
            swiftBot.disableUnderlights();
        }
    }


    // METHOD 3: waitForButton()
    public boolean waitForButton() {

        buttonPressed = ""; 

        while (buttonPressed.equals("")) {
            try {
                Thread.sleep(100); 
            } catch (InterruptedException e) {
               
            }
        }

        return buttonPressed.equals("Y"); 
    }

    private void yButtonPressed() {
        buttonPressed = "Y";
    }


    private void xButtonPressed() {
        buttonPressed = "X";
    }

}
