package org.kafka.etl.kafka.impl;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;

import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class ProducerManager implements IProducerManager {
  private final String kafkaHost;
  private final Integer requestTimeout;
  private final Integer kafkaRetries;
  private final KafkaProducer<String, String> kafkaProducer;

  public ProducerManager(String kafkaHost,
                         Integer requestTimeout,
                         Integer kafkaFetchRetries,
                         Map<String, Object> extConfig) {

    this.kafkaHost = requireNonNull(kafkaHost, "producerHost must not be empty");

    this.requestTimeout = requireNonNull(requestTimeout, "requestTimeout must not be empty");

    this.kafkaRetries = requireNonNull(kafkaFetchRetries, "kafkaFetchRetries must not be null");
    this.kafkaProducer = kafkaProducer(extConfig);
  }

  @Override
  public KafkaProducer<String, String> getProducer() {
    return kafkaProducer;
  }

  @Override
  public void sendEvent(String key, String event, String topic, IProducerCallback callback) {
    requireNonNull(event, "event must not be null");
    sendEvent(getProducer(), key, event, topic, callback);
  }

  @Override
  public void sendEvent(KafkaProducer<String, String> producer,
                        String key,
                        String event,
                        String topic,
                        IProducerCallback callback) {
    requireNonNull(producer, "producer must not be null");
    requireNonNull(event, "event must not be null");
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, event);
    producer.send(record, callback.producerCallback(event, record));
  }

  private KafkaProducer<String, String> kafkaProducer(Map<java.lang.String, Object> kafkaAdditionalProducerConfig) {
    return new KafkaProducer<>(getProducerConfig(kafkaAdditionalProducerConfig));
  }

  private Properties getProducerConfig(Map<String, Object> kafkaAdditionalProducerConfig) {
    return GenericConfig.defaultProducerConfig(kafkaAdditionalProducerConfig,
        kafkaHost,
        requestTimeout,
        kafkaRetries);
  }
}
