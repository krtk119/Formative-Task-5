// Square extends Shape - it inherits name and timeTaken from Shape
public class Square extends Shape {

    private double sideLength;

    public Square(double sideLength) {
        super("Square"); // calls Shape constructor to set the name
        this.sideLength = sideLength;
    }

    // draws the square - 4 sides, turn 90 degrees at each corner
    @Override
    public void draw(SwiftBotController controller) {
        long startTime = System.currentTimeMillis();

        System.out.println("Drawing a square with side: " + sideLength + " cm");

        // a square has 4 equal sides so just repeat 4 times
        for (int i = 1; i <= 4; i++) {
            controller.moveForward(sideLength);
            // dont turn after the last side, already back at start
            if (i < 4) {
                controller.turnRight(90);
            }
        }

        // save how long it took
        setTimeTaken(System.currentTimeMillis() - startTime);
        System.out.println("Square done! Took " + timeTaken + "ms");
    }

    // area of a square = side x side
    @Override
    public double getArea() {
        return sideLength * sideLength;
    }

    // this is what gets written to the log file
    @Override
    public String getLogEntry() {
        return "Square: " + (int) sideLength + " (time: " + timeTaken + "ms)";
    }

    public double getSideLength() {
        return sideLength;
    }
}
