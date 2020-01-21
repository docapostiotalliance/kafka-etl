package org.kafka.etl.kafka.impl;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.kafka.etl.kafka.IProducerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProducerCallback implements IProducerCallback {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProducerCallback.class);

  @Override
  public Callback producerCallback(String event, ProducerRecord<String, String> record) {
    return (metadata, e) -> {
      if (e != null) {
        LOGGER.error(
            "[DefaultProducerCallback][producerCallback] Error while sending record in Kafka: {} - {} - {}",
            e.getMessage(),
            e.getClass().getSimpleName(),
            record.value(),
            e);
      } else {
        LOGGER.info(
            "[DefaultProducerCallback][producerCallback] offset = {}, partition = {}, topic = {}",
            metadata.offset(),
            metadata.partition(),
            metadata.topic());
      }
    };
  }
}
