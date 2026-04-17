package kartik;
import swiftbot.SwiftBotAPI; 
import java.time.*;
import java.time.format.*;
import java.util.Scanner;


public class SpyBot {

    // SwiftBotAPI is a Singleton — only one instance ever exists
    private static final SwiftBotAPI bot      = SwiftBotAPI.INSTANCE;
    private static final Scanner     keyboard = new Scanner(System.in);

    // DWELL_S exposed from Module for use in countdown
    private static final int DWELL_S = 10;

    // ── ANSI colour constants ─────────────────────────────────────────
    static final String R   = "\u001B[0m";      // reset
    static final String CY  = "\u001B[36m";     // cyan
    static final String CYB = "\u001B[96m";     // bright cyan
    static final String GR  = "\u001B[32m";     // green
    static final String YL  = "\u001B[33m";     // yellow
    static final String RD  = "\u001B[31m";     // red
    static final String WH  = "\u001B[37m";     // white
    static final String BLD = "\u001B[1m";      // bold

    // Entry point — initialises logger, shows startup, authenticates operator, starts menu loop
        public static void main(String[] args) throws Exception {

        // Initialise shared modules
        // Logger is shared across all modules — single instance
        Logger logger = new Logger(bot, keyboard);
        // Show ASCII banner and system info
        showStartup();

        // Initial authentication
        // Initial QR auth — null means any location accepted
        Auth initAuth  = new Auth(bot, keyboard, "SEND", null);
        Agent operator = initAuth.authenticate();
        if (operator == null) return;

        // Navigator knows robot starting location from operator QR
        // Navigator tracks current location from authenticated operator
        Navigator navigator = new Navigator(bot, keyboard,
            operator.getLocation());

        // EmergencyModule gets all dependencies injected
        // Shared encoder for emergency broadcasts (no sender)
        MorseEncoder sharedEncoder = new MorseEncoder(bot, keyboard, null);
        EmergencyModule emergency  = new EmergencyModule(bot, keyboard,
            navigator, sharedEncoder, logger);
        HistoryModule history      = new HistoryModule(bot, keyboard, logger);

        // Main menu loop
        // Main menu loop — runs until user exits
        while (true) {
            showMainMenu(operator);
            String choice = keyboard.nextLine().trim();

            switch (choice) {
                case "1":
                    runSendCycle(operator, navigator, logger);
                    break;

                case "2":
                    Auth histAuth = new Auth(bot, keyboard, "HISTORY", null);
                    if (histAuth.authenticate() != null) history.run();
                    break;

                case "3":
                    emergency.run();
                    break;

                case "4":
                    try { bot.disableUnderlights(); } catch (Exception ignored) {}
                    System.out.println("\n  " + GR + "Goodbye, "
                        + operator.getCallsign() + ". Stay safe." + R);
                    System.out.println("  To restart type:");
                    System.out.println("  " + CY + "java -cp .:SwiftBot-API-6.0.0.jar SpyBot" + R);
                    System.out.println();
                    keyboard.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("  " + RD + "[!] Enter 1, 2, 3 or 4." + R);
            }
        }
    }

