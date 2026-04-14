import swiftbot.SwiftBotAPI;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

        // Broadcasts urgent message to ALL other safe houses
// Uses background thread for continuous red strobe during broadcast
        public class EmergencyModule extends Module {
   // ── Injected dependencies ─────────────────────────────────────────
    private final Navigator    navigator;
    private final MorseEncoder encoder;
    private final Logger       logger;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public EmergencyModule(SwiftBotAPI bot, Scanner keyboard,
                           Navigator navigator,
                           MorseEncoder encoder,
                           Logger logger) {
        super(bot, keyboard);
        this.navigator = navigator;
        this.encoder   = encoder;
        this.logger    = logger;
    }

    @Override
            // Steps: scan emergency QR -> validate -> build message -> strobe -> broadcast -> return
        public void run() {
        System.out.println("\n  " + RD + "+==========================================+" + R);
        System.out.println("  " + BLD + RD + "| /\\ EMERGENCY BROADCAST MODE ACTIVE /\\ |" + R);
        System.out.println("  " + RD + "+==========================================+" + R);
        System.out.println();
        System.out.println("  " + RD + "Scan EMERGENCY QR code:" + R);
        System.out.println("  " + WH + "Format: EMERGENCY:callsign:location" + R);
        System.out.println();

        // ── Step 1: Scan emergency QR ─────────────────────────────────
        String raw = null;
        try {
            BufferedImage img = bot.getQRImage();
            if (img != null) raw = bot.decodeQRImage(img);
        } catch (Exception e) { /* camera unavailable */ }

        if (raw == null || raw.trim().isEmpty()) {
            System.out.print("  " + YL + "Camera timeout. Type QR: " + R);
            raw = keyboard.nextLine().trim();
        }

        // ── Step 2: Parse and validate ────────────────────────────────
        String[] parsed = Auth.parseQR(raw.trim());
        if (parsed == null || parsed.length < 3
                || !parsed[2].equals("EMERGENCY")) {
            System.out.println("  " + RD + "[!] Not an emergency QR. Aborted." + R);
            return;
        }

        Agent emAgent = Agent.find(parsed[0], parsed[1]);
        if (emAgent == null) {
            System.out.println("  " + RD + "[!] Agent not recognised. Aborted." + R);
            return;
        }

        System.out.println("  " + BLD + RD + "EMERGENCY ACTIVATED: " + R + RD + emAgent + R);
        System.out.println("  " + WH + "Sender: " + R + emAgent.getCallsign()
            + " (Safe House " + emAgent.getLocation() + ")");
        System.out.println("  " + WH + "Priority: " + R + RD + "URGENT - Time Critical" + R);
        System.out.println("  " + WH + "Protocol: No confirmation required" + R);
        System.out.println();
        System.out.print("  Enter emergency message: ");
        String userMsg = keyboard.nextLine().trim().toUpperCase();
        String fullMsg = "URGENT: " + emAgent.getCallsign() + " " + userMsg;
        System.out.println("  " + RD + "Message: " + fullMsg + R);
        System.out.println();

        // ── Step 3: Determine all OTHER destinations ──────────────────
                // Build list of all destinations EXCEPT sender's location
        List<String> dests = new ArrayList<>();
        for (String loc : new String[]{"A", "B", "C"}) {
            if (!loc.equals(emAgent.getLocation())) dests.add(loc);
        }
        logger.logEmergency(emAgent, fullMsg, dests.toString());

        System.out.println("  " + WH + "Broadcast Progress:" + R);
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        for (String loc : new String[]{"A", "B", "C"}) {
            if (!loc.equals(emAgent.getLocation())) {
                System.out.println("  " + CY + "| " + R + YL + "o " + R + "Safe House " + loc
                    + "    " + YL + "[PENDING]" + R + "              " + CY + "|" + R);
            }
        }
        System.out.println("  " + CY + "+---------------------------------------+" + R);
        System.out.println();
        System.out.println("  " + BLD + RD + "[LED: RED STROBE - 5Hz CONTINUOUS]" + R);

        // ── Step 4: Red strobe in background thread ───────────────────
                // Array used so lambda can modify the flag — Java closure limitation
        final boolean[] strobing = {true};
                // Background thread strobes red LED throughout entire broadcast
        new Thread(() -> {
            while (strobing[0]) {
                setLED(RED); sleep(100); clearLED(); sleep(100);
            }
        }).start();

        // ── Step 5: Broadcast to each destination ─────────────────────
                // Deliver to each destination in sequence — zero dwell between stops
        for (String destLoc : dests) {
            System.out.println("\n  " + RD + "--> Safe House " + destLoc
                + " (zero-dwell transit)" + R);
            System.out.println("  " + WH + "  o Navigating to Safe House " + destLoc + R);
            System.out.println("  " + WH + "  o Zero-dwell transit (no 10s wait)" + R);

            navigator.travelTo(destLoc);

            // Authenticate receiver at this safe house
            Auth auth = new Auth(bot, keyboard, "RECEIVE", destLoc);
            Agent receiver = auth.authenticate();
            if (receiver == null) {
                strobing[0] = false;
                clearLED();
                return;
            }

            // Build a synthetic Message for this delivery leg
            Message emergencyMsg = new Message(emAgent, receiver, fullMsg);
            Delivery delivery = new Delivery(bot, keyboard,
                emergencyMsg, encoder);
            delivery.run();
        }

        // ── Step 6: Return home ───────────────────────────────────────
                // Return robot to original sender after all deliveries complete
        navigator.returnTo(emAgent.getLocation());
        strobing[0] = false;
        clearLED();
        System.out.println("\n  " + GR + "[OK] EMERGENCY BROADCAST COMPLETE." + R);
    }
}
