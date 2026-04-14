import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

    // Data model for a single message — encapsulates all fields privately
// Status starts PENDING, only markDelivered() can change it
    public class Message {

    // ── Formatter shared across all Message instances ─────────────────
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Private fields — all message data locked away ─────────────────
    private final String id;           // unique message ID, e.g. MSG1234567890
    private final Agent  sender;       // who sent it
    private final Agent  receiver;     // who receives it
    private final String plainText;    // the decoded message body
    private final String encodedTime;  // when it was encoded/recorded
    private       String deliveredTime;// when it was delivered (set later)
    private       String status;       // "PENDING" or "DELIVERED"

    public Message(Agent sender, Agent receiver, String plainText) {
            // Unique ID from timestamp — guaranteed unique per session
    this.id           = "MSG" + System.currentTimeMillis();
        this.sender       = sender;
        this.receiver     = receiver;
        this.plainText    = plainText;
        this.encodedTime  = LocalDateTime.now().format(FMT);
        this.deliveredTime = "";
        this.status        = "PENDING";
    }

    /**
     * Constructor used when reconstructing a Message from a log file.
     * All fields provided explicitly.
     */
    public Message(String id, Agent sender, Agent receiver,
                   String plainText, String encodedTime,
                   String deliveredTime, String status) {
        this.id            = id;
        this.sender        = sender;
        this.receiver      = receiver;
        this.plainText     = plainText;
        this.encodedTime   = encodedTime;
        this.deliveredTime = deliveredTime;
        this.status        = status;
    }

    // ── Controlled state change — the only way to mark delivery ───────

    /**
     * Mark this message as delivered and record the delivery timestamp.
     * This is the ONLY way to change message status — enforces integrity.
     */
        // Only way to mark delivered — enforces data integrity
    public void markDelivered() {
        this.deliveredTime = LocalDateTime.now().format(FMT);
        this.status        = "DELIVERED";
    }

    // ── Public getters ────────────────────────────────────────────────

    public String getId()            { return id; }
    public Agent  getSender()        { return sender; }
    public Agent  getReceiver()      { return receiver; }
    public String getPlainText()     { return plainText; }
    public String getEncodedTime()   { return encodedTime; }
    public String getDeliveredTime() { return deliveredTime; }
    public String getStatus()        { return status; }
    public boolean isDelivered()     { return status.equals("DELIVERED"); }

        // Serialises message to pipe-delimited string for log file
    public String toLogLine() {
        return id
            + "|" + sender.getCallsign()
            + "|" + sender.getLocation()
            + "|" + receiver.getCallsign()
            + "|" + receiver.getLocation()
            + "|" + plainText
            + "|" + encodedTime
            + "|" + deliveredTime
            + "|" + status;
    }

        // Deserialises message from log file line — used by HistoryModule
    public static Message fromLogLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] p = line.split("\\|", -1);
        if (p.length < 9) return null;
        Agent sender   = Agent.find(p[1], p[2]);
        Agent receiver = Agent.find(p[3], p[4]);
        if (sender == null)   sender   = Agent.findByLocation(p[2]);
        if (receiver == null) receiver = Agent.findByLocation(p[4]);
        if (sender == null || receiver == null) return null;
        return new Message(p[0], sender, receiver, p[5], p[6], p[7], p[8]);
    }

    @Override
    public String toString() {
        return "[" + id + "] "
            + sender.getCallsign() + " -> " + receiver.getCallsign()
            + " | " + status
            + " | '" + plainText + "'";
    }
}
