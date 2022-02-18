package org.kafka.etl.load;

import com.google.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.kafka.etl.load.IPartitionKeyCalculator;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;

import javax.inject.Named;

import java.util.Properties;

import static org.kafka.etl.ioc.BindedConstants.OUTPUT_TOPIC;

public class KafkaLoader implements ILoad {
  @Inject
  private IProducerManager producerManager;

  @Inject
  private IPartitionKeyCalculator partitionKeyCalculator;

  @Inject
  private IProducerCallback callback;

  @Inject
  @Named(OUTPUT_TOPIC)
  private String outputTopic;

  private KafkaProducer<String, String> producer;

  @Override
  public ILoad init(Properties properties) {
    return this;
  }

  @Override
  public void loadEvent(String originalKey, String event) {
    if (null == producer) {
      producer = producerManager.getProducer();
    }

    producerManager.sendEvent(producer,
        partitionKeyCalculator.generatePartitionKey(originalKey, event),
        event,
        outputTopic,
        callback);
  }

  @Override
  public void close() {
    if (null != producer) {
      producer.close();
    }
  }

  public KafkaLoader setOutputTopic(String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }
}
