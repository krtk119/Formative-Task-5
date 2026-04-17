package kartik;
import swiftbot.SwiftBotAPI; 
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

import kartik.Agent;

    // Handles QR authentication — extends Module for LED and sleep access
// Zero-trust: every operation re-authenticates
    public class Auth extends Module {

    // ── Private fields — set at construction, never changed ──────────
        // Role shown on screen: SEND, RECEIVE, or HISTORY
    private final String role;             // "SEND", "RECEIVE", "HISTORY"
        // If set, QR must match this location — enforces receiver identity
    private final String requiredLocation; // null = any location accepted

    // ── Result stored after successful authentication ─────────────────
    private Agent authenticatedAgent = null;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public Auth(SwiftBotAPI bot, Scanner keyboard,
                String role, String requiredLocation) {
        super(bot, keyboard);
        this.role             = role;
        this.requiredLocation = requiredLocation;
    }

    @Override
    public void run() {
        authenticatedAgent = authenticate();
    }

    public Agent getAuthenticatedAgent() {
        return authenticatedAgent;
    }

        // Main auth loop — up to 3 attempts, lockout between each
    public Agent authenticate() {
        showAuthScreen();

        for (int attempt = 1; attempt <= QR_ATTEMPTS; attempt++) {
            System.out.println("  " + WH + "Attempt " + attempt + " of " + QR_ATTEMPTS + R);
            System.out.println("  Hold QR within 15 cm of camera...\n");

            String raw = scanQR();

            if (raw == null || raw.trim().isEmpty()) {
                fail(attempt, "QR unreadable or blank.");
                if (attempt < QR_ATTEMPTS) lockout();
                continue;
            }

            String[] parsed = parseQR(raw.trim());
            if (parsed == null) {
                fail(attempt, "Invalid format. Expected: callsign:location");
                if (attempt < QR_ATTEMPTS) lockout();
                continue;
            }

            String cs  = parsed[0];
            String loc = parsed[1].toUpperCase();

            Agent found = Agent.find(cs, loc);
            if (found == null) {
                fail(attempt, "Agent not recognised.");
                if (attempt < QR_ATTEMPTS) lockout();
                continue;
            }

            if (requiredLocation != null
                    && !loc.equalsIgnoreCase(requiredLocation)) {
                fail(attempt, "Wrong agent. Message is for Safe House "
                    + requiredLocation + ".");
                if (attempt < QR_ATTEMPTS) lockout();
                continue;
            }

            showSuccess(found);
            return found;
        }

        triggerImposter();
        return null;
    }

        // Polls camera for 15s, falls back to keyboard if no QR detected
    private String scanQR() {
        // Try camera repeatedly for 15 seconds
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            try {
                BufferedImage img = bot.getQRImage();
                if (img != null) {
                    String decoded = bot.decodeQRImage(img);
                    if (decoded != null && !decoded.trim().isEmpty()) {
                        System.out.println("  " + GR + "[CAMERA] QR detected." + R);
                        return decoded.trim();
                    }
                }
            } catch (Exception e) {
                // camera unavailable — break out and fall to keyboard
                break;
            }
            // Show countdown every second
            long remaining = (deadline - System.currentTimeMillis()) / 1000;
            System.out.println("  " + CY + "Scanning... " + remaining + "s remaining" + R);
            sleep(1000);
        }
        // 15 seconds expired — fall back to keyboard
        System.out.println("  " + YL + "Camera could not read QR. Type it manually:" + R);
        System.out.println("  (format: callsign:location  e.g.  Caesar:A)");
        System.out.print("  >> ");
        return keyboard.nextLine().trim();
    }

        // Parses callsign:location format, also handles EMERGENCY: prefix
    public static String[] parseQR(String raw) {
        if (raw == null) return null;
        if (raw.toUpperCase().startsWith("EMERGENCY:")) {
            String[] p = raw.split(":", 3);
            if (p.length == 3 && Agent.isValidLocation(p[2].trim()))
                return new String[]{p[1].trim(), p[2].trim().toUpperCase(),
                                    "EMERGENCY"};
            return null;
        }
        String[] p = raw.split(":", 2);
        if (p.length != 2) return null;
        String loc = p[1].trim().toUpperCase();
        if (!Agent.isValidLocation(loc)) return null;
        return new String[]{p[0].trim(), loc};
    }

    private void showAuthScreen() {
        System.out.println("\n  " + CY + "=========================================================" + R);
        System.out.println("  " + BLD + CYB + "                    AUTHENTICATION" + R);
        System.out.println("  " + CY + "=========================================================" + R);
        System.out.println();
        System.out.println("  " + WH + "Instructions:" + R);
        System.out.println("    " + WH + "o" + R + " Position QR code within 15cm of scanner");
        System.out.println("    " + WH + "o" + R + " Ensure adequate lighting (>300 lux)");
        System.out.println("    " + WH + "o" + R + " Timeout: 15 seconds per attempt");
        System.out.println("    " + WH + "o" + R + " Maximum attempts: 3");
        System.out.println("    " + WH + "o" + R + " Format: callsign:location (e.g. Caesar:A)");
        System.out.println("    " + WH + "o" + R + " Role: " + YL + role + R);
        System.out.println("    " + WH + "o" + R + " Method: QR camera (keyboard fallback if camera fails)");
        System.out.println();
    }

        // Shows error message and pulses RED LED on failed attempt
    private void fail(int attempt, String reason) {
        // Exact wording from SRS FR1 Progressive Failure Handling
        System.out.println("\n  " + RD + "ERROR: INVALID QR CODE. " + reason + R);
        System.out.println("  Try again. Ensure code within 15cm and well lit.");
        System.out.println("  " + YL + "Attempt " + attempt + " of " + QR_ATTEMPTS + "." + R + "\n");
        pulseLED(RED, 200);
    }

        // 5-second lockout between attempts — prevents rapid guessing
    private void lockout() {
        // SRS FR1: "Authentication locked. Try in 5 seconds [5...4...3...2...1]"
        System.out.print("  " + YL + "Authentication locked. Try in " + AUTH_LOCKOUT_S + " seconds [");
        for (int i = AUTH_LOCKOUT_S; i >= 1; i--) {
            System.out.print(i + "...");
            sleep(1000);
        }
        System.out.println("]" + R + "\n");
    }

        // Green LED triple pulse + welcome message on successful auth
    private void showSuccess(Agent agent) {
        // SRS FR1 Success Flow: "AUTHENTICATION SUCCESSFUL. Welcome <callsign>. Press Enter."
        System.out.println("\n  " + GR + "v QR CODE DETECTED" + R);
        System.out.println();
        System.out.println("  " + BLD + GR + "AUTHENTICATION SUCCESSFUL" + R);
        System.out.println("  " + GR + "Welcome, " + agent.getCallsign() + "." + R);
        System.out.println("  " + GR + "Location: Safe House " + agent.getLocation()
            + " (" + agent.getCity() + ")" + R);
        System.out.println();
        System.out.println("  " + WH + "[LED: GREEN pulse x 3]" + R);
        System.out.println("       500ms ON, 300ms OFF, repeat 3 times");
        System.out.println();
        System.out.println("  Session initialized.");
        System.out.println("  Proceeding to main menu...");
        System.out.println();
        for (int i = 0; i < 3; i++) {
            setLED(GREEN); sleep(500); clearLED(); sleep(300);
        }
        System.out.print("  Press Enter to continue...");
        keyboard.nextLine();
    }

        // 3 failures: red strobe 5s, delete all logs, force shutdown
    private void triggerImposter() {
        System.out.println("\n  " + RD + "+==========================================+" + R);
        System.out.println("  " + RD + "| /\\ SECURITY ALERT /\\                    |" + R);
        System.out.println("  " + RD + "+==========================================+" + R);
        System.out.println();
        System.out.println("  " + BLD + RD + "IMPOSTER DETECTED" + R);
        System.out.println();
        System.out.println("  " + RD + "3 consecutive authentication failures detected." + R);
        System.out.println("  " + RD + "Zero-trust security protocol activated." + R);
        System.out.println();
        System.out.println("  " + YL + "ACTIVATING SECURITY PROTOCOL:" + R);
        System.out.println();
        System.out.println("  " + WH + "[LED: RED STROBE - 5 SECONDS]" + R);

        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            setLED(RED); sleep(100); clearLED(); sleep(100);
        }

        System.out.println("    " + RD + "[1] Purging all communication logs..." + R);
        System.out.println("    " + RD + "[2] Deleting authentication records..." + R);
        System.out.println("    " + RD + "[3] Erasing navigation data..." + R);
        System.out.println("    " + RD + "[4] Removing emergency logs..." + R);
        System.out.println("    " + RD + "[5] Clearing cycle analytics..." + R);
        System.out.println();

        for (String f : new String[]{COMM_LOG, CYCLE_LOG, EMRG_LOG}) {
            new File(f).delete();
        }

        sleep(1000);
        clearLED();
        System.out.println("  " + GR + "v SECURITY PROTOCOL COMPLETE" + R);
        System.out.println();
        System.out.println("  " + BLD + RD + "ALL RECORDS DELETED" + R);
        System.out.println("  System reset. Forensic log cleared.");
        System.out.println("  Forcing shutdown...");
        System.out.println();
        System.out.print("  Press any key to exit...");
        keyboard.nextLine();
        System.exit(0);
    }
}
