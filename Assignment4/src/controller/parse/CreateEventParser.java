package controller.parse;


import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Takes in user input if it's a create event command and extracts the necessary information to pass
 * to the model to create the event(s).
 */
public class CreateEventParser implements CommandParser {
  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("create event");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      String subject = ParsingTools.extractSubject(input, "create event ");
      LocalDateTime startTime = extractStartTime(input);
      LocalDateTime endTime = extractEndTime(input);
      RepeatInfo repeatInfo = extractRepeatInfo(input);

      return ParseResult.createEvent(subject, startTime, endTime, repeatInfo);
    } catch (Exception e) {
      return ParseResult.error("Create command error: " + e.getMessage());
    }
  }

  private LocalDateTime extractStartTime(String input) {
    if (input.contains(" from ")) {
      return ParsingTools.extractDateTime(input, " from ", " to ");
    } else if (input.contains(" on ")) {
      return ParsingTools.extractDate(input, " on ");
    } else {
      throw new IllegalArgumentException("Missing start time information");
    }
  }

  private LocalDateTime extractEndTime(String input) {
    if (input.contains(" to ")) {
      return ParsingTools.extractDateTime(input, " to ", " repeats ");
    }
    return null;
  }

  private RepeatInfo extractRepeatInfo(String input) {
    if (!input.toLowerCase().contains(" repeats ")) {
      return null;
    }

    String repeatDays = extractRepeatDays(input);

    if (input.toLowerCase().contains(" for ") && input.toLowerCase().contains(" times")) {
      int repeatTimes = extractRepeatTimes(input);
      return new RepeatInfo(repeatDays, repeatTimes, null);
    } else if (input.toLowerCase().contains(" until ")) {
      LocalDate repeatUntil = extractRepeatUntil(input);
      return new RepeatInfo(repeatDays, null, repeatUntil);
    } else {
      throw new IllegalArgumentException("Invalid repeat format");
    }
  }

  private String extractRepeatDays(String input) {
    int start = input.toLowerCase().indexOf(" repeats ") + " repeats ".length();
    int forPos = input.toLowerCase().indexOf(" for ", start);
    int untilPos = input.toLowerCase().indexOf(" until ", start);

    int end;
    if (forPos != -1 && untilPos != -1) {
      end = Math.min(forPos, untilPos);
    } else if (forPos != -1) {
      end = forPos;
    } else if (untilPos != -1) {
      end = untilPos;
    } else {
      throw new IllegalArgumentException("Invalid repeat format");
    }

    String repeatDays = input.substring(start, end).trim();
    if (repeatDays.isEmpty() || !repeatDays.matches("[MTWRFSU]+")) {
      throw new IllegalArgumentException("Invalid repeat days: " + repeatDays);
    }

    return repeatDays;
  }

  private int extractRepeatTimes(String input) {
    String timesStr = ParsingTools.extractAfterKeyword(input, " for ").split(" ")[0];
    try {
      int times = Integer.parseInt(timesStr);
      if (times <= 0) {
        throw new IllegalArgumentException("Repeat times must be positive: " + times);
      }
      return times;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid repeat times: " + timesStr);
    }
  }

  private LocalDate extractRepeatUntil(String input) {
    String dateStr = ParsingTools.extractAfterKeyword(input, " until ");
    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid until date: " + dateStr);
    }
  }

}


