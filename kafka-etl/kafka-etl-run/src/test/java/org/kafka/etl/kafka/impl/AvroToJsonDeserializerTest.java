package org.kafka.etl.kafka.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AvroToJsonDeserializerTest {

  private AvroToJsonDeserializer avroToJsonDeserializer_noSchema;

  @Before
  public void init() throws IOException {
    String schema = TestUtils.getStringFromResourceFile("/avro_schema.json");

    avroToJsonDeserializer_noSchema = new AvroToJsonDeserializer(schema, 10);
  }

  @Test
  public void test_nullDataDeserialization() {
    // given
    byte[] data = null;
    String topic = "test_topic";

    // when
    String output = avroToJsonDeserializer_noSchema.deserialize(topic, data);

    // then
    assertThat(output).isNull();
  }

  @Test
  public void test_stripFirstOffsets() {
    // given
    byte[] data = "thisis1010 test test".getBytes();

    // when
    byte[] output = avroToJsonDeserializer_noSchema.stripFirstOffsets(data);

    // then
    assertThat(output).isNotNull().hasSize(data.length - 10);
    assertThat(new String(output)).isEqualTo(" test test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_dataDeserialization_wrongSchema() throws IOException {
    // given
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("twitter.avro");
    byte[] data = new byte[inputStream.available()];
    String topic = "test_topic";

    // when
    avroToJsonDeserializer_noSchema.deserialize(topic, data);
  }
}
