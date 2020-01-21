package org.kafka.etl.kafka.impl;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.kafka.etl.kafka.IDeserializer;

import java.util.Map;

public class DefaultDeserializer implements IDeserializer {
  private StringDeserializer deserializer;

  public DefaultDeserializer() {
    deserializer = new StringDeserializer();
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {
    deserializer.configure(map, b);
  }

  @Override
  public String deserialize(String topic, byte[] data) {
    return deserializer.deserialize(topic, data);
  }

  @Override
  public void close() {
    deserializer.close();
  }
}
