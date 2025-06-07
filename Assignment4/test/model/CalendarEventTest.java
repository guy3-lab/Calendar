package model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import model.calendar.Calendar;
import model.calendar.Event;


/**
 * Comprehensive test suite for Calendar class event creation functionality.
 * Tests single event creation, series creation, and duplicate prevention.
 */
public class CalendarEventTest {

  private Calendar calendar;
  private LocalDateTime testStart;
  private LocalDateTime testEnd;
  private LocalDate testDate;
  private LocalDateTime allDayStart;

  @Before
  public void setUp() {
    calendar = new Calendar();
    testStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    testEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    testDate = LocalDate.of(2025, 6, 15);
    allDayStart = LocalDateTime.of(2025, 6, 15, 14, 30); // Any time for all-day
  }

  // ==================== SINGLE EVENT CREATION TESTS ====================

  @Test
  public void testCreateBasicTimedEvent() {
    Event event = calendar.createEvent("Team Meeting", testStart, testEnd);

    // Verify event properties
    assertNotNull("Event should be created", event);
    assertEquals("Subject should match", "Team Meeting", event.getSubject());
    assertEquals("Start time should match", testStart, event.getStart());
    assertEquals("End time should match", testEnd, event.getEnd());
    assertFalse("Should not be all-day event", event.isAllDay());

    // Verify event is in calendar
    assertTrue("Calendar should contain the date", calendar.getCalendar().containsKey(testDate));
    List<Event> dayEvents = calendar.getCalendar().get(testDate);
    assertNotNull("Day events list should not be null", dayEvents);
    assertEquals("Should have exactly one event", 1, dayEvents.size());
    assertSame("Should be the same event object", event, dayEvents.get(0));
  }

  @Test
  public void testCreateAllDayEvent() {
    Event event = calendar.createEvent("Conference", allDayStart, null);

    // Verify all-day characteristics
    assertNotNull("Event should be created", event);
    assertEquals("Subject should match", "Conference", event.getSubject());
    assertTrue("Should be all-day event", event.isAllDay());
    assertEquals("Should start at 8:00 AM", 8, event.getStart().getHour());
    assertEquals("Should end at 5:00 PM", 17, event.getEnd().getHour());
    assertEquals("Should be on correct date", testDate, event.getStart().toLocalDate());

    // Verify in calendar
    assertTrue("Calendar should contain the date", calendar.getCalendar().containsKey(testDate));
    assertEquals("Should have one event", 1, calendar.getCalendar().get(testDate).size());
  }

//  @Test
//  public void testCreateEventWithAllProperties() {
//    Event event = calendar.createEvent("Important Meeting", testStart, testEnd,
//            "Quarterly business review", Location.PHYSICAL, Status.PRIVATE);
//
//    // Verify all properties
//    assertEquals("Subject should match", "Important Meeting", event.getSubject());
//    assertEquals("Start should match", testStart, event.getStart());
//    assertEquals("End should match", testEnd, event.getEnd());
//    assertEquals("Description should match", "Quarterly business review", event.getDesc());
//    assertEquals("Location should match", Location.PHYSICAL, event.getLocation());
//    assertEquals("Status should match", Status.PRIVATE, event.getStatus());
//
//    // Verify in calendar
//    assertTrue("Calendar should contain event", calendar.getCalendar().containsKey(testDate));
//  }

//  @Test
//  public void testCreateEventWithPartialProperties() {
//    Event event = calendar.createEvent("Meeting", testStart, testEnd,
//            "Test description", null, null);
//
//    // Verify provided properties
//    assertEquals("Description should be set", "Test description", event.getDesc());
//
//    // Verify defaults for null properties
//    assertEquals("Location should default to ONLINE", Location.ONLINE, event.getLocation());
//    assertEquals("Status should default to PUBLIC", Status.PUBLIC, event.getStatus());
//  }

