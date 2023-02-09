package kafka.streams.sandbox.serdes.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.streams.sandbox.model.Sentiment;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class SentimentDeserializer implements Deserializer<Sentiment> {
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    @Override
    public Sentiment deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), Sentiment.class);
    }
}
