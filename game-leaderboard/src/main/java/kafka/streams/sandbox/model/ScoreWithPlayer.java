package kafka.streams.sandbox.model;

public class ScoreWithPlayer {
    private final ScoreEvent scoreEvent;
    private final Player player;

    public ScoreWithPlayer(ScoreEvent scoreEvent, Player player) {
        this.scoreEvent = scoreEvent;
        this.player = player;
    }

    public ScoreEvent getScoreEvent() {
        return this.scoreEvent;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String toString() {
        return "{" + " scoreEvent='" + getScoreEvent() + "'" + ", player='" + getPlayer() + "'" + "}";
    }
}
