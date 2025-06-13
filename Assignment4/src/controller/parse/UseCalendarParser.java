package controller.parse;

/**
 * Parser for use calendar commands that the user inputs.
 */
public class UseCalendarParser implements CommandParser {

  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("use calendar ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      // Extract calendar name
      String calendarName = extractCalendarName(input);

      return ParseResult.useCalendar(calendarName);

    } catch (Exception e) {
      return ParseResult.error("Use calendar error: " + e.getMessage());
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

    String name = input.substring(nameIndex + "--name ".length()).trim();

    // Handle quoted names
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    return name;
  }
}