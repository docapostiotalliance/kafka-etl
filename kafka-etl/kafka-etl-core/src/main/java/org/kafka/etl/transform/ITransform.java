package org.kafka.etl.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ITransform {
    String transform(String input);

    class Default implements ITransform {
        private static final Logger LOGGER = LoggerFactory.getLogger(ITransform.class);

        public String transform(String input) {
            LOGGER.debug("[Default][transform] default transformation of value = {}", input);
            return input;
        }
    }
}
