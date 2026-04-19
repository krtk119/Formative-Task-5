import java.util.Scanner;

public class Main_Menu {

	@FunctionalInterface
	private interface TaskLauncher {
		void launch() throws Exception;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		IntegratedUIDisplay display = new IntegratedUIDisplay();

		display.showWelcomeScreen();
		scanner.nextLine();

		boolean running = true;
		while (running) {

			display.showMainMenu();
			display.showInputPrompt();

			String rawInput = scanner.nextLine().trim();

			int choice;
			try {
				choice = Integer.parseInt(rawInput);
			} catch (NumberFormatException e) {
				display.showNonNumericError(rawInput);
				continue;
			}

			switch (choice) {

			case 1:
				runTask(display, "Kartik", "SpyBot", "Covert spy messaging programme",
						() -> kartik.SpyBot.main(new String[] {}));
				break;

			case 2:
				runTask(display, "Fanice", "Traffic Light", "Traffic-light based SwiftBot navigation system",
						() -> fanice.TrafficLightMain.main(new String[] {}));
				break;

			case 3:
				runTask(display, "Kerry", "Mastermind", "Classic Mastermind code-breaking game",
						() -> kerry.Main.main(new String[] {}));
				break;

			case 4:
				runTask(display, "Emmanuel", "ZigZag", "Zigzag journey planner driven by a QR code",
						() -> emmanuel.Main.main(new String[] {}));
				break;

			case 5:
				runTask(display, "Joshua", "Dance", "Hex-driven SwiftBot dance routine programme",
						() -> joshua.DanceProgram.main(new String[] {}));
				break;

			case 6:
				runTask(display, "Amaan", "Draw Shapes", "QR-code shape drawing with the SwiftBot",
						() -> amaan.Main.main(new String[] {}));
				break;

			case 7:
				display.showPendingTaskMessage("Aayan");
				break;

			case 8:
				display.showPendingTaskMessage("Adeeb");
				break;

			case 9:
				display.showPendingTaskMessage("Maqsura");
				break;

			case 10:
				display.showHelpScreen();
				display.showPressEnterPrompt();
				scanner.nextLine();
				break;

			case 11:
				display.showAboutScreen();
				display.showPressEnterPrompt();
				scanner.nextLine();
				break;

			case 0:

				if (confirmExit(display, scanner)) {
					running = false;
				} else {
					display.showExitCancelled();
				}
				break;

			default:
				display.showInvalidChoice(choice);
			}
		}

		display.showGoodbyeScreen();
		scanner.close();
	}

	private static void runTask(IntegratedUIDisplay display, String author, String taskName, String description,
			TaskLauncher launcher) {

		display.showTaskLaunching(author, taskName, description);

		try {
			launcher.launch();
			display.showTaskReturning(author, taskName);
		} catch (Exception ex) {

			String message = (ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getSimpleName();
			display.showTaskCrashMessage(author, message);
		}
	}

	private static boolean confirmExit(IntegratedUIDisplay display, Scanner scanner) {
		display.showExitConfirmationPrompt();
		String response = scanner.nextLine().trim().toUpperCase();
		return response.equals("Y") || response.equals("YES");
	}
}
