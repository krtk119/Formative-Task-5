import swiftbot.SwiftBotAPI;
import java.util.Scanner;

    // Displays cycle performance after every completed send cycle
// CycleData inner class holds all timing values
    public class Analytics extends Module {

        // Inner class — groups all cycle timing data together
    public static class CycleData {
        public final String senderLocation;
        public final String receiverLocation;
        public final long   encSec;
        public final long   navSec;
        public final int    obstacles;
        public final long   obsSec;
        public final long   authSec;
        public final long   delivSec;
        public final long   retSec;
        public final long   totalSec;

        public CycleData(String senderLocation, String receiverLocation,
                         long encSec, long navSec,
                         int obstacles, long obsSec,
                         long authSec, long delivSec,
                         long retSec, long totalSec) {
            this.senderLocation   = senderLocation;
            this.receiverLocation = receiverLocation;
            this.encSec           = encSec;
            this.navSec           = navSec;
            this.obstacles        = obstacles;
            this.obsSec           = obsSec;
            this.authSec          = authSec;
            this.delivSec         = delivSec;
            this.retSec           = retSec;
            this.totalSec         = totalSec;
        }
    }

    // Private field: the data to display ───────────────────────────
    private final CycleData data;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public Analytics(SwiftBotAPI bot, Scanner keyboard, CycleData data) {
        super(bot, keyboard);
        this.data = data;
    }

    @Override
    public void run() {
        show();
    }

    /** Display the full performance breakdown. */
        // Colour-coded: GREEN<60s, YELLOW 60-90s, RED>90s
// Flags encoding as BOTTLENECK if >40% of total time
    public void show() {
        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "      CYCLE PERFORMANCE ANALYSIS" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();
        System.out.println("  " + WH + "Mission Complete: " + R + data.senderLocation
            + " " + CY + "->" + R + " " + data.receiverLocation
            + " " + CY + "->" + R + " " + data.senderLocation);
        System.out.println("  " + WH + "Total Time: " + R + data.totalSec + " seconds");

        // Colour-coded overall rating
        if (data.totalSec < 60) {
            pulseLED(GREEN,  1500);
            System.out.println("  " + GR + "Rating: [LED: GREEN]  FAST" + R);
        } else if (data.totalSec < 90) {
            pulseLED(YELLOW, 1500);
            System.out.println("  " + YL + "Rating: [LED: YELLOW]  GOOD" + R);
        } else {
            pulseLED(RED,    1500);
            System.out.println("  " + RD + "Rating: [LED: RED]  SLOW" + R);
        }

        System.out.println();
        System.out.println("  " + WH + "Time Breakdown:" + R);

        // Encoding
        String bottleneck = data.encSec > data.totalSec * 0.4 ? YL + " <-- BOTTLENECK" + R : "";
        System.out.printf("    " + CY + "Encoding:   " + R + " %3ds (%d%%)%s%n",
            data.encSec, pct(data.encSec, data.totalSec), bottleneck);

        // Navigation
        String obsNote = data.obstacles > 0
            ? YL + " - " + data.obstacles + " obstacle (" + data.obsSec + "s delay)" + R : "";
        System.out.printf("    " + CY + "Navigation: " + R + " %3ds (%d%%)%s%n",
            data.navSec, pct(data.navSec, data.totalSec), obsNote);

        // Auth
        String authNote = data.authSec <= 5 ? GR + " v OPTIMAL" + R : "";
        System.out.printf("    " + CY + "Auth:       " + R + GR + " %3ds (%d%%)%s%n" + R,
            data.authSec, pct(data.authSec, data.totalSec), authNote);

        System.out.printf("    " + CY + "Delivery:   " + R + " %3ds (%d%%)%n",
            data.delivSec, pct(data.delivSec, data.totalSec));
        System.out.printf("    " + CY + "Return:     " + R + " %3ds (%d%%)%n",
            data.retSec, pct(data.retSec, data.totalSec));

        // Obstacle rating
        System.out.println();
        if (data.obstacles == 0) {
            pulseLED(GREEN, 800);
            System.out.println("  " + GR + "Obstacles: none  v OPTIMAL" + R);
        } else {
            for (int i = 0; i < data.obstacles; i++) {
                pulseLED(AMBER, 300);
                sleep(200);
            }
            System.out.println("  " + YL + "Obstacles: "
                + data.obstacles + " [AMBER x" + data.obstacles + "]" + R);
        }

        System.out.println();
        System.out.println("  " + WH + "Logged to: " + R + CYCLE_LOG);
        System.out.println("  " + CY + "=========================================" + R);
    }

    /** Calculate percentage, guarding against divide-by-zero. */
        // Safe percentage calc — guards against divide by zero
    private int pct(long part, long total) {
        return total == 0 ? 0 : (int)(part * 100 / total);
    }
}
