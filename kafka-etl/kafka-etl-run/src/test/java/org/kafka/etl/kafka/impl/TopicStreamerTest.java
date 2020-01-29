package org.kafka.etl.kafka.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IPartitionKeyCalculator;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;
import org.kafka.etl.transform.ITransform;
import org.kafka.etl.transform.impl.DefaultTransform;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicStreamerTest {

  @Mock
  private IConsumerManager consumerManager;

  @Mock
  private IProducerManager producerManager;

  @Mock
  private ITransform transformer;

  @Mock
  private IProducerCallback callback;

  @Mock
  private IAdditionalConfig additionalConfig;

  @Mock
  private IPartitionKeyCalculator partitionKeyCalculator;

  @Mock
  private KafkaConsumer<String, String> consumer;

  @Mock
  private KafkaProducer<String, String> producer;

  @InjectMocks
  private TopicStreamer topicStreamer;

  private String groupId = "gid";

  private String inputTopic = "some_topic";

  private String outputTopic = "some_topic_json";

  private Integer pollTimeout = 2000;

  @Before
  public void init() {
    topicStreamer.setGroupId(groupId).setInputTopic(inputTopic).setOutputTopic(outputTopic)
        .setPollTimeout(pollTimeout);
  }

  @Test
  public void test_ok() {
    // given
    String outputRecord = "foo bar";
    List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
    ConsumerRecord<String, String> consumerRecord =
        new ConsumerRecord<>(inputTopic, 1, 1L, "foo", "bar");
    recordList.add(consumerRecord);
    TopicPartition partition = new TopicPartition(inputTopic, 1);
    Map<TopicPartition, List<ConsumerRecord>> e = new HashMap<>();
    e.put(partition, Collections.singletonList(consumerRecord));
    ConsumerRecords consumerRecords = new ConsumerRecords(e);

    when(transformer.transform(eq("bar"), any())).thenReturn(outputRecord);

    // when
    topicStreamer.processEvents(consumerRecords, partition);

    // then
    verify(partitionKeyCalculator).generatePartitionKey("foo", outputRecord);

    verify(producerManager, atLeastOnce()).sendEvent(eq(producer),
        any(),
        eq("foo bar"),
        eq(inputTopic),
        eq(callback));
  }


}
