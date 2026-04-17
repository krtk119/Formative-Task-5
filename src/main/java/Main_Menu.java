import java.util.Scanner;

public class Main_Menu {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {

            System.out.println("\n========================================");
            System.out.println("   GROUP B32 - SwiftBot Integration");
            System.out.println("========================================");
            System.out.println("1.  Kartik     - SpyBot");
            System.out.println("2.  Fanice     - Traffic Light");
            System.out.println("3.  Kerry      - Mastermind");
            System.out.println("4.  Emmanuel   - ZigZag");
            System.out.println("5.  Joshua     - Dance");
            System.out.println("6.  Amaan      - Draw Shapes");
            System.out.println("7.  Aayan      - Pending");
            System.out.println("8.  Adeeb      - Pending");
            System.out.println("9.  Maqsura    - Pending");
            System.out.println("0.  Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            choice = Integer.parseInt(scanner.nextLine().trim());

            switch (choice) {
                case 1:
                    kartik.SpyBot.main(new String[]{});
                    break;
                case 2:
                    fanice.TrafficLightMain.main(new String[]{});
                    break;
                case 3:
                	  kerry.Main.main(new String[]{});
                    break;
                case 4:
                	  emmanuel.Main.main(new String[]{});
                    break;
                case 5:
                    joshua.DanceProgram.main(new String[]{});
                    break;
                case 6:
                	  amaan.Main.main(new String[]{});
                    break;
                case 7:
                    System.out.println("Aayan's code not yet integrated.");
                    break;
                case 8:
                    System.out.println("Adeeb's code not yet integrated.");
                    break;
                case 9:
                    System.out.println("Maqsura's code not yet integrated.");
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
    }
}
