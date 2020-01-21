package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;
import org.kafka.etl.kafka.ITopicStreamer;
import org.kafka.etl.transform.ITransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.HashMap;
import java.util.UUID;

import static org.kafka.etl.ioc.BindedConstants.GROUP_ID;
import static org.kafka.etl.ioc.BindedConstants.INPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.OUTPUT_TOPIC;

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
  private IAdditionalConfig aditionalConfig;

  @Inject
  @Named(GROUP_ID)
  private String groupId;

  @Inject
  @Named(INPUT_TOPIC)
  private String inputTopic;

  @Inject
  @Named(OUTPUT_TOPIC)
  private String outputTopic;

  private KafkaConsumer<String, String> consumer;
  private KafkaProducer<String, String> producer;

  @PostConstruct
  public void init() {
    consumer = consumerManager.getConsumer(groupId,
        inputTopic,
        aditionalConfig.consumerAdditionalConfig());
    producer = producerManager.getProducer();
  }

  @Override
  public void startStream() {
    while (true) {
      try {
        processPartitions();
      } catch (CommitFailedException e) {
        LOGGER.warn(
            "[TopicStreamer][startStream] Processing took longer than session.timeout.ms : e.type = {}, e.msg = {}",
            e.getClass().getSimpleName(),
            e.getMessage());
      }
    }
  }

  void processPartitions() {
    // TODO
  }
}
