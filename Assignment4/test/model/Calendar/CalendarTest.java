package model.Calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.PropertyType;
import model.Enum.Location;
import model.Enum.Status;

import static org.junit.Assert.assertEquals;

/**
 * The test class that tests the model of the calendar application.
 */
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
    calendar.createEvent("different", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));

    //throws exception for trying to create an existing event
    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"));
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }

    //throws exception if end time is before start time
    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-04T15:00"));
    } catch (Exception e) {
      assertEquals("End time must be after start time", e.getMessage());
    }

    //throws an exception for trying to create an event in an invalid date
    try {
      calendarLeap.createEvent("test", LocalDateTime.parse("2024-02-30T10:00"),
              LocalDateTime.parse("2024-02-30T15:00"));
    } catch (Exception e) {
      assertEquals("Text '2024-02-30T10:00' could not be parsed: Invalid date 'FEBRUARY 30'",
              e.getMessage());
    }

    //throws an exception for trying to create an event series where the event spans over 1 day
    try {
      List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));
      calendar.createSeriesTimes("series", LocalDateTime.parse("2024-02-03T05:00"),
              LocalDateTime.parse("2024-02-04T05:00"), repeatedDays, 3);
    } catch (Exception e) {
      assertEquals("Start date and end date must be the same", e.getMessage());
    }

    //throws an exception when editing an event to an existing event
    try {
      calendar.editEvent(PropertyType.SUBJECT,"different", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"), "test");
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }
  }

  @Test
  public void createEventTest() {
    //creating two events on the same day
    LocalDateTime oct52025Start = LocalDateTime.parse("2025-10-05T10:00");
    LocalDateTime oct52025End = LocalDateTime.parse("2025-10-05T15:00");
    calendar.createEvent("test", oct52025Start, oct52025End);

    LocalDateTime oct52025Start2 = LocalDateTime.parse("2025-10-05T15:00");
    LocalDateTime oct52025End2 = LocalDateTime.parse("2025-10-05T17:00");
    calendar.createEvent("test", oct52025Start2, oct52025End2);

    //confirming that the two events exist in the correct event list of the correct day
    Event event = new Event.EventBuilder("test", oct52025Start).
            end(oct52025End).build();
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

    //checks each day that they have the same event because the event spans from day start to day
    //end
    List<Event> oct31Events = calendar.getCalendar().get(oct31);
    assertEquals(true ,oct31Events.contains(event2));


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

    //first 2 events
    assertEquals(LocalDateTime.parse("2025-12-24T05:00"), events2.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T05:00"), events2.get(2).getStart());

    //first 2 end times
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

    //first 2 events
    assertEquals(LocalDateTime.parse("2025-12-24T05:00"), events2.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-12-26T05:00"), events2.get(2).getStart());

    //first 2 end times
    assertEquals(LocalDateTime.parse("2025-12-24T06:00"), events2.get(0).getEnd());
    assertEquals(LocalDateTime.parse("2025-12-26T06:00"), events2.get(2).getEnd());

    //second repeat
    assertEquals(LocalDateTime.parse("2025-12-31T05:00"), events2.get(1).getStart());
  }

  @Test
  public void editEventTest() {
    LocalDateTime start = LocalDateTime.parse("2025-10-05T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-05T15:00");

    LocalDateTime start2 = LocalDateTime.parse("2025-10-05T15:00");
    LocalDateTime end2 = LocalDateTime.parse("2025-10-05T17:00");



    //Creates the events
    calendar.createEvent("test", start, end);
    calendar.createEvent("test", start2, end2);
    List<Event> events = calendar.getCalendar().get(LocalDate.parse("2025-10-05"));
    assertEquals(2, events.size());

    //Changes subject
    calendar.editEvent(PropertyType.SUBJECT, "test", start, end, "New name");
    assertEquals("New name", events.get(0).getSubject());
    assertEquals("test", events.get(1).getSubject());

    //changes time
    calendar.editEvent(PropertyType.START, "New name", start, end, "2025-10-05T05:00");
    start = LocalDateTime.parse("2025-10-05T05:00");
    assertEquals(LocalDateTime.parse("2025-10-05T05:00"), events.get(0).getStart());
    calendar.editEvent(PropertyType.END, "New name", start, end, "2025-10-05T12:00");
    end = LocalDateTime.parse("2025-10-05T12:00");
    assertEquals(LocalDateTime.parse("2025-10-05T12:00"), events.get(0).getEnd());

    //change by a whole day
    calendar.editEvent(PropertyType.START, "New name", start, end, "2025-10-06T10:00");
    start = LocalDateTime.parse("2025-10-06T10:00");
    end = LocalDateTime.parse("2025-10-06T12:00");
    assertEquals(1, events.size());

    //checking if event exists on the new day
    List<Event> events06 = calendar.getCalendar().get(LocalDate.parse("2025-10-06"));
    assertEquals(1, events06.size());

    //checks that the times got updated correctly
    assertEquals(LocalDateTime.parse("2025-10-06T10:00"), events06.get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-10-06T12:00"), events06.get(0).getEnd());

    //change by a whole day backwards
    calendar.editEvent(PropertyType.START, "test", start2, end2, "2025-10-04T05:00");
    start2 = LocalDateTime.parse("2025-10-04T05:00");
    assertEquals(0, calendar.getCalendar().get(LocalDate.parse("2025-10-05")).size());
    assertEquals(1, calendar.getCalendar().get(LocalDate.parse("2025-10-04")).size());
    assertEquals(start2, calendar.getCalendar().
            get(LocalDate.parse("2025-10-04")).get(0).getStart());
    assertEquals(LocalDateTime.parse("2025-10-05T17:00"), calendar.getCalendar().
            get(LocalDate.parse("2025-10-04")).get(0).getEnd());

    //change location
    Event editingEvent = events06.get(0);
    assertEquals(Location.ONLINE, editingEvent.getLocation());
    calendar.editEvent(PropertyType.LOCATION, "New name", start, end, "pHySiCAL");
    assertEquals(Location.PHYSICAL, editingEvent.getLocation());

    //change status
    assertEquals(Status.PUBLIC, editingEvent.getStatus());
    calendar.editEvent(PropertyType.STATUS, "New name", start, end, "pRIvaTe");
    assertEquals(Status.PRIVATE, editingEvent.getStatus());

    //change description
    assertEquals("", editingEvent.getDesc());
    calendar.editEvent(PropertyType.DESCRIPTION, "New name", start, end, "this is a desc");
    assertEquals("this is a desc", editingEvent.getDesc());
  }

  @Test
  public void editSeriesTest() {
    //Creates an event series
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    LocalDateTime start = LocalDateTime.parse("2025-12-24T05:00");
    LocalDateTime end = LocalDateTime.parse("2025-12-24T06:00");
    calendar.createSeriesTimes("Series", start, end, repeatedDays2, 2);
    List<Event> dec24Events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));
    assertEquals(4, dec24Events.size());

    //edits the start day by 1 day
    calendar.editSeries(PropertyType.START, "Series", start, "2025-12-26T05:00");
    LocalDateTime newStart = LocalDateTime.parse("2025-12-26T05:00");
    assertEquals(false, calendar.getSeries().containsKey(start));
    assertEquals(true, calendar.getSeries().containsKey(newStart));

    //checks that everything was edited accordingly
    List<Event> dec26Series = calendar.getSeries().get(LocalDateTime.parse("2025-12-26T05:00"));
    Event dec26 = new Event.EventBuilder("Series", LocalDateTime.parse("2025-12-26T05:00")).
            end(LocalDateTime.parse("2025-12-26T06:00")).build();
    Event dec28 = new Event.EventBuilder("Series", LocalDateTime.parse("2025-12-28T05:00")).
            end(LocalDateTime.parse("2025-12-28T06:00")).build();
    Event jan2 = new Event.EventBuilder("Series", LocalDateTime.parse("2026-01-02T05:00")).
            end(LocalDateTime.parse("2026-01-02T06:00")).build();
    Event jan4 = new Event.EventBuilder("Series", LocalDateTime.parse("2026-01-04T05:00")).
            end(LocalDateTime.parse("2026-01-04T06:00")).build();

    //checks to see if the series contains all the correct events
    assertEquals(true, dec26Series.contains(dec26));
    assertEquals(true, dec26Series.contains(dec28));
    assertEquals(true, dec26Series.contains(jan2));
    assertEquals(true, dec26Series.contains(jan4));

    assertEquals(jan4.getEnd(), dec26Series.get(0).getEnd());
    assertEquals(jan2.getStart(), dec26Series.get(2).getStart());

    //checks to see if all events contain the same subject
    assertEquals("Series", dec26Series.get(0).getSubject());
    calendar.editSeries(PropertyType.SUBJECT, "Series", newStart, "new Series");
    assertEquals("new Series", dec26Series.get(0).getSubject());
    assertEquals("new Series", dec26Series.get(1).getSubject());
    assertEquals("new Series", dec26Series.get(2).getSubject());
    assertEquals("new Series", dec26Series.get(3).getSubject());

    //changes to the location
    assertEquals(Location.ONLINE, dec26Series.get(0).getLocation());
    calendar.editSeries(PropertyType.LOCATION, "new Series", newStart, "physical");
    assertEquals(Location.PHYSICAL, dec26Series.get(0).getLocation());
    assertEquals(Location.PHYSICAL, dec26Series.get(1).getLocation());
    assertEquals(Location.PHYSICAL, dec26Series.get(2).getLocation());
    assertEquals(Location.PHYSICAL, dec26Series.get(3).getLocation());

    //changes to the status
    assertEquals(Status.PUBLIC, dec26Series.get(0).getStatus());
    calendar.editSeries(PropertyType.STATUS, "new Series", newStart, "private");
    assertEquals(Status.PRIVATE, dec26Series.get(0).getStatus());
    assertEquals(Status.PRIVATE, dec26Series.get(1).getStatus());
    assertEquals(Status.PRIVATE, dec26Series.get(2).getStatus());
    assertEquals(Status.PRIVATE, dec26Series.get(3).getStatus());

    //changes to the description
    assertEquals("", dec26Series.get(0).getDesc());
    calendar.editSeries(PropertyType.DESCRIPTION, "new Series", newStart, "this is a dec");
    assertEquals("this is a dec", dec26Series.get(0).getDesc());
    assertEquals("this is a dec", dec26Series.get(1).getDesc());
    assertEquals("this is a dec", dec26Series.get(2).getDesc());
    assertEquals("this is a dec", dec26Series.get(3).getDesc());
  }

  @Test
  public void editEventsTest() {
    //creates a series
    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesTimes("base", LocalDateTime.parse("2025-12-26T05:00"),
            LocalDateTime.parse("2025-12-26T06:00"), repeatedDays, 2);
    List<Event> dec26Events = calendar.getSeries().get(LocalDateTime.parse("2025-12-26T05:00"));
    assertEquals(4, dec26Events.size());

    //changes the subject to events to jan 2 and after
    calendar.editEvents(PropertyType.SUBJECT, "base", LocalDateTime.parse("2026-01-02T05:00"),
            "Series");
    assertEquals("base", dec26Events.get(0).getSubject()); //first wednesday
    assertEquals("Series", dec26Events.get(1).getSubject()); //second wednesday
    assertEquals("base", dec26Events.get(2).getSubject()); //first friday
    assertEquals("Series", dec26Events.get(3).getSubject()); //second friday

    //changes location starting dec 26 but with only the "base" subject
    calendar.editEvents(PropertyType.LOCATION, "base", LocalDateTime.parse("2025-12-26T05:00"),
            "physical");
    assertEquals(Location.PHYSICAL, dec26Events.get(0).getLocation()); //dec-26
    assertEquals(Location.ONLINE, dec26Events.get(1).getLocation()); //jan-2
    assertEquals(Location.PHYSICAL, dec26Events.get(2).getLocation()); //dec-28
    assertEquals(Location.ONLINE, dec26Events.get(3).getLocation()); //jan-5

    //changes the status starting on jan 2 with only the "Series" subject
    calendar.editEvents(PropertyType.STATUS, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "private");
    assertEquals(Status.PUBLIC, dec26Events.get(0).getStatus()); // dec-26
    assertEquals(Status.PRIVATE, dec26Events.get(1).getStatus()); // jan-2
    assertEquals(Status.PUBLIC, dec26Events.get(2).getStatus()); //dec-28
    assertEquals(Status.PRIVATE, dec26Events.get(3).getStatus()); //jan-5

    //changes the description starting on jan 2 with only the "Series" subject
    calendar.editEvents(PropertyType.DESCRIPTION, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "hello");
    assertEquals("", dec26Events.get(0).getDesc()); // dec-26
    assertEquals("hello", dec26Events.get(1).getDesc()); // jan-2
    assertEquals("", dec26Events.get(2).getDesc()); //dec-28
    assertEquals("hello", dec26Events.get(3).getDesc()); //jan-5

    //Edits the start time of a series that start on Jan 2 to Jan 5, and alters all corresponding
    //events.
    calendar.editEvents(PropertyType.START, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "2026-01-05T06:00");
    List<Event> jan5Events = calendar.getSeries().get(LocalDateTime.parse("2026-01-05T06:00"));

    //two events get removed into another series
    assertEquals(2, dec26Events.size());
    assertEquals(2, jan5Events.size());

    //Edits the end time of a series that start on Jan 5 (the whole Jan 5 series)
    calendar.editEvents(PropertyType.END, "Series", LocalDateTime.parse("2026-01-05T06:00"),
            "2026-01-05T10:00");
    assertEquals(LocalDateTime.parse("2026-01-05T10:00"), calendar.getSeries().
            get(LocalDateTime.parse("2026-01-05T06:00")).get(0).getEnd());
  }

  @Test
  public void printTests() {
    LocalDateTime first = LocalDateTime.parse("2000-10-10T10:00");
    LocalDateTime firstEnd = LocalDateTime.parse("2000-10-11T10:00");
    LocalDateTime second = LocalDateTime.parse("2000-10-10T10:00");
    LocalDateTime secondEnd = LocalDateTime.parse("2000-10-10T15:00");
    calendar.createEvent("event1", first, firstEnd);
    calendar.createEvent("event2", first, firstEnd);
    calendar.createEvent("event3", second, secondEnd);
    List<Event> oct10Events = calendar.getCalendar().get(first.toLocalDate());

    //string that contain the events of the whole day
    String eventsOnDay = "event1, Start Time: 2000-10-10T10:00, End Time: 2000-10-11T10:00, "
            + "Location: ONLINE" + "\n" + "event2, Start Time: 2000-10-10T10:00, "
            + "End Time: 2000-10-11T10:00, Location: ONLINE" + "\n"
            + "event3, Start Time: 2000-10-10T10:00, End Time: 2000-10-10T15:00, Location: ONLINE";

    //string that contains only the events on the specified interval
    String eventsOnInterval = "event1, Start Time: 2000-10-10T10:00, End Time: 2000-10-11T10:00, "
            + "Location: ONLINE" + "\n" + "event2, Start Time: 2000-10-10T10:00, "
            + "End Time: 2000-10-11T10:00, Location: ONLINE";

    assertEquals(eventsOnDay, calendar.printEvents(first.toLocalDate()));
    assertEquals(eventsOnInterval, calendar.printEventsInterval(first, firstEnd));
  }
}