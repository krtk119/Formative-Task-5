package kartik;
import swiftbot.SwiftBotAPI;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



    // Handles all file I/O — writes to spybot_communications.txt and cycle_analysis_log.txt
// Pipe-delimited format: id|sender|location|receiver|location|text|sent|delivered|status
    public class Logger extends Module {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Logger(SwiftBotAPI bot, Scanner keyboard) {
        super(bot, keyboard);
    }

   
    @Override
    public void run() {
        
    }

        // Appends PENDING entry when message is encoded
    public void logPending(Message msg) {
        append(COMM_LOG, msg.toLogLine() + "\n");
        System.out.println("  [LOG] Pending entry: " + msg.getId());
    }

    
        // Finds existing entry by ID and updates to DELIVERED with timestamp
    public void logDelivered(Message msg) {
        List<String> lines = readLines(COMM_LOG);
        List<String> out   = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith(msg.getId() + "|")) {
                line = msg.toLogLine();   // updated line with DELIVERED status
            }
            out.add(line);
        }
        writeLines(COMM_LOG, out);
        System.out.println("  [LOG] " + msg.getId()
            + " -> DELIVERED at " + msg.getDeliveredTime());
    }

   
        // Reads all messages from log file — used by HistoryModule
    public List<Message> getAllMessages() {
        List<Message> result = new ArrayList<>();
        for (String line : readLines(COMM_LOG)) {
            if (line.trim().isEmpty()) continue;
            Message m = Message.fromLogLine(line);
            if (m != null) result.add(m);
        }
        return result;
    }

        // Appends cycle performance data to cycle_analysis_log.txt
    public void logCycle(String sender, String receiver,
                         long encSec, long navSec,
                         int obstacles, long obsSec,
                         long authSec, long delivSec,
                         long retSec, long totalSec) {
        String line = "CYC" + System.currentTimeMillis()
            + "|" + now()
            + "|" + sender
            + "|" + receiver
            + "|" + encSec
            + "|" + navSec
            + "|" + obstacles
            + "|" + obsSec
            + "|" + authSec
            + "|" + delivSec
            + "|" + retSec
            + "|" + totalSec
            + "\n";
        append(CYCLE_LOG, line);
    }

   
        // Appends emergency broadcast record to emergency_log.txt
    public void logEmergency(Agent agent, String message, String dests) {
        append(EMRG_LOG, now()
            + "|EMERGENCY"
            + "|" + agent.getCallsign()
            + "|" + dests
            + "|" + message
            + "\n");
    }

    // ── Private file I/O helpers ──────────────────────────────────────

    /** Append a single line to a file. Creates the file if it does not exist. */
        // Creates file if missing, appends text
    private void append(String path, String text) {
        try (FileWriter fw = new FileWriter(path, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(text);
        } catch (IOException e) {
            System.out.println("  [LOG ERROR] " + e.getMessage());
        }
    }

    /** Read all lines from a file. Returns empty list if file missing. */
        // Returns empty list if file does not exist — safe read
    private List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException ignored) {}
        return lines;
    }

    /** Overwrite a file with the given lines. */
        // Overwrites file with updated lines — used by logDelivered
    private void writeLines(String path, List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
            for (String line : lines) pw.println(line);
        } catch (IOException e) {
            System.out.println("  [LOG ERROR] " + e.getMessage());
        }
    }

    /** Current timestamp formatted for log entries. */
    private String now() {
        return LocalDateTime.now().format(FMT);
    }
}
