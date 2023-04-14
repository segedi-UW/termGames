import java.util.Arrays;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Controller {

    private final Scanner scanner;
    private Map<Character, Game.Play> playKeys;
    private Map<Character, Game.Action> actionKeys;
    
    public Controller(Scanner scanner) {
        if (null == scanner) {
            throw new NullPointerException("Controller scanner cannot be null");
        }
        this.scanner = scanner;
        setDefaultControls();
    }

    private void setDefaultControls() {
        playKeys = new HashMap<>();
        playKeys.put('1', Game.Play.ONE);
        playKeys.put('2', Game.Play.TWO);
        playKeys.put('3', Game.Play.THREE);
        playKeys.put('4', Game.Play.FOUR);
        playKeys.put('5', Game.Play.FIVE);
        playKeys.put('6', Game.Play.SIX);
        playKeys.put('k', Game.Play.KIND_3);
        playKeys.put('K', Game.Play.KIND_4);
        playKeys.put('s', Game.Play.STRAIGHT_4);
        playKeys.put('S', Game.Play.STRAIGHT_5);
        playKeys.put('h', Game.Play.FULL_HOUSE);
        playKeys.put('c', Game.Play.CHANCE);
        playKeys.put('y', Game.Play.YAHTZEE);

        actionKeys = Map.of(
            'r', Game.Action.REROLL,
            'k', Game.Action.KEEP,
            'a', Game.Action.SELECT_ALL,
            'q', Game.Action.QUIT,
            '1', Game.Action.TOGGLE_1,
            '2', Game.Action.TOGGLE_2,
            '3', Game.Action.TOGGLE_3,
            '4', Game.Action.TOGGLE_4,
            '5', Game.Action.TOGGLE_5
        );
    }

    public Map<Character, Game.Play> getPlayKeys() {
        return playKeys;
    }

    public void setPlayKeys(Map<Character, Game.Play> playKeys) {
        this.playKeys = playKeys;
    }

    public Game.Play promptPlay(Player player, Dice dice) {
        return promptPlay(player, dice, playKeys, false);
    }

    public Game.Play promptPlay(Player player, Dice dice, Map<Character, Game.Play> plays, boolean isBonusYahtzee) {
        Game.Play play = null;
        while (null == play) {
            displayPlays(player, dice, plays, isBonusYahtzee);
            String in = promptIn("Enter play: ");
            if (in.length() == 1) {
                play = plays.get(in.charAt(0));
                if (null == play) {
                    displayln("Unrecognized input '" + in + "'");
                }
            } else {
                displayln("Expected one character, please re-enter");
            }
        }
        return play;
    }

    public List<Game.Action> promptAction(Player player, Dice dice) {
        LinkedList<Game.Action> actions = new LinkedList<>();
        while (actions.isEmpty()) {
		    String in = promptIn(player.getName() + ", please enter your action (? for help): ");
		    int length = in.length();
            for (int i = 0; i < length; i++) {
                char c = in.charAt(i);
                if ('?' == c) {
                    displayActions();
                    actions.clear();
                    break;
                }
                Game.Action action = actionKeys.get(c);
                if (null == action) {
                    // restart prompt
                    display("Unrecognized command '" + c + "'");
                    actions.clear();
                    break;
                } else {
                    actions.addLast(action);
                }
            }
        }
        return actions;
    }

    private void displayActions() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Game.Action> entry : actionKeys.entrySet()) {
            sb.append(String.format("%s [%c] ", entry.getValue().name(), entry.getKey()));
        }
        displayln(sb.toString());
    }

    private void displayPlays(Player player, Dice dice, Map<Character, Game.Play> plays, boolean isBonusYahtzee) {
        displayln("\n" + player.getScoreInfoString());
        displaynln("Final ");
        displayDice(dice);
        displayln(player.getName() + " please choose from:\n" + getPlayString(player, dice, plays, isBonusYahtzee));
    }

    private String getPlayString(Player player, Dice dice, Map<Character, Game.Play> plays, boolean isBonusYahtzee) {
        final StringBuilder sb = new StringBuilder();
        List<Map.Entry<Character, Game.Play>> entries =  plays.entrySet().stream().sorted(new YahtzeeEntryComparator())
            .collect(Collectors.toList());
        boolean lineAdded = false;
        final int LENGTH = 26;
        for (Map.Entry<Character, Game.Play> entry : entries) {
            if (!lineAdded && !entry.getValue().isUpper()) {
                sb.append("-".repeat(LENGTH))
                    .append('\n');
                lineAdded = true;
            }
            sb.append(getPlayedString(entry, player, dice, isBonusYahtzee));
        }
        return sb.toString();
    }

    private String getPlayedString(Map.Entry<Character, Game.Play> entry, Player player, Dice dice, boolean isBonusYahtzee) {
        final int pad = 15;
        return DisplayUtil.pad(entry.getValue().name(), pad)
                + " [" + entry.getKey() + "] - "
                + (Game.canPlay(player, entry.getValue(), dice) ? 
                    // only treat yahtzee scores as bonus yahtzee
                    Scorer.score(entry.getValue(), player, dice, isBonusYahtzee) 
                    : "X (" + player.getScore(entry.getValue()) + ")")
                + "\n";
    }

    public void displayDice(Dice dice) {
        StringBuilder sb = new StringBuilder();
        List<Die> rolls = dice.getDice();
        sb.append("Rolls:\n");
        for (int i = 0; i < rolls.size(); i++) {
            Die die = rolls.get(i);
            sb.append(i + 1)
                .append(": ")
                .append(die.getFace());
            if (die.isRolling()) {
                sb.append(" *");
            }
            sb.append("\n");
        }
        displayln(sb.toString());
    }

    public void display(String message) {
        displayln(message);
    }

    private void displayln(String message) {
        System.out.println(message);
    }

    private void displaynln(String message) {
        System.out.print(message);
    }

    public void displayLeaderboard(Player[] players, Player current) {
        Player[] sorted = new Player[players.length];
        System.arraycopy(players, 0, sorted, 0, sorted.length);
        Arrays.sort(sorted);
        StringBuilder builder = new StringBuilder();
        builder.append("\nLeaderboard:\n");
        for (int i = 0; i < sorted.length; i++) {
            Player player = sorted[i];
            builder.append((i + 1) + ". " 
                + (player.equals(current) ? "*" : " ") 
                + String.format("%-" + Game.MAX_PLAYER_NAME + "s  %3d  ", player.getName(), player.score())
                    + " (" + player.getYahtzees() + " Yahtzees" 
                    + (player.hasUpperScoreBonus() ? ", upper score bonus)" : ")") + "\n");
        }
        displayln(builder.toString());
    }

    public Character getKeyForPlay(Game.Play play) {
        for (Map.Entry<Character, Game.Play> entry : playKeys.entrySet()) {
            if (entry.getValue().equals(play)) {
                return entry.getKey();
            }
        }
        throw new NoSuchElementException("No such key for play " + play.name());
    }

    public boolean confirm(String message) {
        String ans = promptIn(message + " (y|n): ");
        return "y".equalsIgnoreCase(ans);
    }

    public void waitOnEnter() {
        promptIn("Press Enter to continue...");
    }

    private String promptIn(String prompt) {
        displaynln(prompt);
        return scanner.nextLine();
    }
}
