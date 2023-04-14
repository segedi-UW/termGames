import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Dice {
    private final List<Die> dice;
    private final int largestDieSides;

    /**
     * A map with the mapping of number of sides to number of dice. For example,
     * the following map:

     * 2 -> 3
     * 4 -> 1
     * 6 -> 4
     * 
     * indicates that this Dice object should have 3 2-sided die, 1 4-sided die, and 4 6-sided die.
     * 
     * @param diceMap
     */
    public Dice(Map<Integer, Integer> diceMap) {
        dice = new ArrayList<>();
        int largestDieSides = -1;
        for (Map.Entry<Integer, Integer> entry : diceMap.entrySet()) {
            int sides = entry.getKey();
            int diceCount = entry.getValue();
            if (largestDieSides < sides) {
                largestDieSides = sides;
            }
            for (int i = 0; i < diceCount ; i++) {
                dice.add(new Die(sides));
            }
        }
        this.largestDieSides = largestDieSides;
    }

    public List<Die> getDice() {
        return dice;
    }

    public List<Integer> roll() {
        List<Integer> rolls = new ArrayList<>();
        for (Die die : dice) {
            rolls.add(die.roll());
        }
        return rolls;
    }

    public boolean isAnyRolling() {
        for (Die die : dice) {
            if (die.isRolling()) {
                return true;
            }
        }
        return false;
    }

    public void setAllIsRolling(boolean isRolling) {
        for (Die die : dice) {
            die.setRolling(isRolling);
        }
    }

    public int sumAll() {
        int sum = 0;
        for (Die die : dice) {
            sum += die.getFace();
        }
        return sum;
    }

    public int getMaxDieSides() {
        return largestDieSides;
    }
}
