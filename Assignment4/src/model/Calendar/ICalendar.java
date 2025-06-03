package model.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import controller.PropertyType;
import model.Enum.Location;
import model.Enum.Status;

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
  void createSeries(String subject, LocalDateTime startTime, LocalDateTime endTime,
                    List<String> repeatDays, int times);

  /**
   * Edits an event depending on the chosen property. When endTime is null, it will change all
   * events starting from the start time.
   * @param property the property that the user wants to change
   * @param subject the subject of the event
   * @param startTime the start time of the event
   * @param endTime the end time of the event
   * @param value the value to be changed into
   */
  void editEvent(PropertyType property, String subject, LocalDateTime startTime,
                 LocalDateTime endTime, String value);

  /**
   * Edits a series of events depending on the property.
   * @param property the property that the user wants to change
   * @param subject the subject of the event
   * @param startTime the start time of the event
   */
  void editSeries(PropertyType property, String subject, LocalDateTime startTime);

  /**
   * Returns the map of months and corresponding days
   * @return the map of months and corresponding days
   */
  Map<LocalDate, List<Event>> getCalendar();

  Map<LocalDateTime, List<Event>> getSeries();
}
