package org.kafka.etl.kafka.impl;

import org.kafka.etl.kafka.IPartitionKeyCalculator;

import java.util.UUID;

public class DefaultPartitionKeyCalculator implements IPartitionKeyCalculator {
  @Override
  public String generatePartitionKey(String message) {
    return UUID.randomUUID().toString();
  }
}
