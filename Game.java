import java.util.Arrays;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class Game {

	public static final int UPPER_SCORE_THRESHOLD = 63;
	public static final int UPPER_SCORE_BONUS = 35;
	public static final int LOW_STRAIGHT_SCORE = 30;
	public static final int HIGH_STRAIGHT_SCORE = 40;
	public static final int FULL_HOUSE_SCORE = 25;
	public static final int ROUNDS = 13;
	public static final int DICE_COUNT = 5;
	public static final int DICE_SIDES = 6;
	public static final int MAX_PLAYER_NAME = 10;

	private static final Play[] sortedPlays;

	static {
		Play[] values = Play.values();
		Arrays.sort(values, new YahtzeeComparator());
		sortedPlays = values;
	}


	public enum Play {
		ONE(true), TWO(true), THREE(true), FOUR(true), FIVE(true), SIX(true), 
		KIND_3, KIND_4, STRAIGHT_4, STRAIGHT_5, FULL_HOUSE, CHANCE, YAHTZEE;

		private final boolean isUpper;

		private Play() {
			this(false);
		}

		private Play(boolean isUpper) {
			this.isUpper = isUpper;
		}

		public boolean isUpper() {
			return isUpper;
		}

	}

	public enum Action {
		REROLL, KEEP, SELECT_ALL, QUIT, TOGGLE_1, TOGGLE_2, TOGGLE_3, TOGGLE_4, TOGGLE_5
	}

	private Controller controller;

	public Game(Controller controller) {
		this.controller = controller;
	}
	
	public static Play[] getSortedPlays() {
		return sortedPlays;
	}

	public void start(Player[] players) {
		// Game Loop
		Player current = null;
		for (int i = 0; i < (ROUNDS*players.length); i++) {
			current = players[i % players.length];
			controller.displayLeaderboard(players, current);
			controller.display("It is " + current.getName() + "'s turn\n\n" + current);
			playerRound(current);
		}
		// Display game end
		int max = -1;
		int score = 0;
		Player[] winners = new Player[players.length];
		int wIndex = 0;
		for (int i = 0; i < players.length; i++) {
			current = players[i];
			score = current.score();
			if (score > max) {
				max = score;
				for (int k = 0; k < winners.length; k++)
					winners[k] = null;
				wIndex = 0;
				winners[wIndex] = current;
				wIndex++;
			} else if (score == max) {
				winners[wIndex] = current;
				wIndex++;
			}
		}
		controller.displayLeaderboard(players, null);
		StringBuilder builder = new StringBuilder();
		if (players.length > 1) {
			printWinner(builder, winners, wIndex, max);
		} else {
			builder.append("Thanks for playing " + winners[0].getName() + ", your total score was " + max);
		}
		controller.display("\n" + builder.toString());
	}

	private void printWinner(StringBuilder builder, Player[] winners, int wIndex, int max) {
		if (wIndex == 1) {
			builder.append("The winner is " + winners[0].getName() + ", with a score of " + max + " points!\n");
		} else {
			builder.append("There was a " + wIndex + " way draw!");
			for (int i = 0; i < wIndex; i++) {
				builder.append(i < wIndex - 1 ? winners[i].getName() + ", " : "and " + winners[i].getName());
			}
			builder.append(" all tied with a score of " + max + " points!\n");
		}
		builder.append("Congratulations!");
	}

	private void playerRound(Player player) {
		Dice dice = new Dice(Map.of(DICE_SIDES, DICE_COUNT));
		controller.display("Rolling dice...");
		dice.roll();
		dice.setAllIsRolling(false);
		int rerolls = 2;
		boolean rolling = true;
		do {
			controller.displayDice(dice);
			controller.display("There are " + rerolls + " rerolls remaining");
			List<Action> actions = controller.promptAction(player, dice);
			for (Action action : actions) {
				switch (action) {
					case KEEP:
						rolling = false;
						break;
					case QUIT:
						System.exit(0);
						break;
					case REROLL:
						rerolls = reroll(dice, rerolls);
						// reset dice reroll after rerolling 
						dice.setAllIsRolling(false);
						break;
					case SELECT_ALL:
						dice.setAllIsRolling(true);
						break;
					case TOGGLE_1:
						dice.getDice().get(0).toggleRolling();;
						break;
					case TOGGLE_2:
						dice.getDice().get(1).toggleRolling();;
						break;
					case TOGGLE_3:
						dice.getDice().get(2).toggleRolling();;
						break;
					case TOGGLE_4:
						dice.getDice().get(3).toggleRolling();;
						break;
					case TOGGLE_5:
						dice.getDice().get(4).toggleRolling();;
						break;
					default:
						throw new NoSuchElementException("Unrecognized Action: " + action);
				}
				if (!rolling) {
					break;
				}
			}
		} while (rolling && rerolls > 0);

		play(player, dice);
	}

	public static int getUpperScoreDiceNumber(Play play) {
		switch (play) {
			case ONE:
				return 1;
			case TWO:
				return 2;
			case THREE:
				return 3;
			case FOUR:
				return 4;
			case FIVE:
				return 5;
			case SIX:
				return 6;
			default:
				throw new IllegalArgumentException("Unrecognized Upper Score Play " + play.name());
		}
	}

	/**
	 * The Play enum is setup in order of score (the first six constants are the uppers).
	 * Returns the corresponding upper play from the dice.
	 * 
	 * 1 -> ONE
	 * 2 -> TWO
	 * ...
	 * 6 -> SIX
	 * 
	 * @param dice
	 * @return
	 */
	public static Play getUpperScorePlayFromDice(int dice) {
		if (dice < 0 || dice > 5) {
			throw new IllegalArgumentException("Dice is out of upper index range");
		}
		Play[] plays = Play.values();
		return plays[dice - 1];
	}

	private void play(Player player, Dice dice) {
		Play play = getPlayablePlay(player, dice);
		boolean isBonusYahtzee = player.hadYahtzee() && Play.YAHTZEE == play;
		int score = Scorer.score(play, player, dice, isBonusYahtzee);
		if (score > 0 || confirmScore(play, score)) {
			if (isBonusYahtzee) {
				// fill a slot in addition to playing the bonus yahtzee
				playBonusYahtzee(player, dice);
			}
			player.play(play, score);
		}
	}

	/**
	 * Play the Yahtzee bonus according to the following rules:
	 * 1. Score the total of the five dice in the appropriate upper section box if
	 * open
	 * 2. Fill the 3 or 4 of a kind boxes with the sum
	 * 3. Pick the Chance or steal any combo (straights or full house get full pts)
	 * 4. Fill a different upper section with a zero
	 * 
	 * @param player
	 * @param rolls
	 */
	private boolean playBonusYahtzee(Player player, Dice rolls) {
		return chooseBonusYahtzeeUpperSection(player, rolls)
		|| chooseBonusYahtzeeKind(player, rolls)
		|| chooseBonusYahtzeeStealCombo(player, rolls)
		|| chooseBonusYahtzeeUpperSectionToZero(player, rolls);
	}

	private boolean chooseBonusYahtzeeUpperSectionToZero(Player player, Dice rolls) {
		List<Play> plays = getUpperPlays();
		boolean played = playAvailable(player, rolls, plays);
		if (!played) {
			throw new IllegalStateException("There was no valid play for the bonus yahtzee");
		}
		return true;
	}

	private List<Play> getUpperPlays() {
		Play[] plays = Play.values();
		List<Play> upperPlays = new ArrayList<>();
		for (Play play : plays) {
			if (play.isUpper) {
				upperPlays.add(play);
			}
		}
		return upperPlays;
	}

	private boolean chooseBonusYahtzeeStealCombo(Player player, Dice rolls) {
		List<Play> plays = List.of(Play.CHANCE, Play.STRAIGHT_4, Play.STRAIGHT_5, Play.FULL_HOUSE);
		return playAvailable(player, rolls, plays);
	}

	private boolean chooseBonusYahtzeeKind(Player player, Dice rolls) {
		List<Play> plays = List.of(Play.KIND_3, Play.KIND_4);
		return playAvailable(player, rolls, plays);
	}

	private boolean playAvailable(Player player, Dice rolls, List<Play> plays) {
		boolean isAvailable = isAnyPlayAvailable(plays, player, rolls);
		if (isAvailable) {
			Play play = getPlayablePlay(player, rolls, createKeyMapFromPlays(plays), true);
			player.play(play, Scorer.score(play, player, rolls, true));
		}
		return isAvailable;
	}

	private Map<Character, Play> createKeyMapFromPlays(List<Play> plays) {
		Map<Character, Play> map = new HashMap<>();
		for (Play play : plays) {
			map.put(controller.getKeyForPlay(play), play);
		}
		return map;
	}

	private boolean isAnyPlayAvailable(List<Play> plays, Player player, Dice dice) {
		for (Play play : plays) {
			if (canPlay(player, play, dice)) {
				return true;
			}
		}
		return false;
	}

	private boolean chooseBonusYahtzeeUpperSection(Player player, Dice rolls) {
		Play play = getUpperScorePlayFromDice(rolls.getDice().get(0).getFace());
		if (canPlay(player, play, rolls)) {
			controller.display("Automatically filling corresponding Upper section " + play.name());
			player.play(play, Scorer.score(play, player, rolls, true));
			controller.waitOnEnter();
			return true;
		}
		return false;
	}

	private Play getPlayablePlay(Player player, Dice rolls) {
		return getPlayablePlay(player, rolls, controller.getPlayKeys(), false);
	}

	private Play getPlayablePlay(Player player, Dice rolls, Map<Character, Play> plays, boolean isBonusYahtzee) {
		Play play = null;
		while (null == play) {
			play = controller.promptPlay(player, rolls, plays, isBonusYahtzee);
			if (!canPlay(player, play, rolls)) {
				play = null;
			}
		}
		return play;
	}

	private boolean confirmScore(Play play, int score) {
		return controller.confirm("Are you sure you want to fill the " + play.name() + " slot with " + score + "?");
	}

	public static boolean isYahtzee(Dice dice) {
		DiceEvaluator evaluator = new DiceEvaluator();
		return evaluator.isKind(dice, Game.DICE_COUNT);
	}

	public static boolean canPlay(Player player, Play play, Dice dice) {
		if (Play.YAHTZEE == play && (!player.isScored(play) || (player.hadYahtzee() && isYahtzee(dice)))) {
			return true;
		}
		return !player.isScored(play);
	}

	private int reroll(Dice dice, int rerolls) {
		if (dice.isAnyRolling()) {
			if (rerolls > 0) {
				rerolls--;
				dice.roll();
			} else {
				controller.display("Cannot reroll, out of rerolls.");
			}
		} else {
			controller.display("No selected dice to roll");
		}
		return rerolls;
	}
}
