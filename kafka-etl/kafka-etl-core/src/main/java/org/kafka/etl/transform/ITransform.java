package org.kafka.etl.transform;

import java.util.Map;

public interface ITransform {
  String transform(String input, Map<String, String> metadata);
}
