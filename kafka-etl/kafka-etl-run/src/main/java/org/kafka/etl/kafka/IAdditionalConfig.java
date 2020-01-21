package org.kafka.etl.kafka;

import java.util.Map;

public interface IAdditionalConfig {
  Map<String, Object> producerAdditionalConfig();

  Map<String, Object> consumerAdditionalConfig();
}
