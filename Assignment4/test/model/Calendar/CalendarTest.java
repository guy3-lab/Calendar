package model.Calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.PropertyType;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CalendarTest {
  Calendar calendar;
  Calendar calendarLeap;

  @Before
  public void setUp() throws Exception {
    calendar = new Calendar();
    calendarLeap = new Calendar();
  }

  @Test
  public void ExceptionTest() {
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));

    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"));
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }

    try {
      calendarLeap.createEvent("test", LocalDateTime.parse("2024-02-30T10:00"),
              LocalDateTime.parse("2024-02-30T15:00"));
    } catch (Exception e) {
      assertEquals("Text '2024-02-30T10:00' could not be parsed: Invalid date 'FEBRUARY 30'",
              e.getMessage());
    }
  }

  @Test
  public void createEventTest() {
    //creating two events on the same day
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T15:00"),
            LocalDateTime.parse("2025-10-05T17:00"));

    //confirming that the two events exist in the correct event list of the correct day
    Event event = new Event.EventBuilder("test", LocalDateTime.parse("2025-10-05T10:00")).
            end(LocalDateTime.parse("2025-10-05T15:00")).build();
    LocalDate dateKey = LocalDate.parse("2025-10-05");
    List<Event> dateEvents = calendar.getCalendar().get(dateKey);
    assertEquals(true, dateEvents.contains(event));
    assertEquals(2, dateEvents.size());


    //confirming that another day's list properly stores a newly created event
    calendar.createEvent("test", LocalDateTime.parse("2025-10-31T10:00"),
            LocalDateTime.parse("2025-11-02T15:00"));

    Event event2 = new Event.EventBuilder("test", LocalDateTime.parse("2025-10-31T10:00")).
            end(LocalDateTime.parse("2025-11-02T15:00")).build();

    LocalDate oct31 = LocalDate.parse("2025-10-31");
//    LocalDate nov1 = LocalDate.parse("2025-11-01");
//    LocalDate nov2 = LocalDate.parse("2025-11-02");

    //checks each day that they have the same event because the event spans from day start to day
    //end
    List<Event> oct31Events = calendar.getCalendar().get(oct31);
//    List<Event> nov1Events = calendar.getCalendar().get(nov1);
//    List<Event> nov2Events = calendar.getCalendar().get(nov2);

    assertEquals(true ,oct31Events.contains(event2));
