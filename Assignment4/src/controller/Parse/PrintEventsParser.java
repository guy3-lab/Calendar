package controller;

import java.time.LocalDate;

public class PrintEventsParser implements CommandParser {
  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("print events ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      if (input.toLowerCase().contains(" from ") && input.toLowerCase().contains(" to ")) {
        LocalDate startDate = extractPrintDate(input, " from ");
        LocalDate endDate = extractPrintDate(input, " to ");
        return ParseResult.printEvents(startDate, endDate);
      } else if (input.toLowerCase().contains(" on ")) {
        LocalDate date = extractPrintDate(input, " on ");
        return ParseResult.printEvents(date, null);
      } else {
        throw new IllegalArgumentException("Invalid print format");
      }
    } catch (Exception e) {
      return ParseResult.error("Print command error: " + e.getMessage());
    }
  }

  private LocalDate extractPrintDate(String input, String pointer) {
    int start = input.toLowerCase().indexOf(pointer.toLowerCase()) + pointer.length();
    int end = (pointer.equals(" from ")) ?
            input.toLowerCase().indexOf(" to ", start) : input.length();
    if (end == -1) end = input.length();

    String dateStr = input.substring(start, end).trim();
    if (dateStr.isEmpty()) {
      throw new IllegalArgumentException("Empty date");
    }

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date: " + dateStr);
    }
  }
}
