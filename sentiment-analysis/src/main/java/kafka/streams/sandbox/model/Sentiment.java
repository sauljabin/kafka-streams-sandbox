package kafka.streams.sandbox.model;

public class Sentiment {
    private Long createdAt;
    private Long id;
    private Double score;
    private String text;
    private String entity;

    public Sentiment(Long createdAt, Long id, Double score, String text, String entity) {
        this.createdAt = createdAt;
        this.id = id;
        this.score = score;
        this.text = text;
        this.entity = entity;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "Sentiment{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", score=" + score +
                ", text='" + text + '\'' +
                ", entity='" + entity + '\'' +
                '}';
    }
}
