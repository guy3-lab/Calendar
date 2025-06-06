package controller.parse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Class that contains tools for parsing the string commands that the user will input.
 */
public class ParsingTools {
  private ParsingTools() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Extract quoted or unquoted subject from input.
   */
  public static String extractSubject(String input, String prefix) {
    if (!input.toLowerCase().startsWith(prefix.toLowerCase())) {
      throw new IllegalArgumentException("Invalid command format");
    }

    int subjectStart = prefix.length();

    if (subjectStart < input.length() && input.charAt(subjectStart) == '"') {
      return extractQuotedSubject(input, subjectStart);
    } else {
      return extractUnquotedSubject(input, subjectStart);
    }
  }

  /**
   * Extract datetime between two pointers.
   */
  public static LocalDateTime extractDateTime(String input, String startPointer,
                                              String endPointer) {
    int start = input.toLowerCase().indexOf(startPointer.toLowerCase());
    if (start == -1) {
      throw new IllegalArgumentException("Missing pointer: " + startPointer);
    }
    start += startPointer.length();

    int end;
    if (endPointer != null) {
      end = input.toLowerCase().indexOf(endPointer.toLowerCase(), start);
      if (end == -1) {end = input.length();}
    } else {
      end = input.length();
    }

    String dateTimeStr = input.substring(start, end).trim();
    if (dateTimeStr.isEmpty()) {
      throw new IllegalArgumentException("Empty datetime value");
    }

    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeStr);
    }
  }

  /**
   * Extract date and convert to datetime - exclusively for all-day events.
   */
  public static LocalDateTime extractDate(String input, String pointer) {
    int start = input.toLowerCase().indexOf(pointer.toLowerCase());
    if (start == -1) {
      throw new IllegalArgumentException("Missing pointer: " + pointer);
    }
    start += pointer.length();

    int end = input.indexOf(" ", start);
    if (end == -1) {end = input.length();}

    String dateStr = input.substring(start, end).trim();
    if (dateStr.isEmpty()) {
      throw new IllegalArgumentException("Empty date value");
    }

    try {
      LocalDate date = LocalDate.parse(dateStr);
      // 8 am default
      return date.atTime(8, 0);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }
  }

  /**
   * Extract value after a keyword.
   */
  public static String extractAfterKeyword(String input, String keyword) {
    int start = input.toLowerCase().indexOf(keyword.toLowerCase()) + keyword.length();
    if (start == keyword.length() - 1) {
      throw new IllegalArgumentException("Missing keyword: " + keyword);
    }

    String value = input.substring(start).trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Empty value after: " + keyword);
    }

    return value;
  }


  // private helper method
  private static String extractQuotedSubject(String input, int startPos) {
    int quoteEnd = input.indexOf('"', startPos + 1);
    if (quoteEnd == -1) {
      throw new IllegalArgumentException("Quotation must close");
    }

    String subject = input.substring(startPos + 1, quoteEnd);
    if (subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }

    return subject;
  }

  //private helper method
  private static String extractUnquotedSubject(String input, int startPos) {
    int fromPos = input.toLowerCase().indexOf(" from ", startPos);
    int onPos = input.toLowerCase().indexOf(" on ", startPos);

    int endPos = -1;
    if (fromPos != -1 && onPos != -1) {
      endPos = Math.min(fromPos, onPos);
    } else if (fromPos != -1) {
      endPos = fromPos;
    } else if (onPos != -1) {
      endPos = onPos;
    }

    if (endPos == -1) {
      throw new IllegalArgumentException("Cannot determine subject boundaries");
    }

    String subject = input.substring(startPos, endPos).trim();
    if (subject.isEmpty()) {
      throw new IllegalArgumentException("Subject can't be empty");
    }

    return subject;
  }
}
