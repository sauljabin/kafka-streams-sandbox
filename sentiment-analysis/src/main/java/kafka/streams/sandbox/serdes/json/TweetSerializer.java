package kafka.streams.sandbox.serdes.json;

import com.google.gson.Gson;
import kafka.streams.sandbox.model.Tweet;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

class TweetSerializer implements Serializer<Tweet> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, Tweet tweet) {
        if (tweet == null) {
            return null;
        }
        return gson.toJson(tweet).getBytes(StandardCharsets.UTF_8);
    }
}