  @Test
  public void testCreateMultiDayEvent() {
    LocalDateTime multiDayEnd = testStart.plusDays(2).plusHours(3);
    Event event = calendar.createEvent("Multi-day Conference", testStart, multiDayEnd);

    // Verify event properties
    assertEquals("Start should be preserved", testStart, event.getStart());
    assertEquals("End should be preserved", multiDayEnd, event.getEnd());
    assertNotEquals("Start and end dates should differ",
            event.getStart().toLocalDate(), event.getEnd().toLocalDate());

    // Verify event appears on all spanned days
    LocalDate day1 = testDate;
    LocalDate day2 = testDate.plusDays(1);
    LocalDate day3 = testDate.plusDays(2);

    assertTrue("Should appear on day 1", calendar.getCalendar().containsKey(day1));
    assertTrue("Should appear on day 2", calendar.getCalendar().containsKey(day2));
    assertTrue("Should appear on day 3", calendar.getCalendar().containsKey(day3));

    // Verify same event object on all days
    Event day1Event = calendar.getCalendar().get(day1).get(0);
    Event day2Event = calendar.getCalendar().get(day2).get(0);
    Event day3Event = calendar.getCalendar().get(day3).get(0);

    assertSame("Should be same object on day 1", event, day1Event);
    assertSame("Should be same object on day 2", event, day2Event);
    assertSame("Should be same object on day 3", event, day3Event);
  }

  @Test
  public void testCreateMultipleEventsOnSameDay() {
    Event event1 = calendar.createEvent("Morning Meeting", testStart, testEnd);
    Event event2 = calendar.createEvent("Afternoon Meeting",
            testStart.plusHours(4), testEnd.plusHours(4));
    Event event3 = calendar.createEvent("Evening Meeting",
            testStart.plusHours(8), testEnd.plusHours(8));

    // Verify all events exist
    assertNotNull("Event 1 should exist", event1);
    assertNotNull("Event 2 should exist", event2);
    assertNotNull("Event 3 should exist", event3);

    // Verify all are in calendar on same day
    assertTrue("Calendar should contain the date", calendar.getCalendar().containsKey(testDate));
    List<Event> dayEvents = calendar.getCalendar().get(testDate);
    assertEquals("Should have 3 events", 3, dayEvents.size());

    // Verify all events are present
    assertTrue("Should contain event 1", dayEvents.contains(event1));
    assertTrue("Should contain event 2", dayEvents.contains(event2));
    assertTrue("Should contain event 3", dayEvents.contains(event3));
  }

  @Test
  public void testCreateEventWithInvalidEndTime() {
    LocalDateTime invalidEnd = testStart.minusHours(1);

    try {
      calendar.createEvent("Invalid Meeting", testStart, invalidEnd);
      fail("Should throw exception for end time before start time");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "End time must be after start time", e.getMessage());
    }

