package kafka.streams.sandbox.model;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HighScores {
    private final TreeSet<Enriched> highScores = new TreeSet<>();

    public HighScores add(final Enriched enriched) {
        highScores.add(enriched);

        if (highScores.size() > 3) {
            highScores.remove(highScores.last());
        }

        return this;
    }

    public List<Enriched> toList() {
        return highScores.stream().collect(Collectors.toList());
    }
}
