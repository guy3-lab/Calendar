package controller.parse;

import java.time.LocalDateTime;

/**
 * Takes the user input if it's an edit event command and extracts the necessary information to
 * pass to the model to edit the event(s).
 */
public class EditEventParser implements CommandParser {
  @Override
  public boolean canHandle(String input) {
    String lower = input.toLowerCase();
    return lower.startsWith("edit event ") ||
            lower.startsWith("edit events ") ||
            lower.startsWith("edit series ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      CommandType editType = determineEditType(input);
      PropertyType property = extractProperty(input);
      String eventSubject = extractEventSubject(input);
      LocalDateTime eventStart;
      LocalDateTime eventEnd = null;

      if (editType == CommandType.EDIT_EVENT && input.contains(" to ")) {
        eventStart = ParsingTools.extractDateTime(input, " from ", " to ");
        eventEnd = ParsingTools.extractDateTime(input, " to ", " with ");
      } else {
        eventStart = ParsingTools.extractDateTime(input, " from ", " with ");
      }
      String newValue = ParsingTools.extractAfterKeyword(input, " with ");

      return ParseResult.editEvent(editType, property, eventSubject, eventStart,
              eventEnd, newValue);
    } catch (Exception e) {
      return ParseResult.error("Edit command error: " + e.getMessage());
    }
  }

  private CommandType determineEditType(String input) {
    String lower = input.toLowerCase();
    if (lower.startsWith("edit event ")) {
      return CommandType.EDIT_EVENT;
    }
    if (lower.startsWith("edit events ")) {
      return CommandType.EDIT_EVENTS;
    }
    if (lower.startsWith("edit series ")) {
      return CommandType.EDIT_SERIES;
    }
    throw new IllegalArgumentException("Unknown edit type");
  }

  private PropertyType extractProperty(String input) {
    String[] parts = input.split("\\s+");
    if (parts.length < 3) {
      throw new IllegalArgumentException("Missing property");
    }

    String property = parts[2].toLowerCase();
    switch (property) {
      case "subject": return PropertyType.SUBJECT;
      case "start": return PropertyType.START;
      case "end": return PropertyType.END;
      case "description": return PropertyType.DESCRIPTION;
      case "location": return PropertyType.LOCATION;
      case "status": return PropertyType.STATUS;
      default: throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  private String extractEventSubject(String input) {
    String[] parts = input.split("\\s+", 4);
    if (parts.length < 4) {
      throw new IllegalArgumentException("Cannot extract event subject");
    }
    String remainingInput = parts[3];
    int fromIndex = remainingInput.toLowerCase().indexOf(" from ");
    if (fromIndex == -1) {
      throw new IllegalArgumentException("Cannot find 'from' keyword");
    }

    String subjectPart = remainingInput.substring(0, fromIndex).trim();

    if (subjectPart.startsWith("\"") && subjectPart.endsWith("\"") && subjectPart.length() > 1) {
      return subjectPart.substring(1, subjectPart.length() - 1);
    }

    return subjectPart;
  }

  private boolean isPropertyKeyword(String word) {
    return word.equals("subject") || word.equals("start") || word.equals("end") ||
            word.equals("description") || word.equals("location") || word.equals("status");
  }
}