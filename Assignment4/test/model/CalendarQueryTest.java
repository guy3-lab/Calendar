package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import model.calendar.Calendar;
import model.calendar.Event;
import model.calendar.ICalendar;
import model.calendar.IEvent;
import model.enums.Location;
import model.enums.Status;

/**
 * Comprehensive test suite for Calendar Query Features (3.1 and 3.2)
 * Tests event listing (print events) and status checking functionality
 * Uses JUnit 4 with extremely robust assertions at every intermediate step
 */
public class CalendarQueryTest {

  private ICalendar calendar;
  private LocalDate testDate;
  private LocalDateTime morning;
  private LocalDateTime afternoon;
  private LocalDateTime evening;

  @Before
  public void setUp() {
    calendar = new Calendar();
    assertNotNull("Calendar should be initialized", calendar);

    testDate = LocalDate.of(2025, 6, 15); // Sunday
    assertNotNull("Test date should be initialized", testDate);
    assertEquals("Test date should be June 15, 2025", LocalDate.of(2025, 6, 15), testDate);

    morning = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    afternoon = LocalDateTime.of(testDate, java.time.LocalTime.of(14, 0));
    evening = LocalDateTime.of(testDate, java.time.LocalTime.of(19, 0));

    assertNotNull("Morning time should be initialized", morning);
    assertNotNull("Afternoon time should be initialized", afternoon);
    assertNotNull("Evening time should be initialized", evening);

    assertEquals("Morning should be 9:00 AM", 9, morning.getHour());
    assertEquals("Afternoon should be 2:00 PM", 14, afternoon.getHour());
    assertEquals("Evening should be 7:00 PM", 19, evening.getHour());

    // Verify calendar is empty initially
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertNotNull("Calendar data should not be null", calendarData);
    assertTrue("Calendar should be empty initially", calendarData.isEmpty());
  }

  // ==================== EVENT LISTING TESTS ====================

  @Test
  public void testPrintEventsOnDateNoEvents() {
    // Verify calendar is empty
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertNotNull("Calendar data should not be null", calendarData);
    assertTrue("Calendar should be empty", calendarData.isEmpty());
    assertFalse("Test date should not exist in calendar", calendarData.containsKey(testDate));

    // Test printing events on empty date
    String result = calendar.printEvents(testDate);
    assertNotNull("Result should not be null", result);
    assertEquals("Should return no events message", "No events on this day", result);

    // Verify calendar is still empty after query
    assertTrue("Calendar should still be empty after query", calendarData.isEmpty());
  }

  @Test
  public void testPrintEventsOnDateSingleEvent() {
    // Verify initial state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should be empty initially", calendarData.isEmpty());

    // Create event
    LocalDateTime endTime = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    IEvent event = calendar.createEvent("Team Meeting", morning, endTime);

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertEquals("Event subject should match", "Team Meeting", event.getSubject());
    assertEquals("Event start time should match", morning, event.getStart());
    assertEquals("Event end time should match", endTime, event.getEnd());
    assertEquals("Default location should be ONLINE", Location.ONLINE, event.getLocation());
    assertEquals("Default status should be PUBLIC", Status.PUBLIC, event.getStatus());

    // Verify calendar state after creation
    assertFalse("Calendar should not be empty after event creation", calendarData.isEmpty());
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    List<IEvent> dayEvents = calendarData.get(testDate);
    assertNotNull("Day events list should not be null", dayEvents);
    assertEquals("Should have exactly one event", 1, dayEvents.size());
    assertEquals("Event in calendar should match created event", event, dayEvents.get(0));

    // Test printing events
    String result = calendar.printEvents(testDate);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify all required fields are present in output
    assertTrue("Result should contain event subject", result.contains("Team Meeting"));
    assertTrue("Result should contain start time", result.contains("Start Time: " + morning));
    assertTrue("Result should contain end time", result.contains("End Time: " + endTime));
    assertTrue("Result should contain location", result.contains("Location: ONLINE"));

    // Verify no extra lines
    String[] lines = result.split("\n");
    assertEquals("Should have exactly one line of output", 1, lines.length);
  }

