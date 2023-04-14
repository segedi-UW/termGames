import java.util.Map;
import java.util.function.Predicate;
import java.util.EnumMap;

public class Scorer {

	private final static int BONUS_DIFFERENTIAL_AVERAGE = 3;
	private static DiceEvaluator evaluator = new DiceEvaluator();

	public interface Scorable {
		public int score(Player player, Dice die, boolean isBonusYahtzee);
	}

	public static class SumMatchScorer implements Scorable {

		private final int match;

		public SumMatchScorer(int match) {
			this.match = match;
		}

		public int score(Player player, Dice dice, boolean isBonusYahtzee) {
			int matchSum = 0;
			for (Die die : dice.getDice()) {
				int face = die.getFace();
				if (face == match) {
					matchSum += face;
				}
			}
			return matchSum;
		}
	}

	public static class YahtzeeScorer implements Scorable {
		public int score(Player player, Dice dice, boolean isBonusYahtzee) {
			if (!evaluator.isKind(dice, Game.DICE_COUNT)) {
				return 0;
			}
			if (!player.isScored(Game.Play.YAHTZEE)) {
				return 50;
			}
			return player.hadYahtzee() ? 100 : 0;
		}
	}

	public static class ConstantScorer implements Scorable {
		private final int score;
		private final Predicate<Dice> predicate;

		public ConstantScorer(int score, Predicate<Dice> predicate) {
			this.predicate = predicate;
			this.score = score;
		}

		public int score(Player player, Dice dice, boolean isBonusYahtzee) {
			return isBonusYahtzee || predicate.test(dice) ? score : 0;
		}
	}

	public static class KindScorer implements Scorable {
		private final int minCount;

		public KindScorer(int minCount) {
			this.minCount = minCount;
		}

		public int score(Player player, Dice dice, boolean isBonusYahtzee) {
			if (evaluator.isKind(dice, minCount)) {
				return dice.sumAll();
			}
			return 0;
		}
	}

	public static class SumScorer implements Scorable {
		public int score(Player player, Dice die, boolean isBonusYahtzee) {
			return die.sumAll();
		}
	}

	private static Map<Game.Play, Scorable> scorers;

	static {
		scorers = new EnumMap<>(Game.Play.class);
		scorers.put(Game.Play.ONE, new SumMatchScorer(1));
		scorers.put(Game.Play.TWO, new SumMatchScorer(2));
		scorers.put(Game.Play.THREE, new SumMatchScorer(3));
		scorers.put(Game.Play.FOUR, new SumMatchScorer(4));
		scorers.put(Game.Play.FIVE, new SumMatchScorer(5));
		scorers.put(Game.Play.SIX, new SumMatchScorer(6));
		scorers.put(Game.Play.STRAIGHT_4, new ConstantScorer(Game.LOW_STRAIGHT_SCORE, dice -> evaluator.isStraight(dice, 4)));
		scorers.put(Game.Play.STRAIGHT_5, new ConstantScorer(Game.HIGH_STRAIGHT_SCORE, dice -> evaluator.isStraight(dice, 5)));
		scorers.put(Game.Play.FULL_HOUSE, new ConstantScorer(Game.FULL_HOUSE_SCORE, dice -> evaluator.isHouse(dice)));
		scorers.put(Game.Play.CHANCE, new SumScorer());
		scorers.put(Game.Play.KIND_3, new KindScorer(3));
		scorers.put(Game.Play.KIND_4, new KindScorer(4));
		scorers.put(Game.Play.YAHTZEE, new YahtzeeScorer());
	}

	public static int calcUpperScore(Map<Game.Play, Integer> scores) {
		return addUpperScoreBonus(
				scores.entrySet().stream()
						.filter(e -> e.getValue() != null)
						.filter(e -> e.getKey().isUpper())
						.map(EnumMap.Entry::getValue)
						.reduce(Integer::sum)
						.orElse(0));
	}

	public static int calcLowerScore(Map<Game.Play, Integer> scores) {
		return scores.entrySet().stream()
				.filter(e -> e.getValue() != null)
				.filter(e -> !e.getKey().isUpper())
				.map(EnumMap.Entry::getValue)
				.reduce(Integer::sum)
				.orElse(0);
	}

	public static int getUpperScoreBonusDifferential(Map<Game.Play, Integer> scores) {
		return scores.entrySet().stream()
						.filter(e -> e.getValue() != null)
						.filter(e -> e.getKey().isUpper())
						.map(Scorer::calcUpperScoreBonusDifferential)
						.reduce(Integer::sum)
						.orElse(0);
	}

	private static int calcUpperScoreBonusDifferential(Map.Entry<Game.Play, Integer> entry) {
		int avg = Game.getUpperScoreDiceNumber(entry.getKey()) * BONUS_DIFFERENTIAL_AVERAGE;
		return entry.getValue() - avg;
	}

	public static int addUpperScoreBonus(int upperScore) {
		return hasUpperScoreBonus(upperScore) ? upperScore + Game.UPPER_SCORE_BONUS : upperScore;
	}

	public static int removeUpperScoreBonus(int upperScore) {
		return hasUpperScoreBonus(upperScore) ? upperScore - Game.UPPER_SCORE_BONUS : upperScore;
	}

	public static int getUpperScoreBonus(int upperScore) {
		return upperScore >= Game.UPPER_SCORE_THRESHOLD ? Game.UPPER_SCORE_BONUS : 0;
	}

	public static boolean hasUpperScoreBonus(int upperScore) {
		return getUpperScoreBonus(upperScore) > 0;
	}

	public static int score(Game.Play play, Player player, Dice dice, boolean isBonusYahtzee) {
		if (!scorers.containsKey(play)) {
			throw new IllegalArgumentException("Play " + play + " does not have a configured Scorable");
		}
		return scorers.get(play).score(player, dice, isBonusYahtzee);
	}



}
