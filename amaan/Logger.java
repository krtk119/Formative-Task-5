import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// this class handles writing all the shape info to a log file when the program ends
public class Logger {

    private ArrayList<Shape> allShapes; // keeps track of every shape drawn

    public Logger() {
        allShapes = new ArrayList<>();
    }

    // call this after each shape is drawn to add it to the list
    public void addShape(Shape shape) {
        allShapes.add(shape);
    }

    // writes everything to a text file and returns the file path
    public String writeLogFile() {

        // if nothing was drawn theres nothing to log
        if (allShapes.isEmpty()) {
            System.out.println("No shapes were drawn so no log file created.");
            return null;
        }

        String filePath = "shapes_log.txt";

        try {
            FileWriter writer = new FileWriter(filePath);

            writer.write("=== Shape Drawing Log ===\n\n");

            // write each shape entry on its own line
            writer.write("Shapes drawn:\n");
            for (int i = 0; i < allShapes.size(); i++) {
                writer.write((i + 1) + ". " + allShapes.get(i).getLogEntry() + "\n");
            }

            writer.write("\n");

            // find and write the largest shape by area
            Shape largest = getLargestShape();
            writer.write("Largest shape (by area): " + largest.getLogEntry() + "\n");

            // find which type was drawn most (square or triangle)
            writer.write("Most drawn shape: " + getMostDrawnType() + "\n");

            // work out the average time
            writer.write("Average time to draw: " + getAverageTime() + "ms\n");

            writer.close();

            System.out.println("Log file saved to: " + filePath);
            return filePath;

        } catch (IOException e) {
            System.out.println("Error: could not write log file. " + e.getMessage());
            return null;
        }
    }

    // goes through all shapes and returns the one with the biggest area
    private Shape getLargestShape() {
        Shape largest = allShapes.get(0);
        for (Shape s : allShapes) {
            if (s.getArea() > largest.getArea()) {
                largest = s;
            }
        }
        return largest;
    }

    // counts squares and triangles and returns which appeared more
    private String getMostDrawnType() {
        int squareCount = 0;
        int triangleCount = 0;

        for (Shape s : allShapes) {
            if (s.getName().equals("Square")) {
                squareCount++;
            } else {
                triangleCount++;
            }
        }

        if (squareCount > triangleCount) {
            return "Square: " + squareCount + " times";
        } else if (triangleCount > squareCount) {
            return "Triangle: " + triangleCount + " times";
        } else {
            return "Square and Triangle drawn equally: " + squareCount + " times each";
        }
    }

    // adds up all the times and divides by number of shapes
    private long getAverageTime() {
        long total = 0;
        for (Shape s : allShapes) {
            total += s.getTimeTaken();
        }
        return total / allShapes.size();
    }
}
