package joshua;
public class HexValidator {

    
    public boolean validate(String hex) {

        if (hex == null || hex.isEmpty()) {
            return false;
        }

        hex = hex.trim();

        if (hex.length() < 1 || hex.length() > 2) {
            return false;
        }

        hex = hex.toUpperCase();

        for (int i = 0; i < hex.length(); i++) {
            if (!isValidHexChar(hex.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public String normalise(String hex) {
        return hex.trim().toUpperCase();
    }


    public int getLength(String hex) {
        return hex.trim().length();
    }

  
    private boolean isValidHexChar(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c >= 'A' && c <= 'F') {
            return true;
        }
        return false;
    }

}
