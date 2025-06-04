package model.Calendar;

import java.time.LocalDateTime;

import model.Enum.Location;
import model.Enum.Status;

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

  void setStatus(Status status);
  void setStart(LocalDateTime start);
  void setEnd(LocalDateTime end);
  void setDesc(String description);
  void setLocation(Location location);
}
