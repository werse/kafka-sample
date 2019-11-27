package com.werse.consumers;

import static com.werse.Constants.CONSUMER_GROUP_1;
import static com.werse.Constants.KAFKA_BOOTSTRAP_SERVERS;
import static com.werse.Constants.WORDS_TOPIC_NAME;
import static com.werse.Constants.now;
import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

@SuppressWarnings("all")
public class MessageConsumer {

  public static void main(String[] args) {
    runKafkaConsumer(CONSUMER_GROUP_1);
  }

  private static void runKafkaConsumer(String group) {
    var consumerId = "de-consumer-" + randomUUID();
    System.out.printf("Running consumer with id: %s, groupId: %s\n", consumerId, group);
    var consumerConfig = consumerProperties(consumerId, group);
    try (var consumer = new KafkaConsumer<String, String>(consumerConfig)) {
      loadMessages(consumer, consumerId, MessageConsumer::printMessage);
    }
  }

  private static void loadMessages(KafkaConsumer<String, String> consumer, String consumerId,
      BiConsumer<String, ConsumerRecord<String, String>> messageConsumerCallback) {
    var noRecordsCount = 0L;
    var messageLimit = Long.MAX_VALUE;
    var messageCounter = 0;
    consumer.subscribe(List.of(WORDS_TOPIC_NAME));
    while (true) {
      var consumerRecords = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
      if (consumerRecords.count() == 0) {
        noRecordsCount++;
        if (noRecordsCount > Long.MAX_VALUE - 1) {
          break;
        } else {
          continue;
        }
      }
      messageCounter += consumerRecords.count();
      consumerRecords.forEach(record -> messageConsumerCallback.accept(consumerId, record));
      if (messageCounter > messageLimit) {
        break;
      }
    }
  }

  private static Properties consumerProperties(String id, String group) {
    var properties = new Properties();
    properties.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
    properties.put("key.deserializer", StringDeserializer.class);
    properties.put("value.deserializer", StringDeserializer.class);
    properties.put("group.id", group);
    properties.put("auto.offset.reset", "latest");
    return properties;
  }

  private static void printMessage(String consumerId, ConsumerRecord<String, String> record) {
    var value = record.value();
    System.out.println(now());
    System.out.println(consumerId);
    System.out.println("record:");
    System.out.println("  key: " + record.key());
    System.out.println("  partition: " + record.partition());
    System.out.println("  offset: " + record.offset() + "\n");
    System.out.println("  headers:");
    record.headers().forEach(header ->
        System.out.println("    " + header.key() + ": " + new String(header.value())));
    System.out.println("  value: " + value);
    System.out.println();
  }
}
