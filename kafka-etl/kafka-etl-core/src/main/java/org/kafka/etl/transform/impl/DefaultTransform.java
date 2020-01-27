package org.kafka.etl.transform.impl;

import org.kafka.etl.transform.ITransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DefaultTransform implements ITransform {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransform.class);

  @Override
  public String transform(String input, Map<String, String> metadata) {
    LOGGER.debug("[Default][transform] default transformation of value = {}, metadata = {}",
        input,
        metadata);
    return input;
  }
}
