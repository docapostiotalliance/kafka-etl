package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.ITopicStreamer;
import org.kafka.etl.load.ILoad;
import org.kafka.etl.transform.ITransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kafka.etl.ioc.BindedConstants.GROUP_ID;
import static org.kafka.etl.ioc.BindedConstants.INPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.OUTPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.POLL_TIMEOUT;

public class TopicStreamer implements ITopicStreamer {
  private static final Logger LOGGER = LoggerFactory.getLogger(TopicStreamer.class);

  @Inject
  private IConsumerManager consumerManager;

  @Inject
  private ITransform transformer;

  @Inject
  private IAdditionalConfig additionalConfig;

  @Inject
  private ILoad loader;

  @Inject
  @Named(GROUP_ID)
  private String groupId;

  @Inject
  @Named(INPUT_TOPIC)
  private String inputTopic;

  @Inject
  @Named(POLL_TIMEOUT)
  private Integer pollTimeout;

  private KafkaConsumer<String, String> consumer;

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
          "[TopicStreamer][processQueueQuietly] Closing consumer and loader : consumer.subscription = {}",
          null == consumer ? null : consumer.subscription());
      loader.close();

      if (null != consumer) {
        consumer.close();
      }
    }
  }

  private void init() {
    consumer = requireNonNull(consumerManager.getConsumer(groupId,
        inputTopic,
        additionalConfig.consumerAdditionalConfig()), "consumer must not be null");
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
    ConsumerRecords<String, String> records;
    try {
      records = consumer.poll(pollTimeout);
    } catch (KafkaException e) {
      LOGGER.warn(
          "[TopicStreamer][processPartitions] encounter an error when polling data, e.type = {}, e.msg = {}, e.cause.type = {}, e.cause.msg = {}",
          e.getClass().getSimpleName(),
          e.getMessage(),
          null == e.getCause() ? EMPTY : e.getCause().getClass().getSimpleName(),
          null == e.getCause() ? EMPTY : e.getCause().getMessage());
      return;
    }

    if (null == records) {
      LOGGER.warn("[TopicStreamer][processPartitions] encounter a null set of records from poll");
      return;
    }

    LOGGER.debug(
        "[TopicStreamer][processPartitions] processing records : count = {}, partitions = {}",
        records.count(),
        records.partitions());
    records.partitions().forEach(partition -> processEvents(records, partition));
  }

  public void processEvents(ConsumerRecords partitionRecords, TopicPartition partition) {
    List<ConsumerRecord<String, String>> records = partitionRecords.records(partition);
    records.stream().filter(r -> isNotBlank(r.key()) && isNotBlank(r.value()))
        .forEach(record -> processMessage(record.key(),
            record.value(),
            new EventKafkaInfos.Builder().offset(record.offset()).topicPartirion(partition)
                .build()));
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
    loader.loadEvent(originalKey, transformed);
    consumer.commitSync(Collections.singletonMap(eventKafkaInfos.getTopicPartition(),
        new OffsetAndMetadata(eventKafkaInfos.getOffset() + 1)));
  }

  public TopicStreamer setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public TopicStreamer setInputTopic(String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public TopicStreamer setPollTimeout(Integer pollTimeout) {
    this.pollTimeout = pollTimeout;
    return this;
  }

  public ILoad getLoader() {
    return loader;
  }
}
