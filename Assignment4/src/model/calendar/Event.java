package model.calendar;

import java.time.LocalDateTime;
import java.time.LocalTime;

import model.enums.Location;
import model.enums.Status;

/**
 * Class that represents an Event that has the fields of a subject, start time, end time,
 * description, location, which are either physical or online, and status, which are either private
 * or public.
 */
public class Event implements IEvent {
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String desc;
  private Location location;
  private Status status;
  private LocalDateTime seriesKey;

  /**
   * constructor that always takes in subject and start date while setting everything else to their
   * default values.
   * @param subject the subject of the event
   * @param start the start date of the event
   */
  public Event(String subject, LocalDateTime start) {
    this.subject = subject;
    this.start = LocalDateTime.of(start.toLocalDate(), LocalTime.of(8, 0));
    this.end = LocalDateTime.of(start.toLocalDate(), LocalTime.of(17, 0));
    this.desc = "";
    this.location = Location.ONLINE;
    this.status = Status.PUBLIC;
    this.seriesKey = null;
  }

  /**
   * private constructor used in the Event builder to create an Event object when there are more
   * than just subject and start date fields.
   * @param subject subject of the event
   * @param start start date of the event
   * @param end end date of the event
   * @param desc description of the event
   * @param location location of the event, either physical or online
   * @param status status of the event, either public or private
   */
  private Event(String subject, LocalDateTime start, LocalDateTime end,
                String desc, Location location, Status status) {
    this.subject = subject;
    this.start = start;
    this.end = end != null ? end : LocalDateTime.of(start.toLocalDate(), LocalTime.of(17, 0));
    this.desc = desc != null ? desc : "";
    this.location = location != null ? location : Location.ONLINE;
    this.status = status != null ? status : Status.PUBLIC;
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public LocalDateTime getStart() {
    return this.start;
  }

  @Override
  public LocalDateTime getEnd() {
    return this.end;
  }

  @Override
  public String getDesc() {
    return this.desc;
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public Status getStatus() {
    return this.status;
  }

  @Override
  public LocalDateTime getSeriesKey() {
    return this.seriesKey;
  }

  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  @Override
  public void setEnd(LocalDateTime end) {
    this.end = end;
  }

  @Override
  public void setDesc(String desc) {
    this.desc = desc;
  }

  @Override
  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public void setStatus(Status status) {
    this.status = status;
  }

  // for testing purposes
  public boolean isAllDay() {
    return start.toLocalTime().equals(LocalTime.of(8, 0)) &&
            end.toLocalTime().equals(LocalTime.of(17, 0)) &&
            start.toLocalDate().equals(end.toLocalDate());
  }

  /**
   * Event builder class to make building more convenient.
   */
  public static class EventBuilder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String desc;
    private Location location;
    private Status status;

    /**
     * Event builder constructor that always has a subject and start date.
     * @param subject the subject
     * @param start the start date
     */
    public EventBuilder(String subject, LocalDateTime start) {
      this.subject = subject;
      this.start = start;
    }

    /**
     * Adds the end field to the constructor.
     * @param end the end time
     * @return the eventbuilder with an end field
     */
    public EventBuilder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Adds the desc field to the constructor.
     * @param desc the desc
     * @return the eventbuilder with a desc field
     */
    public EventBuilder desc(String desc) {
      this.desc = desc;
      return this;
    }

    /**
     * Adds the location field to the constructor.
     * @param location the location
     * @return the eventbuilder with constructor field
     */
    public EventBuilder location(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Adds the status field to the constructor.
     * @param status the status
     * @return the eventbuilder with status field
     */
    public EventBuilder status(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Makes the Event object using the builder fields.
     * @return an Event object
     */
    public Event build() {
      return new Event(subject, start, end, desc, location, status);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event)) { return false; }
    Event event = (Event) o;

    return subject.equals(event.subject)
            && start.equals(event.start)
            && end.equals(event.end)
            && desc.equals(event.desc)
            && location == event.location
            && status == event.status;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(subject, start, end);
  }
}
