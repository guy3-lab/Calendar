package controller;

import java.time.LocalDateTime;

public class CreateParse {
  private CreateParse() {
    throw new IllegalStateException("Utility class");
  }
  static String getSubject(String input) {
    String start = "create event ";
    int subjectIndexStart = start.length();
    int subjectIndexEnd = input.indexOf(" from ");
    int subjectIndexEnd2 = input.indexOf(" on ");
    if (subjectIndexEnd != -1) {
      return getHelper(input, subjectIndexStart, subjectIndexEnd);
    }

    if (subjectIndexEnd2 != -1) {
      return getHelper(input, subjectIndexStart, subjectIndexEnd2);
    }
    throw new IllegalArgumentException("Invalid input: Missing 'from' or 'on'");
  }



  static LocalDateTime getStartTime(String input) {
    int startIndexStart = input.indexOf(" from ");
    int startIndexStart2 = input.indexOf(" on ");
    int startIndexEnd = input.indexOf(" to ");
    int startIndexEnd2 = input.indexOf(" repeats ");
    if (startIndexStart != -1 && startIndexEnd != -1) {
      return LocalDateTime.parse(
              getHelper(input, startIndexStart + " from ".length(), startIndexEnd));
    }

    if (startIndexStart2 != -1 && startIndexEnd2 != -1) {
      return LocalDateTime.parse(
              getHelper(input, startIndexStart2 + " on ".length(), startIndexEnd2));
    }
    throw new IllegalArgumentException("Invalid input: Missing 'to' or 'repeats'");
  }

  static LocalDateTime getEndTime(String input) {
    int endIndexStart = input.indexOf(" to ");
    int endIndexEnd = input.indexOf(" repeats ");
    if (endIndexStart != -1 && endIndexEnd != -1) {
      return LocalDateTime.parse(input.substring((endIndexStart + " to ".length()), endIndexEnd));
    }

    if (endIndexStart != -1) {
      return LocalDateTime.parse(input.substring((endIndexStart + " to ".length())));
    }
    return null;
  }

  static String getRepeats(String input) {
    int repeatsIndexStart = input.indexOf(" repeats ");
    int repeatsIndexEnd = input.indexOf(" for ");
    int repeatsIndexEnd2 = input.indexOf(" until ");
    if (repeatsIndexStart != -1 && repeatsIndexEnd != -1) {
      return getHelper(input, repeatsIndexStart + " repeats ".length(), repeatsIndexEnd);
    }

    if (repeatsIndexStart != -1 && repeatsIndexEnd2 != -1) {
      return getHelper(input, repeatsIndexStart + " repeats ".length(), repeatsIndexEnd2);
    }
    return "";
  }

  static int getTimesRepeat(String input) {
    int timesRepeatIndexStart = input.indexOf(" for ");
    int timesRepeatIndexEnd = input.indexOf(" times ");
    if (timesRepeatIndexStart != -1 && timesRepeatIndexEnd != -1) {
      return Integer.parseInt(
              getHelper(input, timesRepeatIndexStart + " for ".length(), timesRepeatIndexEnd));
    }
    return 0;
  }

  static LocalDateTime getUntil(String input) {
    int untilIndexStart = input.indexOf(" until ");
    if (untilIndexStart != -1) {
      String result = input.substring((untilIndexStart + " until ".length()));
      if (result.isEmpty()) {
        throw new IllegalArgumentException("Empty value found");
      }
    }
    return null;
  }

  static String getHelper(String input, int start, int end) {
    String result = input.substring(start, end);
    if (result.isEmpty()) {
      throw new IllegalArgumentException("Empty value found");
    }
    return result;
  }
}
