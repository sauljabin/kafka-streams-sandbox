package kafka.streams.sandbox.serdes.json;

import kafka.streams.sandbox.model.Sentiment;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class SentimentSerdes implements Serde<Sentiment> {

    @Override
    public Serializer<Sentiment> serializer() {
        return new SentimentSerializer();
    }

    @Override
    public Deserializer<Sentiment> deserializer() {
        return new SentimentDeserializer();
    }
}
