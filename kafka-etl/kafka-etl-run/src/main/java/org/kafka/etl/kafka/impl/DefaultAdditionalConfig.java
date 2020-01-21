package org.kafka.etl.kafka.impl;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultAdditionalConfig implements IAdditionalConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdditionalConfig.class);

  private Integer consumerRecordSize;

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

  public Integer getConsumerRecordSize() {
    return consumerRecordSize;
  }

  public void setConsumerRecordSize(Integer consumerRecordSize) {
    this.consumerRecordSize = consumerRecordSize;
  }

  public Integer getProducerRecordSize() {
    return producerRecordSize;
  }

  public void setProducerRecordSize(Integer producerRecordSize) {
    this.producerRecordSize = producerRecordSize;
  }

  public static class Builder {
    DefaultAdditionalConfig config = new DefaultAdditionalConfig();

    public Builder producerRecordSize(Integer producerRecordSize) {
      config.setProducerRecordSize(producerRecordSize);
      return this;
    }

    public Builder consumerRecordSize(Integer consumerRecordSize) {
      config.setConsumerRecordSize(consumerRecordSize);
      return this;
    }

    public DefaultAdditionalConfig build() {
      return config;
    }
  }
}
