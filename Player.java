import java.lang.StringBuilder;
import java.util.EnumMap;

public class Player implements Comparable<Player> {

	private static final int DIVIDER_LENGTH = 20;

	private String name;

	private EnumMap<Game.Play, Integer> playScores;

	public Player(String name) {
		this.name = name;
		playScores = new EnumMap<>(Game.Play.class);
		reset();
	}

	public boolean hadYahtzee() {
		return getScore(Game.Play.YAHTZEE) != 0;
	}

	public boolean isScored(Game.Play play) {
		return null != playScores.get(play);
	}

	public String getName() {
		return name;
	}

	public int score() {
		return Scorer.calcUpperScore(playScores) + Scorer.calcLowerScore(playScores);
	}

	public int getScore(Game.Play play) {
		Integer score = playScores.get(play);
		return null == score ? 0 : score;
	}

	public void play(Game.Play play, int score) {
		playScores.compute(play, (k, v) -> addScore(v, score));
	}

	public void reset() {
		Game.Play[] playValues = Game.Play.values();
		for (Game.Play play : playValues) {
			playScores.put(play, null);
		}
	}

	private int addScore(Integer oldScore, int score) {
		if (null == oldScore) {
			oldScore = 0;
		}
		return oldScore + score;
	}

	public String getScoreInfoString() {
		int upperTotal = Scorer.calcUpperScore(playScores);
		int lowerTotal = Scorer.calcLowerScore(playScores);
		int bonusYahtzees = getBonusYahtzees();
		StringBuilder builder = new StringBuilder();
		builder.append("Name: " + name + "\n");
		builder.append("Upper Score: " + Scorer.removeUpperScoreBonus(upperTotal) + "\n");
		builder.append("Upper Bonus: " + Scorer.getUpperScoreBonus(upperTotal) + 
			" [" + String.format("%+d", Scorer.getUpperScoreBonusDifferential(playScores)) + "]\n");
		builder.append("Lower Score: " + lowerTotal + "\n");
		builder.append("Bonus Yahtzee: " + bonusYahtzees + " (" + (100 * bonusYahtzees) + ")\n");
		builder.append("Total Score: " + score() + "\n");
		return builder.toString();
	}

	public int getYahtzees() {
		int yahtzeeScore = getScore(Game.Play.YAHTZEE);
		return yahtzeeScore > 0 ? ((yahtzeeScore - 50) / 100) + 1 : 0;
	}

	public int getBonusYahtzees() {
		int yahtzees = getYahtzees();
		return yahtzees > 0 ? yahtzees - 1 : 0;
	}

	public boolean hasUpperScoreBonus() {
		return Scorer.hasUpperScoreBonus(Scorer.calcUpperScore(playScores));
	}

	public String getPlayScoreString() {
		StringBuilder builder = new StringBuilder();
		Game.Play[] values = Game.getSortedPlays();
		boolean addedLine = false;
		final int PAD = 15;
		for (Game.Play play : values) {
			if (!addedLine && !play.isUpper()) {
				addedLine = true;
				builder.append("-".repeat(DIVIDER_LENGTH))
					.append('\n');
			}
			builder.append(DisplayUtil.pad(play.name() + ": ", PAD));
			builder.append(!isScored(play) ? "Open" : getScore(play));
			builder.append("\n");
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getScoreInfoString())
			.append('\n')
			.append(getPlayScoreString());
		return builder.toString();
	}

	@Override
	public int compareTo(Player o) {
		return o.score() - score();
	}

}
