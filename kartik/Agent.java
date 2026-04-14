
// Data model for safe house agents — immutable, private constructor
// Static registry pattern: callers use find() not new Agent()
public class Agent {

    // ──Private fields — not accessible outside this class ───────────
    private final String callsign;   // e.g. "Caesar"
    private final String location;   // "A", "B", or "C"
    private final String city;       // e.g. "Rome"

    // Static registry — shared across all instances, defined once here.
    // Fixed registry of 3 agents — A=Caesar, B=Garibaldi, C=Machiavelli
private static final Agent[] REGISTRY = {
          new Agent("Caesar",      "A", "Rome"),
          new Agent("Garibaldi",   "B", "Sicily"),
          new Agent("Machiavelli", "C", "Florence")
     };

  
    private Agent(String callsign, String location, String city) {
        this.callsign = callsign;
        this.location = location;
        this.city     = city;
    }

    public String getCallsign() { return callsign; }

    public String getLocation() { return location; }

    public String getCity()    { return city; }

   
    // Find agent by both callsign AND location — used for QR validation
public static Agent find(String callsign, String location) {
        for (Agent a : REGISTRY) {
            if (a.callsign.equalsIgnoreCase(callsign)
                    && a.location.equalsIgnoreCase(location)) {
                return a;
            }
        }
        return null;
    }

    
    // Find agent by location only — used to look up destination agent
public static Agent findByLocation(String location) {
        for (Agent a : REGISTRY) {
            if (a.location.equalsIgnoreCase(location)) return a;
        }
        return null;
    }

    
    // Validates location is A, B, or C — used in QR parsing
public static boolean isValidLocation(String location) {
        return location != null &&
               (location.equalsIgnoreCase("A") ||
                location.equalsIgnoreCase("B") ||
                location.equalsIgnoreCase("C"));
    }

    public String toString() {
        return callsign + " @ Safe House " + location + " (" + city + ")";
    }
}
