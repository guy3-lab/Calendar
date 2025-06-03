package model.Calendar;

import java.time.LocalDateTime;
import java.time.LocalTime;

import model.Enum.Location;
import model.Enum.Status;

public class Event {
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String desc;
  private Location location;
  private Status status;


  public Event (String subject, LocalDateTime start) {
    this.subject = subject;
    this.start = start;
    this.end = LocalDateTime.of(start.toLocalDate(), LocalTime.of(17, 0));
    this.desc = "";
    this.location = Location.ONLINE;
    this.status = Status.PUBLIC;
  }


  private Event(String subject, LocalDateTime start, LocalDateTime end,
                String desc, Location location, Status status) {
    this.subject = subject;
    this.start = start;
    /* Updated the following constructor fields. You can't simply default the following three
    fields. For example, if the user inputs 2 pm start time, and 3 pm end time, then the end time
    that will be used is 5 pm, instead of the time they wanted. The same logic applies to
    location and status.
     */
    this.end = (end != null) ? end : LocalDateTime.of(start.toLocalDate(), LocalTime.of(17, 0));
    this.desc = (desc != null) ? desc : "";
    this.location = (location != null) ? location : Location.ONLINE;
    this.status = (status != null) ? status : Status.PUBLIC;
  }

  // Builder inner static class
  public static class EventBuilder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String desc;
    private Location location;
    private Status status;

    public EventBuilder(String subject, LocalDateTime start) {
      this.subject = subject;
      this.start = start;
    }

    public EventBuilder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    public EventBuilder desc(String desc) {
      this.desc = desc;
      return this;
    }

    public EventBuilder location(Location location) {
      this.location = location;
      return this;
    }

    public EventBuilder status(Status status) {
      this.status = status;
      return this;
    }

    public Event build() {
      return new Event(subject, start, end, desc, location, status);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event)) return false;
    Event event = (Event) o;

    return subject.equals(event.subject) &&
            start.equals(event.start) &&
            end.equals(event.end) &&
            desc.equals(event.desc) &&
            location == event.location &&
            status == event.status;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(subject, start, end);
  }
}
