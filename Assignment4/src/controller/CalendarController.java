package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import controller.Parse.CommandParserCoordinator;
import controller.Parse.CommandType;
import controller.Parse.ParseResult;
import controller.Parse.RepeatInfo;
import controller.format.IOutputFormatter;
import controller.format.OutputFormatter;
import model.Calendar;
import model.Event;
import model.ICalendar;

/**
 * Controller that executes parsed commands on the calendar model and formats output.
 * Now uses IOutputFormatter interface for better abstraction and testability.
 */
public class CalendarController {
  private final ICalendar calendar;
  private final IOutputFormatter formatter;

  /**
   * Constructor with dependency injection for better testability.
   * @param calendar the calendar model to use
   * @param formatter the output formatter to use
   */
  public CalendarController(ICalendar calendar, IOutputFormatter formatter) {
    this.calendar = calendar;
    this.formatter = formatter;
  }

  /**
   * Default constructor for convenience - creates default dependencies.
   */
  public CalendarController() {
    this(new Calendar(), new OutputFormatter());
  }

  /**
   * Executes a command string and returns the result.
   * @param commandString the command to execute
   * @return formatted output string, or null if no output needed
   * @throws IllegalArgumentException if command is invalid or execution fails
   */
  public String executeCommand(String commandString) {
    ParseResult parseResult = CommandParserCoordinator.parseCommand(commandString);

    if (!parseResult.isSuccess()) {
      throw new IllegalArgumentException(parseResult.getErrorMessage());
    }

    switch (parseResult.getCommandType()) {
      case CREATE_EVENT:
        return executeCreateEvent(parseResult);
      case EDIT_EVENT:
      case EDIT_EVENTS:
      case EDIT_SERIES:
        return executeEditEvent(parseResult);
      case PRINT_EVENTS:
        return executePrintEvents(parseResult);
      case SHOW_STATUS:
        return executeShowStatus(parseResult);
      case EXIT:
        return null;
      default:
        throw new IllegalArgumentException("Unknown command type: " + parseResult.getCommandType());
    }
  }

  /**
   * Executes create event commands.
   * Now uses formatter.formatSuccess() for consistent success message formatting.
   */
  private String executeCreateEvent(ParseResult parseResult) {
    try {
      if (parseResult.isRepeating()) {
        RepeatInfo repeatInfo = parseResult.getRepeatInfo();
        List<String> repeatDays = parseRepeatDays(repeatInfo.getRepeatDays());

        if (repeatInfo.hasTimeLimit()) {
          calendar.createSeriesTimes(
                  parseResult.getSubject(),
                  parseResult.getStartTime(),
                  parseResult.getEndTime(),
                  repeatDays,
                  repeatInfo.getRepeatTimes()
          );
          return formatter.formatSuccess("Created recurring event series: \"" + parseResult.getSubject() +
                  "\" (" + repeatInfo.getRepeatTimes() + " occurrences)");
        } else if (repeatInfo.hasDateLimit()) {
          calendar.createSeriesUntil(
                  parseResult.getSubject(),
                  parseResult.getStartTime(),
                  parseResult.getEndTime(),
                  repeatDays,
                  repeatInfo.getRepeatUntil()
          );
          return formatter.formatSuccess("Created recurring event series: \"" + parseResult.getSubject() +
                  "\" (until " + repeatInfo.getRepeatUntil() + ")");
        }
      } else {
        Event event = calendar.createEvent(
                parseResult.getSubject(),
                parseResult.getStartTime(),
                parseResult.getEndTime()
        );

        String eventType = parseResult.isAllDay() ? "all-day event" : "event";
        return formatter.formatSuccess("Created " + eventType + ": \"" + parseResult.getSubject() + "\"");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create event: " + e.getMessage());
    }

    return null;
  }

  /**
   * Executes edit event commands.
   * Now uses formatter.formatSuccess() for consistent success message formatting.
   */
  private String executeEditEvent(ParseResult parseResult) {
    try {
      CommandType editType = parseResult.getCommandType();

      switch (editType) {
        case EDIT_EVENT:
          calendar.editEvent(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getEventEnd(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated event: \"" + parseResult.getEventSubject() + "\"");

        case EDIT_EVENTS:
          calendar.editEvents(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated events starting from: \"" + parseResult.getEventSubject() + "\"");

        case EDIT_SERIES:
          calendar.editSeries(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated entire series: \"" + parseResult.getEventSubject() + "\"");

        default:
          throw new IllegalArgumentException("Unknown edit type: " + editType);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Executes print events commands.
   * Uses the formatter interface to format output.
   */
  private String executePrintEvents(ParseResult parseResult) {
    try {
      Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();

      if (parseResult.isPrintRange()) {
        return formatter.formatEventRange(
                calendarData,
                parseResult.getPrintStartDate(),
                parseResult.getPrintEndDate()
        );
      } else {
        return formatter.formatEventsForDate(
                calendarData,
                parseResult.getPrintStartDate()
        );
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to print events: " + e.getMessage());
    }
  }

  /**
   * Executes show status commands.
   */
  private String executeShowStatus(ParseResult parseResult) {
    try {
      LocalDateTime queryTime = parseResult.getStatusDateTime();
      Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();

      boolean isBusy = checkIfBusy(calendarData, queryTime);
      return isBusy ? "busy" : "available";
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to check status: " + e.getMessage());
    }
  }

  /**
   * Checks if the user is busy at a specific date and time.
   * A user is considered busy if the query time falls within any event's time range.
   * <p>
   * Time range logic: queryTime >= eventStart AND queryTime < eventEnd
   * - If query time equals event start time: BUSY (event is starting)
   * - If query time equals event end time: AVAILABLE (event has ended)
   *</p>
   * @param calendarData the calendar data to search
   * @param queryTime the specific moment to check
   * @return true if busy, false if available
   */
  private boolean checkIfBusy(Map<LocalDate, List<Event>> calendarData, LocalDateTime queryTime) {
    LocalDate queryDate = queryTime.toLocalDate();

    if (!calendarData.containsKey(queryDate)) {
      return false;
    }

    List<Event> dayEvents = calendarData.get(queryDate);

    for (Event event : dayEvents) {
      // check if query time falls within event time range [start, end)
      if (!queryTime.isBefore(event.getStart()) && queryTime.isBefore(event.getEnd())) {
        return true; // found overlapping event
      }

      // check multi-day events
      if (event.getStart().toLocalDate().isBefore(queryDate) &&
              event.getEnd().toLocalDate().isAfter(queryDate)) {
        return true;
      }
    }

    return false; // did not find any event(s) overlapping
  }

  /**
   * Converts repeat days string (e.g., "MWF") to list of day names.
   */
  private List<String> parseRepeatDays(String repeatDays) {
    return Arrays.asList(repeatDays.split(""));
  }

  // getters for testing
  public ICalendar getCalendar() {
    return calendar;
  }

  public IOutputFormatter getFormatter() {
    return formatter;
  }
}
