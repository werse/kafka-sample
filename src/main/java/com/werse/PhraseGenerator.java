package com.werse;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class PhraseGenerator {

  public static final ThreadLocalRandom RND = ThreadLocalRandom.current();
  public static final GaussianRandomGenerator GAUSS_RANDOM =
      new GaussianRandomGenerator(new JDKRandomGenerator());

  public static List<String> WORDS;

  static {
    try {
      WORDS = Files.lines(new File("words.txt").toPath()).collect(toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("failed to load words from words.txt");
    }
  }

  public static void main(String[] args) throws IOException {
    try (var printWriter = new PrintWriter(new File("phrases.txt"))) {
      for (int i = 0; i < 10_000; i++) {
        String phrase = generatePhrase();
        printWriter.println(phrase);
      }
    }
  }

  public static String generatePhrase() {
    var builder = new StringBuilder();
    for (int j = 0; j < RND.nextInt(5, 40); j++) {
      builder.append(WORDS.get(getRandomIndex(WORDS.size()))).append(" ");
    }
    return builder.toString();
  }

  private static int getRandomIndex(int size) {
    return (int) Math.round(Math.abs(GAUSS_RANDOM.nextNormalizedDouble()) * size) % size;
  }
}
