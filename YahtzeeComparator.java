import java.util.Comparator;

public class YahtzeeComparator implements Comparator<Game.Play> {
    public int compare(Game.Play a, Game.Play b) {
        int au = a.isUpper() ? 1 : 0;
        int bu = b.isUpper() ? 1 : 0;
        int cmp = bu - au;
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(a.ordinal(), b.ordinal());
    }
}