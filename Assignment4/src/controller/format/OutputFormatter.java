package controller.format;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.calendar.Event;
import model.enums.Location;

/**
 * Handles formatting of output for calendar queries and commands.
 */
public class OutputFormatter implements IOutputFormatter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Formats events for a specific date.
   */
  public String formatEventsForDate(Map<LocalDate, List<Event>> calendar, LocalDate date) {
    if (!calendar.containsKey(date) || calendar.get(date).isEmpty()) {
      return "No events scheduled on " + date.format(DATE_FORMAT);
    }

    List<Event> events = calendar.get(date);
    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(date.format(DATE_FORMAT)).append(":\n");

    List<Event> sortedEvents = events.stream()
            .sorted((e1, e2) -> e1.getStart().compareTo(e2.getStart()))
            .collect(Collectors.toList());

    for (Event event : sortedEvents) {
      result.append(formatEventBullet(event));
    }

    return result.toString().trim();
  }

  /**
   * Formats events for a date range.
   */
  public String formatEventRange(Map<LocalDate, List<Event>> calendar,
                                 LocalDate startDate, LocalDate endDate) {
    StringBuilder result = new StringBuilder();
    result.append("Events from ").append(startDate.format(DATE_FORMAT))
            .append(" to ").append(endDate.format(DATE_FORMAT)).append(":\n");

    boolean hasEvents = false;
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      if (calendar.containsKey(currentDate) && !calendar.get(currentDate).isEmpty()) {
        hasEvents = true;
        result.append("\n").append(currentDate.format(DATE_FORMAT)).append(":\n");

        List<Event> events = calendar.get(currentDate);
        List<Event> sortedEvents = events.stream()
                .sorted((e1, e2) -> e1.getStart().compareTo(e2.getStart()))
                .collect(Collectors.toList());

        for (Event event : sortedEvents) {
          result.append(formatEventBullet(event));
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    if (!hasEvents) {
      return "No events scheduled from " + startDate.format(DATE_FORMAT) +
              " to " + endDate.format(DATE_FORMAT);
    }

    return result.toString().trim();
  }

  /**
   * Formats a list of events for debugging or logging purposes.
   */
  public String formatEventsList(List<Event> events) {
    if (events.isEmpty()) {
      return "No events";
    }

    StringBuilder result = new StringBuilder();
    for (Event event : events) {
      result.append("- ").append(event.getSubject())
              .append(" (").append(formatTimeRange(event.getStart(), event.getEnd())).append(")")
              .append("\n");
    }
    return result.toString().trim();
  }

  /**
   * Formats error messages consistently.
   */
  public String formatError(String message) {
    return "Error: " + message;
  }

  /**
   * Formats success messages consistently.
   */
  public String formatSuccess(String message) {
    return message;
  }

  // private helpers

  /**
   * Formats a single event as a bulleted list item.
   */
  private String formatEventBullet(Event event) {
    StringBuilder bullet = new StringBuilder();
    bullet.append("• ");
    bullet.append(event.getSubject());
    bullet.append(" (");
    bullet.append(formatTimeRange(event.getStart(), event.getEnd()));
    bullet.append(")");

    if (event.getLocation() != null && event.getLocation() != Location.ONLINE) {
      bullet.append(" - Location: ").append(formatLocation(event.getLocation()));
    }

    if (event.getDesc() != null && !event.getDesc().trim().isEmpty()) {
      bullet.append(" - ").append(event.getDesc());
    }

    bullet.append("\n");
    return bullet.toString();
  }

  /**
   * Formats time range for an event.
   */
  private String formatTimeRange(LocalDateTime start, LocalDateTime end) {
    if (isAllDayEvent(start, end)) {
      return "All day";
    }

    if (start.toLocalDate().equals(end.toLocalDate())) {
      // same day event
      return start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT);
    } else {
      // multi day event
      return start.format(DATETIME_FORMAT) + " - " + end.format(DATETIME_FORMAT);
    }
  }

  /**
   * Formats location enum for display.
   */
  private String formatLocation(Location location) {
    switch (location) {
      case ONLINE:
        return "Online";
      case PHYSICAL:
        return "Physical";
      default:
        return location.toString();
    }
  }

  /**
   * Determines if an event is an all-day event based on its times.
   */
  private boolean isAllDayEvent(LocalDateTime start, LocalDateTime end) {
    return start.getHour() == 8 && start.getMinute() == 0 &&
            end.getHour() == 17 && end.getMinute() == 0 &&
            start.toLocalDate().equals(end.toLocalDate());
  }
}