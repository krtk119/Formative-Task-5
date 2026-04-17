import swiftbot.SwiftBotAPI;
import java.util.List;
import java.util.Scanner;

        // Displays message history table — requires re-authentication
// Logger injected via constructor — dependency injection pattern
        public class HistoryModule extends Module {
    // Logger injected via constructor - DEPENDENCY INJECTION
    private final Logger logger;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public HistoryModule(SwiftBotAPI bot, Scanner keyboard, Logger logger) {
        super(bot, keyboard);
        this.logger = logger;
    }

    @Override
    public void run() {
        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "           MESSAGE HISTORY" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();

                // Reads all messages from log file
        List<Message> messages = logger.getAllMessages();

        if (messages.isEmpty()) {
            System.out.println("  " + YL + "No messages found." + R);
            return;
        }

        // Header row
        System.out.println("  " + CY + "+-----------------------+---------+------------+-----------+" + R);
        System.out.printf("  " + CY + "| " + BLD + "%-21s" + R + CY + " | " + BLD + "%-7s" + R + CY + " | " + BLD + "%-10s" + R + CY + " | " + BLD + "%-9s" + R + CY + " |%n" + R,
            "ID", "From>To", "Date", "Status");
        System.out.println("  " + CY + "+-----------------------+---------+------------+-----------+" + R);

        // One row per message with LED feedback per status
        for (Message m : messages) {
            if (m.isDelivered()) {
                pulseLED(GREEN, 150);
                System.out.printf("  " + CY + "| " + R + GR + "%-21s" + R + CY + " | " + R + "%-7s" + CY + " | " + R + "%-10s" + CY + " | " + R + GR + "%-9s" + R + CY + " |%n" + R,
                    m.getId(),
                    m.getSender().getLocation() + ">" + m.getReceiver().getLocation(),
                    m.getEncodedTime().substring(0, 10),
                    m.getStatus());
            } else {
                pulseLED(RED, 150);
                System.out.printf("  " + CY + "| " + R + "%-21s" + CY + " | " + R + "%-7s" + CY + " | " + R + "%-10s" + CY + " | " + R + YL + "%-9s" + R + CY + " |%n" + R,
                    m.getId(),
                    m.getSender().getLocation() + ">" + m.getReceiver().getLocation(),
                    m.getEncodedTime().substring(0, 10),
                    m.getStatus());
            }
        }
        System.out.println("  " + CY + "+-----------------------+---------+------------+-----------+" + R);

        // Detail view
        System.out.println();
        System.out.print("  Enter ID to view detail (or BACK): ");
        String choice = keyboard.nextLine().trim();
        if (choice.equalsIgnoreCase("BACK")) return;

        for (Message m : messages) {
            if (m.getId().equalsIgnoreCase(choice)) {
                showDetail(m);
                return;
            }
        }
        System.out.println("  " + RD + "[!] ID not found." + R);
    }

            // Shows full message detail then triggers kinetic spin
        private void showDetail(Message m) {
        System.out.println();
        System.out.println("  " + CY + "+------------------------------------------+" + R);
        System.out.println("  " + CY + "| " + WH + "ID        : " + R + m.getId());
        System.out.println("  " + CY + "| " + WH + "From      : " + R + m.getSender().getCallsign()
            + " @ Safe House " + m.getSender().getLocation());
        System.out.println("  " + CY + "| " + WH + "To        : " + R + m.getReceiver().getCallsign()
            + " @ Safe House " + m.getReceiver().getLocation());
        System.out.println("  " + CY + "| " + WH + "Message   : " + R + m.getPlainText());
        System.out.println("  " + CY + "| " + WH + "Sent      : " + R + m.getEncodedTime());
        System.out.println("  " + CY + "| " + WH + "Delivered : " + R
            + (m.getDeliveredTime().isEmpty() ? "N/A" : m.getDeliveredTime()));
        System.out.println("  " + CY + "| " + WH + "Status    : " + R
            + (m.isDelivered() ? GR : YL) + m.getStatus() + R);
        System.out.println("  " + CY + "+------------------------------------------+" + R);

        kineticDisplay();
    }

            // Spins robot 360 degrees — TURN_120_MS * 3 = full rotation
        private void kineticDisplay() {
        System.out.println("\n  " + GR + "[KINETIC DISPLAY] Spinning 360 degrees..." + R);
        setLED(GREEN);

        int spinTime = TURN_120_MS * 3;

        try { bot.startMove(WHEEL_SPEED, -WHEEL_SPEED); } catch (Exception e) {}
        sleep(spinTime);
        try { bot.stopMove(); } catch (Exception e) {}

        clearLED();
        System.out.println("  " + GR + "Spin complete." + R);
    }
}
