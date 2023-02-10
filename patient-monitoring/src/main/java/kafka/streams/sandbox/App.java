package kafka.streams.sandbox;

import kafka.streams.sandbox.topology.MonitoringTopology;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class App {

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "patient-monitoring");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");

        KafkaStreams kafkaStreams = new KafkaStreams(MonitoringTopology.build(), config);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        kafkaStreams.start();
    }
}
