package kafka.streams.sandbox.processors;

import kafka.streams.sandbox.model.DigitalTwin;
import kafka.streams.sandbox.model.TurbineState;
import kafka.streams.sandbox.model.TurbineState.Type;
import java.time.Duration;
import java.time.Instant;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static kafka.streams.sandbox.topology.TurbineControllerTopology.DIGITAL_TWIN_STORE;

public class DigitalTwinProcessor implements Processor<String, TurbineState, String, DigitalTwin> {

  private static final Logger log = LoggerFactory.getLogger(DigitalTwinProcessor.class);

  private ProcessorContext<String, DigitalTwin> context;
  private KeyValueStore<String, DigitalTwin> kvStore;
  private Cancellable punctuator;

  @Override
  public void init(ProcessorContext<String, DigitalTwin> context) {
    this.context = context;
    this.kvStore = context.getStateStore(DIGITAL_TWIN_STORE);

    punctuator = this.context.schedule(
        Duration.ofMinutes(5), PunctuationType.WALL_CLOCK_TIME, this::enforceTtl);

    this.context.schedule(
        Duration.ofSeconds(20), PunctuationType.WALL_CLOCK_TIME, (ts) -> context.commit());
  }

  @Override
  public void process(Record<String, TurbineState> record) {
    String key = record.key();
    TurbineState value = record.value();
    System.out.println("Processing: " + value);

    if (value.getType() == null) {
      log.warn("Skipping state update due to unset type (must be: desired, reported)");
      return;
    }

    DigitalTwin digitalTwin = kvStore.get(key);
    if (digitalTwin == null) {
      digitalTwin = new DigitalTwin();
    }

    if (value.getType() == Type.DESIRED) {
      digitalTwin.setDesired(value);
    } else if (value.getType() == Type.REPORTED) {
      digitalTwin.setReported(value);
    }

    log.info("Storing digital twin: {}", digitalTwin);
    kvStore.put(key, digitalTwin);

    Record<String, DigitalTwin> newRecord = new Record<>(record.key(), digitalTwin, record.timestamp());
    context.forward(newRecord);
  }

  @Override
  public void close() {
    punctuator.cancel();
  }

  public void enforceTtl(Long timestamp) {
    try (KeyValueIterator<String, DigitalTwin> iter = kvStore.all()) {
      while (iter.hasNext()) {
        KeyValue<String, DigitalTwin> entry = iter.next();
        log.info("Checking to see if digital twin record has expired: {}", entry.key);
        TurbineState lastReportedState = entry.value.getReported();
        if (lastReportedState == null) {
          continue;
        }

        Instant lastUpdated = Instant.parse(lastReportedState.getTimestamp());
        long daysSinceLastUpdate = Duration.between(lastUpdated, Instant.now()).toDays();
        if (daysSinceLastUpdate >= 7) {
          kvStore.delete(entry.key);
        }
      }
    }
  }
}
