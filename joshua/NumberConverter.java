public class NumberConverter {

    
    public int hexToDecimal(String hex) {

        hex = hex.toUpperCase();

        int result = 0;
        int power  = 0; 

       
        for (int i = hex.length() - 1; i >= 0; i--) {
            int digitValue = hexCharToValue(hex.charAt(i));
            result = result + (digitValue * raiseToPower(16, power));
            power++;
        }

        return result;
    }

  
    public int decimalToOctal(int decimal) {

        if (decimal == 0) {
            return 0;
        }

        String octalDigits = "";
        int remaining = decimal;

        while (remaining > 0) {
            int remainder = remaining % 8;
            octalDigits = remainder + octalDigits; // Prepend each digit
            remaining   = remaining / 8;
        }

        return stringDigitsToInt(octalDigits);
    }

   
    public String decimalToBinary(int decimal) {

        if (decimal == 0) {
            return "0";
        }

        String binaryDigits = "";
        int remaining = decimal;

        while (remaining > 0) {
            int remainder = remaining % 2;
            binaryDigits = remainder + binaryDigits; // Prepend digit
            remaining    = remaining / 2;
        }

        return binaryDigits;
    }

    
    private int hexCharToValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return 0; 
    }

 
    private int raiseToPower(int base, int exponent) {
        if (exponent == 0) {
            return 1;
        }
        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result = result * base;
        }
        return result;
    }


    private int stringDigitsToInt(String s) {
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            int digit = s.charAt(i) - '0';
            result = result * 10 + digit;
        }
        return result;
    }

}
