package com.werse.producers;

import static com.werse.Constants.KAFKA_BOOTSTRAP_SERVERS;
import static com.werse.Constants.WORDS_TOPIC_NAME;
import static com.werse.Constants.now;
import static com.werse.PhraseGenerator.generatePhrase;

import java.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class PhraseProducer {

  private static final long SLEEP_INTERVAL = 10;

  public static void main(String[] args) throws InterruptedException {
    var consumerConfig = producerProperties();
    try (var producer = new KafkaProducer<String, String>(consumerConfig)) {
      var maxValue = Integer.MAX_VALUE;
      var curr = 0;
      while (curr < maxValue) {
        var line = generatePhrase();
        var key = DigestUtils.sha1Hex(line);
        var record = new ProducerRecord<>(WORDS_TOPIC_NAME, key, line);
        producer.send(record);
        curr++;
        if (curr % 100 == 0) {
          System.out.printf("message sent at %s (total: %s)\n", now(), curr);
        }

        Thread.sleep(SLEEP_INTERVAL);
      }
    }
  }

  private static Properties producerProperties() {
    var properties = new Properties();
    properties.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
    properties.put("value.serializer", StringSerializer.class.getName());
    properties.put("key.serializer", StringSerializer.class.getName());
    properties.put("acks", "all");
    return properties;
  }
}
