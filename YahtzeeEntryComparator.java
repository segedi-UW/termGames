import java.util.Map;
import java.util.Map.Entry;

import java.util.Comparator;

public class YahtzeeEntryComparator implements Comparator<Map.Entry> {

    private YahtzeeComparator comparator = new YahtzeeComparator();

    @Override
    public int compare(Entry o1, Entry o2) {
        if (o1.getValue() instanceof Game.Play
            && o2.getValue() instanceof Game.Play) {
                Game.Play p1 = (Game.Play) o1.getValue();
                Game.Play p2 = (Game.Play) o2.getValue();
                return comparator.compare(p1, p2);
        }
        throw new IllegalArgumentException("Tried to compare non-yahtzee entry");
    }
    
}
