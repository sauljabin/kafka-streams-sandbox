package kafka.streams.sandbox.serdes.json;

import com.google.gson.Gson;
import kafka.streams.sandbox.model.Sentiment;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

class SentimentSerializer implements Serializer<Sentiment> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, Sentiment sentiment) {
        if (sentiment == null) {
            return null;
        }
        return gson.toJson(sentiment).getBytes(StandardCharsets.UTF_8);
    }
}
