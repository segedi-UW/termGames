public class DiceEvaluator {

	public boolean isKind(Dice dice, int kindLength) {
		int[] counts = count(dice);
		for (int count : counts) {
			if (count >= kindLength) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the rolls resulted in a full house
	 * 
	 * @param dice The dice rolled
	 * @return true if it is a full house, false otherwise
	 */
	public boolean isHouse(Dice dice) {
		int[] count = count(dice);
		boolean two = false;
		boolean three = false;
		for (int i = 0; i < count.length; i++) {
			if (count[i] == 2)
				two = true;
			else if (count[i] == 3)
				three = true;
		}
		return two && three;
	}

	public boolean isStraight(Dice dice, int number) {
		int[] count = count(dice);
		int maxRow = 0;
		int row = 0;
		for (int i = 0; i < count.length; i++) {
			if (count[i] >= 1)
				row++;
			if (row > maxRow)
				maxRow = row;
			if (count[i] < 1)
				row = 0;
		}
		return maxRow >= number;
	}

	/**
	 * Counts the number of appearances of each dice
	 * 
	 * @param dice the dice to count
	 * @return counts of each side
	 */
	private static int[] count(Dice dice) {
		int[] count = new int[dice.getMaxDieSides()];
		for (Die die : dice.getDice()) {
			// dice side -1 = index
			count[die.getFace() - 1]++;
		}
		return count;
	}
}
