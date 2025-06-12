package controller.parse;

/**
 * Parser for edit calendar commands.
 */
public class EditCalendarParser implements CommandParser {

  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("edit calendar ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      // Extract calendar name
      String calendarName = extractCalendarName(input);

      // Extract property name
      String propertyName = extractPropertyName(input);

      // Extract new value
      String newValue = extractNewValue(input);

      return ParseResult.editCalendar(calendarName, propertyName, newValue);

    } catch (Exception e) {
      return ParseResult.error("Edit calendar error: " + e.getMessage());
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
    int propertyIndex = input.indexOf(" --property ", nameStart);

    if (propertyIndex == -1) {
      throw new IllegalArgumentException("Missing --property parameter");
    }

    String name = input.substring(nameStart, propertyIndex).trim();

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
   * Extracts property name from the command.
   */
  private String extractPropertyName(String input) {
    int propertyIndex = input.indexOf("--property ");
    if (propertyIndex == -1) {
      throw new IllegalArgumentException("Missing --property parameter");
    }

    int propertyStart = propertyIndex + "--property ".length();

    // Find the next space after property name
    int valueStart = input.indexOf(" ", propertyStart);
    if (valueStart == -1) {
      throw new IllegalArgumentException("Missing property value");
    }

    String property = input.substring(propertyStart, valueStart).trim().toLowerCase();

    // Validate property name
    if (!property.equals("name") && !property.equals("timezone")) {
      throw new IllegalArgumentException("Invalid property: " + property + ". " +
              "Must be 'name' or 'timezone'");
    }

    return property;
  }

  /**
   * Extracts the new value for the property.
   */
  private String extractNewValue(String input) {
    int propertyIndex = input.indexOf("--property ");
    if (propertyIndex == -1) {
      throw new IllegalArgumentException("Missing --property parameter");
    }

    int propertyStart = propertyIndex + "--property ".length();
    int valueStart = input.indexOf(" ", propertyStart);

    if (valueStart == -1) {
      throw new IllegalArgumentException("Missing property value");
    }

    String value = input.substring(valueStart + 1).trim();

    // Handle quoted values
    if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }

    if (value.isEmpty()) {
      throw new IllegalArgumentException("Property value cannot be empty");
    }

    return value;
  }
}