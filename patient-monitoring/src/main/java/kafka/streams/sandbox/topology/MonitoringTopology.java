package kafka.streams.sandbox.topology;

import kafka.streams.sandbox.model.BodyTemp;
import kafka.streams.sandbox.model.CombinedVitals;
import kafka.streams.sandbox.model.Pulse;
import kafka.streams.sandbox.serdes.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;

import java.time.Duration;

public class MonitoringTopology {

    public static final String PULSE_EVENTS_TOPIC = "pulse-events";
    public static final String BODY_TEMP_EVENTS_TOPIC = "body-temp-events";
    public static final String BPM_TOPIC = "bpm";
    public static final String SIRS_ALERTS_TOPIC = "sirs-alerts";

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        // step 1 sources (no compacted topics, keyed topics)
        KStream<String, Pulse> pulseEventsStream = builder
                .stream(PULSE_EVENTS_TOPIC,
                        Consumed.with(Serdes.String(), JsonSerdes.Pulse())
                                .withTimestampExtractor(new VitalTimestampExtractor())
                );

        KStream<String, BodyTemp> bodyTempStream = builder
                .stream(BODY_TEMP_EVENTS_TOPIC,
                        Consumed.with(Serdes.String(), JsonSerdes.BodyTemp())
                                .withTimestampExtractor(new VitalTimestampExtractor()));

        // step 2 group pulse to calculate bpm through aggregation
        KGroupedStream<String, Pulse> pulseGroup = pulseEventsStream.groupByKey();

        // step 3 calculate bpm
        KTable<Windowed<String>, Long> countPulseStream = pulseGroup.windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                .count(Materialized.as(BPM_TOPIC));

        // step 4 generate only one ate the end of the minute
        KTable<Windowed<String>, Long> bpmTable = countPulseStream.suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded().shutDownWhenFull()));
        bpmTable.toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value.toString()))
                .peek((key, value) -> System.out.printf("BPM MONITOR -> patient: %s, bpm: %s\n", key, value))
                .to(BPM_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // step 5 filtering
        KStream<Windowed<String>, Long> unkeyHighPulseStream = bpmTable.toStream().filter((key, bpm) -> bpm >= 100);
        KStream<String, BodyTemp> highTemStreams = bodyTempStream.filter((key, bodyTemp) -> bodyTemp.getTemperature() > 100.4);

        // step 6 rekey
        KStream<String, Long> highPulseStream = unkeyHighPulseStream.selectKey((key, value) -> key.key());

        // step 7 join both indicators
        KStream<String, CombinedVitals> combinedVitalsStream = highPulseStream.join(highTemStreams,
                CombinedVitals::new,
                JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)),
                StreamJoined.with(Serdes.String(), Serdes.Long(), JsonSerdes.BodyTemp())
        );

        // step 8 alerts
        combinedVitalsStream
                .peek((key, value) -> System.out.printf("SIRS MONITOR -> patient: %s, bpm: %s, temp: %s\n", key, value.getHeartRate(), value.getBodyTemp().getTemperature()))
                .to(SIRS_ALERTS_TOPIC, Produced.with(Serdes.String(), JsonSerdes.CombinedVitals()));

        return builder.build();
    }
}
