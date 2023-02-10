package kafka.streams.sandbox.topology;

import kafka.streams.sandbox.model.Vital;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.Instant;

public class VitalTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        Vital measurement = (Vital) record.value();
        if (measurement != null && measurement.getTimestamp() != null) {
            String timestamp = measurement.getTimestamp();
            return Instant.parse(timestamp).toEpochMilli();
        }
        return partitionTime;
    }
}
