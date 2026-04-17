// abstract class that Square and Triangle will extend
// I made this so I don't have to repeat the same code in both classes
public abstract class Shape {

    // every shape has a name and a time it took to draw
    protected String name;
    protected long timeTaken; // in milliseconds

    // constructor - sets the name when you create a shape
    public Shape(String name) {
        this.name = name;
        this.timeTaken = 0;
    }

    // these methods have no body here - Square and Triangle must write them
    public abstract void draw(SwiftBotController controller);
    public abstract double getArea();
    public abstract String getLogEntry();

    // simple getters
    public String getName() {
        return name;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long t) {
        timeTaken = t;
    }
}