    // Full send cycle: auth -> encode -> navigate -> deliver -> return -> analytics
        private static void runSendCycle(Agent operator, Navigator navigator,
                                     Logger logger) {
        long cycleStart = System.currentTimeMillis();
        // Reset obstacle counters for fresh cycle
        navigator.resetObstacleStats();

        // Step 1 - Sender authentication
        long authStart = System.currentTimeMillis();
        // Re-authenticate before each send — zero-trust model
        Auth sendAuth  = new Auth(bot, keyboard, "SEND", null);
        Agent sender   = sendAuth.authenticate();
        if (sender == null) return;
        long authSec   = elapsed(authStart);

        // Step 2 - Morse encoding
        long encStart          = System.currentTimeMillis();
        // New encoder instance per send cycle, sender agent passed in
        MorseEncoder senderEnc = new MorseEncoder(bot, keyboard, sender);
        senderEnc.run();
        Message msg = senderEnc.getEncodedMessage();
        if (msg == null) return;
        long encSec = elapsed(encStart);

        // Step 3 - Log as pending
        // Write PENDING entry to log file before navigation starts
        logger.logPending(msg);

        // Step 4 - Navigate to receiver
        long navStart = System.currentTimeMillis();
        // Navigate robot to receiver safe house
        navigator.travelTo(msg.getReceiver().getLocation());
        long navSec   = elapsed(navStart);

        // Step 5 - Red blink signal - message incoming
        System.out.println("\n  " + RD + "[Blinking RED - message incoming...]" + R);
        for (int i = 0; i < 3; i++) {
            try { bot.fillUnderlights(new int[]{255, 0, 0}); }
            catch (Exception e) {}
            sleep(400);
            try { bot.disableUnderlights(); } catch (Exception e) {}
            sleep(300);
        }

        // Step 6 - Receiver authentication
        System.out.println("\n  " + CY + "Waiting for receiver QR code..." + R);
        long rxStart   = System.currentTimeMillis();
        // Authenticate receiver — must match message destination location
        Auth rxAuth    = new Auth(bot, keyboard, "RECEIVE",
            msg.getReceiver().getLocation());
        Agent receiver = rxAuth.authenticate();
        if (receiver == null) return;
        long rxAuthSec = elapsed(rxStart);

        // Step 7 - LED Morse delivery
        long delivStart = System.currentTimeMillis();
        // Delivery uses encoder to look up Morse patterns for LED transmission
        Delivery delivery = new Delivery(bot, keyboard, msg, senderEnc);
        delivery.run();
        long delivSec   = elapsed(delivStart);

        // Step 8 - Mark delivered
        // Update message status to DELIVERED with timestamp
        msg.markDelivered();
        // Update log file with delivered status
        logger.logDelivered(msg);

        System.out.println("\n  " + GR + "+--------------------------------------------------+" + R);
        System.out.println("  " + GR + "| MESSAGE DELIVERED SUCCESSFULLY                   |" + R);
        System.out.println("  " + GR + "| To: " + msg.getReceiver().getCallsign()
            + " @ Safe House " + msg.getReceiver().getLocation() + "          |" + R);
        System.out.println("  " + GR + "+--------------------------------------------------+" + R);

        // Step 9 - Ask receiver what to do next
        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "     WHAT WOULD YOU LIKE TO DO?" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println("  " + WH + "[1]" + R + " Send a message from here");
        System.out.println("  " + WH + "[2]" + R + " Return robot to sender");
        System.out.println("  " + CY + "=========================================" + R);
        System.out.print("  Choice: ");

        // Receiver chooses: reply from here [1] or return robot [2]
        String receiverChoice = keyboard.nextLine().trim();

        long retStart = System.currentTimeMillis();

        // Option 1: receiver becomes sender, full reply cycle runs
        if (receiverChoice.equals("1")) {
            // Receiver wants to send a message from current location
            System.out.println("\n  " + GR + "Receiver is now the sender." + R);

            MorseEncoder receiverEnc = new MorseEncoder(bot, keyboard,
                receiver);
            receiverEnc.run();
            Message replyMsg = receiverEnc.getEncodedMessage();

            if (replyMsg != null) {
                logger.logPending(replyMsg);

                // Navigate to reply destination
                navigator.travelTo(replyMsg.getReceiver().getLocation());

                // Authenticate reply receiver
                Auth replyAuth = new Auth(bot, keyboard, "RECEIVE",
                    replyMsg.getReceiver().getLocation());
                Agent replyReceiver = replyAuth.authenticate();

                if (replyReceiver != null) {
                    // Deliver reply
                    Delivery replyDelivery = new Delivery(bot, keyboard,
                        replyMsg, receiverEnc);
                    replyDelivery.run();

                    replyMsg.markDelivered();
                    logger.logDelivered(replyMsg);

                    System.out.println("\n  " + GR + "Reply delivered successfully." + R);
                }

                // Return to original sender
                System.out.println("\n  " + CY + "Returning to original sender..." + R);
                // Return robot to original sender safe house
        navigator.returnTo(sender.getLocation());

            } else {
                // Reply cancelled - just return
                System.out.println("\n  " + YL + "Reply cancelled. Returning to sender." + R);
                navigator.returnTo(sender.getLocation());
            }

        } else {
            // Return to sender with dwell countdown
            System.out.print("\n  Wait: ");
            for (int i = DWELL_S; i >= 1; i--) {
                System.out.print(YL + i + "... " + R);
                sleep(1000);
            }
            System.out.println("Returning.\n");
            navigator.returnTo(sender.getLocation());
        }

        long retSec = elapsed(retStart);

        // Step 10 - Analytics
        long totalSec = elapsed(cycleStart);
        Analytics.CycleData cycleData = new Analytics.CycleData(
            sender.getLocation(), receiver.getLocation(),
            encSec, navSec,
            navigator.getObstacleCount(),
            navigator.getObstacleTotalSeconds(),
            authSec + rxAuthSec, delivSec, retSec, totalSec
        );

        logger.logCycle(
            sender.getCallsign(), receiver.getCallsign(),
            encSec, navSec,
            navigator.getObstacleCount(),
            navigator.getObstacleTotalSeconds(),
            authSec + rxAuthSec, delivSec, retSec, totalSec
        );

        // Show cycle performance breakdown after every completed cycle
        Analytics analytics = new Analytics(bot, keyboard, cycleData);
        analytics.run();
    }

