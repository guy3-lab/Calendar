package model.Calendar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import model.Enum.Location;
import model.Enum.Months;
import model.Enum.Status;

public interface ICalendar {
  /**
   * Creates an event on a day/days, depending on the start and end times.
   * @param subject the subject of the event
   * @param startTime the starting time of the event
   * @param endTime the ending time of the event. If null, creates a full day event
   */
  void createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime);

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
   * Returns the map of months and corresponding days
   * @return the map of months and corresponding days
   */
  Map<Months, List<Day>> getCalendar();
}
