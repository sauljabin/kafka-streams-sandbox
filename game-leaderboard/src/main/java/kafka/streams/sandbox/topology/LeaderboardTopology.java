package kafka.streams.sandbox.topology;

import kafka.streams.sandbox.model.*;
import kafka.streams.sandbox.serdes.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class LeaderboardTopology {

    public static final String SCORE_EVENTS_TOPIC = "score-events";
    public static final String PLAYERS_TOPIC = "players";
    public static final String PRODUCTS_TOPIC = "products";
    public static final String HIGH_SCORES_TOPIC = "high-scores";

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        // step 1 create source processor using rational abstraction
        KStream<String, ScoreEvent> scoreEventStream =
                builder.stream(SCORE_EVENTS_TOPIC, Consumed.with(Serdes.String(), JsonSerdes.ScoreEvent()));

        KTable<String, Player> playersTable =
                builder.table(PLAYERS_TOPIC, Consumed.with(Serdes.String(), JsonSerdes.Player()));

        GlobalKTable<String, Product> productsGlobalTable =
                builder.globalTable(PRODUCTS_TOPIC, Consumed.with(Serdes.String(), JsonSerdes.Product()));

        // step 2 join score-event and players, it is necessary add keys to score events
        KStream<String, ScoreEvent> keyedScoreEventStream = scoreEventStream.selectKey((key, value) -> value.getPlayerId().toString());
        KStream<String, ScoreWithPlayer> joinScoreWithPlayersStream = keyedScoreEventStream.join(
                playersTable,
                ScoreWithPlayer::new,
                Joined.with(Serdes.String(), JsonSerdes.ScoreEvent(), JsonSerdes.Player())
        );

        // step 3 join the three topics(
        KStream<String, Enriched> enrichedStream = joinScoreWithPlayersStream.join(
                productsGlobalTable,
                (key, scoreWithPlayer) -> scoreWithPlayer.getScoreEvent().getProductId().toString(),
                Enriched::new
        );

        // step 4 group by product key since we want to calculate the high score for each product
        KGroupedStream<String, Enriched> enrichedGroup = enrichedStream.groupBy(
                (key, value) -> value.getPlayerId().toString(),
                Grouped.with(Serdes.String(), JsonSerdes.Enriched())
        );

        // step 5 aggregate to a table
        KTable<String, HighScores> scoresTable = enrichedGroup.aggregate(
                HighScores::new,
                (key, value, highScores) -> highScores.add(value),
                Materialized.<String, HighScores, KeyValueStore<Bytes, byte[]>>as("leader-boards")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(JsonSerdes.HighScores())
        );

        scoresTable.toStream().to(HIGH_SCORES_TOPIC, Produced.with(Serdes.String(), JsonSerdes.HighScores()));

        return builder.build();
    }
}
