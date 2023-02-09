package kafka.streams.sandbox.services;

import kafka.streams.sandbox.model.Sentiment;
import kafka.streams.sandbox.model.Tweet;

import java.util.List;
import java.util.stream.Collectors;

public class SentimentAnalyzer {

    public static List<Sentiment> analyze(Tweet tweet) {
        List<String> entities = List.of("bitcoin", "ethereum");
        return entities.stream()
                .filter(entity -> tweet.getText().toLowerCase().contains(entity))
                .map(entity -> new Sentiment(tweet.getCreatedAt(), tweet.getId(), Math.random(), tweet.getText(), entity))
                .collect(Collectors.toList());
    }

}
