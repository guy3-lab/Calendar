package model.calendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import model.enums.Location;
import model.enums.Status;

/**
 * A calendar class with fields of name and time zone to specify unique calendars.
 */
public class SpecificCalendar extends Calendar implements ISpecificCalendar {
  private String name;
  private ZoneId timeZone;
  private Map<LocalDateTime, LocalDateTime> oldToNewSeries;

  /**
   * Constructor of a specific calendar that takes in a name and timezone.
   * @param name name of the calendar
   * @param timeZone the time zone of the calendar
   */
  public SpecificCalendar(String name, ZoneId timeZone) {
    super();
    this.name = name;
    this.timeZone = timeZone;
    this.oldToNewSeries = new HashMap<>();
  }


  @Override
  public void fullCreate(String subject, LocalDateTime startDate, LocalDateTime endDate, String desc,
                         Location location, Status status) {
    Event event = new Event.EventBuilder(subject, startDate).end(endDate).desc(desc).
            location(location).status(status).build();
    addEventHelper(event, startDate);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public ZoneId getTimeZone() {
    return this.timeZone;
  }

  @Override
  public Map<LocalDateTime, LocalDateTime> getOldToNewSeries() {
    return this.oldToNewSeries;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setTimeZone(ZoneId timeZone) {
    this.timeZone = timeZone;
  }
}
