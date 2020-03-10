package org.kafka.etl.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilsTest {
  @Test
  public void testIsValid_valid() {
    assertThat(JsonUtils.isValid("{\"test\": true}")).isTrue();
    assertThat(JsonUtils.isValid("[]")).isTrue();
    assertThat(JsonUtils.isValid("{}")).isTrue();
  }

  @Test
  public void testIsValid_notValid() {
    assertThat(JsonUtils.isValid("{\"test\": idontknow}")).isFalse();
    assertThat(JsonUtils.isValid("")).isFalse();
    assertThat(JsonUtils.isValid(null)).isFalse();
  }
}
