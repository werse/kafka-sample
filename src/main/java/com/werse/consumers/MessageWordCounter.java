package com.werse.consumers;

import static com.werse.Constants.KAFKA_BOOTSTRAP_SERVERS;
import static com.werse.Constants.WORDS_TOPIC_NAME;
import static com.werse.Constants.now;
import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;

@Slf4j
public class MessageWordCounter {

  public static void main(String[] args) {
    var latch = new CountDownLatch(1);

    var stringSerde = Serdes.String();
    var storeName = "wordsCountTable";

    var streamsBuilder = new StreamsBuilder();
    streamsBuilder.stream(WORDS_TOPIC_NAME, Consumed.with(stringSerde, stringSerde))
        .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
        .groupBy(((key, value) -> value))
        .count(Materialized.as(storeName));

    var streams = new KafkaStreams(streamsBuilder.build(), streamProperties());

    streams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
      System.err.println("exiting");
      System.err.println(throwable.getMessage());
      latch.countDown();
    });

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    try {
      streams.cleanUp();
      streams.start();
      var state = streams.state();
      while (state != State.RUNNING) {
        state = streams.state();
      }

      var storeQueryParameters = StoreQueryParameters
          .fromNameAndType(storeName, QueryableStoreTypes.<String, Long>keyValueStore());

      var store = streams.store(storeQueryParameters);
      for (int i = 0; i < Integer.MAX_VALUE; i++) {
        System.out.println(now());
        System.out.printf("word 'go' repeated %s times\n", store.get("go"));
        System.out.printf("word 'inside' repeated %s times\n", store.get("inside"));
        System.out.println();
        Thread.sleep(2000);
      }

      latch.await();
    } catch (Throwable e) {
      log.error("exiting app with code 1", e);
      System.exit(1);
    }

    log.error("Existing application with status code 0");
    streams.close(Duration.ofSeconds(5));
    System.exit(0);
  }

  private static Properties streamProperties() {
    var properties = new Properties();
    properties.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
    properties.put("application.id", "words_consumer");
    properties.put("default.key.serde", Serdes.String().getClass().getName());
    properties.put("default.value.serde", Serdes.String().getClass().getName());
    properties.put("group.id", randomUUID().toString());
    properties.put("auto.offset.reset", "earliest");
    properties.put("max.poll.records", 10000);
    properties.put("processing.guarantee", "exactly_once");
    properties.put("cache.max.bytes.buffering", 0);
    properties.put("state.dir", "C:/dev/projects/java/kafka-sample/store_log");
    return properties;
  }
}
