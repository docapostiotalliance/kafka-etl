package org.kafka.etl.kafka.impl;

import org.kafka.etl.kafka.IAdditionalConfig;

import java.util.HashMap;
import java.util.Map;

public class DefaultAdditionalConfig implements IAdditionalConfig {
  @Override
  public Map<String, Object> producerAdditionalConfig() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Object> consumerAdditionalConfig() {
    return new HashMap<>();
  }
}
