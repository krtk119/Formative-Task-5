import swiftbot.SwiftBotAPI;
import swiftbot.Button;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

    // Encodes user button/keyboard input into a Message object
// Dual input: physical X/Y/A/B buttons OR keyboard characters simultaneously
    public class MorseEncoder extends Module {

        // Two-way lookup: char->morse and morse->char
    private final Map<String, String> TO_MORSE = new LinkedHashMap<>();
    private final Map<String, String> TO_CHAR  = new LinkedHashMap<>();
    private Message encodedMessage = null;
    private final Agent sender;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public MorseEncoder(SwiftBotAPI bot, Scanner keyboard, Agent sender) {
        super(bot, keyboard);
        this.sender = sender;
        loadDictionary();
    }

    @Override
    public void run() {
        encodedMessage = encode();
    }

    public Message getEncodedMessage() {
        return encodedMessage;
    }

    public String toMorse(String ch) {
        return TO_MORSE.get(ch.toUpperCase());
    }

    public String toChar(String pattern) {
        return TO_CHAR.getOrDefault(pattern, "?");
    }

        // Loads morse_dictionary.txt, falls back to built-in table if missing
    private void loadDictionary() {
        try (BufferedReader br = new BufferedReader(
                new FileReader(MORSE_DICT))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\s+", 2);
                if (p.length == 2) {
                    TO_MORSE.put(p[0].toUpperCase(), p[1]);
                    TO_CHAR.put(p[1], p[0].toUpperCase());
                }
            }
            System.out.println("  " + GR + "[OK] Loaded " + MORSE_DICT
                + " (" + TO_MORSE.size() + " entries)" + R);
        } catch (Exception e) {
            System.out.println("  " + YL + "[!] " + MORSE_DICT
                + " not found — using built-in table." + R);
            loadBuiltIn();
        }
        if (TO_MORSE.isEmpty()) {
            System.out.println("  " + YL + "[!] Dictionary empty or wrong format"
                + " — using built-in table." + R);
            loadBuiltIn();
        }
    }

    private void loadBuiltIn() {
        String[][] t = {
            {"A",".-"},  {"B","-..."}, {"C","-.-."}, {"D","-.."},
            {"E","."},   {"F","..-."}, {"G","--."},  {"H","...."},
            {"I",".."},  {"J",".---"}, {"K","-.-"},  {"L",".-.."},
            {"M","--"},  {"N","-."},   {"O","---"},  {"P",".--."},
            {"Q","--.-"},{"R",".-."},  {"S","..."},  {"T","-"},
            {"U","..-"}, {"V","...-"}, {"W",".--"},  {"X","-..-"},
            {"Y","-.--"},{"Z","--.."},
            {"0","-----"},{"1",".----"},{"2","..---"},{"3","...--"},
            {"4","....-"},{"5","....."},{"6","-...."},{"7","--..."},
            {"8","---.."}, {"9","----."},
            {".",".-.-.-"},{",","--..--"},{"?","..--.."}
        };
        for (String[] e : t) {
            TO_MORSE.put(e[0], e[1]);
            TO_CHAR.put(e[1], e[0]);
        }
    }

        // Main encoding loop — reads dots/dashes, builds message word by word
