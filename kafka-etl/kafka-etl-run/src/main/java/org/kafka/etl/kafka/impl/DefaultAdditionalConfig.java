package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static org.kafka.etl.ioc.BindedConstants.CONSUMER_RECORD_SIZE;
import static org.kafka.etl.ioc.BindedConstants.PRODUCER_RECORD_SIZE;

public class DefaultAdditionalConfig implements IAdditionalConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdditionalConfig.class);

  @Inject
  @Named(CONSUMER_RECORD_SIZE)
  private Integer consumerRecordSize;

  @Inject
  @Named(PRODUCER_RECORD_SIZE)
  private Integer producerRecordSize;

  @Override
  public Map<String, Object> producerAdditionalConfig() {
    Map<String, Object> config = new HashMap<>();
    LOGGER.debug("[DefaultAdditionalConfig][producerAdditionalConfig] add config {} with value {}",
        ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
        producerRecordSize);
    if (null != producerRecordSize) {
      config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, producerRecordSize);
    }
    return config;
  }

  @Override
  public Map<String, Object> consumerAdditionalConfig() {
    Map<String, Object> config = new HashMap<>();
    LOGGER.debug("[DefaultAdditionalConfig][consumerAdditionalConfig] add config {} with value {}",
        ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
        consumerRecordSize);
    if (null != consumerRecordSize) {
      config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, consumerRecordSize);
    }
    return config;
  }
}
