package model.calendar;

import java.time.LocalDateTime;

import model.enums.Location;
import model.enums.Status;

/**
 * the Event object interface that contains getter and setter methods.
 */
public interface IEvent {

  /**
   * Gets the subject field.
   * @return the subject
   */
  String getSubject();

  /**
   * Gets the start field.
   * @return the start
   */
  LocalDateTime getStart();

  /**
   * Gets the end field.
   * @return the end
   */
  LocalDateTime getEnd();

  /**
   * Gets the desc field.
   * @return the desc
   */
  String getDesc();

  /**
   * Gets the location field.
   * @return the location
   */
  Location getLocation();

  /**
   * Gets the status field.
   * @return the status
   */
  Status getStatus();

  /**
   * Gets the series key.
   * @return the original series it was in
   */
  LocalDateTime getSeriesKey();

  /**
   * sets the status field.
   * @param status the status to be changed
   */
  void setStatus(Status status);

  /**
   * sets the start date.
   * @param start the start date to be changed to
   */
  void setStart(LocalDateTime start);

  /**
   * sets the end date.
   * @param end the end date to be changed to
   */
  void setEnd(LocalDateTime end);

  /**
   * sets the description.
   * @param description the new description to be changed to
   */
  void setDesc(String description);

  /**
   * sets the new location.
   * @param location the new location
   */
  void setLocation(Location location);

  /**
   * sets the new subject.
   * @param subject the new subject
   */
  void setSubject(String subject);
}
