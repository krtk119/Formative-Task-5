package kerry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Utils {

    public static final Scanner SHARED_SCANNER = new Scanner(System.in);

    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String YELLOW = "yellow";
    public static final String ORANGE = "orange";
    public static final String PINK = "pink";

    public static final List<String> AVAILABLE_COLOURS = Arrays.asList(
            RED, GREEN, BLUE, YELLOW, ORANGE, PINK
    );

    private Utils() {
    }

    public static void printTitle(String title) {
        printDivider();
        System.out.println(title);
        printDivider();
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------");
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static String normaliseColour(String colour) {
        if (colour == null) {
            return null;
        }
        return colour.trim().toLowerCase();
    }

    public static boolean isValidColour(String colour) {
        String normalised = normaliseColour(colour);
        return normalised != null && AVAILABLE_COLOURS.contains(normalised);
    }

    public static boolean hasDuplicates(List<String> colours) {
        if (colours == null) {
            return false;
        }

        List<String> seen = new ArrayList<>();
        for (String colour : colours) {
            String normalised = normaliseColour(colour);
            if (normalised == null) {
                continue;
            }
            if (seen.contains(normalised)) {
                return true;
            }
            seen.add(normalised);
        }
        return false;
    }

    public static String coloursToDisplay(List<String> colours) {
        if (colours == null || colours.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < colours.size(); i++) {
            String colour = normaliseColour(colours.get(i));
            builder.append(colour == null ? "unknown" : colour);
            if (i < colours.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static String formatColourList(List<String> colours) {
        return coloursToDisplay(colours);
    }

    public static void sleepSilently(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}