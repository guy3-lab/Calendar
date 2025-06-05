package controller;

import java.time.LocalDateTime;

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
      LocalDateTime eventStart = ParsingTools.extractDateTime(input, " from ", " to ");
      LocalDateTime eventEnd = (editType == CommandType.EDIT_EVENT && input.contains(" to ")) ?
              ParsingTools.extractDateTime(input, " to ", " with ") : null;
      String newValue = ParsingTools.extractAfterKeyword(input, " with ");

      return ParseResult.editEvent(editType, property, eventSubject, eventStart, eventEnd, newValue);
    } catch (Exception e) {
      return ParseResult.error("Edit command error: " + e.getMessage());
    }
  }

  private CommandType determineEditType(String input) {
    String lower = input.toLowerCase();
    if (lower.startsWith("edit event ")) return CommandType.EDIT_EVENT;
    if (lower.startsWith("edit events ")) return CommandType.EDIT_EVENTS;
    if (lower.startsWith("edit series ")) return CommandType.EDIT_SERIES;
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
    String[] parts = input.split("\\s+");
    StringBuilder subject = new StringBuilder();

    boolean foundProperty = false;
    for (String part : parts) {
      if (foundProperty && !part.equalsIgnoreCase("from")) {
        if (subject.length() > 0) subject.append(" ");
        subject.append(part);
      } else if (foundProperty && part.equalsIgnoreCase("from")) {
        break;
      } else if (isPropertyKeyword(part.toLowerCase())) {
        foundProperty = true;
      }
    }

    if (subject.length() == 0) {
      throw new IllegalArgumentException("Cannot extract event subject");
    }

    return subject.toString().trim();
  }

  private boolean isPropertyKeyword(String word) {
    return word.equals("subject") || word.equals("start") || word.equals("end") ||
            word.equals("description") || word.equals("location") || word.equals("status");
  }
}
