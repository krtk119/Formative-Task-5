package joshua;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SessionLogger {

    private final NumberConverter converter = new NumberConverter();

    public List<String> getSortedValues(List<String> hexValues) {
        return sortHexValues(hexValues);
    }

 
    public String saveToFile(List<String> hexValues) {

        List<String> sortedValues = sortHexValues(hexValues);
        String fileName = "dance_log.txt";

        try {
            // FileWriter false = overwrite existing file
            FileWriter writer = new FileWriter(fileName, false);

            writer.write("SWIFTBOT DANCE PROGRAM - Session Log\n");
            writer.write("=====================================\n");
            writer.write("Hex values entered this session (sorted ascending):\n");
            writer.write("\n");

            for (String hex : sortedValues) {
                writer.write(hex + "\n");
            }

            writer.write("\n");
            writer.write("Total values: " + sortedValues.size() + "\n");

            writer.close(); 

            return fileName;

        } catch (IOException e) {
            System.out.println("Error saving log file: " + e.getMessage());
            return "ERROR - log file could not be saved";
        }
    }

 
    private List<String> sortHexValues(List<String> hexValues) {

        // Work on a copy - never modify the original session list
        List<String> sorted = new ArrayList<>(hexValues);

        // OUTER LOOP: each pass guarantees the largest unsorted item is at end
        for (int i = 0; i < sorted.size() - 1; i++) {

            // INNER LOOP: compare each adjacent pair in the unsorted section
            for (int j = 0; j < sorted.size() - 1 - i; j++) {

                // Convert both values to decimal for numeric comparison
                int val1 = converter.hexToDecimal(sorted.get(j));
                int val2 = converter.hexToDecimal(sorted.get(j + 1));

                // Swap if left is greater than right
                if (val1 > val2) {
                    String temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                }
            }
        }

        return sorted;
    }

}
