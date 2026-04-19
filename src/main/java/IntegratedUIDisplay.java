public class IntegratedUIDisplay {

	// Group and module identifiers - kept as constants so they only need
	// changing in one place if the team number or cohort ever changes
	public static final String GROUP_NUMBER = "B32";
	public static final String MODULE_CODE = "CS1704";

	// ANSI colour codes
	static final String RESET = "\u001B[0m";
	static final String BOLD = "\u001B[1m";
	static final String DIM = "\u001B[2m";
	static final String RED = "\u001B[31m";
	static final String GREEN = "\u001B[32m";
	static final String YELLOW = "\u001B[33m";
	static final String BLUE = "\u001B[34m";
	static final String MAGENTA = "\u001B[35m";
	static final String CYAN = "\u001B[36m";
	static final String WHITE = "\u001B[37m";

	// Bright ANSI colour codes
	static final String BRIGHT_RED = "\u001B[91m";
	static final String BRIGHT_GREEN = "\u001B[92m";
	static final String BRIGHT_YELLOW = "\u001B[93m";
	static final String BRIGHT_BLUE = "\u001B[94m";
	static final String BRIGHT_MAGENTA = "\u001B[95m";
	static final String BRIGHT_CYAN = "\u001B[96m";

	// Separator lines used across every screen - same width everywhere so the
	// layout looks tidy and the sections never drift out of alignment
	static final String SEPARATOR = CYAN + "============================================================" + RESET;
	static final String SUB_SEPARATOR = CYAN + "------------------------------------------------------------" + RESET;

	public void showWelcomeScreen() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_CYAN + "              SWIFTBOT INTEGRATION HUB" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BRIGHT_GREEN + " ____ _________     _   _ _   _ ____  " + RESET);
		System.out.println(BRIGHT_GREEN + "| __ )___ /___ \\   | | | | | | | __ ) " + RESET);
		System.out.println(BRIGHT_GREEN + "|  _ \\ |_ \\ __) |  | |_| | | | |  _ \\ " + RESET);
		System.out.println(BRIGHT_GREEN + "| |_) |__) / __/   |  _  | |_| | |_) |" + RESET);
		System.out.println(BRIGHT_GREEN + "|____/____/_____|  |_| |_|\\___/|____/ " + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "  Welcome to the Group " + GROUP_NUMBER + " integrated SwiftBot programme." + RESET);
		System.out.println();
		System.out.println("  This unified application brings together the individual");
		System.out.println("  SwiftBot implementations from every group member into one");
		System.out.println("  menu-driven programme. Select any task from the main menu");
		System.out.println("  and it will run without you ever needing to leave the app.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "  [MODULE]" + RESET + " " + MODULE_CODE + " Group Project");
		System.out.println(CYAN + "  [GROUP] " + RESET + " " + GROUP_NUMBER);
		System.out.println(
				YELLOW + "  [STATE] " + RESET + "IDLE - Press " + BOLD + "ENTER" + RESET + " to open the main menu");
		System.out.println(SEPARATOR);
	}

	public void showMainMenu() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_CYAN + "                      MAIN MENU" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "Awaiting user selection");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + MAGENTA + "  INTEGRATED TASKS" + RESET);
		System.out.println(
				GREEN + "    [1]" + RESET + "  Kartik    - SpyBot           " + DIM + "(Covert Messaging)" + RESET);
		System.out.println(
				GREEN + "    [2]" + RESET + "  Fanice    - Traffic Light    " + DIM + "(Navigation System)" + RESET);
		System.out.println(
				GREEN + "    [3]" + RESET + "  Kerry     - Mastermind       " + DIM + "(Code-Breaking Game)" + RESET);
		System.out.println(
				GREEN + "    [4]" + RESET + "  Emmanuel  - ZigZag           " + DIM + "(Journey Planner)" + RESET);
		System.out.println(
				GREEN + "    [5]" + RESET + "  Joshua    - Dance            " + DIM + "(Hex-Driven Routines)" + RESET);
		System.out.println(
				GREEN + "    [6]" + RESET + "  Amaan     - Draw Shapes      " + DIM + "(QR-Based Drawing)" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + MAGENTA + "  PENDING TASKS " + DIM + "(not yet integrated)" + RESET);
		System.out.println(DIM + YELLOW + "    [7]" + RESET + DIM + "  Aayan     - Pending" + RESET);
		System.out.println(DIM + YELLOW + "    [8]" + RESET + DIM + "  Adeeb     - Pending" + RESET);
		System.out.println(DIM + YELLOW + "    [9]" + RESET + DIM + "  Maqsura   - Pending" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + MAGENTA + "  OTHER OPTIONS" + RESET);
		System.out.println(CYAN + "   [10]" + RESET + "  Help      " + DIM + "(navigation instructions)" + RESET);
		System.out.println(CYAN + "   [11]" + RESET + "  About     " + DIM + "(group information)" + RESET);
		System.out.println(RED + "    [0]" + RESET + "  Exit      " + DIM + "(shut down the programme)" + RESET);
		System.out.println(SEPARATOR);
	}

	public void showInputPrompt() {
		System.out.print(BRIGHT_YELLOW + "Enter your choice: " + RESET);
	}

	public void showTaskLaunching(String author, String taskName, String description) {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_GREEN + "            LAUNCHING TASK" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "HANDING OVER TO TASK");
		System.out.println(BOLD + "  Author      :" + RESET + " " + author);
		System.out.println(BOLD + "  Task name   :" + RESET + " " + taskName);
		System.out.println(BOLD + "  Description :" + RESET + " " + description);
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " Control will now pass to the task's own interface.");
		System.out.println(CYAN + "[INFO]" + RESET + " You will return to this menu once the task finishes.");
		System.out.println(SEPARATOR);
		System.out.println();
	}

	public void showTaskReturning(String author, String taskName) {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_GREEN + "            TASK COMPLETED" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "RETURNED TO INTEGRATION HUB");
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " " + author + "'s task " + BOLD + "'" + taskName + "'"
				+ RESET + " has finished.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " You are back at the main menu.");
		System.out
				.println(CYAN + "[INFO]" + RESET + " Select another task or press " + BOLD + "0" + RESET + " to exit.");
		System.out.println(SEPARATOR);
	}

	public void showTaskCrashMessage(String author, String errorMessage) {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_RED + "   [ERROR] TASK CRASHED UNEXPECTEDLY" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(RED + "What went wrong:" + RESET);
		System.out.println("  " + author + "'s task threw an unexpected exception.");
		System.out.println("  Message: " + BOLD + errorMessage + RESET);
		System.out.println(RED + "What happens next:" + RESET);
		System.out.println("  The error has been contained - the menu is still running.");
		System.out.println("  You can try another task or exit the programme.");
		System.out.println(CYAN + "[INFO]" + RESET + " If this repeats, let the task's author know so");
		System.out.println("       they can investigate their individual implementation.");
		System.out.println(SEPARATOR);
	}

	public void showInvalidChoice(int choice) {
		System.out.println();
		System.out.println(
				BRIGHT_RED + "[ERROR]" + RESET + " '" + BOLD + choice + RESET + "' is not a valid menu option.");
		System.out.println(CYAN + "[INFO] " + RESET + " Please choose a number from the menu: 0 to 11.");
	}

	public void showNonNumericError(String input) {
		System.out.println();
		System.out.println(BRIGHT_RED + "[ERROR]" + RESET + " '" + BOLD + input + RESET + "' is not a number.");
		System.out.println(CYAN + "[INFO] " + RESET + " Please type a whole number, then press ENTER.");
	}

	public void showPendingTaskMessage(String author) {
		System.out.println();
		System.out.println(BRIGHT_YELLOW + "[NOTICE]" + RESET + " " + author + "'s task has not been integrated yet.");
		System.out.println(CYAN + "[INFO]  " + RESET + " This slot will be filled in a future revision.");
		System.out.println(CYAN + "[INFO]  " + RESET + " For now, please pick one of the integrated tasks.");
	}

	public void showHelpScreen() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_CYAN + "                   HELP - HOW TO USE" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "How the menu works:" + RESET);
		System.out.println("  Each option on the main menu has a number next to it");
		System.out.println("  in square brackets. Type that number and press ENTER");
		System.out.println("  to run the option.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "What each option does:" + RESET);
		System.out.println("  " + GREEN + "[1] to [6]" + RESET + "  Run one of the six integrated tasks.");
		System.out.println("               Each task takes full control of the console");
		System.out.println("               and, where appropriate, the SwiftBot hardware.");
		System.out.println("               When the task finishes you come straight back");
		System.out.println("               here.");
		System.out.println();
		System.out.println("  " + YELLOW + "[7] to [9]" + RESET + "  Pending tasks. These print a short");
		System.out.println("               notice; they are not yet runnable.");
		System.out.println();
		System.out.println("  " + CYAN + "[10]" + RESET + "        Show this help screen.");
		System.out.println("  " + CYAN + "[11]" + RESET + "        Show the About screen.");
		System.out.println("  " + RED + "[0]" + RESET + "         Quit the programme. You will be asked to");
		System.out.println("               confirm before the programme actually exits.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "If something goes wrong:" + RESET);
		System.out.println("  - Typed a number that isn't on the menu? Just try again.");
		System.out.println("  - Typed letters by mistake? The menu will ask again.");
		System.out.println("  - A task crashed? The menu will still be here - pick");
		System.out.println("    another one or exit the programme cleanly.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "A note on the hardware:" + RESET);
		System.out.println("  Several tasks interact with a physical SwiftBot over I2C.");
		System.out.println("  If you see an initialisation error, you are most likely");
		System.out.println("  running the programme on a PC rather than a Raspberry Pi,");
		System.out.println("  or I2C is disabled on the Pi. The individual tasks will");
		System.out.println("  tell you how to fix this if it happens.");
		System.out.println(SEPARATOR);
	}

	public void showAboutScreen() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_CYAN + "                      ABOUT" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "  Group " + GROUP_NUMBER + " - SwiftBot Integration Programme" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Module:" + RESET);
		System.out.println("  " + MODULE_CODE + " Group Project");
		System.out.println("  Department of Computer Science, Brunel University London");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Purpose:" + RESET);
		System.out.println("  Built for Formative Task 5 (Code Integration Task) of the");
		System.out.println("  CS1704 module. The goal of the task is to combine the");
		System.out.println("  separate SwiftBot programmes the group members wrote for");
		System.out.println("  Assignment 3 of CS1814 into a single cohesive application");
		System.out.println("  that can be navigated from one menu.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BOLD + "Contributors and their tasks:" + RESET);
		System.out.println("  " + GREEN + "Kartik   " + RESET + " - SpyBot (covert messaging platform)");
		System.out.println("  " + GREEN + "Fanice   " + RESET + " - Traffic Light navigation system");
		System.out.println("  " + GREEN + "Kerry    " + RESET + " - Mastermind code-breaking game");
		System.out.println("  " + GREEN + "Emmanuel " + RESET + " - ZigZag journey planner");
		System.out.println("  " + GREEN + "Joshua   " + RESET + " - Hex-driven dance programme");
		System.out.println("  " + GREEN + "Amaan    " + RESET + " - QR-based shape drawing");
		System.out.println("  " + DIM + YELLOW + "Aayan    " + RESET + " - " + DIM + "(integration pending)" + RESET);
		System.out.println("  " + DIM + YELLOW + "Adeeb    " + RESET + " - " + DIM + "(integration pending)" + RESET);
		System.out.println("  " + DIM + YELLOW + "Maqsura  " + RESET + " - " + DIM + "(integration pending)" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(CYAN + "[INFO]" + RESET + " Individual task details and authorship are");
		System.out.println("       documented in each member's source files.");
		System.out.println(SEPARATOR);
	}

	public void showExitConfirmationPrompt() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_YELLOW + "               EXIT CONFIRMATION" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(YELLOW + "[STATE] " + RESET + "WAITING FOR CONFIRMATION");
		System.out.println(BOLD + "Are you sure you want to exit the integrated programme?" + RESET);
		System.out.println(SUB_SEPARATOR);
		System.out.println(GREEN + "  [Y]" + RESET + "  Yes - shut the programme down");
		System.out.println(RED + "  [N]" + RESET + "  No  - take me back to the main menu");
		System.out.println(SEPARATOR);
		System.out.print(BRIGHT_YELLOW + "Enter Y or N: " + RESET);
	}

	public void showExitCancelled() {
		System.out.println();
		System.out.println(BRIGHT_GREEN + "[OK]" + RESET + " Exit cancelled - returning to the main menu.");
	}

	public void showGoodbyeScreen() {
		System.out.println();
		System.out.println(SEPARATOR);
		System.out.println(BOLD + BRIGHT_MAGENTA + "            SHUTTING DOWN" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BRIGHT_CYAN + "  ____  ___   ___  ____  ______   _______ " + RESET);
		System.out.println(BRIGHT_CYAN + " / ___|/ _ \\ / _ \\|  _ \\| __ ) \\ / / ____|" + RESET);
		System.out.println(BRIGHT_CYAN + "| |  _| | | | | | | | | |  _ \\\\ V /|  _|  " + RESET);
		System.out.println(BRIGHT_CYAN + "| |_| | |_| | |_| | |_| | |_) || | | |___ " + RESET);
		System.out.println(BRIGHT_CYAN + " \\____|\\___/ \\___/|____/|____/ |_| |_____|" + RESET);
		System.out.println(SEPARATOR);
		System.out.println(BOLD + "  Thank you for using the Group " + GROUP_NUMBER + " SwiftBot programme." + RESET);
		System.out.println("  All sessions have ended and the menu will now close.");
		System.out.println(SUB_SEPARATOR);
		System.out.println(BRIGHT_GREEN + "[STATE]" + RESET + " Programme terminated safely.");
		System.out.println(SEPARATOR);
		System.out.println();
	}

	public void showPressEnterPrompt() {
		System.out.print(BRIGHT_YELLOW + "Press ENTER to continue..." + RESET);
	}

	public void showInfo(String message) {
		System.out.println(CYAN + "[INFO]" + RESET + " " + message);
	}

	public void showWarning(String message) {
		System.out.println(BRIGHT_YELLOW + "[WARN]" + RESET + " " + message);
	}

	public void showError(String message) {
		System.out.println(BRIGHT_RED + "[ERROR]" + RESET + " " + message);
	}

	public void showSeparator() {
		System.out.println(SEPARATOR);
	}

	public void showSubSeparator() {
		System.out.println(SUB_SEPARATOR);
	}
}