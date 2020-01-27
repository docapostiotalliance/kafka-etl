package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IPartitionKeyCalculator;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;
import org.kafka.etl.kafka.ITopicStreamer;
import org.kafka.etl.transform.ITransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.kafka.etl.ioc.BindedConstants.GROUP_ID;
import static org.kafka.etl.ioc.BindedConstants.INPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.OUTPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.POLL_TIMEOUT;

public class TopicStreamer implements ITopicStreamer {
  private static final Logger LOGGER = LoggerFactory.getLogger(TopicStreamer.class);

  @Inject
  private IConsumerManager consumerManager;

  @Inject
  private IProducerManager producerManager;

  @Inject
  private ITransform transformer;

  @Inject
  private IProducerCallback callback;

  @Inject
  private IAdditionalConfig additionalConfig;

  @Inject
  private IPartitionKeyCalculator partitionKeyCalculator;

  @Inject
  @Named(GROUP_ID)
  private String groupId;

  @Inject
  @Named(INPUT_TOPIC)
  private String inputTopic;

  @Inject
  @Named(OUTPUT_TOPIC)
  private String outputTopic;

  @Inject
  @Named(POLL_TIMEOUT)
  private Integer pollTimeout;

  private KafkaConsumer<String, String> consumer;
  private KafkaProducer<String, String> producer;


  public TopicStreamer(IConsumerManager consumerManager,
                       IProducerManager producerManager,
                       ITransform transformer,
                       IProducerCallback callback,
                       IAdditionalConfig additionalConfig,
                       IPartitionKeyCalculator partitionKeyCalculator,
                       String groupId,
                       String inputTopic,
                       String outputTopic,
                       Integer pollTimeout,
                       KafkaConsumer<String, String> consumer,
                       KafkaProducer<String, String> producer) {
    this.consumerManager = consumerManager;
    this.producerManager = producerManager;
    this.transformer = transformer;
    this.callback = callback;
    this.additionalConfig = additionalConfig;
    this.partitionKeyCalculator = partitionKeyCalculator;
    this.groupId = groupId;
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.pollTimeout = pollTimeout;
    this.consumer = consumer;
    this.producer = producer;
  }

  public TopicStreamer() {
    // do nothing
  }

  @Override
  public void startStream() {
    init();

    try {
      processQueue();
    } catch (Exception e) {
      LOGGER.info(
          "[TopicStreamer][processQueueQuietly] Shutdown : msg = {}, type = {}, consumer = {}",
          e.getMessage(),
          e.getClass().getSimpleName(),
          null == consumer ? null : consumer.subscription(),
          e);
    } finally {
      LOGGER.info(
          "[TopicStreamer][processQueueQuietly] Closing consumer and producer : consumer.subscription = {}",
          null == consumer ? null : consumer.subscription());
      if (null != producer) {
        producer.close();
      }

      if (null != consumer) {
        consumer.close();
      }
    }
  }

  private void init() {
    consumer = requireNonNull(consumerManager.getConsumer(groupId,
        inputTopic,
        additionalConfig.consumerAdditionalConfig()), "consumer must not be null");
    producer = requireNonNull(producerManager.getProducer(), "producer must not be null");
  }

  private void processQueue() {
    while (true) {
      try {
        processPartitions();
      } catch (CommitFailedException e) {
        LOGGER.warn(
            "[TopicStreamer][processQueue] Processing took longer than session.timeout.ms : e.type = {}, e.msg = {}",
            e.getClass().getSimpleName(),
            e.getMessage());
      }
    }
  }

  private void processPartitions() {
    ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
    LOGGER.debug(
        "[TopicStreamer][processPartitions] processing records : count = {}, partitions = {}",
        records.count(),
        records.partitions());
    records.partitions().forEach(partition -> processEvents(records, partition));
  }

  public void processEvents(ConsumerRecords partitionRecords, TopicPartition partition) {
    List<ConsumerRecord<String, String>> records = partitionRecords.records(partition);
    records.stream().forEach(record -> processMessage(record.key(),
        record.value(),
        new EventKafkaInfos.Builder().offset(record.offset()).topicPartirion(partition).build()));
  }

  private void processMessage(String originalKey, String event, EventKafkaInfos eventKafkaInfos) {
    LOGGER.info("[TopicStreamer][processMessage] process the following record : {}",
        eventKafkaInfos);
    Map<String, String> metadata = new HashMap<>();
    metadata.put("offset", String.valueOf(eventKafkaInfos.getOffset()));
    metadata.put("topic", eventKafkaInfos.getTopic());
    metadata.put("partition", String.valueOf(eventKafkaInfos.getPartition()));
    metadata.put("key", originalKey);
    String transformed = transformer.transform(event, metadata);
    producerManager.sendEvent(producer,
        partitionKeyCalculator.generatePartitionKey(originalKey, transformed),
        transformed,
        inputTopic,
        callback);
    consumer.commitSync(Collections.singletonMap(eventKafkaInfos.getTopicPartition(),
        new OffsetAndMetadata(eventKafkaInfos.getOffset() + 1)));
  }
}
