package model.multicalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import model.calendar.Event;
import model.calendar.IEvent;
import model.calendar.ISpecificCalendar;
import model.calendar.SpecificCalendar;
import model.enums.Location;
import model.enums.Status;

/**
 * The class that represents multiple unique calendars.
 */
public class MultiCalendar implements IMultiCalendar {
  private List<ISpecificCalendar> calendars;
  private ISpecificCalendar current;

  /**
   * Constructs a multi calendar with an empty list of calendars.
   */
  public MultiCalendar() {
    calendars = new ArrayList<>();
  }

  @Override
  public void addCalendar(String name, ZoneId timezone) {
    // check if this calendar already exists
    for(ISpecificCalendar cal: calendars){
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
    ISpecificCalendar found = null;
    // find the calendar
    for (ISpecificCalendar calendar : calendars) {
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
        if (found.getName().equals(value)) {
          break;
        }

        for (ISpecificCalendar cal : calendars) {
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
    for (ISpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        this.current = calendar;
        return;
      }
    }
    throw new IllegalArgumentException("Calendar " + name + " not found");
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime date, String calendarName,
                        LocalDateTime targetDate) {
    IEvent event = getEventFromCurrent(eventName, date);
    LocalDateTime endTime = event.getEnd();
    String desc = event.getDesc();
    Location location = event.getLocation();
    Status status = event.getStatus();

    ISpecificCalendar targetCalendar = null;
    for (ISpecificCalendar calendar : calendars) {
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

    LocalDateTime originalSeriesKey = inSeries(eventName, date);
    if (originalSeriesKey != null) {
      Event eventInSeries = new Event.EventBuilder(eventName, targetDate).end(newEndTime).
              desc(desc).location(location).status(status).build();
      putIntoSeries(originalSeriesKey, eventInSeries, targetCalendar);
    }
    targetCalendar.fullCreate(eventName, targetDate, newEndTime, desc, location, status);
  }

  //gets the event from the current calendar
  private IEvent getEventFromCurrent(String eventName, LocalDateTime date) {
    isCalendarChosen();
    ISpecificCalendar currentCalendar = this.current;
    IEvent currentEvent = null;
    int count = 0;
    List<IEvent> currentEvents = currentCalendar.getCalendar().get(date.toLocalDate());
    for (IEvent event : currentEvents) {
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

  //checks whether an event is in a series
  private LocalDateTime inSeries(String eventName, LocalDateTime date) {
    isCalendarChosen();
    ISpecificCalendar currentCalendar = this.current;
    for (Map.Entry<LocalDateTime, List<IEvent>> series : currentCalendar.getSeries().entrySet()) {
      for (IEvent event : series.getValue()) {
        if (event.getSubject().equals(eventName) && event.getStart().equals(date)) {
          return series.getKey();
        }
      }
    }
    return null;
  }

  //Checks if there's a corresponding key to the original key for the series, and if so, add the
  //event to the existing series for the target calendar. Otherwise, create that new series with
  //this event startTime as the key.
  private void putIntoSeries(LocalDateTime originalSeriesKey, IEvent event,
                             ISpecificCalendar targetCalendar) {
    if (targetCalendar.getOldToNewSeries().containsKey(originalSeriesKey)) {
      LocalDateTime newKey = targetCalendar.getOldToNewSeries().get(originalSeriesKey);
      targetCalendar.getSeries().get(newKey).add(event);
    } else {
      targetCalendar.getOldToNewSeries().put(originalSeriesKey, event.getStart());

      List<IEvent> newSeries = new ArrayList<>();
      newSeries.add(event);
      targetCalendar.getSeries().put(event.getStart(), newSeries);
    }
  }

  //checks whether a calendar is chosen or being used
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
    List<IEvent> currentEvents = this.current.getCalendar().get(date);

    ZoneId currentZoneID = this.current.getTimeZone();
    ZoneId targetZoneID;
    for (ISpecificCalendar calendar : calendars) {
      if (calendar.getName().equals(calendarName)) {
        targetZoneID = calendar.getTimeZone();
        for (IEvent event : currentEvents) {
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
  public List<ISpecificCalendar> getCalendars() {
    return this.calendars;
  }

  @Override
  public ISpecificCalendar getCurrent() {
    return this.current;
  }
}
