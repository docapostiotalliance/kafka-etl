package org.kafka.etl.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class JsonUtils {
  public static final ObjectMapper MAPPER = initObjectMapper();

  private JsonUtils() {
    // Nothing to do.
  }

  public static ObjectMapper initObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .setDateFormat(new ISO8601DateFormat())
        .setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return objectMapper;
  }

  public static String toJson(final Object o) {
    try {
      return MAPPER.writeValueAsString(o);
    } catch (IOException ioe) {
      String msg = "Error convert to json from object " + o.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static boolean isValid(final String json, final ObjectMapper mapper) {
    if (isBlank(json)) {
      return false;
    }

    try {
      mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
      mapper.readTree(json);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static boolean isValid(final String json) {
    return isValid(json, MAPPER);
  }
}