  @Test
  public void testPrintEventsOnDateMultipleEvents() {
    // Verify initial state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should be empty initially", calendarData.isEmpty());

    // Create multiple events
    LocalDateTime morning10 = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime afternoon15 = LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0));
    LocalDateTime evening20 = LocalDateTime.of(testDate, java.time.LocalTime.of(20, 0));

    IEvent event1 = calendar.createEvent("Morning Meeting", morning, morning10);
    IEvent event2 = calendar.createEvent("Lunch", afternoon, afternoon15);
    IEvent event3 = calendar.createEvent("Dinner", evening, evening20);

    // Verify each event creation
    assertNotNull("First event should be created", event1);
    assertNotNull("Second event should be created", event2);
    assertNotNull("Third event should be created", event3);

    assertEquals("First event subject should match", "Morning Meeting", event1.getSubject());
    assertEquals("Second event subject should match", "Lunch", event2.getSubject());
    assertEquals("Third event subject should match", "Dinner", event3.getSubject());

    // Verify calendar state
    assertFalse("Calendar should not be empty", calendarData.isEmpty());
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    List<IEvent> dayEvents = calendarData.get(testDate);
    assertNotNull("Day events list should not be null", dayEvents);
    assertEquals("Should have exactly three events", 3, dayEvents.size());

    // Verify all events are in the calendar
    assertTrue("Calendar should contain first event", dayEvents.contains(event1));
    assertTrue("Calendar should contain second event", dayEvents.contains(event2));
    assertTrue("Calendar should contain third event", dayEvents.contains(event3));

    // Test printing events
    String result = calendar.printEvents(testDate);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify all events are present in output
    assertTrue("Result should contain Morning Meeting", result.contains("Morning Meeting"));
    assertTrue("Result should contain Lunch", result.contains("Lunch"));
    assertTrue("Result should contain Dinner", result.contains("Dinner"));

    // Verify line count
    String[] lines = result.split("\n");
    assertEquals("Should have exactly three lines of output", 3, lines.length);

    // Verify each line contains required information
    for (String line : lines) {
      assertNotNull("Each line should not be null", line);
      assertFalse("Each line should not be empty", line.trim().isEmpty());
      assertTrue("Each line should contain Start Time", line.contains("Start Time:"));
      assertTrue("Each line should contain End Time", line.contains("End Time:"));
      assertTrue("Each line should contain Location", line.contains("Location:"));
    }
  }

  @Test
  public void testPrintEventsOnDateAllDayEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create all-day event (no end time specified)
    Event allDayEvent = calendar.createEvent("Holiday", morning, null);

    // Verify event creation and all-day properties
    assertNotNull("All-day event should be created", allDayEvent);
    assertEquals("Event subject should match", "Holiday", allDayEvent.getSubject());

    // Verify all-day time boundaries
    assertEquals("All-day start should be 8:00 AM", 8, allDayEvent.getStart().getHour());
    assertEquals("All-day start minutes should be 0", 0, allDayEvent.getStart().getMinute());
    assertEquals("All-day end should be 5:00 PM", 17, allDayEvent.getEnd().getHour());
    assertEquals("All-day end minutes should be 0", 0, allDayEvent.getEnd().getMinute());
    assertEquals("All-day event should be on same date", testDate,
            allDayEvent.getStart().toLocalDate());
    assertEquals("All-day event end should be on same date", testDate,
            allDayEvent.getEnd().toLocalDate());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event on test date", 1, calendarData.get(testDate).size());

    // Test printing events
    String result = calendar.printEvents(testDate);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify output content
    assertTrue("Result should contain Holiday", result.contains("Holiday"));
    assertTrue("Result should show 8:00 start time", result.contains("T08:00"));
    assertTrue("Result should show 17:00 end time", result.contains("T17:00"));
    assertTrue("Result should contain location", result.contains("Location: ONLINE"));

    // Verify single line output
    String[] lines = result.split("\n");
    assertEquals("Should have exactly one line of output", 1, lines.length);
  }

  @Test
  public void testPrintEventsIntervalNoEvents() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Define interval
    LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(testDate.plusDays(2), java.time.LocalTime.of(18, 0));

    assertNotNull("Start time should not be null", start);
    assertNotNull("End time should not be null", end);
    assertTrue("End should be after start", end.isAfter(start));

    // Test interval with no events
    String result = calendar.printEventsInterval(start, end);
    assertNotNull("Result should not be null", result);
    assertEquals("Should return empty string for no events", "", result);

    // Verify calendar remains empty
    assertTrue("Calendar should still be empty", calendar.getCalendar().isEmpty());
  }

  @Test
  public void testPrintEventsIntervalSingleDay() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create events
    LocalDateTime morning10 = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime afternoon15 = LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0));

    Event event1 = calendar.createEvent("Meeting 1", morning, morning10);
    Event event2 = calendar.createEvent("Meeting 2", afternoon, afternoon15);

    // Verify event creation
    assertNotNull("First event should be created", event1);
    assertNotNull("Second event should be created", event2);

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have two events", 2, calendarData.get(testDate).size());

    // Define interval that covers the entire day
    LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(testDate, java.time.LocalTime.of(23, 59));

    assertTrue("Interval should cover morning event", !event1.getStart().isBefore(start)
            && event1.getStart().isBefore(end));
    assertTrue("Interval should cover afternoon event", !event2.getStart().isBefore(start)
            && event2.getStart().isBefore(end));

    // Test interval
    String result = calendar.printEventsInterval(start, end);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify content
    assertTrue("Result should contain Meeting 1", result.contains("Meeting 1"));
    assertTrue("Result should contain Meeting 2", result.contains("Meeting 2"));

    // Verify line count
    String[] lines = result.split("\n");
    assertEquals("Should have two lines of output", 2, lines.length);
  }

  @Test
  public void testPrintEventsIntervalMultipleDays() {
    // Define test dates
    LocalDate day1 = testDate;
    LocalDate day2 = testDate.plusDays(1);
    LocalDate day3 = testDate.plusDays(2);

    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create events across multiple days
    Event event1 = calendar.createEvent("Day 1 Event",
            LocalDateTime.of(day1, java.time.LocalTime.of(9, 0)),
            LocalDateTime.of(day1, java.time.LocalTime.of(10, 0)));
    Event event2 = calendar.createEvent("Day 2 Event",
            LocalDateTime.of(day2, java.time.LocalTime.of(14, 0)),
            LocalDateTime.of(day2, java.time.LocalTime.of(15, 0)));
    Event event3 = calendar.createEvent("Day 3 Event",
            LocalDateTime.of(day3, java.time.LocalTime.of(16, 0)),
            LocalDateTime.of(day3, java.time.LocalTime.of(17, 0)));

    // Verify event creation
    assertNotNull("Day 1 event should be created", event1);
    assertNotNull("Day 2 event should be created", event2);
    assertNotNull("Day 3 event should be created", event3);

    assertEquals("Day 1 event should be on correct date", day1, event1.getStart().toLocalDate());
    assertEquals("Day 2 event should be on correct date", day2, event2.getStart().toLocalDate());
    assertEquals("Day 3 event should be on correct date", day3, event3.getStart().toLocalDate());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain day 1", calendarData.containsKey(day1));
    assertTrue("Calendar should contain day 2", calendarData.containsKey(day2));
    assertTrue("Calendar should contain day 3", calendarData.containsKey(day3));
    assertEquals("Day 1 should have one event", 1, calendarData.get(day1).size());
    assertEquals("Day 2 should have one event", 1, calendarData.get(day2).size());
    assertEquals("Day 3 should have one event", 1, calendarData.get(day3).size());

    // Define interval covering all three days
    LocalDateTime start = LocalDateTime.of(day1, java.time.LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(day3, java.time.LocalTime.of(18, 0));

    // Test interval
    String result = calendar.printEventsInterval(start, end);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify all events are included
    assertTrue("Result should contain Day 1 Event", result.contains("Day 1 Event"));
    assertTrue("Result should contain Day 2 Event", result.contains("Day 2 Event"));
    assertTrue("Result should contain Day 3 Event", result.contains("Day 3 Event"));

    // Verify line count
    String[] lines = result.split("\n");
    assertEquals("Should have three lines of output", 3, lines.length);
  }

  @Test
  public void testPrintEventsIntervalFilterByStartTime() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create events before and after the filter time
    Event earlyEvent = calendar.createEvent("Early Event",
            LocalDateTime.of(testDate, java.time.LocalTime.of(7, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0)));
    Event lateEvent = calendar.createEvent("Late Event",
            LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(16, 0)));

    // Verify event creation
    assertNotNull("Early event should be created", earlyEvent);
    assertNotNull("Late event should be created", lateEvent);
    assertEquals("Early event should start at 7:00", 7, earlyEvent.getStart().getHour());
    assertEquals("Late event should start at 15:00", 15, lateEvent.getStart().getHour());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have two events", 2, calendarData.get(testDate).size());

    // Filter should exclude events that start before 10:00
    LocalDateTime filterStart = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime filterEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(18, 0));

    // Verify filter logic
    assertTrue("Early event should start before filter",
            earlyEvent.getStart().isBefore(filterStart));
    assertFalse("Late event should not start before filter",
            lateEvent.getStart().isBefore(filterStart));

    // Test filtered interval
    String result = calendar.printEventsInterval(filterStart, filterEnd);
    assertNotNull("Result should not be null", result);

    // Verify filtering worked correctly
    assertFalse("Result should not contain Early Event", result.contains("Early Event"));
    assertTrue("Result should contain Late Event", result.contains("Late Event"));

    // Verify only one line in output
    String[] lines = result.split("\n");
    assertEquals("Should have exactly one line of output", 1, lines.length);
  }

  @Test
  public void testPrintEventsMultiDayEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create a multi-day event
    LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    LocalDateTime end = LocalDateTime.of(testDate.plusDays(2), java.time.LocalTime.of(17, 0));

    Event multiDayEvent = calendar.createEvent("Conference", start, end);

    // Verify event creation
    assertNotNull("Multi-day event should be created", multiDayEvent);
    assertEquals("Event subject should match", "Conference", multiDayEvent.getSubject());
    assertEquals("Event start should match", start, multiDayEvent.getStart());
    assertEquals("Event end should match", end, multiDayEvent.getEnd());


    // Verify the event spans multiple days
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    assertNotEquals("Start and end dates should be different", startDate, endDate);
    assertEquals("Event should span 3 days", 2, endDate.toEpochDay() - startDate.toEpochDay());

    // Verify calendar state - event should appear on all days it spans
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain start date", calendarData.containsKey(testDate));
    assertTrue("Calendar should contain middle date",
            calendarData.containsKey(testDate.plusDays(1)));
    assertTrue("Calendar should contain end date", calendarData.containsKey(testDate.plusDays(2)));

    // Verify event appears on each day
    assertEquals("Start date should have one event", 1, calendarData.get(testDate).size());
    assertEquals("Middle date should have one event", 1,
            calendarData.get(testDate.plusDays(1)).size());
    assertEquals("End date should have one event", 1,
            calendarData.get(testDate.plusDays(2)).size());

    // Verify it's the same event object on all days
    assertEquals("Should be same event on start date", multiDayEvent,
            calendarData.get(testDate).get(0));
    assertEquals("Should be same event on middle date", multiDayEvent,
            calendarData.get(testDate.plusDays(1)).get(0));
    assertEquals("Should be same event on end date", multiDayEvent,
            calendarData.get(testDate.plusDays(2)).get(0));

    // Test printing on each day
    String day1Result = calendar.printEvents(testDate);
    String day2Result = calendar.printEvents(testDate.plusDays(1));
    String day3Result = calendar.printEvents(testDate.plusDays(2));

    // Verify event appears on all days
    assertNotNull("Day 1 result should not be null", day1Result);
    assertNotNull("Day 2 result should not be null", day2Result);
    assertNotNull("Day 3 result should not be null", day3Result);

    assertTrue("Conference should appear on day 1", day1Result.contains("Conference"));
    assertTrue("Conference should appear on day 2", day2Result.contains("Conference"));
    assertTrue("Conference should appear on day 3", day3Result.contains("Conference"));

    // Verify each result has exactly one line
    assertEquals("Day 1 should have one line", 1, day1Result.split("\n").length);
    assertEquals("Day 2 should have one line", 1, day2Result.split("\n").length);
    assertEquals("Day 3 should have one line", 1, day3Result.split("\n").length);
  }

  @Test
  public void testPrintEventsFromSeries() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());
    assertTrue("Series should be empty initially", calendar.getSeries().isEmpty());

    // Create event series
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    LocalDateTime seriesStart = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    LocalDateTime seriesEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    calendar.createSeriesTimes("Weekly Meeting", seriesStart, seriesEnd, repeatDays, 3);

    // Verify series creation
    Map<LocalDateTime, List<IEvent>> seriesData = calendar.getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());
    assertTrue("Series should contain the start time", seriesData.containsKey(seriesStart));

    List<IEvent> seriesEvents = seriesData.get(seriesStart);
    assertNotNull("Series events should not be null", seriesEvents);
    assertFalse("Series should have events", seriesEvents.isEmpty());

    // Calculate expected dates (M, W, F starting from testDate which is Sunday)
    LocalDate monday = testDate.plusDays(1);      // June 16, 2025 (Monday)
    LocalDate wednesday = testDate.plusDays(3);   // June 18, 2025 (Wednesday)
    LocalDate friday = testDate.plusDays(5);      // June 20, 2025 (Friday)

    // Verify calendar contains the expected dates
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));
    assertTrue("Calendar should contain Friday", calendarData.containsKey(friday));

    // Test printing on different days
    String mondayResult = calendar.printEvents(monday);
    String wednesdayResult = calendar.printEvents(wednesday);
    String fridayResult = calendar.printEvents(friday);
    String sundayResult = calendar.printEvents(testDate); // Should be empty

    // Verify results
    assertNotNull("Monday result should not be null", mondayResult);
    assertNotNull("Wednesday result should not be null", wednesdayResult);
    assertNotNull("Friday result should not be null", fridayResult);
    assertNotNull("Sunday result should not be null", sundayResult);

    assertTrue("Monday should have Weekly Meeting", mondayResult.contains("Weekly Meeting"));
    assertTrue("Wednesday should have Weekly Meeting", wednesdayResult.contains("Weekly Meeting"));
    assertTrue("Friday should have Weekly Meeting", fridayResult.contains("Weekly Meeting"));
    assertEquals("Sunday should have no events", "No events on this day", sundayResult);

    // Verify each result has exactly one line
    assertEquals("Monday should have one event", 1, mondayResult.split("\n").length);
    assertEquals("Wednesday should have one event", 1, wednesdayResult.split("\n").length);
    assertEquals("Friday should have one event", 1, fridayResult.split("\n").length);

    // Verify time consistency across series
    assertTrue("Monday event should have correct time", mondayResult.contains("T09:00"));
    assertTrue("Wednesday event should have correct time", wednesdayResult.contains("T09:00"));
    assertTrue("Friday event should have correct time", fridayResult.contains("T09:00"));
  }

  // ==================== STATUS CHECKING TESTS ====================

  @Test
  public void testShowStatusAvailableNoEvents() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime queryTime = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    assertNotNull("Query time should not be null", queryTime);

    // Test status with no events
    String status = calendar.showStatus(queryTime);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be available", "available", status);

    // Verify calendar remains empty
    assertTrue("Calendar should still be empty", calendar.getCalendar().isEmpty());
  }

  @Test
  public void testShowStatusBusyAtEventStart() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = morning;
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    // Create event
    Event event = calendar.createEvent("Meeting", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertEquals("Event start should match", eventStart, event.getStart());
    assertEquals("Event end should match", eventEnd, event.getEnd());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event", 1, calendarData.get(testDate).size());

    // Test status at event start time
    String status = calendar.showStatus(eventStart);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be busy at event start", "busy", status);
  }

  @Test
  public void testShowStatusBusyDuringEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = morning;
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(11, 0));
    LocalDateTime queryTime = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 30));

    // Verify query time is during event
    assertFalse("Query time should be after event start", queryTime.isBefore(eventStart));
    assertTrue("Query time should be before event end", queryTime.isBefore(eventEnd));

    // Create event
    Event event = calendar.createEvent("Meeting", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertEquals("Event duration should be 2 hours", 2, eventEnd.getHour() - eventStart.getHour());

    // Test status during event
    String status = calendar.showStatus(queryTime);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be busy during event", "busy", status);
  }

  @Test
  public void testShowStatusAvailableAtEventEnd() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = morning;
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    // Create event
    Event event = calendar.createEvent("Meeting", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertTrue("Event end should be after start", eventEnd.isAfter(eventStart));

    // Test status at event end time
    String status = calendar.showStatus(eventEnd);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be available at event end", "available", status);
  }

  @Test
  public void testShowStatusAvailableBeforeEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = morning;
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime queryTime = LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0));

    // Verify timing
    assertTrue("Query time should be before event start", queryTime.isBefore(eventStart));

    // Create event
    Event event = calendar.createEvent("Meeting", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);

    // Test status before event
    String status = calendar.showStatus(queryTime);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be available before event", "available", status);
  }

  @Test
  public void testShowStatusAvailableAfterEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = morning;
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime queryTime = LocalDateTime.of(testDate, java.time.LocalTime.of(11, 0));

    // Verify timing
    assertTrue("Query time should be after event end", queryTime.isAfter(eventEnd));

    // Create event
    Event event = calendar.createEvent("Meeting", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);

    // Test status after event
    String status = calendar.showStatus(queryTime);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be available after event", "available", status);
  }

  @Test
  public void testShowStatusBusyWithOverlappingEvents() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create overlapping events
    LocalDateTime meeting1Start = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    LocalDateTime meeting1End = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 30));
    LocalDateTime meeting2Start = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime meeting2End = LocalDateTime.of(testDate, java.time.LocalTime.of(11, 0));

    // Verify overlap
    assertTrue("Meetings should overlap", meeting1End.isAfter(meeting2Start));

    Event event1 = calendar.createEvent("Meeting 1", meeting1Start, meeting1End);
    Event event2 = calendar.createEvent("Meeting 2", meeting2Start, meeting2End);

    // Verify event creation
    assertNotNull("First event should be created", event1);
    assertNotNull("Second event should be created", event2);

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have two events", 2, calendarData.get(testDate).size());

    // Test status during overlap period
    LocalDateTime queryTime = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 15));

    // Verify query time is in both events
    assertTrue("Query should be during meeting 1", !queryTime.isBefore(meeting1Start)
            && queryTime.isBefore(meeting1End));
    assertTrue("Query should be during meeting 2", !queryTime.isBefore(meeting2Start)
            && queryTime.isBefore(meeting2End));

    String status = calendar.showStatus(queryTime);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be busy during overlap", "busy", status);
  }

  @Test
  public void testShowStatusBusyWithAllDayEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create all-day event (8am-5pm)
    Event allDayEvent = calendar.createEvent("Holiday", morning, null);

    // Verify event creation and all-day properties
    assertNotNull("All-day event should be created", allDayEvent);
    assertEquals("All-day should start at 8:00", 8, allDayEvent.getStart().getHour());
    assertEquals("All-day should end at 17:00", 17, allDayEvent.getEnd().getHour());

    // Test various times during the day
    LocalDateTime midMorning = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));
    LocalDateTime noon = LocalDateTime.of(testDate, java.time.LocalTime.of(12, 0));
    LocalDateTime midAfternoon = LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0));
    LocalDateTime evening = LocalDateTime.of(testDate, java.time.LocalTime.of(18, 0));

    // Verify times are within/outside all-day event
    assertTrue("Mid-morning should be during all-day event",
            !midMorning.isBefore(allDayEvent.getStart())
                    && midMorning.isBefore(allDayEvent.getEnd()));
    assertTrue("Noon should be during all-day event",
            !noon.isBefore(allDayEvent.getStart()) && noon.isBefore(allDayEvent.getEnd()));
    assertTrue("Mid-afternoon should be during all-day event",
            !midAfternoon.isBefore(allDayEvent.getStart())
                    && midAfternoon.isBefore(allDayEvent.getEnd()));
    assertFalse("Evening should be after all-day event",
            !evening.isBefore(allDayEvent.getStart()) && evening.isBefore(allDayEvent.getEnd()));

    // Test status at different times
    assertEquals("Should be busy at mid-morning", "busy", calendar.showStatus(midMorning));
    assertEquals("Should be busy at noon", "busy", calendar.showStatus(noon));
    assertEquals("Should be busy at mid-afternoon", "busy", calendar.showStatus(midAfternoon));
    assertEquals("Should be available in evening", "available", calendar.showStatus(evening));

    // Test boundary conditions
    assertEquals("Should be busy at start", "busy", calendar.showStatus(allDayEvent.getStart()));
    assertEquals("Should be available at end", "available",
            calendar.showStatus(allDayEvent.getEnd()));
  }

  @Test
  public void testShowStatusBusyWithMultiDayEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create multi-day event
    LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    LocalDateTime end = LocalDateTime.of(testDate.plusDays(2), java.time.LocalTime.of(17, 0));

    Event multiDayEvent = calendar.createEvent("Conference", start, end);

    // Verify event creation
    assertNotNull("Multi-day event should be created", multiDayEvent);
    assertNotEquals("Start and end should be on different days",
            start.toLocalDate(), end.toLocalDate());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain start date", calendarData.containsKey(testDate));
    assertTrue("Calendar should contain middle date",
            calendarData.containsKey(testDate.plusDays(1)));
    assertTrue("Calendar should contain end date", calendarData.containsKey(testDate.plusDays(2)));

    // Test status on middle day
    LocalDateTime middleDayQuery = LocalDateTime.of(testDate.plusDays(1),
            java.time.LocalTime.of(12, 0));

    // Verify middle day is between start and end
    assertTrue("Middle day should be after start", middleDayQuery.isAfter(start));
    assertTrue("Middle day should be before end", middleDayQuery.isBefore(end));

    String status = calendar.showStatus(middleDayQuery);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be busy on middle day", "busy", status);

    // Test status on start day
    LocalDateTime startDayQuery = LocalDateTime.of(testDate, java.time.LocalTime.of(12, 0));
    assertEquals("Should be busy on start day", "busy", calendar.showStatus(startDayQuery));

    // Test status on end day
    LocalDateTime endDayQuery = LocalDateTime.of(testDate.plusDays(2),
            java.time.LocalTime.of(12, 0));
    assertEquals("Should be busy on end day", "busy", calendar.showStatus(endDayQuery));
  }

  @Test
  public void testShowStatusAvailableOnDifferentDate() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create event
    Event event = calendar.createEvent("Meeting", morning,
            LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0)));

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertEquals("Event should be on test date", testDate, event.getStart().toLocalDate());

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertFalse("Calendar should not contain different date",
            calendarData.containsKey(testDate.plusDays(1)));

    // Query on a different date
    LocalDateTime differentDate = LocalDateTime.of(testDate.plusDays(1),
            java.time.LocalTime.of(9, 0));

    // Verify different date
    assertNotEquals("Query date should be different", testDate, differentDate.toLocalDate());

    String status = calendar.showStatus(differentDate);
    assertNotNull("Status should not be null", status);
    assertEquals("Status should be available on different date", "available", status);
  }

  @Test
  public void testShowStatusBusyWithRecurringEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());
    assertTrue("Series should be empty initially", calendar.getSeries().isEmpty());

    // Create recurring event
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    LocalDateTime seriesStart = LocalDateTime.of(testDate.plusDays(1),
            java.time.LocalTime.of(9, 0)); // Monday
    LocalDateTime seriesEnd = LocalDateTime.of(testDate.plusDays(1), java.time.LocalTime.of(10, 0));

    calendar.createSeriesTimes("Weekly Meeting", seriesStart, seriesEnd, repeatDays, 3);

    // Verify series creation
    Map<LocalDateTime, List<IEvent>> seriesData = calendar.getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());
    assertTrue("Series should contain start time", seriesData.containsKey(seriesStart));

    // Calculate dates
    LocalDate monday = testDate.plusDays(1);
    LocalDate wednesday = testDate.plusDays(3);
    LocalDate friday = testDate.plusDays(5);
    LocalDate tuesday = testDate.plusDays(2);

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));
    assertTrue("Calendar should contain Friday", calendarData.containsKey(friday));
    assertFalse("Calendar should not contain Tuesday", calendarData.containsKey(tuesday));

    // Test status on recurring event days
    LocalDateTime mondayQuery = LocalDateTime.of(monday, java.time.LocalTime.of(9, 30));
    LocalDateTime wednesdayQuery = LocalDateTime.of(wednesday, java.time.LocalTime.of(9, 30));
    LocalDateTime tuesdayQuery = LocalDateTime.of(tuesday, java.time.LocalTime.of(9, 30));

    // Verify query times are during event hours
    assertTrue("Monday query should be during event", mondayQuery.getHour() == 9
            && mondayQuery.getMinute() == 30);
    assertTrue("Wednesday query should be during event",
            wednesdayQuery.getHour() == 9 && wednesdayQuery.getMinute() == 30);

    String mondayStatus = calendar.showStatus(mondayQuery);
    String wednesdayStatus = calendar.showStatus(wednesdayQuery);
    String tuesdayStatus = calendar.showStatus(tuesdayQuery);

    // Verify status results
    assertNotNull("Monday status should not be null", mondayStatus);
    assertNotNull("Wednesday status should not be null", wednesdayStatus);
    assertNotNull("Tuesday status should not be null", tuesdayStatus);

    assertEquals("Should be busy on Monday", "busy", mondayStatus);
    assertEquals("Should be busy on Wednesday", "busy", wednesdayStatus);
    assertEquals("Should be available on Tuesday", "available", tuesdayStatus);
  }

  // ==================== EDGE CASES AND ERROR CONDITIONS ====================

  @Test
  public void testPrintEventsEmptyCalendar() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Test multiple dates
    String result1 = calendar.printEvents(testDate);
    String result2 = calendar.printEvents(testDate.plusDays(1));
    String result3 = calendar.printEvents(testDate.minusDays(1));

    // Verify all results
    assertNotNull("Result 1 should not be null", result1);
    assertNotNull("Result 2 should not be null", result2);
    assertNotNull("Result 3 should not be null", result3);

    assertEquals("All results should be no events message", "No events on this day", result1);
    assertEquals("All results should be no events message", "No events on this day", result2);
    assertEquals("All results should be no events message", "No events on this day", result3);

    // Verify calendar remains empty
    assertTrue("Calendar should still be empty", calendar.getCalendar().isEmpty());
  }

  @Test
  public void testPrintEventsIntervalInvalidRange() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create invalid range (start after end)
    LocalDateTime start = LocalDateTime.of(testDate.plusDays(1), java.time.LocalTime.of(9, 0));
    LocalDateTime end = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));

    // Verify range is invalid
    assertTrue("Start should be after end", start.isAfter(end));

    // Test with invalid range
    String result = calendar.printEventsInterval(start, end);

    // Should handle gracefully
    assertNotNull("Result should not be null", result);
    assertEquals("Should return empty result for invalid range", "", result);
  }

  @Test
  public void testShowStatusLeapYear() {
    // Test with leap year date
    LocalDate leapDate = LocalDate.of(2024, 2, 29);
    LocalDateTime leapQuery = LocalDateTime.of(leapDate, java.time.LocalTime.of(12, 0));

    // Verify leap date is valid
    assertNotNull("Leap date should be valid", leapDate);
    assertEquals("Should be February 29", 29, leapDate.getDayOfMonth());
    assertEquals("Should be February", 2, leapDate.getMonthValue());
    assertEquals("Should be 2024", 2024, leapDate.getYear());

    // Test status on leap year date
    String status = calendar.showStatus(leapQuery);
    assertNotNull("Status should not be null", status);
    assertEquals("Should be available on leap year date", "available", status);
  }

  @Test
  public void testPrintEventsWithDifferentLocations() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Create online event
    Event onlineEvent = calendar.createEvent("Online Meeting", morning,
            LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0)));

    // Verify online event
    assertNotNull("Online event should be created", onlineEvent);
    assertEquals("Default location should be ONLINE", Location.ONLINE, onlineEvent.getLocation());

    // Create physical event using builder
    Event physicalEvent = new Event.EventBuilder("In-Person Meeting", afternoon)
            .end(LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0)))
            .location(Location.PHYSICAL)
            .build();

    // Verify physical event
    assertNotNull("Physical event should be created", physicalEvent);
    assertEquals("Location should be PHYSICAL", Location.PHYSICAL, physicalEvent.getLocation());

    // Add physical event to calendar manually
    calendar.getCalendar().computeIfAbsent(testDate,
            k -> new java.util.ArrayList<>()).add(physicalEvent);

    // Verify calendar state
    Map<LocalDate, List<IEvent>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have two events", 2, calendarData.get(testDate).size());

    // Test printing
    String result = calendar.printEvents(testDate);
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());

    // Verify both locations appear
    assertTrue("Should contain ONLINE location", result.contains("Location: ONLINE"));
    assertTrue("Should contain PHYSICAL location", result.contains("Location: PHYSICAL"));

    // Verify both event names appear
    assertTrue("Should contain Online Meeting", result.contains("Online Meeting"));
    assertTrue("Should contain In-Person Meeting", result.contains("In-Person Meeting"));

    // Verify line count
    String[] lines = result.split("\n");
    assertEquals("Should have two lines", 2, lines.length);
  }

  @Test
  public void testStatusBoundaryConditions() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    LocalDateTime eventStart = LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0));
    LocalDateTime eventEnd = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    // Create event
    Event event = calendar.createEvent("Boundary Test", eventStart, eventEnd);

    // Verify event creation
    assertNotNull("Event should be created", event);
    assertEquals("Event should have 1 hour duration", 1, eventEnd.getHour() - eventStart.getHour());

    // Test exact boundary conditions
    String startStatus = calendar.showStatus(eventStart);
    String endStatus = calendar.showStatus(eventEnd);

    assertNotNull("Start status should not be null", startStatus);
    assertNotNull("End status should not be null", endStatus);

    assertEquals("Should be busy at event start", "busy", startStatus);
    assertEquals("Should be available at event end", "available", endStatus);

    // Test one minute before and after
    LocalDateTime beforeStart = eventStart.minusMinutes(1);
    LocalDateTime afterEnd = eventEnd.plusMinutes(1);

    String beforeStatus = calendar.showStatus(beforeStart);
    String afterStatus = calendar.showStatus(afterEnd);

    assertNotNull("Before status should not be null", beforeStatus);
    assertNotNull("After status should not be null", afterStatus);

    assertEquals("Should be available before event", "available", beforeStatus);
    assertEquals("Should be available after event", "available", afterStatus);

    // Verify timing relationships
    assertTrue("Before time should be before start", beforeStart.isBefore(eventStart));
    assertTrue("After time should be after end", afterEnd.isAfter(eventEnd));
    assertEquals("Before and start should differ by 1 minute", 1,
            java.time.Duration.between(beforeStart, eventStart).toMinutes());
    assertEquals("End and after should differ by 1 minute", 1,
            java.time.Duration.between(eventEnd, afterEnd).toMinutes());
  }
}