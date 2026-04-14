// Triangle extends Shape - same as Square but with 3 sides and different angles
public class Triangle extends Shape {

    private double sideA;
    private double sideB;
    private double sideC;

    // I'll store the angles so I can print them in the log file
    private double angleA;
    private double angleB;
    private double angleC;

    public Triangle(double sideA, double sideB, double sideC) {
        super("Triangle");
        this.sideA = sideA;
        this.sideB = sideB;
        this.sideC = sideC;
        calculateAngles(); // work out the angles from the side lengths
    }

    // checks if 3 sides can actually make a triangle
    // I made this static so I can call it before creating the object
    // e.g. Triangle.isValidTriangle(16, 30, 24)
    public static boolean isValidTriangle(double a, double b, double c) {
        return (a + b > c) && (a + c > b) && (b + c > a);
    }

    // uses the law of cosines to find each angle from the side lengths
    // formula: cos(A) = (b^2 + c^2 - a^2) / (2 * b * c)
    private void calculateAngles() {
        angleA = Math.toDegrees(Math.acos((sideB * sideB + sideC * sideC - sideA * sideA) / (2 * sideB * sideC)));
        angleB = Math.toDegrees(Math.acos((sideA * sideA + sideC * sideC - sideB * sideB) / (2 * sideA * sideC)));
        angleC = 180 - angleA - angleB; // angles always add up to 180
    }

    @Override
    public void draw(SwiftBotController controller) {
        long startTime = System.currentTimeMillis();

        System.out.println("Drawing triangle with sides: " + sideA + ", " + sideB + ", " + sideC + " cm");
        System.out.printf("Angles: A=%.2f  B=%.2f  C=%.2f%n", angleA, angleB, angleC);

        // move side A, then turn the exterior angle at corner A
        // exterior angle = 180 - interior angle (this is how much the robot turns)
        controller.moveForward(sideA);
        controller.turnRight(180 - angleA);

        // move side B, then turn exterior angle at corner B
        controller.moveForward(sideB);
        controller.turnRight(180 - angleB);

        // move side C - no turn needed after last side
        controller.moveForward(sideC);

        setTimeTaken(System.currentTimeMillis() - startTime);
        System.out.println("Triangle done! Took " + timeTaken + "ms");
    }

    // Heron's formula: area = sqrt(s*(s-a)*(s-b)*(s-c)) where s = half the perimeter
    @Override
    public double getArea() {
        double s = (sideA + sideB + sideC) / 2;
        return Math.sqrt(s * (s - sideA) * (s - sideB) * (s - sideC));
    }

    @Override
    public String getLogEntry() {
        return String.format("Triangle: %d, %d, %d (angles: %.2f, %.2f, %.2f; time: %dms)",
            (int) sideA, (int) sideB, (int) sideC, angleA, angleB, angleC, timeTaken);
    }

    public double getSideA() { return sideA; }
    public double getSideB() { return sideB; }
    public double getSideC() { return sideC; }
    public double getAngleA() { return angleA; }
    public double getAngleB() { return angleB; }
    public double getAngleC() { return angleC; }
}
