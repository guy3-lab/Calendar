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
    calendars = new ArrayList<SpecificCalendar>();
  }

  @Override
  public void addCalendar(String name, ZoneId timezone) {
    SpecificCalendar calendar = new SpecificCalendar(name, timezone);
    this.calendars.add(calendar);
  }

  @Override
  public void editCalendar(String name, String property, String value) {
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        switch (property) {
          case "name":
            calendar.setName(value);
            break;
          case "timezone":
            calendar.setTimeZone(ZoneId.of(value));
            break;
          default: //no default as the controller ensures that only valid inputs can be put
        }
      }
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
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime date, String calendarName,
                        LocalDateTime targetDate) {
    Event event = getEventFromCurrent(eventName, date);
    LocalDateTime endTime = event.getEnd();
    String desc = event.getDesc();
    Location location = event.getLocation();
    Status status = event.getStatus();

    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(calendarName)) {
        long betweenDays = ChronoUnit.DAYS.between(date, targetDate);
        long betweenMinutes = ChronoUnit.MINUTES.between(date.toLocalTime(),
                targetDate.toLocalTime());
        LocalDateTime newEndTime = endTime.plusDays(betweenDays).plusMinutes(betweenMinutes);

        calendar.fullCreate(eventName, targetDate, newEndTime, desc, location, status);
        break;
      }
    }
  }

  //gets the event from the current calendar
  private Event getEventFromCurrent(String eventName, LocalDateTime date) {
    isCalendarChosen();
    SpecificCalendar currentCalendar = this.current;
    List<Event> currentEvents = currentCalendar.getCalendar().get(date.toLocalDate());
    for (Event event : currentEvents) {
      if (event.getSubject().equals(eventName) && event.getStart().equals(date)) {
        return event;
      }
    }
    throw new IllegalArgumentException("Event doesn't exist in current");
  }

  private void isCalendarChosen() {
    if (current == null) {
      throw new IllegalStateException("Current calendar is not yet chosen");
    }
  }



  @Override
  public void copyEvents(LocalDate date, String calendarName, LocalDate targetDate) {
    isCalendarChosen();
    SpecificCalendar currentCalendar = this.current;
    copyEventsHelper(currentCalendar, date, calendarName, targetDate);
  }


  @Override
  public void copyEventsInterval(LocalDate startDate, LocalDate endDate, String calendarName,
                                 LocalDate targetDate) {
    isCalendarChosen();
    SpecificCalendar currentCalendar = this.current;
    while(!startDate.isAfter(endDate)) {
      copyEventsHelper(currentCalendar, startDate, calendarName, targetDate);
      startDate = startDate.plusDays(1);
      targetDate = targetDate.plusDays(1);
    }
  }

  //copies all events
  private void copyEventsHelper(SpecificCalendar currentCalendar, LocalDate date,
                                String calendarName, LocalDate targetDate) {
    List<Event> currentEvents = currentCalendar.getCalendar().get(date);

    SpecificCalendar targetCalendar = null;
    for (SpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(calendarName)) {
        targetCalendar = calendar;
        break;
      }
    }

    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar not found: " + calendarName);
    }

    ZoneId currentZoneID = currentCalendar.getTimeZone();
    ZoneId targetZoneID = targetCalendar.getTimeZone();

    for (Event event : currentEvents) {
      String eventName = event.getSubject();
      LocalDateTime eventDate = event.getStart();
      ZonedDateTime dateWithZoneID = eventDate.atZone(currentZoneID);
      ZonedDateTime targetDateWithZoneID = dateWithZoneID.withZoneSameInstant(targetZoneID);

      LocalTime endTime = targetDateWithZoneID.toLocalTime();
      LocalDateTime newDate = LocalDateTime.of(targetDate, endTime);

      copyEvent(eventName, eventDate, calendarName, newDate);
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
