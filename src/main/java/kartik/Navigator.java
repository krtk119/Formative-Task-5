package kartik;
 import swiftbot.SwiftBotAPI; 
import java.util.Scanner;



    // Handles all robot movement — tracks current location, handles obstacles
// Geometric turn logic: 150deg turn + 30deg correction per edge
    public class Navigator extends Module {

        // Tracks where robot is — updated on every arrival
    private String currentLocation;
    private int    obstacleCount;
    private long   obstacleTotalMs;
    private String targetLocation;

    // ── ANSI colours ──────────────────────────────────────────────────
    private static final String R   = "\u001B[0m";
    private static final String CY  = "\u001B[36m";
    private static final String CYB = "\u001B[96m";
    private static final String GR  = "\u001B[32m";
    private static final String YL  = "\u001B[33m";
    private static final String RD  = "\u001B[31m";
    private static final String WH  = "\u001B[37m";
    private static final String BLD = "\u001B[1m";

    public Navigator(SwiftBotAPI bot, Scanner keyboard,
                     String startLocation) {
        super(bot, keyboard);
        this.currentLocation = startLocation;
        this.obstacleCount   = 0;
        this.obstacleTotalMs = 0;
    }

    public void setTarget(String target) {
        this.targetLocation = target;
    }

    @Override
    public void run() {
        if (targetLocation != null) {
            travelTo(targetLocation);
        }
    }

        // Moves robot from current location to target safe house
    public void travelTo(String target) {
        if (currentLocation.equals(target)) {
            System.out.println("  " + YL + "[NAV] Already at Safe House " + target + R);
            return;
        }

        Agent destAgent = Agent.findByLocation(target);

        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "       AUTONOMOUS NAVIGATION" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();
        System.out.println("  " + WH + "Route: Safe House " + currentLocation
            + " " + CY + "->" + R + " Safe House " + target + R);
        System.out.println("  " + WH + "Total Distance: 50cm" + R);
        System.out.println();
        System.out.println("  " + CY + "        [A]" + R);
        System.out.println("  " + CY + "       / | \\" + R);
        System.out.println("  " + CY + "      /  |  \\   >> Moving to destination" + R);
        System.out.println("  " + CY + "    [B]--+--[C]" + R);
        System.out.println("  " + CY + "  -----------------------------------------" + R);

        setLED(YELLOW);
        resetObstacleStats();

        boolean anticlockwise = isAnticlockwise(currentLocation, target);

        if (anticlockwise) {
            System.out.println("  " + YL + "[NAV] Turning 150 anticlockwise..." + R);
            turn(150, false);
        } else {
            System.out.println("  " + YL + "[NAV] Turning 150 clockwise..." + R);
            turn(150, true);
        }

        driveForward();

        if (anticlockwise) {
            System.out.println("  " + YL + "[NAV] Turning 30 clockwise to home position..." + R);
            turn(30, true);
        } else {
            System.out.println("  " + YL + "[NAV] Turning 30 anticlockwise to home position..." + R);
            turn(30, false);
        }

        clearLED();
        currentLocation = target;

        System.out.println();
        System.out.println("  " + GR + "v ARRIVED AT DESTINATION" + R);
        System.out.println("  " + GR + "Location: Safe House " + target
            + (destAgent != null ? " (" + destAgent.getCity() + ")" : "") + R);
        System.out.println("  Ready to deliver.");
    }

        // Returns robot to origin — sets all underlights to different colours
    public void returnTo(String origin) {
        System.out.println("\n  " + CY + "[NAV] Returning to Safe House "
            + origin + "..." + R);

        setAllUnderlights();

        Agent destAgent = Agent.findByLocation(origin);

        System.out.println("\n  " + CY + "=========================================" + R);
        System.out.println("  " + BLD + CYB + "       AUTONOMOUS NAVIGATION" + R);
        System.out.println("  " + CY + "=========================================" + R);
        System.out.println();
        System.out.println("  " + WH + "Route: Safe House " + currentLocation
            + " " + CY + "->" + R + " Safe House " + origin + R);
        System.out.println("  " + WH + "Total Distance: 50cm" + R);
        System.out.println();
        System.out.println("  " + CY + "        [A]" + R);
        System.out.println("  " + CY + "       / | \\" + R);
        System.out.println("  " + CY + "      /  |  \\   >> Returning to origin" + R);
        System.out.println("  " + CY + "    [B]--+--[C]" + R);
        System.out.println("  " + CY + "  -----------------------------------------" + R);

        boolean anticlockwise = !isAnticlockwise(origin, currentLocation);

        if (anticlockwise) {
            System.out.println("  " + YL + "[NAV] Turning 150 anticlockwise..." + R);
            turn(150, false);
        } else {
            System.out.println("  " + YL + "[NAV] Turning 150 clockwise..." + R);
            turn(150, true);
        }

        driveForward();

        if (anticlockwise) {
            System.out.println("  " + YL + "[NAV] Turning 30 clockwise to home position..." + R);
            turn(30, true);
        } else {
            System.out.println("  " + YL + "[NAV] Turning 30 anticlockwise to home position..." + R);
            turn(30, false);
        }

        clearLED();
        currentLocation = origin;

        System.out.println("\n  " + GR + "Returned to Safe House " + origin
            + ". Mission complete." + R);
    }

    public String getCurrentLocation() { return currentLocation; }
    public int    getObstacleCount()   { return obstacleCount; }
    public long   getObstacleTotalSeconds() { return obstacleTotalMs / 1000; }

    public void resetObstacleStats() {
        obstacleCount   = 0;
        obstacleTotalMs = 0;
    }

        // A->B, B->C, C->A are anticlockwise on the physical triangle
    private boolean isAnticlockwise(String from, String to) {
        if (from.equals("A") && to.equals("B")) return true;
        if (from.equals("B") && to.equals("C")) return true;
        if (from.equals("C") && to.equals("A")) return true;
        return false;
    }

        // Converts degrees to milliseconds using TURN_120_MS calibration constant
    private void turn(int degrees, boolean clockwise) {
        int ms = (int)(TURN_120_MS * (degrees / 120.0));
        try {
            if (clockwise) {
                bot.startMove( WHEEL_SPEED + WHEEL_TRIM,
                              -(WHEEL_SPEED - WHEEL_TRIM));
            } else {
                bot.startMove(-(WHEEL_SPEED + WHEEL_TRIM),
                               WHEEL_SPEED - WHEEL_TRIM);
            }
            sleep(ms);
            bot.stopMove();
        } catch (Exception e) {
            System.out.println("  " + RD + "[!] Turn error: " + e.getMessage() + R);
        }
        sleep(300);
    }

        // Drives 50cm forward — checks for obstacles every 500ms
    private void driveForward() {
        long start  = System.currentTimeMillis();
        long finish = start + DRIVE_50CM_MS;

        try {
            bot.startMove(WHEEL_SPEED + WHEEL_TRIM,
                          WHEEL_SPEED - WHEEL_TRIM);
        } catch (Exception e) {}

        while (System.currentTimeMillis() < finish) {
            double dist = getDistance();
            if (dist > 0 && dist < OBSTACLE_CM) {
                try { bot.stopMove(); } catch (Exception e) {}
                handleObstacle(dist);
                setLED(YELLOW);
                try {
                    bot.startMove(WHEEL_SPEED + WHEEL_TRIM,
                                  WHEEL_SPEED - WHEEL_TRIM);
                } catch (Exception e) {}
                finish += OBSTACLE_WAIT_S * 1000L;
            }
            long el  = System.currentTimeMillis() - start;
            int  pct = (int) Math.min(100, el * 100 / DRIVE_50CM_MS);
            int  cm  = (int) Math.min(50,  50.0 * el / DRIVE_50CM_MS);
            System.out.println("  " + GR + "Progress: [" + progressBar(pct)
                + "] " + pct + "%" + R + "  (" + cm + "cm / 50cm)");
            sleep(500);
        }
        try { bot.stopMove(); } catch (Exception e) {}
    }

        // Stops, waits 10s, re-checks — loops until path is clear
    private void handleObstacle(double d) {
        obstacleCount++;
        long waitStart = System.currentTimeMillis();

        System.out.println();
        System.out.println("  " + YL + "+-----------------------------------------+" + R);
        System.out.println("  " + BLD + YL + "| /\\ OBSTACLE DETECTED                    |" + R);
        System.out.println("  " + YL + "+-----------------------------------------+" + R);
        System.out.printf("  " + YL + "| Distance: %.1f cm (threshold: %d cm)%n" + R, d, OBSTACLE_CM);
        System.out.println("  " + YL + "| Action: Waiting for clearance           |" + R);
        System.out.println("  " + YL + "| Retry: Infinite (until clear)           |" + R);
        System.out.println("  " + YL + "+-----------------------------------------+" + R);
        System.out.println();
        System.out.println("  " + WH + "[LED: YELLOW ON]" + R);
        System.out.println();
        System.out.println("  System: Obstacle negotiation in progress.");
        System.out.println("          Will retry automatically after countdown...");
        setLED(YELLOW);

        boolean clear = false;
        while (!clear) {
            System.out.print("  " + YL + "Countdown: [");
            for (int i = OBSTACLE_WAIT_S; i >= 1; i--) {
                System.out.print(i + "...");
                sleep(1000);
            }
            System.out.println("0]" + R);
            double nd = getDistance();
            if (nd <= 0 || nd >= OBSTACLE_CM) {
                clear = true;
                System.out.println("  " + GR + "[OK] Path clear. Resuming." + R);
            } else {
                System.out.printf("  " + YL + "Still blocked (%.1f cm). Retrying...%n" + R, nd);
            }
        }
        obstacleTotalMs += (System.currentTimeMillis() - waitStart);
    }

        // Sets 6 underlights to 6 different colours — used during return journey
    private void setAllUnderlights() {
        try {
            bot.setUnderlight(swiftbot.Underlight.FRONT_LEFT,
                new int[]{255, 255, 255});
            bot.setUnderlight(swiftbot.Underlight.FRONT_RIGHT,
                new int[]{0, 0, 255});
            bot.setUnderlight(swiftbot.Underlight.MIDDLE_LEFT,
                new int[]{255, 191, 0});
            bot.setUnderlight(swiftbot.Underlight.MIDDLE_RIGHT,
                new int[]{255, 0, 0});
            bot.setUnderlight(swiftbot.Underlight.BACK_LEFT,
                new int[]{0, 255, 0});
            bot.setUnderlight(swiftbot.Underlight.BACK_RIGHT,
                new int[]{255, 255, 255});
        } catch (Exception e) {}
    }

    private String progressBar(int pct) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++)
            sb.append(i < pct / 10 ? "=" : " ");
        return sb.toString();
    }
}
