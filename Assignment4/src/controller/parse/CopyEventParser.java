package controller.parse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parser for copy event and copy events commands.
 */
public class CopyEventParser implements CommandParser {

  @Override
  public boolean canHandle(String input) {
    String lower = input.toLowerCase();
    return lower.startsWith("copy event ") || lower.startsWith("copy events ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      String lower = input.toLowerCase();

      if (lower.startsWith("copy event ")) {
        return parseCopySingleEvent(input);
      } else if (lower.contains(" between ") && lower.contains(" and ")) {
        return parseCopyEventsBetween(input);
      } else {
        return parseCopyEventsOnDay(input);
      }

    } catch (Exception e) {
      return ParseResult.error("Copy event error: " + e.getMessage());
    }
  }

  /**
   * Parses copy single event command.
   */
  private ParseResult parseCopySingleEvent(String input) {
    // Extract event name
    String eventName = extractEventName(input);

    // Extract source date/time
    LocalDateTime sourceDateTime = extractSourceDateTime(input);

    // Extract target calendar name
    String targetCalendar = extractTargetCalendar(input);

    // Extract target date/time
    LocalDateTime targetDateTime = extractTargetDateTime(input);

    return ParseResult.copySingleEvent(eventName, sourceDateTime, targetCalendar, targetDateTime);
  }

  /**
   * Parses copy events on day command.
   */
  private ParseResult parseCopyEventsOnDay(String input) {
    // Extract source date
    LocalDate sourceDate = extractSourceDate(input);

    // Extract target calendar
    String targetCalendar = extractTargetCalendar(input);

    // Extract target date
    LocalDate targetDate = extractTargetDate(input);

    return ParseResult.copyEventsOnDay(sourceDate, targetCalendar, targetDate);
  }

  /**
   * Parses copy events between dates command.
   */
  private ParseResult parseCopyEventsBetween(String input) {
    // Extract start date
    LocalDate startDate = extractBetweenStartDate(input);

    // Extract end date
    LocalDate endDate = extractBetweenEndDate(input);

    // Extract target calendar
    String targetCalendar = extractTargetCalendar(input);

    // Extract target date
    LocalDate targetDate = extractTargetDate(input);

    return ParseResult.copyEventsBetween(startDate, endDate, targetCalendar, targetDate);
  }

  /**
   * Extracts event name from copy event command.
   */
  private String extractEventName(String input) {
    int start = "copy event ".length();
    int onIndex = input.toLowerCase().indexOf(" on ", start);

    if (onIndex == -1) {
      throw new IllegalArgumentException("Missing 'on' keyword");
    }

    String name = input.substring(start, onIndex).trim();

    // Handle quoted names
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Event name cannot be empty");
    }

    return name;
  }

  /**
   * Extracts source date/time for single event copy.
   */
  private LocalDateTime extractSourceDateTime(String input) {
    int onIndex = input.toLowerCase().indexOf(" on ");
    if (onIndex == -1) {
      throw new IllegalArgumentException("Missing 'on' keyword");
    }

    int start = onIndex + " on ".length();
    int targetIndex = input.toLowerCase().indexOf(" --target ", start);

    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String dateTimeStr = input.substring(start, targetIndex).trim();

    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid source date/time: " + dateTimeStr);
    }
  }

  /**
   * Extracts source date for copy events on day.
   */
  private LocalDate extractSourceDate(String input) {
    int onIndex = input.toLowerCase().indexOf(" on ");
    if (onIndex == -1) {
      throw new IllegalArgumentException("Missing 'on' keyword");
    }

    int start = onIndex + " on ".length();
    int targetIndex = input.toLowerCase().indexOf(" --target ", start);

    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String dateStr = input.substring(start, targetIndex).trim();

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid source date: " + dateStr);
    }
  }

  /**
   * Extracts start date for copy between command.
   */
  private LocalDate extractBetweenStartDate(String input) {
    int betweenIndex = input.toLowerCase().indexOf(" between ");
    if (betweenIndex == -1) {
      throw new IllegalArgumentException("Missing 'between' keyword");
    }

    int start = betweenIndex + " between ".length();
    int andIndex = input.toLowerCase().indexOf(" and ", start);

    if (andIndex == -1) {
      throw new IllegalArgumentException("Missing 'and' keyword");
    }

    String dateStr = input.substring(start, andIndex).trim();

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid start date: " + dateStr);
    }
  }

  /**
   * Extracts end date for copy between command.
   */
  private LocalDate extractBetweenEndDate(String input) {
    int andIndex = input.toLowerCase().indexOf(" and ");
    if (andIndex == -1) {
      throw new IllegalArgumentException("Missing 'and' keyword");
    }

    int start = andIndex + " and ".length();
    int targetIndex = input.toLowerCase().indexOf(" --target ", start);

    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    String dateStr = input.substring(start, targetIndex).trim();

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid end date: " + dateStr);
    }
  }

  /**
   * Extracts target calendar name.
   */
  private String extractTargetCalendar(String input) {
    int targetIndex = input.indexOf("--target ");
    if (targetIndex == -1) {
      throw new IllegalArgumentException("Missing --target parameter");
    }

    int start = targetIndex + "--target ".length();
    int toIndex = input.toLowerCase().indexOf(" to ", start);

    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    String name = input.substring(start, toIndex).trim();

    // Handle quoted names
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Target calendar name cannot be empty");
    }

    return name;
  }

  /**
   * Extracts target date/time for single event copy.
   */
  private LocalDateTime extractTargetDateTime(String input) {
    int toIndex = input.toLowerCase().lastIndexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    String dateTimeStr = input.substring(toIndex + " to ".length()).trim();

    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid target date/time: " + dateTimeStr);
    }
  }

  /**
   * Extracts target date for copy events commands.
   */
  private LocalDate extractTargetDate(String input) {
    int toIndex = input.toLowerCase().lastIndexOf(" to ");
    if (toIndex == -1) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    String dateStr = input.substring(toIndex + " to ".length()).trim();

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid target date: " + dateStr);
    }
  }
}