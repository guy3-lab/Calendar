
package model.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import controller.PropertyType;


/**
 * The calendar interface that contains all the methods required to edit and create events.
 */
public interface ICalendar {
  /**
   * Creates an event on a day/days, depending on the start and end times.
   * @param subject the subject of the event
   * @param startTime the starting time of the event
   * @param endTime the ending time of the event. If null, creates a full day event
   */
  Event createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * Creates a series of events depending on how many times it repeats for.
   * @param subject the subject of the event
   * @param startTime the starting time of the event
   * @param endTime the ending time of the event
   * @param repeatDays the days that are to be repeated
   * @param times the amount of times that it'll repeat
   */
  void createSeriesTimes(String subject, LocalDateTime startTime, LocalDateTime endTime,
                         List<String> repeatDays, int times);

  /**
   * Creates a series of events depending on when to stop repeating.
   * @param subject the subject of the event
   * @param startTime the starting time of the event
   * @param endTime the ending time of the event
   * @param repeatDays the days that are to be repeated
   * @param until repeat the events until this date
   */
  void createSeriesUntil(String subject, LocalDateTime startTime, LocalDateTime endTime,
                         List<String> repeatDays, LocalDate until);

  /**
   * Edits an event depending on the chosen property.
   * @param property the property that the user wants to change
   * @param subject the subject of the event
   * @param startTime the start time of the event
   * @param endTime the end time of the event
   * @param value the value to be changed into
   */
  void editEvent(PropertyType property, String subject, LocalDateTime startTime,
                 LocalDateTime endTime, String value);

  /**
   * Edits the events in a series starting at a specific date regarding the chosen property.
   * @param property the property the user wants to change
   * @param subject the subject of the event
   * @param startTime the starting time that the user wants to start editing from
   * @param value the value that the user wants to change the property to
   */
  void editEvents(PropertyType property, String subject, LocalDateTime startTime, String value);

  /**
   * Edits a series of events depending on the property.
   * @param property the property that the user wants to change
   * @param subject the subject of the event
   * @param startTime the start time of the event
   */
  void editSeries(PropertyType property, String subject, LocalDateTime startTime, String value);

  /**
   * returns a string of all the events in the given day.
   * @param day the given day
   * @return a string of all the events
   */
  String printEvents(LocalDate day);

  /**
   * returns a string of all the events in the given interval.
   * @param start the start time
   * @param end the end time
   * @return the events that are within the start and end time
   */
  String printEventsInterval(LocalDateTime start, LocalDateTime end);

  /**
   * Returns the map of months and corresponding days.
   * @return the map of months and corresponding days
   */
  Map<LocalDate, List<Event>> getCalendar();

  /**
   * returns the map of the events that are in series.
   * @return the map of the events that are in series
   */
  Map<LocalDateTime, List<Event>> getSeries();
}
