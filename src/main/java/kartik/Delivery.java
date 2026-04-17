import swiftbot.SwiftBotAPI;
import java.util.Scanner;

    // Transmits message via LED Morse code
// Payload = sender location + space + message body
    public class Delivery extends Module {

    private final Message      message;  // message to deliver
    private final MorseEncoder encoder;  // used to look up Morse patterns

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public Delivery(SwiftBotAPI bot, Scanner keyboard,
                    Message message, MorseEncoder encoder) {
        super(bot, keyboard);
        this.message = message;
        this.encoder = encoder;
    }

    @Override
    public void run() {
        transmit();
    }

        // Splits payload into words, transmits each word then word-end signal
    public void transmit() {
        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "          MESSAGE DELIVERY" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();
        System.out.println("  " + GR + "v ARRIVED AT DESTINATION" + R);
        System.out.println("  Location: Safe House " + message.getReceiver().getLocation());
        System.out.println();
        System.out.println("  Receiver Authentication: " + GR + "COMPLETE v" + R);
        System.out.println("  Receiver: " + message.getReceiver().getCallsign());
        System.out.println();
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        System.out.println("  " + CY + "| LED TRANSMISSION IN PROGRESS         |" + R);
        System.out.println("  " + CY + "|                                       |" + R);
        System.out.println("  " + WH + "| LED Encoding Reference:               |" + R);
        System.out.println("  " + WH + "| Dot: White 500ms | Dash: Blue 1500ms  |" + R);
        System.out.println("  " + WH + "| End Char: Amber 1000ms                |" + R);
        System.out.println("  " + WH + "| End Word: Red 2000ms                  |" + R);
        System.out.println("  " + WH + "| End Message: Green 3000ms  Gap: 200ms |" + R);
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        System.out.println();
        System.out.println("  From: " + message.getSender().getCallsign()
            + " @ Safe House " + message.getSender().getLocation());
        System.out.println("  " + YL + "Status: Transmitting..." + R);
        System.out.println();

        // Full payload = sender location + space + message body
            // Prepend sender location so receiver knows who sent it
    String payload = (message.getSender().getLocation()
            + " " + message.getPlainText()).toUpperCase();

        for (String word : payload.trim().split("\\s+")) {
            transmitWord(word);
            System.out.println("  [END WORD] RED");
            setLED(RED);
            sleep(END_WORD_MS);
            clearLED();
            sleep(LED_GAP_MS);
        }

        System.out.println("  [END MSG] GREEN");
        setLED(GREEN);
        sleep(END_MSG_MS);
        clearLED();
        System.out.println("\n  " + GR + "[OK] TRANSMISSION COMPLETE" + R);
    }

        // Transmits each character in the word with end-char AMBER signal
    private void transmitWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            String ch      = String.valueOf(word.charAt(i));
            String pattern = encoder.toMorse(ch);
            if (pattern == null) continue;

            System.out.print("  '" + ch + "' [" + pattern + "] --> ");
            transmitCharacter(pattern);

            System.out.println("| AMBER");
            setLED(AMBER);
            sleep(END_CHAR_MS);
            clearLED();
            sleep(LED_GAP_MS);
        }
    }

        // Dot=WHITE 500ms, Dash=BLUE 1500ms, gap 200ms between each
    private void transmitCharacter(String pattern) {
        for (char sym : pattern.toCharArray()) {
            if (sym == '.') {
                System.out.print("WHITE ");
                setLED(WHITE);
                sleep(DOT_MS);
            } else {
                System.out.print("BLUE ");
                setLED(BLUE);
                sleep(DASH_MS);
            }
            clearLED();
            sleep(LED_GAP_MS);
        }
    }
}
