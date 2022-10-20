package org.kafka.etl.load;

public interface IPartitionKeyCalculator {
  String generatePartitionKey(String originalKey, String message);
}
