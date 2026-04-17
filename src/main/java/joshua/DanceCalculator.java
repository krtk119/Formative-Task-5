
public class DanceCalculator {

   
    private static final int MAX_SPEED = 100;


    // METHOD 1: calculateSpeed(int octal)
    public int calculateSpeed(int octal) {

        if (octal < 50) {
            return octal + 50; 
        }

        if (octal > MAX_SPEED) {
            return MAX_SPEED; 
        }

        return octal;
    }


    // METHOD 2: calculateRGB(int decimal)
    public RGBColour calculateRGB(int decimal) {

        int red   = decimal;
        int green = (decimal % 80) * 3;

        // Calculate blue manually
        int blue;
        if (red >= green) {
            blue = red;
        } else {
            blue = green;
        }

        // Returns a new RGBColour object containing all three components      
        return new RGBColour(red, green, blue);
    }

}
