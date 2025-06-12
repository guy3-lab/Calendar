package controller.parse;

import java.time.ZoneId;

/**
 * Parser for create calendar commands.
 */
public class CreateCalendarParser implements CommandParser {

  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("create calendar ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      // Extract calendar name
      String calendarName = extractCalendarName(input);

      // Extract timezone
      ZoneId timezone = extractTimezone(input);

      return ParseResult.createCalendar(calendarName, timezone);

    } catch (Exception e) {
      return ParseResult.error("Create calendar error: " + e.getMessage());
    }
  }

  /**
   * Extracts calendar name from the command.
   */
  private String extractCalendarName(String input) {
    int nameIndex = input.indexOf("--name ");
    if (nameIndex == -1) {
      throw new IllegalArgumentException("Missing --name parameter");
    }

    int nameStart = nameIndex + "--name ".length();
    int timezoneIndex = input.indexOf(" --timezone ", nameStart);

    if (timezoneIndex == -1) {
      throw new IllegalArgumentException("Missing --timezone parameter");
    }

    String name = input.substring(nameStart, timezoneIndex).trim();

    // Handle quoted names
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    return name;
  }

  /**
   * Extracts timezone from the command.
   */
  private ZoneId extractTimezone(String input) {
    int tzIndex = input.indexOf("--timezone ");
    if (tzIndex == -1) {
      throw new IllegalArgumentException("Missing --timezone parameter");
    }

    String timezone = input.substring(tzIndex + "--timezone ".length()).trim();

    if (timezone.isEmpty()) {
      throw new IllegalArgumentException("Timezone cannot be empty");
    }

    try {
      return ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }
  }
}