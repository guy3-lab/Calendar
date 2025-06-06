package controller.format;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import model.Calendar.Event;

/**
 * Interface for formatting calendar output.
 * Allows different formatting implementations and improves testability.
 *
 * <p>This interface defines the contract for all output formatters in the calendar application.
 * Implementations can provide different formatting styles (e.g., JSON, XML, compact, verbose).
 * </p>
 */
public interface IOutputFormatter {

  /**
   * Formats events for a specific date.
   * @param calendar the calendar data containing events
   * @param date the date to format events for
   * @return formatted string representation of events on the given date
   */
  String formatEventsForDate(Map<LocalDate, List<Event>> calendar, LocalDate date);

  /**
   * Formats events for a date range.
   * @param calendar the calendar data containing events
   * @param startDate the start date of the range (inclusive)
   * @param endDate the end date of the range (inclusive)
   * @return formatted string representation of events in the date range
   */
  String formatEventRange(Map<LocalDate, List<Event>> calendar, LocalDate startDate, LocalDate endDate);

  /**
   * Formats a list of events for debugging or logging purposes.
   * @param events the list of events to format
   * @return formatted string representation of the event list
   */
  String formatEventsList(List<Event> events);

  /**
   * Formats error messages consistently.
   * @param message the error message to format
   * @return formatted error message
   */
  String formatError(String message);

  /**
   * Formats success messages consistently.
   * @param message the success message to format
   * @return formatted success message
   */
  String formatSuccess(String message);
}
