package com.werse.producers;

import static com.werse.Constants.JSON_TOPIC_NAME;
import static com.werse.PhraseGenerator.generatePhrase;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.stream.Collectors.toList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

@SuppressWarnings("all")
public class JsonProducer {

  public static final List<String> customers = IntStream.range(0, 12)
      .mapToObj(i -> UUID.randomUUID())
      .map(UUID::toString)
      .collect(toList());

  public static final List<String> entityKeys = IntStream.range(0, 100)
      .mapToObj(i -> UUID.randomUUID())
      .map(UUID::toString)
      .collect(toList());

  public static final List<String> states = List
      .of("started", "in progress", "filished", "cancelled");

  public static void main(String[] args) throws IOException, InterruptedException {
    var consumerConfig = producerProperties();
    var rnd = current();
    var objectMapper = new ObjectMapper();
    var messageCount = 500;
    try (var producer = new KafkaProducer<String, String>(consumerConfig)) {
      for (int i = 0; i < messageCount; i++) {
        var state = getRandomValue(states);
        var customerIndex = rnd.nextInt(customers.size());
        var customer = customers.get(customerIndex);
        var entityKey = getRandomValue(entityKeys);
        var value = Map.of(
            "customer", customer,
            "entityKey", entityKey,
            "state", state,
            "phrase", generatePhrase());
        var key = DigestUtils.sha1Hex(entityKey + "|" + customer);

        producer.send(new ProducerRecord<>(
            JSON_TOPIC_NAME, customerIndex, key, objectMapper.writeValueAsString(value)));
      }
    }

    System.out.printf("%s records was send to topic: %s\n", messageCount, JSON_TOPIC_NAME);
  }

  private static Properties producerProperties() {
    var properties = new Properties();
    properties.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    properties.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ACKS_CONFIG, "all");
    return properties;
  }

  public static <T> T getRandomValue(List<T> list) {
    return list.get(current().nextInt(list.size()));
  }
}
