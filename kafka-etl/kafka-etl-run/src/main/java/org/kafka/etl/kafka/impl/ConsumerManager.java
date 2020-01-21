package org.kafka.etl.kafka.impl;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class ConsumerManager implements IConsumerManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerManager.class);

  private final Integer maxPoll;
  private final String kafkaHosts;
  private final Integer requestTimeout;
  private final Integer sessionTimeout;
  private final IDeserializer keyDeserializer;
  private final IDeserializer valueDeserializer;

  public ConsumerManager(String kafkaHosts,
                         Integer requestTimeout,
                         Integer sessionTimeout,
                         Integer maxPoll,
                         IDeserializer keyDeserializer,
                         IDeserializer valueDeserializer) {
    this.maxPoll = requireNonNull(maxPoll, "maxPoll must not be empty");
    this.kafkaHosts = requireNonNull(kafkaHosts, "kafkaHosts must not be empty");
    this.requestTimeout = requireNonNull(requestTimeout, "requestTimeout must not be empty");
    this.sessionTimeout = requireNonNull(sessionTimeout, "sessionTimeout must not be empty");
    this.keyDeserializer = requireNonNull(keyDeserializer, "keyDeserializer must not be null");
    this.valueDeserializer =
        requireNonNull(valueDeserializer, "valueDeserializer must not be null");
  }

  @Override
  public KafkaConsumer<String, String> getConsumer(String groupId,
                                                   String topic,
                                                   Map<String, Object> consumerAdditionalConfig) {
    LOGGER.info(
        "[ConsumerManager][getConsumer][singleTopic] Creating consumer with the groupId config : groupId = {}, topic = {}",
        groupId,
        topic);
    return getConsumer(groupId, Collections.singletonList(topic), consumerAdditionalConfig);
  }

  @Override
  public KafkaConsumer<String, String> getConsumer(String groupId,
                                                   List<String> topics,
                                                   Map<String, Object> consumerAdditionalConfig) {
    Properties config = getConsumerConfig(groupId, consumerAdditionalConfig);
    KafkaConsumer<String, String> consumer =
        new KafkaConsumer<>(config, keyDeserializer, valueDeserializer);
    consumer.subscribe(topics);
    LOGGER.info(
        "[ConsumerManager][getConsumer][topics] Creating consumer with the following config : topics = {}, group = {}, conf = {}",
        topics,
        groupId,
        config);
    return consumer;
  }

  private Properties getConsumerConfig(String groupId,
                                       Map<String, Object> consumerAdditionalConfig) {
    return GenericConfig.defaultConsumerConfig(groupId,
        consumerAdditionalConfig,
        kafkaHosts,
        requestTimeout,
        sessionTimeout,
        maxPoll);
  }
}
