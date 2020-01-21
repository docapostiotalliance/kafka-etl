package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.kafka.etl.kafka.IAdditionalConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static org.kafka.etl.ioc.BindedConstants.CONSUMER_RECORD_SIZE;
import static org.kafka.etl.ioc.BindedConstants.PRODUCER_RECORD_SIZE;

public class DefaultAdditionalConfig implements IAdditionalConfig {
  @Inject
  @Named(CONSUMER_RECORD_SIZE)
  private Integer consumerRecordSize;

  @Inject
  @Named(PRODUCER_RECORD_SIZE)
  private Integer producerRecordSize;

  @Override
  public Map<String, Object> producerAdditionalConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, producerRecordSize);
    return config;
  }

  @Override
  public Map<String, Object> consumerAdditionalConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, consumerRecordSize);
    return config;
  }
}
