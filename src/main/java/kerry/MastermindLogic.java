import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MastermindLogic {
    private final Random random;

    public MastermindLogic() {
        this(new Random());
    }

    public MastermindLogic(Random random) {
        this.random = random;
    }

    public List<String> generateSecretCode(int codeLength, boolean allowRepeats) {
        if (codeLength < 1 || codeLength > Utils.AVAILABLE_COLOURS.size()) {
            throw new IllegalArgumentException("Invalid code length: " + codeLength);
        }

        if (!allowRepeats) {
            List<String> colours = new ArrayList<>(Utils.AVAILABLE_COLOURS);
            Collections.shuffle(colours, random);
            return new ArrayList<>(colours.subList(0, codeLength));
        }

        List<String> code = new ArrayList<>();
        for (int index = 0; index < codeLength; index++) {
            code.add(Utils.AVAILABLE_COLOURS.get(random.nextInt(Utils.AVAILABLE_COLOURS.size())));
        }
        return code;
    }

    public String evaluateGuess(List<String> secretCode, List<String> guess) {
        validateComparableLists(secretCode, guess);

        int exactMatches = 0;
        Map<String, Integer> secretCounts = new HashMap<>();
        Map<String, Integer> guessCounts = new HashMap<>();

        for (int index = 0; index < secretCode.size(); index++) {
            String secretColour = secretCode.get(index);
            String guessColour = guess.get(index);

            if (secretColour.equals(guessColour)) {
                exactMatches++;
            } else {
                secretCounts.put(secretColour, secretCounts.getOrDefault(secretColour, 0) + 1);
                guessCounts.put(guessColour, guessCounts.getOrDefault(guessColour, 0) + 1);
            }
        }

        int partialMatches = 0;
        for (Map.Entry<String, Integer> entry : guessCounts.entrySet()) {
            String colour = entry.getKey();
            int guessCount = entry.getValue();
            int secretCount = secretCounts.getOrDefault(colour, 0);
            partialMatches += Math.min(guessCount, secretCount);
        }

        return "+".repeat(exactMatches) + "-".repeat(partialMatches);
    }

    public boolean isWinningGuess(List<String> secretCode, List<String> guess) {
        validateComparableLists(secretCode, guess);
        return secretCode.equals(guess);
    }

    private void validateComparableLists(List<String> secretCode, List<String> guess) {
        if (secretCode == null || guess == null || secretCode.size() != guess.size()) {
            throw new IllegalArgumentException("Secret code and guess must be non-null and of equal length.");
        }
    }
}
