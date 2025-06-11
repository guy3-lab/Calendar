package model.multicalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


import model.calendar.Event;
import model.calendar.SpecificCalendar;
import model.enums.Location;
import model.enums.Status;

/**
 * The class that represents multiple unique calendars.
 */
public class MultiCalendar implements IMultiCalendar {
  private List<SpecificCalendar> calendars;
  private SpecificCalendar current;

  /**
   * Constructs a multi calendar with an empty list of calendars.
   */
  public MultiCalendar() {
    calendars = new ArrayList<>();
  }

  @Override
  public void addCalendar(String name, ZoneId timezone) {
    // check if this calendar already exists
    for(SpecificCalendar cal: calendars){
      if(cal.getName().equals(name)){
        throw new IllegalArgumentException("Calendar with name " + name + " already exists.");
      }
    }

    // throw if timezone isn't valid
    try {
      ZoneId.of(timezone.getId());
    } catch(Exception e){
        throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }

    SpecificCalendar calendar = new SpecificCalendar(name, timezone);
    this.calendars.add(calendar);
  }

  @Override
  public void editCalendar(String name, String property, String value) {
    SpecificCalendar found = null;
    // find the calendar
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        found = calendar;
        break;
      }
    }
    // throw if it doesn't exist
    if (found == null) {
      throw new IllegalArgumentException("Calendar " + name + " not found.");
    }

    switch (property.toLowerCase()) {
      case "name":
        // check for duplicate name
        for (SpecificCalendar cal : calendars) {
          if (!cal.equals(found) && cal.getName().equals(value)) {
            throw new IllegalArgumentException("Calendar with name " + value + " already exists.");
          }
        }
        found.setName(value);
        break;
      case "timezone":
        try {
          found.setTimeZone(ZoneId.of(value));
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + value);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }
  }

  @Override
  public void useCalendar(String name) {
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        this.current = calendar;
        break;
      }
    }
    throw new IllegalArgumentException("Calendar " + name + " not found.");
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime date, String calendarName,
                        LocalDateTime targetDate) {
    Event event = getEventFromCurrent(eventName, date);
    LocalDateTime endTime = event.getEnd();
    String desc = event.getDesc();
    Location location = event.getLocation();
    Status status = event.getStatus();

    int count = 0;
    ZoneId current = this.current.getTimeZone();
    SpecificCalendar targetCalendar = null;
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(calendarName)) {
        targetCalendar = calendar;
      }
    }

    if (targetCalendar == null) {
      throw new IllegalArgumentException("No target calendar found");
    }

    long betweenDays = ChronoUnit.DAYS.between(date, targetDate);
    long betweenMinutes = ChronoUnit.MINUTES.between(date.toLocalTime(),
            targetDate.toLocalTime());
    LocalDateTime newEndTime = endTime.plusDays(betweenDays).plusMinutes(betweenMinutes);

    targetCalendar.fullCreate(eventName, targetDate, newEndTime, desc, location, status);
  }

  //gets the event from the current calendar
  private Event getEventFromCurrent(String eventName, LocalDateTime date) {
    isCalendarChosen();
    SpecificCalendar currentCalendar = this.current;
    Event currentEvent = null;
    int count = 0;
    List<Event> currentEvents = currentCalendar.getCalendar().get(date.toLocalDate());
    for (Event event : currentEvents) {
      if (event.getSubject().equals(eventName) && event.getStart().equals(date)) {
        currentEvent = event;
        count++;
      }
    }
    if (count > 1) {
      throw new IllegalArgumentException("Multiple events found");
    } else if (currentEvent == null) {
      throw new IllegalArgumentException("No event found");
    } else {
      return currentEvent;
    }
  }

  private void isCalendarChosen() {
    if (current == null) {
      throw new IllegalStateException("Current calendar is not yet chosen");
    }
  }



  @Override
  public void copyEvents(LocalDate date, String calendarName, LocalDate targetDate) {
    isCalendarChosen();
    copyEventsHelper(date, calendarName, targetDate);
  }


  @Override
  public void copyEventsInterval(LocalDate startDate, LocalDate endDate, String calendarName,
                                 LocalDate targetDate) {
    isCalendarChosen();
    while(!startDate.isAfter(endDate)) {
      copyEventsHelper(startDate, calendarName, targetDate);
      startDate = startDate.plusDays(1);
      targetDate = targetDate.plusDays(1);
    }
  }

  //copies all events
  private void copyEventsHelper(LocalDate date,
                                String calendarName, LocalDate targetDate) {
    List<Event> currentEvents = this.current.getCalendar().get(date);

    ZoneId currentZoneID = this.current.getTimeZone();
    ZoneId targetZoneID;
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(calendarName)) {
        targetZoneID = calendar.getTimeZone();
        for (Event event : currentEvents) {
          String eventName = event.getSubject();
          LocalDateTime eventDate = event.getStart();

          ZonedDateTime dateWithZoneID = eventDate.atZone(currentZoneID);
          ZonedDateTime newZonedDateTime = dateWithZoneID.withZoneSameInstant(targetZoneID);
          LocalDateTime targetDateTime = newZonedDateTime.toLocalDateTime();

          long betweenDays = ChronoUnit.DAYS.between(targetDateTime.toLocalDate(), targetDate);

          copyEvent(eventName, eventDate, calendarName, targetDateTime.plusDays(betweenDays));
        }
        break;
      }
    }
  }

  @Override
  public List<SpecificCalendar> getCalendars() {
    return this.calendars;
  }

  @Override
  public SpecificCalendar getCurrent() {
    return this.current;
  }
}
