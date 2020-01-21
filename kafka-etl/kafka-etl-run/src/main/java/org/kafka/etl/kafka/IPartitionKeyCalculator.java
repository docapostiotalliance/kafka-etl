package org.kafka.etl.kafka;

public interface IPartitionKeyCalculator {
  String generatePartitionKey(String originalKey, String message);
}
