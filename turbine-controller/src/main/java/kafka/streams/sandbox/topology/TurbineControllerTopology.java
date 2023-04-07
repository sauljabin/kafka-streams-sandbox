package kafka.streams.sandbox.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import kafka.streams.sandbox.model.DigitalTwin;
import kafka.streams.sandbox.processors.DigitalTwinProcessor;
import kafka.streams.sandbox.processors.HighWindsFlatmapProcessor;
import kafka.streams.sandbox.serdes.json.JsonSerdes;

public class TurbineControllerTopology {
    public static final String DIGITAL_TWINS_TOPIC = "digital-twins";
    public static final String DIGITAL_TWIN_SINK = "Digital Twin Sink";
    public static final String DIGITAL_TWIN_PROCESSOR = "Digital Twin Processor";
    public static final String DIGITAL_TWIN_STORE = "digital-twin-store";
    public static final String HIGH_WINDS_FLATMAP_PROCESSOR = "High Winds Flatmap Processor";
    public static final String REPORTED_STATE_EVENTS_SOURCE = "Reported State Events";
    public static final String DESIRED_STATE_EVENTS_SOURCE = "Desired State Events";
    public static final String REPORTED_STATE_EVENTS_TOPIC = "reported-state-events";
    public static final String DESIRED_STATE_EVENTS_TOPIC = "desired-state-events";

    public static Topology build() {
        Topology topology = new Topology();

        // step 1 sources
        topology.addSource(
                DESIRED_STATE_EVENTS_SOURCE,
                Serdes.String().deserializer(),
                JsonSerdes.TurbineState().deserializer(),
                DESIRED_STATE_EVENTS_TOPIC);

        topology.addSource(
                REPORTED_STATE_EVENTS_SOURCE,
                Serdes.String().deserializer(),
                JsonSerdes.TurbineState().deserializer(),
                REPORTED_STATE_EVENTS_TOPIC);

        // step 2 generate a shutdown signal if there are dangerous conditions
        topology.addProcessor(
                HIGH_WINDS_FLATMAP_PROCESSOR,
                HighWindsFlatmapProcessor::new,
                REPORTED_STATE_EVENTS_SOURCE);

        // step 3 stateful digital twin
        StoreBuilder<KeyValueStore<String, DigitalTwin>> storeBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(DIGITAL_TWIN_STORE),
                Serdes.String(),
                JsonSerdes.DigitalTwin());

        topology.addProcessor(
                DIGITAL_TWIN_PROCESSOR,
                DigitalTwinProcessor::new,
                HIGH_WINDS_FLATMAP_PROCESSOR,
                DESIRED_STATE_EVENTS_SOURCE);

        topology.addStateStore(storeBuilder, DIGITAL_TWIN_PROCESSOR);

        // step 4 save digital twins to a topic
        topology.addSink(
                DIGITAL_TWIN_SINK,
                DIGITAL_TWINS_TOPIC,
                Serdes.String().serializer(),
                JsonSerdes.DigitalTwin().serializer(),
                DIGITAL_TWIN_PROCESSOR);

        return topology;
    }
}
