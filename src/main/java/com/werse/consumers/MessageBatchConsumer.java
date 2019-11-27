package com.werse.consumers;

import static com.werse.Constants.CONSUMER_GROUP_1;
import static com.werse.Constants.KAFKA_BOOTSTRAP_SERVERS;
import static com.werse.Constants.WORDS_TOPIC_NAME;
import static com.werse.Constants.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class MessageBatchConsumer {

  public static void main(String[] args) {
    runKafkaConsumer();
  }

  private static void runKafkaConsumer() {
    var group = CONSUMER_GROUP_1;
    var consumerId = "de-consumer-" + randomUUID();
    System.out.printf("Running consumer with id: %s, groupId: %s\n", consumerId, group);
    var consumerConfig = consumerProperties(group);
    try (var consumer = new KafkaConsumer<String, String>(consumerConfig)) {
      loadMessages(consumer);
    }
  }

  private static void loadMessages(KafkaConsumer<String, String> consumer) {
    consumer.subscribe(List.of(WORDS_TOPIC_NAME));
    while (true) {
      var consumerRecords = consumer.poll(Duration.of(100, MILLIS));
      var cnt = consumerRecords.count();
      if (cnt > 0) {
        System.out.printf("[%s] received messages count: %s\n", now(), cnt);
      }
    }
  }

  private static Properties consumerProperties(String group) {
    var properties = new Properties();
    properties.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
    properties.put("key.deserializer", StringDeserializer.class);
    properties.put("value.deserializer", StringDeserializer.class);
    properties.put("group.id", group);
    properties.put("auto.offset.reset", "earliest");
    properties.put("fetch.min.bytes", "10485760");
    properties.put("max.poll.records", "100");
    properties.put("fetch.max.wait.ms", "2500");

    return properties;
  }
}