//    assertEquals(true ,nov1Events.contains(event2));
//    assertEquals(true ,nov2Events.contains(event2));

    //checks the endTime and startTime field to ensure it has the proper end date
    assertEquals(LocalDateTime.parse("2025-11-02T15:00"), event2.getEnd());

    //creates an event without an end field, which therefore is a full day event
    calendar.createEvent("test", LocalDateTime.parse("2025-02-27T05:00"), null);
    Event eventNoEndTime = new Event("test",
            LocalDateTime.parse("2025-02-27T05:00"));
    LocalDate feb27 = LocalDate.parse("2025-02-27");
    List<Event> feb27Events = calendar.getCalendar().get(feb27);
    assertEquals(true ,feb27Events.contains(eventNoEndTime));
    assertEquals(LocalDateTime.parse("2025-02-27T17:00"), eventNoEndTime.getEnd());
    assertEquals(LocalDateTime.parse("2025-02-27T08:00"), eventNoEndTime.getStart());
  }

  @Test
  public void createSeriesTimesTest() {

    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));

    //full day event series
    calendar.createSeriesTimes("Series1", LocalDateTime.parse("2025-12-24T05:00"), null,
            repeatedDays, 3);
    List<Event> events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    //checks if the correct amount of events are added to the map
    assertEquals(9, events.size());

    //first 3 events
    assertEquals(LocalDateTime.parse("2025-12-24T08:00"), events.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-25T08:00"), events.get(3).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T08:00"), events.get(6).getStart());

    //first 3 end times
    assertEquals(LocalDateTime.parse("2025-12-24T17:00"), events.get(0).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-25T17:00"), events.get(3).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-26T17:00"), events.get(6).getEnd());

    //second repeat
    assertEquals(LocalDateTime.parse("2025-12-31T08:00"), events.get(1).getStart());
    assertEquals(LocalDateTime.parse("2026-01-01T08:00"), events.get(4).getStart());
    assertEquals(LocalDateTime.parse("2026-01-02T08:00"), events.get(7).getStart());

    //third repeat
    assertEquals(LocalDateTime.parse("2026-01-07T08:00"), events.get(2).getStart());
    assertEquals(LocalDateTime.parse("2026-01-08T08:00"), events.get(5).getStart());
    assertEquals(LocalDateTime.parse("2026-01-09T08:00"), events.get(8).getStart());

    //Events with an end time
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesTimes("Series2", LocalDateTime.parse("2025-12-24T05:00"),
            LocalDateTime.parse("2025-12-24T06:00"), repeatedDays2, 2);
    List<Event> events2 = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    //checks if the correct amount of events are added to the map
    assertEquals(4, events2.size());

    //first 3 events
    assertEquals(LocalDateTime.parse("2025-12-24T05:00"), events2.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T05:00"), events2.get(2).getStart());

    //first 3 end times
    assertEquals(LocalDateTime.parse("2025-12-24T06:00"), events2.get(0).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-26T06:00"), events2.get(2).getEnd());

    //second repeat
    assertEquals(LocalDateTime.parse("2025-12-31T05:00"), events2.get(1).getStart());
    assertEquals(LocalDateTime.parse("2026-01-02T05:00"), events2.get(3).getStart());
  }

  @Test
  public void createSeriesUntil() {
    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));

    //full day event series
    calendar.createSeriesUntil("Series1", LocalDateTime.parse("2025-12-24T05:00"), null,
            repeatedDays, LocalDate.parse("2026-01-03"));
    List<Event> events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));
    assertEquals(6, events.size());
    //first 3 events
    assertEquals(LocalDateTime.parse("2025-12-24T08:00"), events.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-25T08:00"), events.get(2).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T08:00"), events.get(4).getStart());

    //first 3 end times
    assertEquals(LocalDateTime.parse("2025-12-24T17:00"), events.get(0).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-25T17:00"), events.get(2).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-26T17:00"), events.get(4).getEnd());

    //second repeat
    assertEquals(LocalDateTime.parse("2025-12-31T08:00"), events.get(1).getStart());
    assertEquals(LocalDateTime.parse("2026-01-01T08:00"), events.get(3).getStart());
    assertEquals(LocalDateTime.parse("2026-01-02T08:00"), events.get(5).getStart());

    //Events with an end time
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesUntil("Series2", LocalDateTime.parse("2025-12-24T05:00"),
            LocalDateTime.parse("2025-12-24T06:00"), repeatedDays2, LocalDate.parse("2026-01-01"));
    List<Event> events2 = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    //checks if the correct amount of events are added to the map
    assertEquals(3, events2.size());

    //first 3 events
    assertEquals(LocalDateTime.parse("2025-12-24T05:00"), events2.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T05:00"), events2.get(2).getStart());

    //first 3 end times
    assertEquals(LocalDateTime.parse("2025-12-24T06:00"), events2.get(0).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-26T06:00"), events2.get(2).getEnd());

    //second repeat
    assertEquals(LocalDateTime.parse("2025-12-31T05:00"), events2.get(1).getStart());
  }

  @Test
  public void editEventTest() {
    //Creates the events
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T15:00"),
            LocalDateTime.parse("2025-10-05T17:00"));
    calendar.editEvent(PropertyType.SUBJECT, "test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"), "New name");
    assertEquals(2, calendar.getCalendar().get(LocalDate.parse("2025-10-05")).size());

    //Changes name
    assertEquals("New name", calendar.getCalendar().get(LocalDate.parse("2025-10-05")).
            get(0).getSubject());
    assertEquals("test", calendar.getCalendar().get(LocalDate.parse("2025-10-05")).
            get(1).getSubject());

    //changes time
    calendar.editEvent(PropertyType.START, "New name", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"), "2025-10-05T05:00");
    assertEquals(LocalDateTime.parse("2025-10-05T05:00"), calendar.getCalendar().
            get(LocalDate.parse("2025-10-05")).
            get(0).getStart());

    //change by a whole day
    calendar.editEvent(PropertyType.START, "New name", LocalDateTime.parse("2025-10-05T05:00"),
            LocalDateTime.parse("2025-10-05T15:00"), "2025-10-06T10:00");
    assertEquals(1, calendar.getCalendar().get(LocalDate.parse("2025-10-05")).size());
    assertEquals(1, calendar.getCalendar().get(LocalDate.parse("2025-10-06")).size());
    assertEquals(LocalDateTime.parse("2025-10-06T10:00"), calendar.getCalendar().
            get(LocalDate.parse("2025-10-06")).
            get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-10-06T15:00"), calendar.getCalendar().
            get(LocalDate.parse("2025-10-06")).
            get(0).getEnd());
  }

  @Test
  public void editSeriesTest() {
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesTimes("Series", LocalDateTime.parse("2025-12-24T05:00"),
            LocalDateTime.parse("2025-12-24T06:00"), repeatedDays2, 2);
    List<Event> dec24Events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));
    assertEquals(4, dec24Events.size());

    calendar.editSeries(PropertyType.START, "Series", LocalDateTime.parse("2025-12-24T05:00"),
            "2025-12-26T05:00");
    assertEquals(false, calendar.getSeries().containsKey(LocalDateTime.parse("2025-12-24T05:00")));
    assertEquals(true, calendar.getSeries().containsKey(LocalDateTime.parse("2025-12-26T05:00")));
    assertEquals(LocalDateTime.parse("2025-12-26T06:00"), calendar.getCalendar().
            get(LocalDate.parse("2025-12-26")).
            get(0).getEnd());
  }
}