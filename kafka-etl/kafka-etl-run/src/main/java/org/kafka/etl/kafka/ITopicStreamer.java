package org.kafka.etl.kafka;

public interface ITopicStreamer {
  void startStream();

  default void startStreamAsync() {
    new Thread(() -> startStream()).start();;
  }
}
