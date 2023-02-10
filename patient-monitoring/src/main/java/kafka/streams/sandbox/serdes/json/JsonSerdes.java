package kafka.streams.sandbox.serdes.json;

import kafka.streams.sandbox.model.BodyTemp;
import kafka.streams.sandbox.model.CombinedVitals;
import kafka.streams.sandbox.model.Pulse;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {
    public static Serde<Pulse> Pulse() {
        JsonSerializer<Pulse> serializer = new JsonSerializer<>();
        JsonDeserializer<Pulse> deserializer = new JsonDeserializer<>(Pulse.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<BodyTemp> BodyTemp() {
        JsonSerializer<BodyTemp> serializer = new JsonSerializer<>();
        JsonDeserializer<BodyTemp> deserializer = new JsonDeserializer<>(BodyTemp.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<CombinedVitals> CombinedVitals() {
        JsonSerializer<CombinedVitals> serializer = new JsonSerializer<>();
        JsonDeserializer<CombinedVitals> deserializer = new JsonDeserializer<>(CombinedVitals.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}
