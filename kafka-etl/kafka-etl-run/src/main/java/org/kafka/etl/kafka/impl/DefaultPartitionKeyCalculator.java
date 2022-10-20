package org.kafka.etl.kafka.impl;

import org.kafka.etl.load.IPartitionKeyCalculator;

public class DefaultPartitionKeyCalculator implements IPartitionKeyCalculator {
  @Override
  public String generatePartitionKey(String originalKey, String message) {
    return originalKey;
  }
}
