package model.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.PropertyType;
import model.Enum.Location;
import model.Enum.Status;
import model.Enum.WeekDays;

/**
 * represents a Calendar object that contains a year, and a list of months and its days.
 */
public class Calendar implements ICalendar{
  private final Map<LocalDate, List<Event>> calendar;
  private Map<LocalDateTime, List<Event>> series;

  /**
   * Creates a calendar object that takes in a year as an argument to account for leap years.
   */
  public Calendar() {
    this.calendar = new HashMap<LocalDate, List<Event>>();
    this.series = new HashMap<LocalDateTime, List<Event>>();
  }

  @Override
  public Event createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime) {
    Event event;
    if (endTime == null) {
      event = new Event(subject, startTime);
      addEventHelper(event, startTime);
      return event;

    } else {
      event = new Event.EventBuilder(subject, startTime).end(endTime).build();
      while (!startTime.toLocalDate().isAfter(endTime.toLocalDate())) {
        addEventHelper(event, startTime);

        startTime = startTime.plusDays(1);
      }
      return event;
    }
  }

  /**
   * Adds the event to a day specified.
   * @param event the event being added
   * @param startTime the time to add the event
   */
  private void addEventHelper(Event event, LocalDateTime startTime) {
    LocalDate startDate = startTime.toLocalDate();
    List<Event> events;
    if (!calendar.containsKey(startDate)) {
      calendar.put(startDate, new ArrayList<Event>());
      events = calendar.get(startDate);
      events.add(event);
    } else {
      events = calendar.get(startDate);
      if (events.contains(event)) {
        throw new IllegalArgumentException("Event already exists");
      }
      events.add(event);
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
        weekdayRanges[i][1] = endTime.plusDays(difference);
      }
    }
    return weekdayRanges;
  }

  @Override
  public void editEvent(PropertyType property, String subject, LocalDateTime startTime,
                        LocalDateTime endTime, String value) {
    if (endTime == null) {
      for (Map.Entry<LocalDate, List<Event>> entry : calendar.entrySet()) {
        for (Event e : entry.getValue()) {
          if (!e.getStart().isBefore(startTime) && e.getSubject().equals(subject)) {
            editEventsHelper(e, property, value);
          }
        }
      }
    } else {
      List<Event> events = this.calendar.get(startTime.toLocalDate());
      for (Event e : events) {
        if (e.getSubject().equals(subject) && e.getStart().equals(startTime) &&
                e.getEnd().equals(endTime)) {
          editEventsHelper(e, property, value);
        }
      }
    }
  }

  /**
   * Checks the property and edits the field of the event accordingly.
   * @param e the event being edited
   * @param property the property the user chose to edit
   * @param value the value to be changed to
   */
  private void editEventsHelper(Event e, PropertyType property, String value) {
    switch(property) {
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
        if (this.calendar.containsKey(original.toLocalDate()) &&
                (!start.toLocalDate().equals(original.toLocalDate()))) {
          this.calendar.get(original.toLocalDate()).remove(e);

          if (this.calendar.containsKey(start.toLocalDate())) {
            this.calendar.get(start.toLocalDate()).add(e);
          } else {
            List<Event> events = new ArrayList<>();
            this.calendar.put(start.toLocalDate(), events);
            this.calendar.get(start.toLocalDate()).add(e);
          }
        }
        e.setStart(start);
        break;
      case END:
        LocalDateTime end = LocalDateTime.parse(value);
        e.setEnd(end);
        break;
      case DESCRIPTION:
        e.setDesc(value);
        break;
      case LOCATION:
        e.setLocation(Location.valueOf(value));
        break;
      case STATUS:
        e.setStatus(Status.valueOf(value));
        break;
    }
  }

  @Override
  public void editSeries(PropertyType property, String subject, LocalDateTime startTime, String value) {
    for (Map.Entry<LocalDateTime, List<Event>> entry : series.entrySet()) {
      if (entry.getKey().equals(startTime)) {
        List<Event> events = entry.getValue();
        for (Event e : events) {
          switch (property) {
            case START:
              LocalDateTime start = LocalDateTime.parse(value);
              LocalDateTime original = e.getStart();

              e.setStart(start);
              e.setEnd(e.getEnd().plusDays(ChronoUnit.DAYS.between(original, start)));

              //adds into a new series
              if (this.series.containsKey(start)) {
              } else {
                this.series.put(start, new ArrayList<>());
              }
              this.series.get(start).add(e);

              //removes the original series
              this.series.remove(original);

            default:
              editEventsHelper(e, property, value);
          }
        }
      }
    }
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