// First word must be destination (A, B, or C)
    private Message encode() {
        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "    MESSAGE ENCODING - MORSE CODE" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();
        System.out.println("  " + WH + "Button Controls:" + R);
        System.out.println("    " + GR + "[X]" + R + " Dot (.)  |  " + GR + "[Y]" + R + " Dash (-)  |  "
            + GR + "[A]" + R + " End Char  |  " + GR + "[B]" + R + " End Word");
        System.out.println("    " + YL + "[0 pattern: -----]" + R + " End Message");
        System.out.println("    Type " + RD + "CANCEL" + R + " to abort.");
        System.out.println();

        final String[] buttonInput = {""};

        bot.enableButton(Button.X, () -> {
            buttonInput[0] = "X";
        });

        bot.enableButton(Button.Y, () -> {
            buttonInput[0] = "Y";
        });

        bot.enableButton(Button.A, () -> {
            buttonInput[0] = "A";
        });

        bot.enableButton(Button.B, () -> {
            buttonInput[0] = "B";
        });

        StringBuilder symbols = new StringBuilder();
        StringBuilder word    = new StringBuilder();
        StringBuilder message = new StringBuilder();
        String  dest      = null;
        boolean firstWord = true;
        int chars = 0, words = 0;

        while (true) {
            printStatus(dest, symbols, word, message, chars, words);

            String in = "";
            while (in.isEmpty()) {
                if (!buttonInput[0].isEmpty()) {
                    in = buttonInput[0];
                    buttonInput[0] = "";
                    break;
                }
                try {
                    if (System.in.available() > 0) {
                        in = keyboard.nextLine().trim().toUpperCase();
                    }
                } catch (Exception ignored) {}
                sleep(50);
            }

            System.out.println();

            if (in.isEmpty()) continue;

            if (in.equals("CANCEL")) {
                System.out.println("  " + YL + "Cancelled." + R);
                bot.disableAllButtons();
                return null;
            }

            switch (in) {
                case "X":
                    symbols.append(".");
                    pulseLED(YELLOW, FEEDBACK_MS);
                    System.out.println("  " + GR + "[DOT] " + symbols + R);
                    break;

                case "Y":
                    symbols.append("-");
                    pulseLED(YELLOW, FEEDBACK_MS);
                    System.out.println("  " + GR + "[DASH] " + symbols + R);
                    break;

                case "A":
                    if (symbols.length() == 0) {
                        System.out.println("  " + RD + "[!] Enter X or Y first." + R);
                        break;
                    }
                    String pat = symbols.toString();

                        // ----- is the end-of-message signal (Morse for 0)
    if (pat.equals("-----")) {
                        if (word.length() > 0) {
                            String w = word.toString().toUpperCase();
                            if (firstWord) {
                                String err = validateDest(w);
                                if (err != null) {
                                    System.out.println("  " + RD + "[!] " + err + R);
                                    symbols.setLength(0);
                                    word.setLength(0);
                                    break;
                                }
                                dest = w;
                                firstWord = false;
                            } else {
                                if (message.length() > 0) message.append(" ");
                                message.append(w);
                                words++;
                            }
                            word.setLength(0);
                        }
                        symbols.setLength(0);
                        System.out.println("  " + GR + "[END OF MESSAGE]" + R);

                        if (dest == null || message.length() == 0) {
                            System.out.println("  " + RD + "[!] Incomplete message." + R);
                            bot.disableAllButtons();
                            return null;
                        }
                        bot.disableAllButtons();
                        return buildMessage(dest, message.toString());
                    }

                    String decoded = toChar(pat);
                    if (decoded.equals("?")) {
                        System.out.println("  " + RD + "[!] Unknown pattern: " + pat + R);
                        symbols.setLength(0);
                        break;
                    }
                    pulseLED(YELLOW, FEEDBACK_MS);
                    word.append(decoded);
                    chars++;
                    System.out.println("  " + GR + "[CHAR] '" + decoded + "'" + R + "  word: " + YL + word + R);
                    symbols.setLength(0);
                    break;

                case "B":
                    if (symbols.length() > 0) {
                        String d = toChar(symbols.toString());
                        if (!d.equals("?")) {
                            word.append(d);
                            chars++;
                        }
                        symbols.setLength(0);
                    }
                    if (word.length() == 0) {
                        System.out.println("  " + RD + "[!] Empty word." + R);
                        break;
                    }
                    pulseLED(YELLOW, FEEDBACK_MS);
                    String w = word.toString().toUpperCase();
                    if (firstWord) {
                        String err = validateDest(w);
                        if (err != null) {
                            System.out.println("  " + RD + "[!] " + err + R);
                            System.out.print("  Retry destination? (Y/N): ");
                            if (!keyboard.nextLine().trim().equalsIgnoreCase("Y")) {
                                bot.disableAllButtons();
                                return null;
                            }
                            word.setLength(0);
                            break;
                        }
                        dest = w;
                        firstWord = false;
                        Agent destAgent = Agent.findByLocation(dest);
                        System.out.println("  " + GR + "[DEST] Safe House " + dest
                            + (destAgent != null
                                ? " (" + destAgent.getCity() + ")" : "") + R);
                        word.setLength(0);
                    } else {
                        if (message.length() > 0) message.append(" ");
                        message.append(w);
                        words++;
                        System.out.println("  " + GR + "[WORD] '" + w + R
                            + "'  message: " + YL + message + R);
                        word.setLength(0);
                    }
                    break;

                default:
                    System.out.println("  " + RD
                        + "[!] Unknown: '" + in + "'. Use X, Y, A, B or CANCEL." + R);
            }
        }
    }

        // Confirms message with user before creating Message object
    private Message buildMessage(String destLocation, String text) {
        if (text.length() > 200) text = text.substring(0, 200);

        Agent receiver = Agent.findByLocation(destLocation);
        if (receiver == null) {
            System.out.println("  " + RD + "[!] Unknown destination." + R);
            return null;
        }

        System.out.println();
        System.out.println("  " + CY + "+------------------------------------------+" + R);
        System.out.println("  " + CY + "| " + BLD + "CONFIRM" + R + CY + "                                  |" + R);
        System.out.println("  " + CY + "| " + WH + "To  : " + R + receiver);
        System.out.println("  " + CY + "| " + WH + "Msg : " + R + text);
        System.out.println("  " + CY + "+------------------------------------------+" + R);
        System.out.print("  Send? (Y/N): ");

        if (!keyboard.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  " + YL + "Discarded." + R);
            return null;
        }

        return new Message(sender, receiver, text);
    }

        // Checks destination is A/B/C and not sender's own location
    private String validateDest(String w) {
        if (!Agent.isValidLocation(w))
            return "First word must be A, B or C.";
        if (sender != null && w.equalsIgnoreCase(sender.getLocation()))
            return "Cannot send to your own safe house.";
        return null;
    }

        // Prints real-time status panel: dest, current char, word, buffer
    private void printStatus(String dest, StringBuilder symbols,
                              StringBuilder word, StringBuilder message,
                              int chars, int words) {
        Agent destAgent = dest == null ? null : Agent.findByLocation(dest);
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        System.out.println("  " + CY + "| " + WH + "Destination: " + R
            + YL + (dest == null
            ? "(not yet set)"
            : "Safe House " + dest
              + (destAgent != null ? " (" + destAgent.getCity() + ")" : "")) + R);
        System.out.println("  " + CY + "|" + R);
        System.out.println("  " + CY + "| " + WH + "Current Character: " + R
            + GR + (symbols.length() == 0 ? "--" : symbols) + R);
        System.out.println("  " + CY + "| " + WH + "Current Word:      " + R
            + GR + (word.length() == 0 ? "--" : word) + R);
        System.out.println("  " + CY + "| " + WH + "Message Buffer:    " + R
            + GR + (message.length() == 0 ? "(empty)" : message) + R);
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        System.out.println();
        System.out.println("  " + WH + "[LED: YELLOW pulse <200ms]" + R);
        System.out.println();
        System.out.println("  Status: " + YL + "Encoding in progress..." + R);
        System.out.println("  Characters: " + chars + "  |  Words: " + words);
        System.out.println();
        System.out.println("  " + WH + "Waiting for button press..." + R);
        System.out.print("  >> ");
    }
}
