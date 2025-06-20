package model.multicalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


import model.calendar.ISpecificCalendar;

/**
 * interface that represents a multitude of calendars that can be accessed and edited. Each calendar
 * can also have its events accessed and edited as well.
 */
public interface IMultiCalendar {

  /**
   * Adds and creates a calendar to the list of calendars.
   * @param name name of the calendar
   * @param timezone the timezone of the calendar
   */
  void addCalendar(String name, ZoneId timezone);

  /**
   * Edits the calendar by its name.
   * @param name the name of the calendar
   * @param property the property to be edited
   * @param value the new value
   */
  void editCalendar(String name, String property, String value);

  /**
   * Sets the 'current' field of the MultiCalendar to the specified calendar to be used.
   * @param name the name of the calendar
   */
  void useCalendar(String name);

  /**
   * Copies an event from the current calendar to the specified calendar and date.
   * @param eventName the name/subject of the event
   * @param date the date and time of event
   * @param calendarName the specified calendar
   * @param targetDate the specified date and time
   */
  void copyEvent(String eventName, LocalDateTime date, String calendarName,
                 LocalDateTime targetDate);

  /**
   * Similar to copyEvent, except it copies all events from the specified date to the new
   * calendar and new date.
   * @param date the specified date
   * @param calendarName the calendar
   * @param targetDate the new date
   */
  void copyEvents(LocalDate date, String calendarName, LocalDate targetDate);

  /**
   * Similar to copyEvents, except it copies all events between a certain interval
   * to the new calendar and new date.
   * @param startDate the starting date to copy events
   * @param endDate the ending date to copy events
   * @param calendarName the calendar to make copies to
   * @param targetDate the new date
   */
  void copyEventsInterval(LocalDate startDate, LocalDate endDate, String calendarName,
                          LocalDate targetDate);

  /**
   * Gets the list of calendars.
   * @return a list of calendars
   */
  List<ISpecificCalendar> getCalendars();

  /**
   * returns the calendar currently in use.
   * @return the SpecificCalendar
   */
  ISpecificCalendar getCurrent();
}
