import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner;

    public InputHandler() {
        this.scanner = Utils.SHARED_SCANNER;
    }

    public int readMenuChoice(int minimum, int maximum) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Handled below.
            }
            System.out.println("Invalid menu choice. Please enter a number between " + minimum + " and " + maximum + ".");
            System.out.print("> ");
        }
    }

    public int readIntInRange(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Handled below.
            }
            System.out.println("Invalid input. Please enter a whole number between " + minimum + " and " + maximum + ".");
        }
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
