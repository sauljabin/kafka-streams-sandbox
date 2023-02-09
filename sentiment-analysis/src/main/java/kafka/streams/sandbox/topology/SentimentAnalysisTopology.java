package kafka.streams.sandbox.topology;

import kafka.streams.sandbox.model.Sentiment;
import kafka.streams.sandbox.model.Tweet;
import kafka.streams.sandbox.serdes.json.SentimentSerdes;
import kafka.streams.sandbox.serdes.json.TweetSerdes;
import kafka.streams.sandbox.services.SentimentAnalyzer;
import kafka.streams.sandbox.services.Translator;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.Map;

public class SentimentAnalysisTopology {

    public static final String SOURCE_TOPIC = "tweets";
    public static final String ENGLISH = "en";
    public static final String BRANCH_PREFIX = "branch-";
    public static final String CRYPTO_SENTIMENT_TOPIC = "crypto-sentiment";

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        // step 1 get tweets
        KStream<String, Tweet> tweetsStream = builder.stream(SOURCE_TOPIC, Consumed.with(Serdes.String(), new TweetSerdes()));
        tweetsStream.print(Printed.<String, Tweet>toSysOut().withLabel("tweetStream"));

        // step 2 filter retweets
        KStream<String, Tweet> noRetweetedStream = tweetsStream.filterNot((key, value) -> value.isRetweet());
        noRetweetedStream.print(Printed.<String, Tweet>toSysOut().withLabel("noRetweetedStream"));

        // step 3 branching
        Map<String, KStream<String, Tweet>> branches = noRetweetedStream.split(Named.as(BRANCH_PREFIX))
                .branch((key, value) -> value.getLang().equals(ENGLISH), Branched.as("englishStream"))
                .defaultBranch(Branched.as("nonEnglishStream"));

        KStream<String, Tweet> englishStream = branches.get(BRANCH_PREFIX + "englishStream");
        englishStream.print(Printed.<String, Tweet>toSysOut().withLabel("englishStream"));

        KStream<String, Tweet> nonEnglishStream = branches.get(BRANCH_PREFIX + "nonEnglishStream");
        nonEnglishStream.print(Printed.<String, Tweet>toSysOut().withLabel("nonEnglishStream"));

        // step 4 translate non english tweets
        KStream<String, Tweet> translatedStream = nonEnglishStream.mapValues((readOnlyKey, tweet) -> {
                    tweet.setText(Translator.translate(tweet.getLang(), ENGLISH, tweet.getText()));
                    return tweet;
                }
        );
        translatedStream.print(Printed.<String, Tweet>toSysOut().withLabel("translatedStream"));

        // step 5 merge
        KStream<String, Tweet> mergedStream = englishStream.merge(translatedStream);
        mergedStream.print(Printed.<String, Tweet>toSysOut().withLabel("mergedStream"));

        // step 6 add sentiment
        KStream<String, Sentiment> sentimentStream = mergedStream.flatMapValues(tweet -> SentimentAnalyzer.analyze(tweet));
        sentimentStream.print(Printed.<String, Sentiment>toSysOut().withLabel("mergedStream"));

        // step 7 produce sentiment to crypto-sentiment
        sentimentStream.selectKey((key, sentiment) -> sentiment.getId())
                .to(CRYPTO_SENTIMENT_TOPIC, Produced.with(Serdes.Long(), new SentimentSerdes()));

        return builder.build();
    }
}
