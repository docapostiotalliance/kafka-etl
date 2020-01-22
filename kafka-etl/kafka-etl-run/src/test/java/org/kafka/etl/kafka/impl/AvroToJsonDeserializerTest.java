package org.kafka.etl.kafka.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AvroToJsonDeserializerTest {

    private AvroToJsonDeserializer avroToJsonDeserializer_noSchema;

    @Before
    public void init() throws IOException {
        String schema = TestUtils.getStringFromResourceFile("/avro_schema.json");

        avroToJsonDeserializer_noSchema = new AvroToJsonDeserializer(schema);
    }

    @Test()
    public void test_nullDataDeserialization() {
        byte[] data = null;
        String topic = "test_topic";

        String output = avroToJsonDeserializer_noSchema.deserialize(topic, data);
        assertThat(output).isNull();

    }
}
