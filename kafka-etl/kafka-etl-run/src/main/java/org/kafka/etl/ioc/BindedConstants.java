package org.kafka.etl.ioc;

public class BindedConstants {
  public static final String GROUP_ID = "GROUP_ID";
  public static final String INPUT_TOPIC = "INPUT_TOPIC";
  public static final String OUTPUT_TOPIC = "OUTPUT_TOPIC";
  public static final String POLL_TIMEOUT = "POLL_TIMEOUT";
  public static final String CONSUMER_RECORD_SIZE = "CONSUMER_RECORD_SIZE";
  public static final String PRODUCER_RECORD_SIZE = "PRODUCER_RECORD_SIZE";

  private BindedConstants() {
    // Nothing to do.
  }
}
