package amaan;
import java.util.ArrayList;

// this class takes the raw text from a QR code and turns it into Shape objects
// e.g. "S:30&T:16:30:24" becomes a list with a Square and a Triangle in it
public class QRCodeParser {

    // takes the qr code string and returns a list of shapes
    // returns null if the input is completely invalid
    public ArrayList<Shape> parse(String qrText) {

        ArrayList<Shape> shapes = new ArrayList<>();

        // split by & to handle multiple shapes e.g. "S:30&T:16:30:24"
        String[] parts = qrText.split("&");

        // check user hasnt put more than 5 shapes in one qr code
        if (parts.length > 5) {
            System.out.println("Error: you can only put up to 5 shapes in one QR code.");
            return null;
        }

        // go through each shape command one by one
        for (String part : parts) {
            part = part.trim(); // remove any accidental spaces

            Shape shape = parseOneShape(part);

            // if parseOneShape returned null it means that part was invalid
            // we just skip it and carry on with the rest
            if (shape == null) {
                System.out.println("Skipping invalid shape: " + part);
            } else {
                shapes.add(shape);
            }
        }

        // if every single shape was invalid, return null
        if (shapes.isEmpty()) {
            System.out.println("Error: no valid shapes found in QR code.");
            return null;
        }

        return shapes;
    }

    // parses a single shape command like "S:30" or "T:16:30:24"
    // returns null if anything is wrong with it
    private Shape parseOneShape(String text) {

        // split by colon to separate shape type from measurements
        // e.g. "S:30" becomes ["S", "30"]
        // e.g. "T:16:30:24" becomes ["T", "16", "30", "24"]
        String[] tokens = text.split(":");

        if (tokens.length == 0) {
            System.out.println("Error: empty input.");
            return null;
        }

        String shapeType = tokens[0].toUpperCase(); // make it uppercase so s and S both work

        if (shapeType.equals("S")) {
            return parseSquare(tokens);
        } else if (shapeType.equals("T")) {
            return parseTriangle(tokens);
        } else {
            System.out.println("Error: unknown shape '" + tokens[0] + "'. Use S for square or T for triangle.");
            return null;
        }
    }

    // parses square input - needs exactly 1 measurement
    // tokens should look like ["S", "30"]
    private Shape parseSquare(String[] tokens) {

        // check we have the right number of values
        if (tokens.length != 2) {
            System.out.println("Error: square needs exactly 1 measurement. Example: S:30");
            return null;
        }

        // try to convert the side length to a number
        double side;
        try {
            side = Double.parseDouble(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + tokens[1] + "' is not a valid number.");
            return null;
        }

        // check it's within the allowed range
        if (!isValidLength(side)) {
            System.out.println("Error: side length must be between 15 and 85 cm. You entered: " + side);
            return null;
        }

        return new Square(side);
    }

    // parses triangle input - needs exactly 3 measurements
    // tokens should look like ["T", "16", "30", "24"]
    private Shape parseTriangle(String[] tokens) {

        if (tokens.length != 4) {
            System.out.println("Error: triangle needs exactly 3 measurements. Example: T:16:30:24");
            return null;
        }

        // try to parse all 3 side lengths
        double a, b, c;
        try {
            a = Double.parseDouble(tokens[1]);
            b = Double.parseDouble(tokens[2]);
            c = Double.parseDouble(tokens[3]);
        } catch (NumberFormatException e) {
            System.out.println("Error: one of the triangle sides is not a valid number.");
            return null;
        }

        // check all sides are in the valid range
        if (!isValidLength(a) || !isValidLength(b) || !isValidLength(c)) {
            System.out.println("Error: all sides must be between 15 and 85 cm.");
            return null;
        }

        // check the 3 sides can actually form a triangle
        if (!Triangle.isValidTriangle(a, b, c)) {
            System.out.println("Error: " + a + ", " + b + ", " + c + " cannot form a triangle.");
            System.out.println("(Each side must be less than the sum of the other two sides)");
            return null;
        }

        return new Triangle(a, b, c);
    }

    // checks a length is between 15 and 85 cm as required by the task
    private boolean isValidLength(double length) {
        return length >= 15 && length <= 85;
    }
}
