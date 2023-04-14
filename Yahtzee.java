import java.util.Scanner;
public class Yahtzee {
	public static void main(String[] args) {
		// TODO: Implement main method
		if (args.length == 0) {
			prompt();
		} else {
			switch (args[0]) {
				case "-i":
					// independent game
					game();
					break;
				case "-d":
					// dependent game
					break;
			}
		}
	}

	private static void prompt() {
		// TODO
		System.out.println("Would you like to play as a scoresheet?");
	}

	private static void game() {
		Scanner scan = new Scanner(System.in);
		// Get number of players etc
		int numPlayers = ask("How many players are there?", scan, 1, 10);
		Player[] players = new Player[numPlayers];
		String name = "";
		for (int i = 0; i < numPlayers; i++) {
			System.out.print("What is player " + (i + 1) + "'s name? ");
			name = scan.nextLine();
			players[i] = new Player(name);
		}
		Game game = new Game(new Controller(scan));
		game.start(players);
	}

	private static int ask(String msg, Scanner scan, int min, int max) {
		boolean invalid = true;
		int input = 0;
		do {
			System.out.print(msg + " (" + min + "-" + max + "): ");
			if (scan.hasNextLine()) {
				if (scan.hasNextInt()) {
					input = scan.nextInt();
					scan.nextLine();
					if (input >= min && input <= max)
						invalid = false;
				} else
					scan.nextLine();
			}

		} while (invalid);
		return input;
	}

}
