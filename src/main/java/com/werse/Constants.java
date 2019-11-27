package com.werse;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Constants {

  public static final String WORDS_TOPIC_NAME = "de-words";
  public static final String JSON_TOPIC_NAME = "de-json";
  public static final String CONSUMER_GROUP_1 = "de-cg1";
  public static final String CONSUMER_GROUP_2 = "de-cg2";
  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka:9092";

  public static List<String> WORDS;

  static {
    try {
      WORDS = Files.lines(new File("words.txt").toPath()).collect(toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("failed to load words from words.txt");
    }
  }

  public static String now() {
    return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

}
