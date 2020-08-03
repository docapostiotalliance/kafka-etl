package org.kafka.etl.utils;

import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.kafka.etl.kafka.impl.TestUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

import static org.kafka.etl.utils.PropertiesUtils.fromJson;

public class PropertiesUtilsTest {
  @Test
  public void testFromJson_nominal() throws IOException {
    // Given
    String json = TestUtils.getStringFromResourceFile("/properties.json");
    JsonObject jsonObj = new JsonObject(json);

    // When
    Properties prop = fromJson(jsonObj);

    // Then
    assertThat(prop).isNotNull();
    assertThat(prop.get("test")).isEqualTo("value");
    assertThat(prop.get("testInteger")).isEqualTo(1);
    assertThat(prop.get("testBoolean")).isEqualTo(true);
    assertThat(prop.get("testObject")).isNotNull();
  }

  @Test
  public void testFromJson_null() {
    // Given
    JsonObject jsonObj = null;

    // When
    Properties prop = fromJson(jsonObj);

    // Then
    assertThat(prop).isNull();
  }
}
