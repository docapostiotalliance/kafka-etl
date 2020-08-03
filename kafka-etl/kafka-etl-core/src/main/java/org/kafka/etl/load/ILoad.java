package org.kafka.etl.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public interface ILoad {

  ILoad init(Properties properties);

  void loadEvent(String originalKey, String event);

  void close();

  class Default implements ILoad {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILoad.class);


    @Override
    public ILoad init(Properties properties) {
      return this;
    }

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
