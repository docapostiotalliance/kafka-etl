package org.kafka.etl.utils;

import io.vertx.core.json.JsonObject;

import java.util.Properties;

public class PropertiesUtils {
  public static Properties fromJson(JsonObject properties) {
    if (null == properties) {
      return null;
    }

    Properties prop = new Properties();
    properties.stream().forEach(p -> prop.put(p.getKey(), p.getValue()));
    return prop;
  }

  private PropertiesUtils() {
    // Nothing to do.
  }
}
