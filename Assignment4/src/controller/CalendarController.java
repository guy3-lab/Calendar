package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import controller.parse.CommandParserCoordinator;
import controller.parse.CommandType;
import controller.parse.ParseResult;
import controller.parse.RepeatInfo;
import controller.format.IOutputFormatter;
import controller.format.OutputFormatter;
import model.calendar.Calendar;
import model.calendar.Event;
import model.calendar.ICalendar;
import model.calendar.SpecificCalendar;
import model.multicalendar.IMultiCalendar;
import model.multicalendar.MultiCalendar;

/**
 * Controller that executes parsed commands on the calendar model and formats output.
 * Now supports multi-calendar functionality.
 */
public class CalendarController {
  private final IMultiCalendar multiCalendar;
  private final IOutputFormatter formatter;

  /**
   * Constructor with dependency injection for better testability.
   * @param multiCalendar the multi-calendar model to use
   * @param formatter the output formatter to use
   */
  public CalendarController(IMultiCalendar multiCalendar, IOutputFormatter formatter) {
    this.multiCalendar = multiCalendar;
    this.formatter = formatter;
  }

  /**
   * Default constructor for convenience - creates default dependencies.
   */
  public CalendarController() {
    this(new MultiCalendar(), new OutputFormatter());
  }

  /**
   * Legacy constructor for backwards compatibility.
   * @param calendar single calendar (not used in multi-calendar mode)
   * @param formatter the output formatter to use
   */
  public CalendarController(ICalendar calendar, IOutputFormatter formatter) {
    this(new MultiCalendar(), formatter);
    // Create a default calendar for backwards compatibility
    this.multiCalendar.addCalendar("default", java.time.ZoneId.systemDefault());
    this.multiCalendar.useCalendar("default");
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
      case CREATE_CALENDAR:
        return executeCreateCalendar(parseResult);
      case EDIT_CALENDAR:
        return executeEditCalendar(parseResult);
      case USE_CALENDAR:
        return executeUseCalendar(parseResult);
      case COPY_SINGLE_EVENT:
        return executeCopySingleEvent(parseResult);
      case COPY_EVENTS_ON_DAY:
        return executeCopyEventsOnDay(parseResult);
      case COPY_EVENTS_BETWEEN:
        return executeCopyEventsBetween(parseResult);
      case EXIT:
        return null;
      default:
        throw new IllegalArgumentException("Unknown command type: " + parseResult.getCommandType());
    }
  }

  /**
   * Executes create calendar command.
   */
  private String executeCreateCalendar(ParseResult parseResult) {
    try {
      // Check for duplicate calendar names
      for (SpecificCalendar cal : multiCalendar.getCalendars()) {
        if (cal.getName().equals(parseResult.getCalendarName())) {
          throw new IllegalArgumentException("Calendar with name '" +
                  parseResult.getCalendarName() + "' already exists");
        }
      }

      multiCalendar.addCalendar(parseResult.getCalendarName(), parseResult.getTimezone());

      return formatter.formatSuccess("Created calendar: \"" +
              parseResult.getCalendarName() + "\" with timezone " +
              parseResult.getTimezone());
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create calendar: " + e.getMessage());
    }
  }

  /**
   * Executes edit calendar command.
   */
  private String executeEditCalendar(ParseResult parseResult) {
    try {
      // Check if calendar exists
      boolean found = false;
      for (SpecificCalendar cal : multiCalendar.getCalendars()) {
        if (cal.getName().equals(parseResult.getCalendarName())) {
          found = true;
          break;
        }
      }

      if (!found) {
        throw new IllegalArgumentException("Calendar '" + parseResult.getCalendarName() +
                "' not found");
      }

      // Check for duplicate name if editing name
      if (parseResult.getPropertyName().equals("name")) {
        for (SpecificCalendar cal : multiCalendar.getCalendars()) {
          if (!cal.getName().equals(parseResult.getCalendarName()) &&
                  cal.getName().equals(parseResult.getPropertyValue())) {
            throw new IllegalArgumentException("Calendar with name '" +
                    parseResult.getPropertyValue() + "' already exists");
          }
        }
      }

      multiCalendar.editCalendar(parseResult.getCalendarName(),
              parseResult.getPropertyName(), parseResult.getPropertyValue());

      return formatter.formatSuccess("Updated calendar \"" +
              parseResult.getCalendarName() + "\" " + parseResult.getPropertyName() +
              " to: " + parseResult.getPropertyValue());
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit calendar: " + e.getMessage());
    }
  }

  /**
   * Executes use calendar command.
   */
  private String executeUseCalendar(ParseResult parseResult) {
    try {
      // Check if calendar exists
      boolean found = false;
      for (SpecificCalendar cal : multiCalendar.getCalendars()) {
        if (cal.getName().equals(parseResult.getCalendarName())) {
          found = true;
          break;
        }
      }

      if (!found) {
        throw new IllegalArgumentException("Calendar '" + parseResult.getCalendarName() +
                "' not found");
      }

      multiCalendar.useCalendar(parseResult.getCalendarName());

      return formatter.formatSuccess("Now using calendar: \"" +
              parseResult.getCalendarName() + "\"");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to use calendar: " + e.getMessage());
    }
  }

  /**
   * Executes copy single event command.
   */
  private String executeCopySingleEvent(ParseResult parseResult) {
    try {
      checkCalendarInUse();

      multiCalendar.copyEvent(parseResult.getSubject(), parseResult.getStartTime(),
              parseResult.getTargetCalendarName(), parseResult.getTargetDateTime());

      return formatter.formatSuccess("Copied event \"" + parseResult.getSubject() +
              "\" to calendar \"" + parseResult.getTargetCalendarName() + "\"");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy event: " + e.getMessage());
    }
  }

  /**
   * Executes copy events on day command.
   */
  private String executeCopyEventsOnDay(ParseResult parseResult) {
    try {
      checkCalendarInUse();

      multiCalendar.copyEvents(parseResult.getSourceDate(),
              parseResult.getTargetCalendarName(), parseResult.getTargetDate());

      return formatter.formatSuccess("Copied events from " + parseResult.getSourceDate() +
              " to calendar \"" + parseResult.getTargetCalendarName() + "\"");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy events: " + e.getMessage());
    }
  }

  /**
   * Executes copy events between dates command.
   */
  private String executeCopyEventsBetween(ParseResult parseResult) {
    try {
      checkCalendarInUse();

      multiCalendar.copyEventsInterval(parseResult.getCopyStartDate(),
              parseResult.getCopyEndDate(), parseResult.getTargetCalendarName(),
              parseResult.getTargetDate());

      return formatter.formatSuccess("Copied events from " + parseResult.getCopyStartDate() +
              " to " + parseResult.getCopyEndDate() +
              " to calendar \"" + parseResult.getTargetCalendarName() + "\"");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy events: " + e.getMessage());
    }
  }

  /**
   * Checks if a calendar is currently in use.
   */
  private void checkCalendarInUse() {
    if (multiCalendar.getCurrent() == null) {
      throw new IllegalStateException("No calendar currently in use. Use 'use calendar' command first");
    }
  }

  /**
   * Gets the current calendar, throwing exception if none is set.
   */
  private SpecificCalendar getCurrentCalendar() {
    checkCalendarInUse();
    return multiCalendar.getCurrent();
  }

  /**
   * Executes create event commands.
   */
  private String executeCreateEvent(ParseResult parseResult) {
    try {
      SpecificCalendar current = getCurrentCalendar();

      if (parseResult.isRepeating()) {
        RepeatInfo repeatInfo = parseResult.getRepeatInfo();
        List<String> repeatDays = parseRepeatDays(repeatInfo.getRepeatDays());

        if (repeatInfo.hasTimeLimit()) {
          current.createSeriesTimes(
                  parseResult.getSubject(),
                  parseResult.getStartTime(),
                  parseResult.getEndTime(),
                  repeatDays,
                  repeatInfo.getRepeatTimes()
          );
          return formatter.formatSuccess("Created recurring event series: \""
                  + parseResult.getSubject()
                  + "\" (" + repeatInfo.getRepeatTimes() + " occurrences)");
        } else if (repeatInfo.hasDateLimit()) {
          current.createSeriesUntil(
                  parseResult.getSubject(),
                  parseResult.getStartTime(),
                  parseResult.getEndTime(),
                  repeatDays,
                  repeatInfo.getRepeatUntil()
          );
          return formatter.formatSuccess("Created recurring event series: \""
                  + parseResult.getSubject() + "\" (until " + repeatInfo.getRepeatUntil() + ")");
        }
      } else {
        Event event = current.createEvent(
                parseResult.getSubject(),
                parseResult.getStartTime(),
                parseResult.getEndTime()
        );

        String eventType = parseResult.isAllDay() ? "all-day event" : "event";
        return formatter.formatSuccess("Created "
                + eventType + ": \"" + parseResult.getSubject() + "\"");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create event: " + e.getMessage());
    }

    return null;
  }

  /**
   * Executes edit event commands.
   */
  private String executeEditEvent(ParseResult parseResult) {
    try {
      SpecificCalendar current = getCurrentCalendar();
      CommandType editType = parseResult.getCommandType();

      switch (editType) {
        case EDIT_EVENT:
          current.editEvent(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getEventEnd(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated event: \""
                  + parseResult.getEventSubject() + "\"");

        case EDIT_EVENTS:
          current.editEvents(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated events starting from: \""
                  + parseResult.getEventSubject() + "\"");

        case EDIT_SERIES:
          current.editSeries(
                  parseResult.getProperty(),
                  parseResult.getEventSubject(),
                  parseResult.getEventStart(),
                  parseResult.getNewValue()
          );
          return formatter.formatSuccess("Updated entire series: \""
                  + parseResult.getEventSubject() + "\"");

        default:
          throw new IllegalArgumentException("Unknown edit type: " + editType);
      }
    } catch (IllegalStateException e) {
      // Re-throw IllegalStateException as is
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Executes print events commands.
   */
  private String executePrintEvents(ParseResult parseResult) {
    try {
      SpecificCalendar current = getCurrentCalendar();

      if (parseResult.isPrintRange()) {
        return current.printEventsInterval(parseResult.getPrintStartDate(),
                parseResult.getPrintEndDate());
      } else {
        return current.printEvents(parseResult.getPrintStartDate().toLocalDate());
      }
    } catch (IllegalStateException e) {
      // Re-throw IllegalStateException as is
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to print events: " + e.getMessage());
    }
  }

  /**
   * Executes show status commands.
   */
  private String executeShowStatus(ParseResult parseResult) {
    try {
      SpecificCalendar current = getCurrentCalendar();
      LocalDateTime queryTime = parseResult.getStatusDateTime();
      Map<LocalDate, List<Event>> calendarData = current.getCalendar();

      boolean isBusy = checkIfBusy(calendarData, queryTime);
      return isBusy ? "busy" : "available";
    } catch (IllegalStateException e) {
      // Re-throw IllegalStateException as is
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to check status: " + e.getMessage());
    }
  }

  /**
   * Checks if the user is busy at a specific date and time.
   */
  private boolean checkIfBusy(Map<LocalDate, List<Event>> calendarData, LocalDateTime queryTime) {
    LocalDate queryDate = queryTime.toLocalDate();

    if (!calendarData.containsKey(queryDate)) {
      return false;
    }

    List<Event> dayEvents = calendarData.get(queryDate);

    for (Event event : dayEvents) {
      if (!queryTime.isBefore(event.getStart()) && queryTime.isBefore(event.getEnd())) {
        return true;
      }

      if (event.getStart().toLocalDate().isBefore(queryDate) &&
              event.getEnd().toLocalDate().isAfter(queryDate)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Converts repeat days string (e.g., "MWF") to list of day names.
   */
  private List<String> parseRepeatDays(String repeatDays) {
    return Arrays.asList(repeatDays.split(""));
  }

  // getters for testing
  public IMultiCalendar getMultiCalendar() {
    return multiCalendar;
  }

  public IOutputFormatter getFormatter() {
    return formatter;
  }
}