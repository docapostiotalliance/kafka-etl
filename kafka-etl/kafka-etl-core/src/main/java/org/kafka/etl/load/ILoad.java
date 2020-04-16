package org.kafka.etl.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ILoad {

  void loadEvent(String originalKey, String event);

  void close();

  class Default implements ILoad {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILoad.class);

    @Override
    public void loadEvent(String originalKey, String event) {
      LOGGER.info("[ILoad][Default] load event into nothing: originalKey = {}, event = {}",
          originalKey,
          event);
    }

    @Override
    public void close() {
      // Do nothing
    }
  }
}
