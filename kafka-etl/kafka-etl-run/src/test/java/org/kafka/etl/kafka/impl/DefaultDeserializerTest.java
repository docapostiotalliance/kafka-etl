package org.kafka.etl.kafka.impl;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDeserializerTest {

    @Mock
    private StringDeserializer deserializer;

    @Before
    public void start() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullData() {
        byte[] data = null;
        String topic = "test_topic";

        String output = deserializer.deserialize(topic, data);

        assertThat(output).isNull();
    }

    @Test
    public void testNonNullData() {
        byte[] data = new byte[]{0};
        String topic = "test_topic";

        when(deserializer.deserialize(anyString(), any())).thenReturn("data");

        String output = deserializer.deserialize(topic, data);

        assertThat(output).isNotNull();
    }
}