    // Verify no event was created
    assertFalse("Calendar should not contain date after failed creation",
            calendar.getCalendar().containsKey(testDate));
  }

  // ==================== DUPLICATE PREVENTION TESTS ====================

  @Test
  public void testPreventDuplicateSingleDayEvent() {
    // Create first event
    Event event1 = calendar.createEvent("Meeting", testStart, testEnd);
    assertNotNull("First event should be created", event1);

    // Try to create identical event
    try {
      calendar.createEvent("Meeting", testStart, testEnd);
      fail("Should throw exception for duplicate event");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "Event already exists", e.getMessage());
    }

    // Verify only one event exists
    assertEquals("Should still have only one event",
            1, calendar.getCalendar().get(testDate).size());
  }

  @Test
  public void testPreventDuplicateMultiDayEvent() {
    LocalDateTime multiDayEnd = testStart.plusDays(1);

    // Create first multi-day event
    Event event1 = calendar.createEvent("Conference", testStart, multiDayEnd);
    assertNotNull("First event should be created", event1);

    // Try to create identical multi-day event
    try {
      calendar.createEvent("Conference", testStart, multiDayEnd);
      fail("Should throw exception for duplicate multi-day event");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "Event already exists", e.getMessage());
    }

    // Verify only one event exists on each day
    assertEquals("Day 1 should have only one event",
            1, calendar.getCalendar().get(testDate).size());
    assertEquals("Day 2 should have only one event",
            1, calendar.getCalendar().get(testDate.plusDays(1)).size());
  }

  @Test
  public void testAllowSimilarEventsWithDifferentProperties() {
    // Create original event
    Event event1 = calendar.createEvent("Meeting", testStart, testEnd);

    // Different subject - should be allowed
    Event event2 = calendar.createEvent("Different Meeting", testStart, testEnd);
    assertNotNull("Event with different subject should be allowed", event2);

    // Different start time - should be allowed
    Event event3 = calendar.createEvent("Meeting", testStart.plusHours(1), testEnd.plusHours(1));
    assertNotNull("Event with different start time should be allowed", event3);

    // Different end time - should be allowed
    Event event4 = calendar.createEvent("Meeting", testStart, testEnd.plusHours(1));
    assertNotNull("Event with different end time should be allowed", event4);

    // Verify all events exist
    List<Event> dayEvents = calendar.getCalendar().get(testDate);
    assertEquals("Should have 4 different events", 4, dayEvents.size());
  }

  @Test
  public void testDuplicateDetectionAcrossMultipleDays() {
    LocalDateTime multiDayEnd = testStart.plusDays(2);

    // Create multi-day event
    calendar.createEvent("Long Conference", testStart, multiDayEnd);

    // Try to create event with same properties on any of the spanned days
    try {
      calendar.createEvent("Long Conference", testStart, multiDayEnd);
      fail("Should detect duplicate across multiple days");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "Event already exists", e.getMessage());
    }
  }

  @Test
  public void testAllowOverlappingEventsWithDifferentProperties() {
    // Create first event
    Event event1 = calendar.createEvent("Meeting 1", testStart, testEnd);

    // Create overlapping event with different subject
    LocalDateTime overlappingStart = testStart.plusMinutes(30);
    LocalDateTime overlappingEnd = testEnd.plusMinutes(30);
    Event event2 = calendar.createEvent("Meeting 2", overlappingStart, overlappingEnd);

    assertNotNull("Overlapping event should be allowed", event2);
    assertEquals("Should have 2 events on the day",
            2, calendar.getCalendar().get(testDate).size());
  }

  // ==================== EVENT SERIES CREATION TESTS ====================

  @Test
  public void testCreateSeriesWithTimesBasic() {
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    calendar.createSeriesTimes("Weekly Meeting", testStart, testEnd, repeatDays, 3);

    // Verify series is tracked
    assertTrue("Series should be tracked", calendar.getSeries().containsKey(testStart));
    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertNotNull("Series events should not be null", seriesEvents);
    assertEquals("Should create 9 events (3 days × 3 times)", 9, seriesEvents.size());

    // Verify events have correct subjects
    for (Event event : seriesEvents) {
      assertEquals("All events should have same subject", "Weekly Meeting", event.getSubject());
    }

    // Count total events in calendar
    long totalEvents = calendar.getCalendar().values().stream()
            .mapToLong(List::size)
            .sum();
    assertEquals("Calendar should contain 9 events total", 9, totalEvents);
  }

  @Test
  public void testCreateSeriesWithTimesValidation() {
    List<String> repeatDays = Arrays.asList("M", "T");

    // Test with specific occurrences
    calendar.createSeriesTimes("Daily Standup", testStart, testEnd, repeatDays, 4);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should create 8 events (2 days × 4 times)", 8, seriesEvents.size());

    // Verify events are spaced correctly (weekly)
    Event firstMonday = seriesEvents.get(0);
    Event secondMonday = seriesEvents.get(1);

    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
            firstMonday.getStart().toLocalDate(),
            secondMonday.getStart().toLocalDate());
    assertEquals("Monday events should be 7 days apart", 7, daysBetween);
  }

  @Test
  public void testCreateSeriesUntilDate() {
    List<String> repeatDays = Arrays.asList("M", "W");
    LocalDate until = testDate.plusWeeks(2);

    calendar.createSeriesUntil("Recurring Meeting", testStart, testEnd, repeatDays, until);

    // Verify series exists
    assertTrue("Series should be tracked", calendar.getSeries().containsKey(testStart));
    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertNotNull("Series events should not be null", seriesEvents);

    // Verify no events after until date
    for (Event event : seriesEvents) {
      assertFalse("No event should be after until date",
              event.getStart().toLocalDate().isAfter(until));
    }

    // Verify we have events for at least 3 weeks (including start week)
    assertTrue("Should have at least 4 events", seriesEvents.size() >= 4);
  }

