package model.Calendar;

import java.time.LocalDateTime;
import java.time.LocalTime;

import model.Enum.Location;
import model.Enum.Status;

public class Event {
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private Location location;
  private Status status;


  private Event(String subject, LocalDateTime start, LocalDateTime end, Location location, Status status) {
    this.subject = subject;
    this.start = start;
    /* Updated the following constructor fields. You can't simply default the following three
    fields. For example, if the user inputs 2 pm start time, and 3 pm end time, then the end time
    that will be used is 5 pm, instead of the time they wanted. The same logic applies to
    location and status.
     */
    this.end = (end != null) ? end : LocalDateTime.of(start.toLocalDate(), LocalTime.of(17, 0));
    this.location = (location != null) ? location : Location.ONLINE;
    this.status = (status != null) ? status : Status.PUBLIC;
  }

  // Builder inner static class
  public static class EventBuilder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
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

    public EventBuilder location(Location location) {
      this.location = location;
      return this;
    }

    public EventBuilder status(Status status) {
      this.status = status;
      return this;
    }

    public Event build() {
      return new Event(subject, start, end, location, status);
    }
  }
}