    // Displays ASCII art banner, system info, and zero-trust warning
        private static void showStartup() {
        for (int i = 0; i < 50; i++) System.out.println();
        // ── Big ASCII art banner ──────────────────────────────────────
        System.out.println(CY + "  ============================================================" + R);
        System.out.println(BLD + CYB + "   ███████ ██████  ██    ██ ██████   ██████  ████████" + R);
        System.out.println(BLD + CYB + "   ██      ██   ██  ██  ██  ██   ██ ██    ██    ██   " + R);
        System.out.println(BLD + CYB + "   ███████ ██████    ████   ██████  ██    ██    ██   " + R);
        System.out.println(BLD + CYB + "        ██ ██         ██    ██   ██ ██    ██    ██   " + R);
        System.out.println(BLD + CYB + "   ███████ ██         ██    ██████   ██████     ██   " + R);
        System.out.println(CY + "  ============================================================" + R);
        System.out.println(BLD + WH + "        SECURE COMMUNICATION SYSTEM v2.0" + R);
        System.out.println(CY + "  ============================================================" + R);
        System.out.println();
        System.out.println("  " + WH + "System Version:    " + R + "2.0");
        System.out.println("  " + WH + "Network Topology:  " + R + "Triangular (A-B-C)");
        System.out.println("  " + WH + "Scale:             " + R + "1:40  (50cm = 2km real distance)");
        System.out.println();
        System.out.println("  " + CY + "Initializing system components..." + R);
        System.out.println();
        System.out.println("    " + GR + "v" + R + " Loading morse_dictionary.txt");
        System.out.println("    " + GR + "v" + R + " Initializing SwiftBot hardware   " + WH + "(SwiftBotAPI.INSTANCE - Singleton)" + R);
        System.out.println("    " + GR + "v" + R + " Configuring authentication module " + WH + "(Auth extends Module)" + R);
        System.out.println("    " + GR + "v" + R + " Setting up navigation system      " + WH + "(Navigator extends Module)" + R);
        System.out.println("    " + GR + "v" + R + " Loading logging infrastructure    " + WH + "(MorseEncoder)" + R);
        System.out.println();
        System.out.println("  " + GR + "System ready." + R);
        System.out.println();
        System.out.println("  " + YL + "+-------------------------------------------------------+" + R);
        System.out.println("  " + YL + "| /\\ ZERO-TRUST AUTHENTICATION REQUIRED               |" + R);
        System.out.println("  " + YL + "|                                                       |" + R);
        System.out.println("  " + YL + "| All operations require QR verification               |" + R);
        System.out.println("  " + YL + "| Maximum attempts: 3                                   |" + R);
        System.out.println("  " + YL + "| IMPOSTER protocol: Auto-activate                      |" + R);
        System.out.println("  " + YL + "+-------------------------------------------------------+" + R);
        System.out.println();
        System.out.println("    " + WH + "Safe House A = " + R + "Caesar      (Rome)");
        System.out.println("    " + WH + "Safe House B = " + R + "Garibaldi   (Sicily)");
        System.out.println("    " + WH + "Safe House C = " + R + "Machiavelli (Florence)");
        System.out.println();
    }

    // Shows boxed main menu with operator info
        private static void showMainMenu(Agent operator) {
        System.out.println("\n  " + CY + "============================================================" + R);
        System.out.println("  " + BLD + CYB + "          SPYBOT - SECURE COMMUNICATION SYSTEM" + R);
        System.out.println("  " + CY + "============================================================" + R);
        System.out.println();
        System.out.println("  " + GR + "Status: Authenticated v" + R);
        System.out.println("  " + WH + "User:     " + R + operator.getCallsign());
        System.out.println("  " + WH + "Location: " + R + "Safe House " + operator.getLocation()
            + " (" + operator.getCity() + ")");
        System.out.println("  " + WH + "Session:  " + R + GR + "Active" + R);
        System.out.println();
        System.out.println("  " + CY + "+----------------------------------------------------------+" + R);
        System.out.println("  " + CY + "| " + BLD + "MAIN MENU" + R + CY + "                                               |" + R);
        System.out.println("  " + CY + "|                                                          |" + R);
        System.out.println("  " + CY + "| " + R + WH + "[1]" + R + " Send Message                                     " + CY + "|" + R);
        System.out.println("  " + CY + "|     " + R + "Authenticate and encode message                   " + CY + "|" + R);
        System.out.println("  " + CY + "|                                                          |" + R);
        System.out.println("  " + CY + "| " + R + WH + "[2]" + R + " View Message History                             " + CY + "|" + R);
        System.out.println("  " + CY + "|     " + R + "Review communication logs                         " + CY + "|" + R);
        System.out.println("  " + CY + "|                                                          |" + R);
        System.out.println("  " + CY + "| " + R + WH + "[3]" + R + " Emergency Broadcast                              " + CY + "|" + R);
        System.out.println("  " + CY + "|     " + R + "Priority multi-destination alert                  " + CY + "|" + R);
        System.out.println("  " + CY + "|                                                          |" + R);
        System.out.println("  " + CY + "| " + R + WH + "[4]" + R + " Exit System                                      " + CY + "|" + R);
        System.out.println("  " + CY + "|     " + R + "Secure logout and shutdown                        " + CY + "|" + R);
        System.out.println("  " + CY + "|                                                          |" + R);
        System.out.println("  " + CY + "+----------------------------------------------------------+" + R);
        System.out.println();
        System.out.println("  " + YL + "Note: Each operation requires QR re-authentication" + R);
        System.out.println();
        System.out.print("  Enter your choice (1-4): ");
    }

    // Helper: returns seconds elapsed since startMs
        private static long elapsed(long startMs) {
        return (System.currentTimeMillis() - startMs) / 1000;
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
