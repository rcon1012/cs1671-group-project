import java.util.Comparator;

public class ScoreComparator implements Comparator<Score> {
	@Override
    public int compare(Score self, Score other) {
        return self.compareTo(other);
    }
}