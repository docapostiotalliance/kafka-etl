package org.kafka.etl.transform;

import java.util.Map;
import java.util.Optional;

public interface ITransform {
  Optional<String> transform(String input, Map<String, String> metadata);
}
