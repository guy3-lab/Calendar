package model.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Parse.PropertyType;
import model.Enum.Location;
import model.Enum.Status;
import model.Enum.WeekDays;

/**
 * represents a Calendar object that contains a year, and a list of months and its days.
 */
public class Calendar implements ICalendar {
  private final Map<LocalDate, List<Event>> calendar;
  private Map<LocalDateTime, List<Event>> series;

  /**
   * Creates a calendar object that takes in a year as an argument to account for leap years.
   */
  public Calendar() {
    this.calendar = new HashMap<>();
    this.series = new HashMap<>();
  }

  @Override
  public Event createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime) {
    Event event;
    if (endTime == null) {
      event = new Event(subject, startTime);
    } else {
      checkEndTimeAfterStart(endTime, startTime);
      event = new Event.EventBuilder(subject, startTime).end(endTime).build();
    }
    addEventHelper(event, startTime);
    return event;
  }

  // adds event to all days that are spanned (covers series logic)
  private void addEventHelper(Event event, LocalDateTime startTime) {
    LocalDate startDate = startTime.toLocalDate();
    LocalDate endDate = event.getEnd().toLocalDate();

    LocalDate currentDate = startDate;
    while(!currentDate.isAfter(endDate)){
      addToCalendarDate(currentDate, event);
      currentDate = currentDate.plusDays(1);
    }
  }

  // adds an event to a specific date in the calendar
  private void addToCalendarDate(LocalDate date, Event event){
    if(!calendar.containsKey(date)){
      calendar.put(date, new ArrayList<>());
    }

    List<Event> events = calendar.get(date); // get the list associated with the date key

    // check and bar duplicates
    if(date.equals(event.getStart().toLocalDate())){
      alreadyExistsInCalendar(events, event);
    }
    events.add(event); // NOW we add the event to the list
  }

  //checks if there's an event with the same fields
  private void alreadyExistsInCalendar(List<Event> events, Event event) {
    for (Event e : events) {
      if (e != event && e.equals(event)) {
        throw new IllegalArgumentException("Event already exists");
      }
    }
  }

  // removes ALL days that are spanned when edited to a different day/time
  private void removeEventFromAllDates(Event event) {
    LocalDate startDate = event.getStart().toLocalDate();
    LocalDate endDate = event.getEnd().toLocalDate();

    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      if (calendar.containsKey(currentDate)) {
        calendar.get(currentDate).remove(event);
        if (calendar.get(currentDate).isEmpty()) {
          calendar.remove(currentDate);
        }
      }
      currentDate = currentDate.plusDays(1);
    }
  }

  @Override
  public void createSeriesTimes(String subject, LocalDateTime startTime, LocalDateTime endTime,
                                List<String> repeatDays, int times) {
    this.series.put(startTime, new ArrayList<Event>());
    LocalDateTime[][] weekdayRanges = createSeriesHelper(repeatDays, startTime, endTime);

    for (LocalDateTime[] day : weekdayRanges) {
      for (int t = 0; t < times; t++) {
        this.series.get(startTime).add(createEvent(subject, day[0], day[1]));
        day[0] = day[0].plusDays(7);
        if (endTime == null) {
          day[1] = null;
        } else {
          day[1] = day[1].plusDays(7);
        }
      }
    }
  }

  @Override
  public void createSeriesUntil(String subject, LocalDateTime startTime, LocalDateTime endTime,
                                List<String> repeatDays, LocalDate until) {
    this.series.put(startTime, new ArrayList<Event>());
    LocalDateTime[][] weekdayRanges = createSeriesHelper(repeatDays, startTime, endTime);

    for (LocalDateTime[] day : weekdayRanges) {
      while (!day[0].toLocalDate().isAfter(until)) {
        this.series.get(startTime).add(createEvent(subject, day[0], day[1]));
        day[0] = day[0].plusDays(7);
        if (endTime == null) {
          day[1] = null;
        } else {
          day[1] = day[1].plusDays(7);
        }
      }
    }
  }

  //checks if the start time and end time are of the same day
  private void checkEventIsOneDay(LocalDateTime startDate, LocalDateTime endDate) {
    LocalDate start = startDate.toLocalDate();
    LocalDate end = endDate.toLocalDate();
    if (!start.equals(end)) {
      throw new IllegalArgumentException("Start date and end date must be the same");
    }
  }

  //Gets the repeated local dates that are stored in a 2d array
  private LocalDateTime[][] createSeriesHelper(List<String> repeatDays, LocalDateTime startTime,
                                               LocalDateTime endTime) {
    LocalDateTime[][] weekdayRanges = new LocalDateTime[repeatDays.size()][2];

    for (int i = 0; i < repeatDays.size(); i++) {
      int dayNum = WeekDays.getDay(repeatDays.get(i));
      int currentDay = startTime.getDayOfWeek().getValue();

      int difference = (dayNum - currentDay + 7) % 7;

      weekdayRanges[i][0] = startTime.plusDays(difference);
      if (endTime == null) {
        weekdayRanges[i][1] = null;
      } else {
        checkEventIsOneDay(startTime, endTime);
        weekdayRanges[i][1] = endTime.plusDays(difference);
      }
    }
    return weekdayRanges;
  }

  @Override
  public void editEvent(PropertyType property, String subject, LocalDateTime startTime,
                        LocalDateTime endTime, String value) {
    List<Event> events = this.calendar.get(startTime.toLocalDate());
    for (Event e : events) {
      if (e.getSubject().equals(subject) && e.getStart().equals(startTime) &&
              e.getEnd().equals(endTime)) {
        editEventHelper(e, property, value);
        alreadyExistsInCalendar(events, e);
        break;
      }
    }
  }

  //checks the property to be changed and edits the fields of the event accordingly
  private void editEventHelper(Event e, PropertyType property, String value) {
    switch (property) {
      case SUBJECT:
        e.setSubject(value);
        break;
      case START:
        LocalDateTime start = LocalDateTime.parse(value);
        LocalDateTime original = e.getStart();

        //updates the end time as well
        if (start.isAfter(original)) {
          e.setEnd(e.getEnd().plusDays(ChronoUnit.DAYS.between(original, start)));
        }

        //removes the event from the series
        if (this.series.containsKey(original)) {
          this.series.get(original).remove(e);
        }

        //removes from the calendar key to a new one
        removeAndAddToCalendar(original, e, start);
        e.setStart(start);
        break;
      case END:
        LocalDateTime end = LocalDateTime.parse(value);
        checkEndTimeAfterStart(end, e.getStart());
        e.setEnd(end);
        break;
      case DESCRIPTION:
        e.setDesc(value);
        break;
      case LOCATION:
        e.setLocation(Location.valueOf(value.toUpperCase()));
        break;
      case STATUS:
        e.setStatus(Status.valueOf(value.toUpperCase()));
        break;
    }
  }


  // original implementation iterated over a changing list - dangerous due to index out of bounds
  @Override
  public void editEvents(PropertyType property, String subject,
                         LocalDateTime startTime, String value) {
    List<Event> eventsToModify = new ArrayList<>();
    LocalDateTime seriesKey = null;

    for (Map.Entry<LocalDateTime, List<Event>> entry : series.entrySet()) {
      for (Event e : entry.getValue()) {
        if (e.getStart().equals(startTime) && e.getSubject().equals(subject)) {
          seriesKey = entry.getKey();
          for (Event event : entry.getValue()) {
            if (!event.getStart().isBefore(startTime) && event.getSubject().equals(subject)) {
              eventsToModify.add(event);
            }
          }
          break;
        }
      }
      if (seriesKey != null) break;
    }

    for (Event event : eventsToModify) {
      editEventsHelper(event, property, seriesKey, startTime, value);
    }
  }

  // same dangerous issue as editEvents
  @Override
  public void editSeries(PropertyType property, String subject,
                         LocalDateTime startTime, String value) {
    LocalDateTime seriesKey = null;
    List<Event> targetSeries = null;

    for (Map.Entry<LocalDateTime, List<Event>> entry : series.entrySet()) {
      for (Event e : entry.getValue()) {
        if (e.getStart().equals(startTime) && e.getSubject().equals(subject)) {
          seriesKey = entry.getKey();
          targetSeries = new ArrayList<>(entry.getValue());
          break;
        }
      }
      if (targetSeries != null) break;
    }

    if (targetSeries == null) {
      throw new IllegalArgumentException("Event not found in any series");
    }

    // now modify all events in the series
    for (Event event : targetSeries) {
      if (event.getSubject().equals(subject)) {
        editEventsHelper(event, property, seriesKey, startTime, value);
      }
    }

    // Clean up if needed
    removeSeries(property, seriesKey);
  }

  //switch case for series
  private void editEventsHelper(Event e, PropertyType property, LocalDateTime key,
                                LocalDateTime base, String value) {
    long between;
    switch (property) {
      case START:
        between = ChronoUnit.DAYS.between(base, LocalDateTime.parse(value));
        LocalDateTime start = LocalDateTime.parse(value);
        LocalDateTime newDate = e.getStart().plusDays(between);

        if (this.series.containsKey(key)) {
          this.series.get(key).remove(e);
        }

        //adds into a new series
        if (!this.series.containsKey(start)) {
          this.series.put(start, new ArrayList<>());
        }
        this.series.get(start).add(e);

        removeAndAddToCalendar(e.getStart(), e, newDate);
        checkEndTimeAfterStart(newDate, e.getEnd());

        e.setStart(LocalDateTime.of(newDate.toLocalDate(), start.toLocalTime()));
        e.setEnd(LocalDateTime.of(newDate.toLocalDate(), e.getEnd().toLocalTime()));
        break;

      //checks for exception, and then goes to the helper method to mutate
      case END:
        between = ChronoUnit.DAYS.between(base, LocalDateTime.parse(value));
        LocalDateTime end = e.getEnd().plusDays(between);
        LocalTime endTime = LocalDateTime.parse(value).toLocalTime();

        //check exceptions
        checkEndTimeAfterStart(end, e.getStart());
        checkEventIsOneDay(e.getStart().plusDays(between), end);

        e.setEnd(LocalDateTime.of(e.getEnd().toLocalDate(), endTime));
        break;

      default:
        editEventHelper(e, property, value);
    }
  }

  // removes event from span of days -> sets a new date -> adds event to span of days
  private void removeAndAddToCalendar(LocalDateTime original, Event e, LocalDateTime newDate) {
    removeEventFromAllDates(e);
    e.setStart(newDate);
    addEventHelper(e, newDate);
  }

  //removes a series
  private void removeSeries(PropertyType property, LocalDateTime start) {
    if (property == PropertyType.START && this.series.get(start) != null
            && this.series.get(start).isEmpty()) {
      this.series.remove(start);
    }
  }

  //checks that the end time is not before the start time
  private void checkEndTimeAfterStart(LocalDateTime end, LocalDateTime start) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time must be after start time");
    }
  }

  @Override
  public String printEvents(LocalDate day) {
    List<String> events = new ArrayList<>();
    if (this.calendar.containsKey(day)) {
      List<Event> eventList = this.calendar.get(day);
      for (Event e : eventList) {
        events.add(printHelper(e));
      }
    } else {
      return "No events on this day";
    }
    return String.join("\n", events);
  }

  @Override
  public String printEventsInterval(LocalDateTime start, LocalDateTime end) {
    LocalDate day = start.toLocalDate();
    List<String> events = new ArrayList<>();
    if (this.calendar.containsKey(day)) {
      List<Event> eventList = this.calendar.get(day);
      for (Event e : eventList) {
        if (e.getStart().equals(start) && e.getEnd().equals(end)) {
          events.add(printHelper(e));
        }
      }
    } else {
      return "No events on this interval";
    }
    return String.join("\n", events);
  }

  //creates the string to return
  private String printHelper(Event e) {
    String event = "";
    event += e.getSubject() + ", " + "Start Time: " + e.getStart() + ", "
            + "End Time: " + e.getEnd() + ", " + "Location: " + e.getLocation();
    return event;
  }

  @Override
  public String showStatus(LocalDateTime queryTime) {
    LocalDate queryDate = queryTime.toLocalDate();

    if(!this.calendar.containsKey(queryDate)){
      return "available";
    }

    List<Event> events = this.calendar.get(queryDate);

    for (Event e : events) {
      if (!queryTime.isBefore(e.getStart()) && queryTime.isBefore(e.getEnd())) {
        return "busy";
      }
    }
    return "available";
  }

  @Override
  public Map<LocalDate, List<Event>> getCalendar() {
    return calendar;
  }

  @Override
  public Map<LocalDateTime, List<Event>> getSeries() {
    return series;
  }
}
