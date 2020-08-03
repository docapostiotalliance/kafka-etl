package org.kafka.etl.kafka.impl;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDeserializerTest {
  private StringDeserializer deserializer;

  @Before
  public void start() {
    deserializer = new StringDeserializer();
  }

  @Test
  public void testNullData() {
    // given
    byte[] data = null;
    String topic = "test_topic";

    // when
    String output = deserializer.deserialize(topic, data);

    // then
    assertThat(output).isNull();
  }

  @Test
  public void testNonNullData() {
    // given
    byte[] data = new byte[] {0};
    String topic = "test_topic";

    // when
    String output = deserializer.deserialize(topic, data);

    // then
    assertThat(output).isNotNull();
  }
}