//  @Test
//  public void testCreateSeriesWithAllProperties() {
//    List<String> repeatDays = Arrays.asList("T", "R");
//    calendar.createSeriesTimes("Team Sync", testStart, testEnd, repeatDays, 2,
//            "Weekly team synchronization", Location.PHYSICAL, Status.PRIVATE);
//
//    List<Event> seriesEvents = calendar.getSeries().get(testStart);
//    assertEquals("Should create 4 events (2 days × 2 times)", 4, seriesEvents.size());
//
//    // Verify all events have the specified properties
//    for (Event event : seriesEvents) {
//      assertEquals("Subject should match", "Team Sync", event.getSubject());
//      assertEquals("Description should match", "Weekly team synchronization", event.getDesc());
//      assertEquals("Location should match", Location.PHYSICAL, event.getLocation());
//      assertEquals("Status should match", Status.PRIVATE, event.getStatus());
//    }
//  }

  @Test
  public void testCreateAllDaySeriesWithTimes() {
    List<String> repeatDays = List.of("F");
    calendar.createSeriesTimes("Friday Off", allDayStart, null, repeatDays, 3);

    List<Event> seriesEvents = calendar.getSeries().get(allDayStart);
    assertEquals("Should create 3 all-day events", 3, seriesEvents.size());

    // Verify all events are all-day
    for (Event event : seriesEvents) {
      assertTrue("Each event should be all-day", event.isAllDay());
      assertEquals("Should start at 8:00 AM", 8, event.getStart().getHour());
      assertEquals("Should end at 5:00 PM", 17, event.getEnd().getHour());
    }
  }

  @Test
  public void testCreateAllDaySeriesUntilDate() {
    List<String> repeatDays = Arrays.asList("S", "U");
    LocalDate until = testDate.plusWeeks(2);

    calendar.createSeriesUntil("Weekend Events", allDayStart, null, repeatDays, until);

    List<Event> seriesEvents = calendar.getSeries().get(allDayStart);
    assertTrue("Should create multiple weekend events", seriesEvents.size() >= 2);

    for (Event event : seriesEvents) {
      assertTrue("Each event should be all-day", event.isAllDay());
    }
  }

  @Test
  public void testSeriesEventsHaveSameStartTime() {
    List<String> repeatDays = Arrays.asList("M", "T", "W", "R", "F");
    calendar.createSeriesTimes("Daily Meeting", testStart, testEnd, repeatDays, 2);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);

    // Verify all events have same time of day
    int expectedHour = testStart.getHour();
    int expectedMinute = testStart.getMinute();

    for (Event event : seriesEvents) {
      assertEquals("All events should have same start hour",
              expectedHour, event.getStart().getHour());
      assertEquals("All events should have same start minute",
              expectedMinute, event.getStart().getMinute());
      assertEquals("All events should have same end hour",
              testEnd.getHour(), event.getEnd().getHour());
      assertEquals("All events should have same end minute",
              testEnd.getMinute(), event.getEnd().getMinute());
    }
  }

  @Test
  public void testSeriesEventsAreOnCorrectDays() {
    // Test with Monday (M=1), Wednesday (W=3), Friday (F=5)
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    calendar.createSeriesTimes("MWF Meeting", testStart, testEnd, repeatDays, 2);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 6 events (3 days × 2 weeks)", 6, seriesEvents.size());

    // Verify days of week
    for (Event event : seriesEvents) {
      int dayOfWeek = event.getStart().getDayOfWeek().getValue();
      assertTrue("Event should be on Monday, Wednesday, or Friday",
              dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 5);
    }
  }

  @Test
  public void testCreateSeriesWithSingleDay() {
    List<String> repeatDays = List.of("W");
    calendar.createSeriesTimes("Wednesday Meeting", testStart, testEnd, repeatDays, 4);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should create 4 Wednesday events", 4, seriesEvents.size());

    // Verify all are on Wednesday (day 3)
    for (Event event : seriesEvents) {
      assertEquals("All events should be on Wednesday",
              3, event.getStart().getDayOfWeek().getValue());
    }

    // Verify they are spaced 7 days apart
    for (int i = 1; i < seriesEvents.size(); i++) {
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
              seriesEvents.get(i - 1).getStart().toLocalDate(),
              seriesEvents.get(i).getStart().toLocalDate());
      assertEquals("Events should be 7 days apart", 7, daysBetween);
    }
  }

  @Test
  public void testCreateSeriesWithAllDaysOfWeek() {
    List<String> repeatDays = Arrays.asList("M", "T", "W", "R", "F", "S", "U");
    calendar.createSeriesTimes("Daily Events", testStart, testEnd, repeatDays, 1);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should create 7 events (one for each day)", 7, seriesEvents.size());

    // Verify we have all days of the week
    boolean[] daysFound = new boolean[8]; // Index 1-7 for Monday-Sunday
    for (Event event : seriesEvents) {
      int dayOfWeek = event.getStart().getDayOfWeek().getValue();
      daysFound[dayOfWeek] = true;
    }

    for (int i = 1; i <= 7; i++) {
      assertTrue("Should have event on day " + i, daysFound[i]);
    }
  }

  @Test
  public void testSeriesDuplicatePrevention() {
    List<String> repeatDays = Arrays.asList("M", "W");

    // Create first series
    calendar.createSeriesTimes("Meeting Series", testStart, testEnd, repeatDays, 2);

    // Verify first series exists
    assertEquals("Should have 4 events in first series",
            4, calendar.getSeries().get(testStart).size());

    // Try to create overlapping series (should detect duplicates)
    try {
      calendar.createSeriesTimes("Meeting Series", testStart, testEnd, repeatDays, 1);
      fail("Should prevent duplicate events in series");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "Event already exists", e.getMessage());
    }
  }

  @Test
  public void testMultipleDifferentSeries() {
    List<String> weekdaySeries = Arrays.asList("M", "T", "W", "R", "F");
    List<String> weekendSeries = Arrays.asList("S", "U");

    LocalDateTime weekdayTime = testStart;
    LocalDateTime weekendTime = testStart.plusHours(2);

    // Create weekday series
    calendar.createSeriesTimes("Weekday Work", weekdayTime, testEnd, weekdaySeries, 1);

    // Create weekend series at different time
    calendar.createSeriesTimes("Weekend Fun", weekendTime, testEnd.plusHours(2), weekendSeries, 1);

    // Verify both series exist
    assertTrue("Weekday series should exist", calendar.getSeries().containsKey(weekdayTime));
    assertTrue("Weekend series should exist", calendar.getSeries().containsKey(weekendTime));

    assertEquals("Weekday series should have 5 events",
            5, calendar.getSeries().get(weekdayTime).size());
    assertEquals("Weekend series should have 2 events",
            2, calendar.getSeries().get(weekendTime).size());
  }

  @Test
  public void testSeriesCalendarIntegration() {
    List<String> repeatDays = Arrays.asList("M", "F");
    calendar.createSeriesTimes("Bi-weekly Check", testStart, testEnd, repeatDays, 2);

    // Verify events appear in main calendar
    Map<LocalDate, List<Event>> mainCalendar = calendar.getCalendar();

    int eventCount = 0;
    for (List<Event> dayEvents : mainCalendar.values()) {
      eventCount += dayEvents.size();
    }

    assertEquals("Main calendar should contain all series events", 4, eventCount);

    // Verify series events and calendar events are the same objects
    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    for (Event seriesEvent : seriesEvents) {
      LocalDate eventDate = seriesEvent.getStart().toLocalDate();
      List<Event> dayEvents = mainCalendar.get(eventDate);
      assertTrue("Calendar should contain the series event", dayEvents.contains(seriesEvent));
    }
  }
}