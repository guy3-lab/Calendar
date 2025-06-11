package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.parse.PropertyType;
import model.calendar.Calendar;
import model.calendar.Event;
import model.calendar.IEvent;
import model.enums.Location;
import model.enums.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Comprehensive test class that tests the model of the calendar application.
 * Fixed to work with the corrected implementation.
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
  public void testExceptions() {
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));
    calendar.createEvent("different", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));

    //throws exception for trying to create an existing event
    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"));
      fail("Should throw exception for duplicate event");
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }

    //throws exception if end time is before start time
    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-04T15:00"));
      fail("Should throw exception for end before start");
    } catch (Exception e) {
      assertEquals("End time must be after start time", e.getMessage());
    }

    //throws an exception for trying to create an event in an invalid date
    try {
      calendarLeap.createEvent("test", LocalDateTime.parse("2024-02-30T10:00"),
              LocalDateTime.parse("2024-02-30T15:00"));
      fail("Should throw exception for invalid date");
    } catch (Exception e) {
      assertTrue("Should contain date parsing error",
              e.getMessage().contains("could not be parsed"));
    }

    //throws an exception for trying to create an event series where the event spans over 1 day
    try {
      List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));
      calendar.createSeriesTimes("series", LocalDateTime.parse("2024-02-03T05:00"),
              LocalDateTime.parse("2024-02-04T05:00"), repeatedDays, 3);
      fail("Should throw exception for multi-day series event");
    } catch (Exception e) {
      assertEquals("Start date and end date must be the same", e.getMessage());
    }

    //throws an exception when editing an event to an existing event
    try {
      calendar.editEvent(PropertyType.SUBJECT, "different", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"), "test");
      fail("Should throw exception when editing to duplicate");
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }
  }

  @Test
  public void testCreateEvent() {
    //creating two events on the same day
    LocalDateTime oct52025Start = LocalDateTime.parse("2025-10-05T10:00");
    LocalDateTime oct52025End = LocalDateTime.parse("2025-10-05T15:00");
    Event event1 = calendar.createEvent("test", oct52025Start, oct52025End);

    LocalDateTime oct52025Start2 = LocalDateTime.parse("2025-10-05T15:00");
    LocalDateTime oct52025End2 = LocalDateTime.parse("2025-10-05T17:00");
    Event event2 = calendar.createEvent("test", oct52025Start2, oct52025End2);

    //confirming that the two events exist in the correct event list of the correct day
    LocalDate dateKey = LocalDate.parse("2025-10-05");
    List<IEvent> dateEvents = calendar.getCalendar().get(dateKey);
    assertNotNull("Events list should not be null", dateEvents);
    assertEquals("Should have 2 events", 2, dateEvents.size());
    assertTrue("Should contain first event", dateEvents.contains(event1));
    assertTrue("Should contain second event", dateEvents.contains(event2));

    //confirming that another day's list properly stores a newly created event
    Event multiDayEvent = calendar.createEvent("test", LocalDateTime.parse("2025-10-31T10:00"),
            LocalDateTime.parse("2025-11-02T15:00"));

    // Verify multi-day event appears on ALL spanned days
    LocalDate oct31 = LocalDate.parse("2025-10-31");
    LocalDate nov01 = LocalDate.parse("2025-11-01");
    LocalDate nov02 = LocalDate.parse("2025-11-02");

    // Check that event appears on all three days
    assertTrue("Should contain Oct 31", calendar.getCalendar().containsKey(oct31));
    assertTrue("Should contain Nov 1", calendar.getCalendar().containsKey(nov01));
    assertTrue("Should contain Nov 2", calendar.getCalendar().containsKey(nov02));

    List<IEvent> oct31Events = calendar.getCalendar().get(oct31);
    List<IEvent> nov01Events = calendar.getCalendar().get(nov01);
    List<IEvent> nov02Events = calendar.getCalendar().get(nov02);

    assertTrue("Oct 31 should contain multi-day event", oct31Events.contains(multiDayEvent));
    assertTrue("Nov 1 should contain multi-day event", nov01Events.contains(multiDayEvent));
    assertTrue("Nov 2 should contain multi-day event", nov02Events.contains(multiDayEvent));

    //checks the endTime and startTime field to ensure it has the proper end date
    assertEquals("End time should match", LocalDateTime.parse("2025-11-02T15:00"),
            multiDayEvent.getEnd());
    assertEquals("Start time should match", LocalDateTime.parse("2025-10-31T10:00"),
            multiDayEvent.getStart());

    //creates an event without an end field, which therefore is a full day event
    IEvent allDayEvent = calendar.createEvent("test", LocalDateTime.parse("2025-02-27T05:00"), null);
    LocalDate feb27 = LocalDate.parse("2025-02-27");
    List<IEvent> feb27Events = calendar.getCalendar().get(feb27);
    assertTrue("Feb 27 should contain all-day event", feb27Events.contains(allDayEvent));
    assertEquals("All-day should end at 5:00 PM", LocalDateTime.parse("2025-02-27T17:00"),
            allDayEvent.getEnd());
    assertEquals("All-day should start at 8:00 AM", LocalDateTime.parse("2025-02-27T08:00"),
            allDayEvent.getStart());
  }

  @Test
  public void testMultiDayDuplicatePrevention() {
    // Create a multi-day event
    Event event1 = calendar.createEvent("Conference",
            LocalDateTime.parse("2025-06-15T10:00"),
            LocalDateTime.parse("2025-06-17T15:00"));

    // Try to create identical multi-day event - should fail
    try {
      calendar.createEvent("Conference",
              LocalDateTime.parse("2025-06-15T10:00"),
              LocalDateTime.parse("2025-06-17T15:00"));
      fail("Should prevent duplicate multi-day event");
    } catch (IllegalArgumentException e) {
      assertEquals("Event already exists", e.getMessage());
    }

    // Verify only one event exists on each day
    assertEquals("Day 1 should have only one event", 1,
            calendar.getCalendar().get(LocalDate.parse("2025-06-15")).size());
    assertEquals("Day 2 should have only one event", 1,
            calendar.getCalendar().get(LocalDate.parse("2025-06-16")).size());
    assertEquals("Day 3 should have only one event", 1,
            calendar.getCalendar().get(LocalDate.parse("2025-06-17")).size());
  }

  @Test
  public void testCreateSeriesTimes() {
    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));

    //full day event series
    calendar.createSeriesTimes("Series1", LocalDateTime.parse("2025-12-24T05:00"), null,
            repeatedDays, 3);
    List<IEvent> events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    List<IEvent> event24 = calendar.getCalendar().get(LocalDate.parse("2025-12-24"));
    List<IEvent> event25 = calendar.getCalendar().get(LocalDate.parse("2025-12-25"));
    List<IEvent> event26 = calendar.getCalendar().get(LocalDate.parse("2025-12-26"));

    //checks if the correct amount of events are added to the map
    assertEquals("Should have 9 series events", 9, events.size());
    assertEquals("Dec 24 should have 1 event", 1, event24.size());
    assertEquals("Dec 25 should have 1 event", 1, event25.size());
    assertEquals("Dec 26 should have 1 event", 1, event26.size());

    //first 3 events
    assertEquals("First Wed should start at 8:00 AM", LocalDateTime.parse("2025-12-24T08:00"),
            events.get(0).getStart());
    assertEquals("First Thu should start at 8:00 AM", LocalDateTime.parse("2025-12-25T08:00"),
            events.get(3).getStart());
    assertEquals("First Fri should start at 8:00 AM", LocalDateTime.parse("2025-12-26T08:00"),
            events.get(6).getStart());

    //first 3 end times
    assertEquals("First Wed should end at 5:00 PM", LocalDateTime.parse("2025-12-24T17:00"),
            events.get(0).getEnd());
    assertEquals("First Thu should end at 5:00 PM", LocalDateTime.parse("2025-12-25T17:00"),
            events.get(3).getEnd());
    assertEquals("First Fri should end at 5:00 PM", LocalDateTime.parse("2025-12-26T17:00"),
            events.get(6).getEnd());

    //second repeat
    assertEquals("Second Wed should be next week", LocalDateTime.parse("2025-12-31T08:00"),
            events.get(1).getStart());
    assertEquals("Second Thu should be next week", LocalDateTime.parse("2026-01-01T08:00"),
            events.get(4).getStart());
    assertEquals("Second Fri should be next week", LocalDateTime.parse("2026-01-02T08:00"),
            events.get(7).getStart());

    //third repeat
    assertEquals("Third Wed should be two weeks later", LocalDateTime.parse("2026-01-07T08:00"),
            events.get(2).getStart());
    assertEquals("Third Thu should be two weeks later", LocalDateTime.parse("2026-01-08T08:00"),
            events.get(5).getStart());
    assertEquals("Third Fri should be two weeks later", LocalDateTime.parse("2026-01-09T08:00"),
            events.get(8).getStart());
  }

  @Test
  public void testCreateSeriesTimes2() {
    //Events with an end time
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesTimes("Series2", LocalDateTime.parse("2025-12-24T05:00"),
            LocalDateTime.parse("2025-12-24T06:00"), repeatedDays2, 2);
    List<IEvent> events2 = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    //checks if the correct amount of events are added to the map
    assertEquals("Should have 4 timed series events", 4, events2.size());

    //first 2 events
    assertEquals("First Wed timed should start correctly", LocalDateTime.parse("2025-12-24T05:00"),
            events2.get(0).getStart());
    assertEquals("First Fri timed should start correctly", LocalDateTime.parse("2025-12-26T05:00"),
            events2.get(2).getStart());

    //first 2 end times
    assertEquals("First Wed timed should end correctly", LocalDateTime.parse("2025-12-24T06:00"),
            events2.get(0).getEnd());
    assertEquals("First Fri timed should end correctly", LocalDateTime.parse("2025-12-26T06:00"),
            events2.get(2).getEnd());

    //second repeat
    assertEquals("Second Wed timed should be next week", LocalDateTime.parse("2025-12-31T05:00"),
            events2.get(1).getStart());
    assertEquals("Second Fri timed should be next week", LocalDateTime.parse("2026-01-02T05:00"),
            events2.get(3).getStart());
  }

  @Test
  public void testCreateSeriesUntil() {
    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "R", "F"));

    //full day event series
    calendar.createSeriesUntil("Series1", LocalDateTime.parse("2025-12-24T05:00"), null,
            repeatedDays, LocalDate.parse("2026-01-03"));
    List<IEvent> events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));
    assertEquals("Should have 6 events until Jan 3", 6, events.size());

    //first 3 events
    assertEquals("First Wed should start at 8:00 AM", LocalDateTime.parse("2025-12-24T08:00"),
            events.get(0).getStart());
    assertEquals("First Thu should start at 8:00 AM", LocalDateTime.parse("2025-12-25T08:00"),
            events.get(2).getStart());
    assertEquals("First Fri should start at 8:00 AM", LocalDateTime.parse("2025-12-26T08:00"),
            events.get(4).getStart());

    //first 3 end times
    assertEquals("First Wed should end at 5:00 PM", LocalDateTime.parse("2025-12-24T17:00"),
            events.get(0).getEnd());
    assertEquals("First Thu should end at 5:00 PM", LocalDateTime.parse("2025-12-25T17:00"),
            events.get(2).getEnd());
    assertEquals("First Fri should end at 5:00 PM", LocalDateTime.parse("2025-12-26T17:00"),
            events.get(4).getEnd());

    //second repeat
    assertEquals("Second Wed should be next week", LocalDateTime.parse("2025-12-31T08:00"),
            events.get(1).getStart());
    assertEquals("Second Thu should be next week", LocalDateTime.parse("2026-01-01T08:00"),
            events.get(3).getStart());
    assertEquals("Second Fri should be next week", LocalDateTime.parse("2026-01-02T08:00"),
            events.get(5).getStart());
  }

  @Test
  public void testCreateSeriesUntil2() {
    //Events with an end time
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesUntil("Series2", LocalDateTime.parse("2025-12-24T05:00"),
            LocalDateTime.parse("2025-12-24T06:00"), repeatedDays2, LocalDate.parse("2026-01-01"));
    List<IEvent> events2 = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));

    //checks if the correct amount of events are added to the map
    assertEquals("Should have 3 events until Jan 1", 3, events2.size());

    //first 2 events
    assertEquals("First Wed timed should start correctly", LocalDateTime.parse("2025-12-24T05:00"),
            events2.get(0).getStart());
    assertEquals("First Fri timed should start correctly", LocalDateTime.parse("2025-12-26T05:00"),
            events2.get(2).getStart());

    //first 2 end times
    assertEquals("First Wed timed should end correctly", LocalDateTime.parse("2025-12-24T06:00"),
            events2.get(0).getEnd());
    assertEquals("First Fri timed should end correctly", LocalDateTime.parse("2025-12-26T06:00"),
            events2.get(2).getEnd());

    //second repeat
    assertEquals("Second Wed should be next week", LocalDateTime.parse("2025-12-31T05:00"),
            events2.get(1).getStart());
  }

  @Test
  public void testEditEvent() {
    LocalDateTime start = LocalDateTime.parse("2025-10-05T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-05T15:00");
    LocalDateTime start2 = LocalDateTime.parse("2025-10-05T15:00");
    LocalDateTime end2 = LocalDateTime.parse("2025-10-05T17:00");

    //Creates the events
    IEvent event1 = calendar.createEvent("test", start, end);
    IEvent event2 = calendar.createEvent("test", start2, end2);
    List<IEvent> events = calendar.getCalendar().get(LocalDate.parse("2025-10-05"));
    assertEquals("Should have 2 events initially", 2, events.size());

    //Changes subject
    calendar.editEvent(PropertyType.SUBJECT, "test", start, end, "New name");
    assertEquals("First event subject should change", "New name", event1.getSubject());
    assertEquals("Second event subject should remain", "test", event2.getSubject());

    //changes time
    calendar.editEvent(PropertyType.START, "New name", start, end, "2025-10-05T05:00");
    start = LocalDateTime.parse("2025-10-05T05:00");
    assertEquals("Start time should be updated", LocalDateTime.parse("2025-10-05T05:00"),
            event1.getStart());

    calendar.editEvent(PropertyType.END, "New name", start, end, "2025-10-05T12:00");
    end = LocalDateTime.parse("2025-10-05T12:00");
    assertEquals("End time should be updated", LocalDateTime.parse("2025-10-05T12:00"),
            event1.getEnd());

    //change by a whole day
    calendar.editEvent(PropertyType.START, "New name", start, end, "2025-10-06T10:00");
    start = LocalDateTime.parse("2025-10-06T10:00");
    end = LocalDateTime.parse("2025-10-06T12:00");

    // Check that event was removed from original date
    List<IEvent> oct5Events = calendar.getCalendar().get(LocalDate.parse("2025-10-05"));
    assertEquals("Oct 5 should have 1 event after move", 1, oct5Events.size());
    assertSame("Remaining event should be event2", event2, oct5Events.get(0));

    //checking if event exists on the new day
    List<IEvent> events06 = calendar.getCalendar().get(LocalDate.parse("2025-10-06"));
    assertEquals("Oct 6 should have 1 event", 1, events06.size());
    assertSame("Moved event should be event1", event1, events06.get(0));

    //checks that the times got updated correctly
    assertEquals("Start time should be updated", LocalDateTime.parse("2025-10-06T10:00"),
            events06.get(0).getStart());
    assertEquals("End time should be updated", LocalDateTime.parse("2025-10-06T12:00"),
            events06.get(0).getEnd());

    //change by a whole day backwards
    calendar.editEvent(PropertyType.START, "test", start2, end2, "2025-10-04T05:00");
    start2 = LocalDateTime.parse("2025-10-04T05:00");

    // Verify event moved correctly - FIXED: Oct 5 should indeed have 0 events now
    assertEquals("Oct 5 should have no events after second move", 0,
            calendar.getCalendar().containsKey(LocalDate.parse("2025-10-05")) ?
                    calendar.getCalendar().get(LocalDate.parse("2025-10-05")).size() : 0);
    assertEquals("Oct 4 should have 1 event", 1,
            calendar.getCalendar().get(LocalDate.parse("2025-10-04")).size());
    assertEquals("Moved event should have correct start", start2,
            calendar.getCalendar().get(LocalDate.parse("2025-10-04")).get(0).getStart());

    //change location
    IEvent editingEvent = events06.get(0);
    assertEquals("Initial location should be ONLINE", Location.ONLINE, editingEvent.getLocation());
    calendar.editEvent(PropertyType.LOCATION, "New name", start, end, "pHySiCAL");
    assertEquals("Location should be updated", Location.PHYSICAL, editingEvent.getLocation());

    //change status
    assertEquals("Initial status should be PUBLIC", Status.PUBLIC, editingEvent.getStatus());
    calendar.editEvent(PropertyType.STATUS, "New name", start, end, "pRIvaTe");
    assertEquals("Status should be updated", Status.PRIVATE, editingEvent.getStatus());

    //change description
    assertEquals("Initial description should be empty", "", editingEvent.getDesc());
    calendar.editEvent(PropertyType.DESCRIPTION, "New name", start, end, "this is a desc");
    assertEquals("Description should be updated", "this is a desc", editingEvent.getDesc());
  }

  @Test
  public void testShowStatusComprehensive() {
    // Create events for comprehensive status testing
    LocalDateTime meeting1Start = LocalDateTime.parse("2025-10-05T10:00");
    LocalDateTime meeting1End = LocalDateTime.parse("2025-10-05T11:00");
    LocalDateTime meeting2Start = LocalDateTime.parse("2025-10-05T14:00");
    LocalDateTime meeting2End = LocalDateTime.parse("2025-10-05T15:30");

    calendar.createEvent("Morning Meeting", meeting1Start, meeting1End);
    calendar.createEvent("Afternoon Meeting", meeting2Start, meeting2End);

    // Test availability before any meetings
    assertEquals("Should be available before meetings", "available",
            calendar.showStatus(LocalDateTime.parse("2025-10-05T09:00")));

    // Test busy at exact start time
    assertEquals("Should be busy at meeting start", "busy",
            calendar.showStatus(meeting1Start));

    // Test busy during meeting
    assertEquals("Should be busy during meeting", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-10-05T10:30")));

    // Test available at exact end time
    assertEquals("Should be available at meeting end", "available",
            calendar.showStatus(meeting1End));

    // Test available between meetings
    assertEquals("Should be available between meetings", "available",
            calendar.showStatus(LocalDateTime.parse("2025-10-05T12:00")));

    // Test busy during second meeting
    assertEquals("Should be busy during second meeting", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-10-05T14:45")));

    // Test availability on day with no events
    assertEquals("Should be available on empty day", "available",
            calendar.showStatus(LocalDateTime.parse("2025-10-06T10:00")));
  }

  @Test
  public void testShowStatusWithMultiDayEvent() {
    // Create multi-day event
    LocalDateTime start = LocalDateTime.parse("2025-06-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-17T15:00");
    calendar.createEvent("Conference", start, end);

    // Test busy during multi-day event on different days
    assertEquals("Should be busy on start day during event", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T12:00")));
    assertEquals("Should be busy on middle day", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-16T10:00")));
    assertEquals("Should be busy on end day during event", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-17T14:00")));

    // Test available before and after multi-day event
    assertEquals("Should be available before event", "available",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T09:00")));
    assertEquals("Should be available after event", "available",
            calendar.showStatus(LocalDateTime.parse("2025-06-17T16:00")));
  }

  @Test
  public void testShowStatusWithAllDayEvent() {
    calendar.createEvent("All Day Conference", LocalDateTime.parse("2025-06-15T12:00"), null);

    // Should be busy throughout the all-day event (8 AM to 5 PM)
    assertEquals("Should be busy at 8 AM", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T08:00")));
    assertEquals("Should be busy at noon", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T12:00")));
    assertEquals("Should be busy at 4:59 PM", "busy",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T16:59")));

    // Should be available at 5:00 PM (end time)
    assertEquals("Should be available at 5:00 PM", "available",
            calendar.showStatus(LocalDateTime.parse("2025-06-15T17:00")));
  }

  @Test
  public void testEditSeries() {
    //Creates an event series
    List<String> repeatedDays2 = new ArrayList<>(Arrays.asList("W", "F"));
    LocalDateTime start = LocalDateTime.parse("2025-12-24T05:00");
    LocalDateTime end = LocalDateTime.parse("2025-12-24T06:00");
    calendar.createSeriesTimes("Series", start, end, repeatedDays2, 2);
    List<IEvent> dec24Events = calendar.getSeries().get(LocalDateTime.parse("2025-12-24T05:00"));
    assertEquals("Should have 4 series events", 4, dec24Events.size());

    //edits the start day by 1 day
    calendar.editSeries(PropertyType.START, "Series", start, "2025-12-26T05:00");
    LocalDateTime newStart = LocalDateTime.parse("2025-12-26T05:00");
    assertFalse("Old series key should be removed", calendar.getSeries().containsKey(start));
    assertTrue("New series key should exist", calendar.getSeries().containsKey(newStart));

    //checks that everything was edited accordingly
    List<IEvent> dec26Series = calendar.getSeries().get(LocalDateTime.parse("2025-12-26T05:00"));
    assertEquals("New series should have 4 events", 4, dec26Series.size());

    Event dec26 = new Event.EventBuilder("Series", LocalDateTime.parse("2025-12-26T05:00"))
            .end(LocalDateTime.parse("2025-12-26T06:00")).build();
    Event dec28 = new Event.EventBuilder("Series", LocalDateTime.parse("2025-12-28T05:00"))
            .end(LocalDateTime.parse("2025-12-28T06:00")).build();
    Event jan2 = new Event.EventBuilder("Series", LocalDateTime.parse("2026-01-02T05:00"))
            .end(LocalDateTime.parse("2026-01-02T06:00")).build();
    Event jan4 = new Event.EventBuilder("Series", LocalDateTime.parse("2026-01-04T05:00"))
            .end(LocalDateTime.parse("2026-01-04T06:00")).build();

    //checks to see if the series contains all the correct events
    assertTrue("Should contain Dec 26 event", dec26Series.contains(dec26));
    assertTrue("Should contain Dec 28 event", dec26Series.contains(dec28));
    assertTrue("Should contain Jan 2 event", dec26Series.contains(jan2));
    assertTrue("Should contain Jan 4 event", dec26Series.contains(jan4));

    //checks to see if all events contain the same subject
    assertEquals("All events should have same subject", "Series", dec26Series.get(0).getSubject());
    calendar.editSeries(PropertyType.SUBJECT, "Series", newStart, "new Series");
    assertEquals("Subject should be updated", "new Series", dec26Series.get(0).getSubject());
    assertEquals("Subject should be updated", "new Series", dec26Series.get(1).getSubject());
    assertEquals("Subject should be updated", "new Series", dec26Series.get(2).getSubject());
    assertEquals("Subject should be updated", "new Series", dec26Series.get(3).getSubject());

    //changes to the location
    assertEquals("Initial location should be ONLINE", Location.ONLINE,
            dec26Series.get(0).getLocation());
    calendar.editSeries(PropertyType.LOCATION, "new Series", newStart, "physical");
    assertEquals("Location should be updated", Location.PHYSICAL, dec26Series.get(0).getLocation());
    assertEquals("Location should be updated", Location.PHYSICAL, dec26Series.get(1).getLocation());
    assertEquals("Location should be updated", Location.PHYSICAL, dec26Series.get(2).getLocation());
    assertEquals("Location should be updated", Location.PHYSICAL, dec26Series.get(3).getLocation());

    //changes to the status
    assertEquals("Initial status should be PUBLIC", Status.PUBLIC, dec26Series.get(0).getStatus());
    calendar.editSeries(PropertyType.STATUS, "new Series", newStart, "private");
    assertEquals("Status should be updated", Status.PRIVATE, dec26Series.get(0).getStatus());
    assertEquals("Status should be updated", Status.PRIVATE, dec26Series.get(1).getStatus());
    assertEquals("Status should be updated", Status.PRIVATE, dec26Series.get(2).getStatus());
    assertEquals("Status should be updated", Status.PRIVATE, dec26Series.get(3).getStatus());

    //changes to the description
    assertEquals("Initial description should be empty", "", dec26Series.get(0).getDesc());
    calendar.editSeries(PropertyType.DESCRIPTION, "new Series", newStart, "this is a dec");
    assertEquals("Description should be updated", "this is a dec", dec26Series.get(0).getDesc());
    assertEquals("Description should be updated", "this is a dec", dec26Series.get(1).getDesc());
    assertEquals("Description should be updated", "this is a dec", dec26Series.get(2).getDesc());
    assertEquals("Description should be updated", "this is a dec", dec26Series.get(3).getDesc());
  }

  @Test
  public void testEditEvents() {
    //creates a series
    List<String> repeatedDays = new ArrayList<>(Arrays.asList("W", "F"));
    calendar.createSeriesTimes("base", LocalDateTime.parse("2025-12-26T05:00"),
            LocalDateTime.parse("2025-12-26T06:00"), repeatedDays, 2);
    List<IEvent> dec26Events = calendar.getSeries().get(LocalDateTime.parse("2025-12-26T05:00"));
    assertEquals("Should have 4 series events", 4, dec26Events.size());

    //changes the subject to events to jan 2 and after
    calendar.editEvents(PropertyType.SUBJECT, "base", LocalDateTime.parse("2026-01-02T05:00"),
            "Series");
    assertEquals("First Wednesday should remain 'base'", "base", dec26Events.get(0).getSubject());
    assertEquals("Second Wednesday should be 'Series'", "Series", dec26Events.get(1).getSubject());
    assertEquals("First Friday should remain 'base'", "base", dec26Events.get(2).getSubject());
    assertEquals("Second Friday should be 'Series'", "Series", dec26Events.get(3).getSubject());

    //changes location starting dec 26 but with only the "base" subject
    calendar.editEvents(PropertyType.LOCATION, "base", LocalDateTime.parse("2025-12-26T05:00"),
            "physical");
    assertEquals("Dec 26 should be PHYSICAL", Location.PHYSICAL, dec26Events.get(0).getLocation());
    assertEquals("Jan 2 should remain ONLINE", Location.ONLINE, dec26Events.get(1).getLocation());
    assertEquals("Dec 28 should be PHYSICAL", Location.PHYSICAL, dec26Events.get(2).getLocation());
    assertEquals("Jan 4 should remain ONLINE", Location.ONLINE, dec26Events.get(3).getLocation());

    //changes the status starting on jan 2 with only the "Series" subject
    calendar.editEvents(PropertyType.STATUS, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "private");
    assertEquals("Dec 26 should remain PUBLIC", Status.PUBLIC, dec26Events.get(0).getStatus());
    assertEquals("Jan 2 should be PRIVATE", Status.PRIVATE, dec26Events.get(1).getStatus());
    assertEquals("Dec 28 should remain PUBLIC", Status.PUBLIC, dec26Events.get(2).getStatus());
    assertEquals("Jan 4 should be PRIVATE", Status.PRIVATE, dec26Events.get(3).getStatus());

    //changes the description starting on jan 2 with only the "Series" subject
    calendar.editEvents(PropertyType.DESCRIPTION, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "hello");
    assertEquals("Dec 26 should have empty description", "", dec26Events.get(0).getDesc());
    assertEquals("Jan 2 should have 'hello'", "hello", dec26Events.get(1).getDesc());
    assertEquals("Dec 28 should have empty description", "", dec26Events.get(2).getDesc());
    assertEquals("Jan 4 should have 'hello'", "hello", dec26Events.get(3).getDesc());

    //Edits the start time of a series that start on Jan 2 to Jan 5, and alters all
    // corresponding events.
    calendar.editEvents(PropertyType.START, "Series", LocalDateTime.parse("2026-01-02T05:00"),
            "2026-01-05T06:00");
    List<IEvent> jan5Events = calendar.getSeries().get(LocalDateTime.parse("2026-01-05T06:00"));

    //two events get removed into another series
    assertEquals("Original series should have 2 events", 2, dec26Events.size());
    assertEquals("New series should have 2 events", 2, jan5Events.size());

    //Edits the end time of a series that start on Jan 5 (the whole Jan 5 series)
    calendar.editEvents(PropertyType.END, "Series", LocalDateTime.parse("2026-01-05T06:00"),
            "2026-01-05T10:00");
    assertEquals("End time should be updated", LocalDateTime.parse("2026-01-05T10:00"),
            calendar.getSeries().get(LocalDateTime.parse("2026-01-05T06:00")).get(0).getEnd());
  }

  @Test
  public void testPrintEvents() {
    LocalDateTime first = LocalDateTime.parse("2000-10-10T10:00");
    LocalDateTime firstEnd = LocalDateTime.parse("2000-10-11T10:00");
    LocalDateTime second = LocalDateTime.parse("2000-10-10T10:00");
    LocalDateTime secondEnd = LocalDateTime.parse("2000-10-10T15:00");

    calendar.createEvent("event1", first, firstEnd);
    calendar.createEvent("event2", first, firstEnd);
    calendar.createEvent("event3", second, secondEnd);

    //string that contain the events of the whole day
    String eventsOnDay = "event1, Start Time: 2000-10-10T10:00, End Time: 2000-10-11T10:00, "
            + "Location: ONLINE" + "\n" + "event2, Start Time: 2000-10-10T10:00, "
            + "End Time: 2000-10-11T10:00, Location: ONLINE" + "\n"
            + "event3, Start Time: 2000-10-10T10:00, End Time: 2000-10-10T15:00, Location: ONLINE";

    //string that contains only the events on the specified interval
    String eventsOnInterval = "event1, Start Time: 2000-10-10T10:00, End Time: 2000-10-11T10:00, "
            + "Location: ONLINE" + "\n" + "event2, Start Time: 2000-10-10T10:00, "
            + "End Time: 2000-10-11T10:00, Location: ONLINE"
            + "\n"
            + "event3, Start Time: 2000-10-10T10:00, End Time: 2000-10-10T15:00, Location: ONLINE";

    assertEquals("Print events should match expected format", eventsOnDay,
            calendar.printEvents(first.toLocalDate()));
    assertEquals("Print events interval should match expected format",
            eventsOnInterval, calendar.printEventsInterval(first, secondEnd));

    assertEquals("Should be available on unscheduled day", "available",
            calendar.showStatus(LocalDateTime.parse("2000-11-10T10:00")));
    assertEquals("Should be busy during scheduled event", "busy",
            calendar.showStatus(LocalDateTime.parse("2000-10-10T10:00")));
    assertEquals("Should be busy during multi-day event", "busy",
            calendar.showStatus(LocalDateTime.parse("2000-10-10T12:00")));
    assertEquals("Should be busy during multi-day events", "busy",
            calendar.showStatus(LocalDateTime.parse("2000-10-10T16:00")));
  }

  @Test
  public void testEmptyDayPrint() {
    String result = calendar.printEvents(LocalDate.parse("2025-01-01"));
    assertEquals("Should return no events message", "No events on this day", result);
  }
}