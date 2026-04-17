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
            System.out.println("5.  Joshua     - Dance Program");
            System.out.println("6.  Amaan      - Coming soon");
            System.out.println("7.  Aayan      - Coming soon");
            System.out.println("8.  Adeeb      - Coming soon");
            System.out.println("9.  Maqsura    - Coming soon");
            System.out.println("10. Hanifah    - Coming soon");
            System.out.println("11. Damilola   - Coming soon");
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
                    // TO ADD: kerry.MastermindMain.main(new String[]{});
                    System.out.println("Kerry's code not yet integrated.");
                    break;
                case 4:
                    // TO ADD: emmanuel.ZigZagMain.main(new String[]{});
                    System.out.println("Emmanuel's code not yet integrated.");
                    break;
                case 5:
                    joshua.DanceProgram.main(new String[]{});
                    break;
                case 6:
                    // TO ADD: amaan.[ClassName].main(new String[]{});
                    System.out.println("Amaan's code not yet integrated.");
                    break;
                case 7:
                    // TO ADD: aayan.[ClassName].main(new String[]{});
                    System.out.println("Aayan's code not yet integrated.");
                    break;
                case 8:
                    // TO ADD: adeeb.[ClassName].main(new String[]{});
                    System.out.println("Adeeb's code not yet integrated.");
                    break;
                case 9:
                    // TO ADD: maqsura.[ClassName].main(new String[]{});
                    System.out.println("Maqsura's code not yet integrated.");
                    break;
                case 10:
                    // TO ADD: hanifah.[ClassName].main(new String[]{});
                    System.out.println("Hanifah's code not yet integrated.");
                    break;
                case 11:
                    // TO ADD: damilola.[ClassName].main(new String[]{});
                    System.out.println("Damilola's code not yet integrated.");
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
