package kafka.streams.sandbox.processors;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.streams.sandbox.model.TurbineState;
import kafka.streams.sandbox.model.TurbineState.Power;
import kafka.streams.sandbox.model.TurbineState.Type;

public class HighWindsFlatmapProcessor implements Processor<String, TurbineState, String, TurbineState> {

  private static final Logger log = LoggerFactory.getLogger(HighWindsFlatmapProcessor.class);

  private ProcessorContext<String, TurbineState> context;

  @Override
  public void init(ProcessorContext<String, TurbineState> context) {
    this.context = context;
  }

  @Override
  public void process(Record<String, TurbineState> record) {
    TurbineState reported = record.value();

    context.forward(record);

    if (reported.getWindSpeedMph() > 65 && reported.getPower() == Power.ON) {
      log.info("high winds detected. sending shutdown signal");
      
      TurbineState desired = TurbineState.clone(reported);
      desired.setPower(Power.OFF);
      desired.setType(Type.DESIRED);
      
      Record<String, TurbineState> newRecord = new Record<>(record.key(), desired, record.timestamp());
      context.forward(newRecord);
    }
  }

  @Override
  public void close() {

  }
}
