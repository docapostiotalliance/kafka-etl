package org.kafka.etl.kafka.impl;

import org.apache.kafka.common.TopicPartition;

public class EventKafkaInfos {
  private Long offset;

  private TopicPartition topicPartition;

  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  public Integer getPartition() {
    return topicPartition.partition();
  }

  public String getTopic() {
    return topicPartition.topic();
  }

  public TopicPartition getTopicPartition() {
    return topicPartition;
  }

  public void setTopicPartition(TopicPartition topicPartition) {
    this.topicPartition = topicPartition;
  }

  public static class Builder {
    EventKafkaInfos infos = new EventKafkaInfos();

    public Builder offset(Long offset) {
      infos.setOffset(offset);
      return this;
    }

    public Builder topicPartirion(TopicPartition topicPartition) {
      infos.setTopicPartition(topicPartition);
      return this;
    }

    public EventKafkaInfos build() {
      return infos;
    }
  }

  @Override
  public String toString() {
    return "EventKafkaInfos{" + "offset="
        + offset
        + ", partition="
        + topicPartition.partition()
        + '}';
  }
}
