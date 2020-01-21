package org.kafka.etl.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Map;

public interface IConsumerManager {
  KafkaConsumer<String, String> getConsumer(String groupId,
                                            String topic,
                                            Map<String, Object> kafkaConsumerAdditionalConfig);

  KafkaConsumer<String, String> getConsumer(String groupId,
                                            List<String> topics,
                                            Map<String, Object> kafkaAdditionalConsumerConfig);
}
