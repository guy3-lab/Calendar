package model.calendar;

import java.time.LocalDateTime;
import java.time.ZoneId;

import model.enums.Location;
import model.enums.Status;

/**
 * Interface of a specific calendar and the methods it'll need.
 */
public interface ISpecificCalendar {

  /**
   * creates a full creation of an event.
   * @param subject the subject of the event
   * @param startDate the starting date of the event
   * @param endDate the ending date of the event
   * @param desc the description of the event
   * @param location the location of the event
   * @param status the status of the event
   */
  void fullCreate(String subject, LocalDateTime startDate, LocalDateTime endDate, String desc,
                  Location location, Status status);

  /**
   * Gets the name of the specific calendar.
   * @return the String name
   */
  String getName();

  /**
   * Gets the timezone of the specific calendar.
   * @return the ZoneId of the timezone
   */
  ZoneId getTimeZone();

  /**
   * sets the name field to a new name.
   * @param name the new name
   */
  void setName(String name);

  /**
   * sets the timezone field to the new timezone.
   * @param timeZone the new timezone
   */
  void setTimeZone(ZoneId timeZone);
}
