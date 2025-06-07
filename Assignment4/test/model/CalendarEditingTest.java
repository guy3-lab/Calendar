package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import controller.parse.PropertyType;
import model.calendar.Calendar;
import model.calendar.Event;
import model.enums.Location;
import model.enums.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Comprehensive test suite for Calendar editing functionality.
 * Tests both single event editing (2.1) and series editing in all three modes (2.2).
 */
public class CalendarEditingTest {

  private Calendar calendar;
  private LocalDateTime testStart;
  private LocalDateTime testEnd;
  private LocalDate testDate;

  @Before
  public void setUp() {
    calendar = new Calendar();
    testStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    testEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    testDate = LocalDate.of(2025, 6, 15);
  }

  // ==================== SINGLE EVENT EDITING TESTS ====================

  /**
   * Tests editing a single event's subject property.
   */
  @Test
  public void testEditEventSubject() {
    // Create initial event
    Event event = calendar.createEvent("Original Meeting", testStart, testEnd);
    assertNotNull("Event should be created", event);
    assertEquals("Initial subject should be set", "Original Meeting", event.getSubject());

    // Verify event is in calendar
    assertTrue("Calendar should contain date", calendar.getCalendar().containsKey(testDate));
    List<Event> dayEvents = calendar.getCalendar().get(testDate);
    assertEquals("Should have one event", 1, dayEvents.size());
    assertSame("Should be same event object", event, dayEvents.get(0));

    // Edit the subject
    calendar.editEvent(PropertyType.SUBJECT, "Original Meeting", testStart, testEnd,
            "Updated Meeting");

    // Verify subject was changed
    assertEquals("Subject should be updated", "Updated Meeting", event.getSubject());

    // Verify other properties unchanged
    assertEquals("Start time should remain same", testStart, event.getStart());
    assertEquals("End time should remain same", testEnd, event.getEnd());
    assertEquals("Location should remain default", Location.ONLINE, event.getLocation());
    assertEquals("Status should remain default", Status.PUBLIC, event.getStatus());
    assertEquals("Description should remain empty", "", event.getDesc());

    // Verify event still in same location in calendar
    assertTrue("Calendar should still contain date", calendar.getCalendar().containsKey(testDate));
    List<Event> updatedDayEvents = calendar.getCalendar().get(testDate);
    assertEquals("Should still have one event", 1, updatedDayEvents.size());
    assertSame("Should be same event object", event, updatedDayEvents.get(0));
  }

  /**
   * Tests editing a single event's start time within the same day.
   */
  @Test
  public void testEditEventStartTime() {
    // Create initial event
    LocalDateTime originalStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime originalEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    Event event = calendar.createEvent("Meeting", originalStart, originalEnd);

    // Verify initial state
    assertEquals("Initial start should be set", originalStart, event.getStart());
    assertEquals("Initial end should be set", originalEnd, event.getEnd());
    assertTrue("Should be on original date", calendar.getCalendar().containsKey(testDate));
    assertEquals("Should have one event on original date", 1,
            calendar.getCalendar().get(testDate).size());

    // Edit start time to later same day
    LocalDateTime newStart = LocalDateTime.of(2025, 6, 15, 14, 0);
    calendar.editEvent(PropertyType.START, "Meeting", originalStart, originalEnd,
            newStart.toString());

    // Verify start time updated
    assertEquals("Start time should be updated", newStart, event.getStart());
    assertEquals("End time should be updated proportionally", originalEnd, event.getEnd());

    // Verify still on same date
    assertTrue("Should still be on same date", calendar.getCalendar().containsKey(testDate));
    assertEquals("Should still have one event", 1, calendar.getCalendar().get(testDate).size());
    assertSame("Should be same event object", event, calendar.getCalendar().get(testDate).get(0));
  }

  /**
   * Tests editing an event's start time to move it to a different date.
   */
  @Test
  public void testEditEventStartTimeToNewDate() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);

    // Verify initial placement
    assertTrue("Should be on original date", calendar.getCalendar().containsKey(testDate));
    assertEquals("Should have one event on original date", 1,
            calendar.getCalendar().get(testDate).size());

    // Edit start time to different date
    LocalDateTime newStart = LocalDateTime.of(2025, 6, 16, 10, 0);
    LocalDate newDate = LocalDate.of(2025, 6, 16);
    calendar.editEvent(PropertyType.START, "Meeting", testStart, testEnd, newStart.toString());

    // Verify event moved to new date
    assertEquals("Start time should be updated", newStart, event.getStart());

    // Verify event removed from original date
    assertFalse("Original date should not have events",
            calendar.getCalendar().containsKey(testDate) &&
                    !calendar.getCalendar().get(testDate).isEmpty());

    // Verify event added to new date
    assertTrue("New date should contain event", calendar.getCalendar().containsKey(newDate));
    List<Event> newDayEvents = calendar.getCalendar().get(newDate);
    assertEquals("New date should have one event", 1, newDayEvents.size());
    assertSame("Should be same event object", event, newDayEvents.get(0));
  }

  /**
   * Tests editing a single event's end time.
   */
  @Test
  public void testEditEventEndTime() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);
    assertEquals("Initial end time should be set", testEnd, event.getEnd());

    // Edit end time
    LocalDateTime newEnd = LocalDateTime.of(2025, 6, 15, 12, 30);
    calendar.editEvent(PropertyType.END, "Meeting", testStart, testEnd, newEnd.toString());

    // Verify end time updated
    assertEquals("End time should be updated", newEnd, event.getEnd());

    // Verify start time unchanged
    assertEquals("Start time should remain same", testStart, event.getStart());

    // Verify still in same calendar location
    assertTrue("Should still be on same date", calendar.getCalendar().containsKey(testDate));
    assertEquals("Should still have one event", 1, calendar.getCalendar().get(testDate).size());
  }

  /**
   * Tests editing a single event's description property.
   */
  @Test
  public void testEditEventDescription() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);
    assertEquals("Initial description should be empty", "", event.getDesc());

    // Edit description
    String newDescription = "Quarterly business review meeting";
    calendar.editEvent(PropertyType.DESCRIPTION, "Meeting", testStart, testEnd, newDescription);

    // Verify description updated
    assertEquals("Description should be updated", newDescription, event.getDesc());

    // Verify other properties unchanged
    assertEquals("Subject should remain same", "Meeting", event.getSubject());
    assertEquals("Start time should remain same", testStart, event.getStart());
    assertEquals("End time should remain same", testEnd, event.getEnd());
  }

  /**
   * Tests editing a single event's location property.
   */
  @Test
  public void testEditEventLocation() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);
    assertEquals("Initial location should be ONLINE", Location.ONLINE, event.getLocation());

    // Edit location to PHYSICAL
    calendar.editEvent(PropertyType.LOCATION, "Meeting", testStart, testEnd, "PHYSICAL");

    // Verify location updated
    assertEquals("Location should be updated to PHYSICAL", Location.PHYSICAL, event.getLocation());

    // Edit location back to ONLINE
    calendar.editEvent(PropertyType.LOCATION, "Meeting", testStart, testEnd, "ONLINE");

    // Verify location updated again
    assertEquals("Location should be updated back to ONLINE", Location.ONLINE, event.getLocation());
  }

  /**
   * Tests that location editing accepts case-insensitive input.
   */
  @Test
  public void testEditEventLocationCaseInsensitive() {
    Event event = calendar.createEvent("Meeting", testStart, testEnd);

    // Test various case combinations
    calendar.editEvent(PropertyType.LOCATION, "Meeting", testStart, testEnd, "physical");
    assertEquals("Lowercase should work", Location.PHYSICAL, event.getLocation());

    calendar.editEvent(PropertyType.LOCATION, "Meeting", testStart, testEnd, "PhYsIcAl");
    assertEquals("Mixed case should work", Location.PHYSICAL, event.getLocation());

    calendar.editEvent(PropertyType.LOCATION, "Meeting", testStart, testEnd, "ONLINE");
    assertEquals("Uppercase should work", Location.ONLINE, event.getLocation());
  }

  /**
   * Tests editing a single event's status property.
   */
  @Test
  public void testEditEventStatus() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);
    assertEquals("Initial status should be PUBLIC", Status.PUBLIC, event.getStatus());

    // Edit status to PRIVATE
    calendar.editEvent(PropertyType.STATUS, "Meeting", testStart, testEnd, "PRIVATE");

    // Verify status updated
    assertEquals("Status should be updated to PRIVATE", Status.PRIVATE, event.getStatus());

    // Edit status back to PUBLIC
    calendar.editEvent(PropertyType.STATUS, "Meeting", testStart, testEnd, "PUBLIC");

    // Verify status updated again
    assertEquals("Status should be updated back to PUBLIC", Status.PUBLIC, event.getStatus());
  }

  /**
   * Tests that status editing accepts case-insensitive input.
   */
  @Test
  public void testEditEventStatusCaseInsensitive() {
    Event event = calendar.createEvent("Meeting", testStart, testEnd);

    // Test various case combinations
    calendar.editEvent(PropertyType.STATUS, "Meeting", testStart, testEnd, "private");
    assertEquals("Lowercase should work", Status.PRIVATE, event.getStatus());

    calendar.editEvent(PropertyType.STATUS, "Meeting", testStart, testEnd, "PrIvAtE");
    assertEquals("Mixed case should work", Status.PRIVATE, event.getStatus());

    calendar.editEvent(PropertyType.STATUS, "Meeting", testStart, testEnd, "PUBLIC");
    assertEquals("Uppercase should work", Status.PUBLIC, event.getStatus());
  }

  /**
   * Tests editing multiple properties of a single event sequentially.
   */
  @Test
  public void testEditEventMultipleProperties() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);

    // Edit multiple properties in sequence
    calendar.editEvent(PropertyType.SUBJECT, "Meeting", testStart, testEnd, "Important Meeting");
    assertEquals("Subject should be updated", "Important Meeting", event.getSubject());

    calendar.editEvent(PropertyType.DESCRIPTION, "Important Meeting", testStart, testEnd,
            "Critical discussion");
    assertEquals("Description should be updated", "Critical discussion", event.getDesc());

    calendar.editEvent(PropertyType.LOCATION, "Important Meeting", testStart, testEnd, "PHYSICAL");
    assertEquals("Location should be updated", Location.PHYSICAL, event.getLocation());

    calendar.editEvent(PropertyType.STATUS, "Important Meeting", testStart, testEnd, "PRIVATE");
    assertEquals("Status should be updated", Status.PRIVATE, event.getStatus());

    // Verify all properties are correct
    assertEquals("Subject should remain updated", "Important Meeting", event.getSubject());
    assertEquals("Description should remain updated", "Critical discussion", event.getDesc());
    assertEquals("Location should remain updated", Location.PHYSICAL, event.getLocation());
    assertEquals("Status should remain updated", Status.PRIVATE, event.getStatus());
    assertEquals("Start time should remain unchanged", testStart, event.getStart());
    assertEquals("End time should remain unchanged", testEnd, event.getEnd());
  }

  /**
   * Tests editing properties of a multi-day event.
   */
  @Test
  public void testEditEventWithMultiDayEvent() {
    // Create multi-day event
    LocalDateTime multiStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime multiEnd = LocalDateTime.of(2025, 6, 17, 15, 0);
    Event multiEvent = calendar.createEvent("Conference", multiStart, multiEnd);

    // Verify initial placement on all days
    LocalDate day1 = LocalDate.of(2025, 6, 15);
    LocalDate day2 = LocalDate.of(2025, 6, 16);
    LocalDate day3 = LocalDate.of(2025, 6, 17);

    assertTrue("Should be on day 1", calendar.getCalendar().containsKey(day1));
    assertTrue("Should be on day 2", calendar.getCalendar().containsKey(day2));
    assertTrue("Should be on day 3", calendar.getCalendar().containsKey(day3));

    // Edit subject
    calendar.editEvent(PropertyType.SUBJECT, "Conference", multiStart, multiEnd,
            "Updated Conference");

    // Verify subject changed
    assertEquals("Subject should be updated", "Updated Conference", multiEvent.getSubject());

    // Verify still on all days
    assertTrue("Should still be on day 1", calendar.getCalendar().containsKey(day1));
    assertTrue("Should still be on day 2", calendar.getCalendar().containsKey(day2));
    assertTrue("Should still be on day 3", calendar.getCalendar().containsKey(day3));

    // Verify same object on all days
    assertSame("Same object on day 1", multiEvent, calendar.getCalendar().get(day1).get(0));
    assertSame("Same object on day 2", multiEvent, calendar.getCalendar().get(day2).get(0));
    assertSame("Same object on day 3", multiEvent, calendar.getCalendar().get(day3).get(0));
  }

  /**
   * Tests that editing end time to before start time throws exception.
   */
  @Test
  public void testEditEventInvalidEndBeforeStart() {
    // Create initial event
    Event event = calendar.createEvent("Meeting", testStart, testEnd);

    // Try to edit end time to before start time
    LocalDateTime invalidEnd = testStart.minusHours(1);

    try {
      calendar.editEvent(PropertyType.END, "Meeting", testStart, testEnd, invalidEnd.toString());
      fail("Should throw exception for invalid end time");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "End time must be after start time", e.getMessage());
    }

    // Verify event unchanged
    assertEquals("End time should remain unchanged", testEnd, event.getEnd());
    assertEquals("Start time should remain unchanged", testStart, event.getStart());
  }

  /**
   * Tests that editing an event to match another existing event throws exception.
   */
  @Test
  public void testEditEventDuplicateCreation() {
    // Create two different events
    Event event1 = calendar.createEvent("Meeting 1", testStart, testEnd);
    Event event2 = calendar.createEvent("Meeting 2", testStart.plusHours(1), testEnd.plusHours(1));

    // Try to edit first event to match second event
    try {
      calendar.editEvent(PropertyType.SUBJECT, "Meeting 1", testStart, testEnd, "Meeting 2");
    } catch (IllegalArgumentException e) {
      assertEquals("Should have specific error message",
              "Event already exists", e.getMessage());
    }

    // Verify first event unchanged
    assertNotEquals("Subject should not remain unchanged", "Meeting 1", event1.getSubject());
  }

  /**
   * Tests attempting to edit a non-existent event.
   */
  @Test
  public void testEditNonExistentEvent() {
    // Create an event
    calendar.createEvent("Real Meeting", testStart, testEnd);

    // Try to edit non-existent event (should not crash, but won't find anything to edit)
    // This should complete without error since the loop just won't find a matching event
    calendar.editEvent(PropertyType.SUBJECT, "Fake Meeting", testStart, testEnd, "Updated");

    // Original event should remain unchanged
    List<Event> events = calendar.getCalendar().get(testDate);
    assertEquals("Should still have one event", 1, events.size());
    assertEquals("Subject should remain unchanged", "Real Meeting", events.get(0).getSubject());
  }

  // ==================== SERIES EDITING TESTS ====================

  // ========== EDIT SINGLE EVENT IN SERIES ==========

  /**
   * Tests editing a single event within a series without affecting other events.
   */
  @Test
  public void testEditSingleEventInSeries() {
    // Create a series
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    calendar.createSeriesTimes("Weekly Meeting", testStart, testEnd, repeatDays, 3);

    // Verify series created
    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertNotNull("Series should exist", seriesEvents);
    assertEquals("Should have 9 events (3 days × 3 times)", 9, seriesEvents.size());

    // All events should initially have same subject
    for (Event event : seriesEvents) {
      assertEquals("All events should have original subject", "Weekly Meeting", event.getSubject());
    }

    // Edit a single event in the series
    LocalDateTime specificEventStart = seriesEvents.get(0).getStart();
    LocalDateTime specificEventEnd = seriesEvents.get(0).getEnd();

    calendar.editEvent(PropertyType.SUBJECT, "Weekly Meeting", specificEventStart,
            specificEventEnd, "Special Meeting");

    // Verify only that specific event was changed
    assertEquals("Specific event should be updated", "Special Meeting",
            seriesEvents.get(0).getSubject());

    // Verify other events in series unchanged
    for (int i = 1; i < seriesEvents.size(); i++) {
      assertEquals("Other events should remain unchanged", "Weekly Meeting",
              seriesEvents.get(i).getSubject());
    }

    // Verify series still exists
    assertTrue("Series should still exist", calendar.getSeries().containsKey(testStart));
    assertEquals("Series should still have all events", 9,
            calendar.getSeries().get(testStart).size());
  }

  /**
   * Tests editing the location of a single event within a series.
   */
  @Test
  public void testEditSingleEventInSeriesChangeLocation() {
    // Create a series
    List<String> repeatDays = Arrays.asList("T", "R");
    calendar.createSeriesTimes("Team Sync", testStart, testEnd, repeatDays, 2);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 4 events", 4, seriesEvents.size());

    // Verify all events have default location
    for (Event event : seriesEvents) {
      assertEquals("All events should have ONLINE location", Location.ONLINE, event.getLocation());
    }

    // Edit location of first event only
    Event firstEvent = seriesEvents.get(0);
    calendar.editEvent(PropertyType.LOCATION, "Team Sync", firstEvent.getStart(),
            firstEvent.getEnd(), "PHYSICAL");

    // Verify only first event changed
    assertEquals("First event should be PHYSICAL", Location.PHYSICAL,
            seriesEvents.get(0).getLocation());
    assertEquals("Second event should remain ONLINE", Location.ONLINE,
            seriesEvents.get(1).getLocation());
    assertEquals("Third event should remain ONLINE", Location.ONLINE,
            seriesEvents.get(2).getLocation());
    assertEquals("Fourth event should remain ONLINE", Location.ONLINE,
            seriesEvents.get(3).getLocation());
  }

  /**
   * Tests editing the time of a single event within a series.
   */
  @Test
  public void testEditSingleEventInSeriesChangeTime() {
    // Create a series
    List<String> repeatDays = Arrays.asList("W");
    calendar.createSeriesTimes("Weekly Check", testStart, testEnd, repeatDays, 3);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 3 events", 3, seriesEvents.size());

    // Store original times
    LocalDateTime originalStart1 = seriesEvents.get(0).getStart();
    LocalDateTime originalStart2 = seriesEvents.get(1).getStart();
    LocalDateTime originalStart3 = seriesEvents.get(2).getStart();

    // Edit start time of second event
    LocalDateTime newStart = originalStart2.plusHours(1);
    calendar.editEvent(PropertyType.START, "Weekly Check", originalStart2,
            seriesEvents.get(1).getEnd(), newStart.toString());

    // Verify only second event time changed
    assertEquals("First event time should remain unchanged", originalStart1,
            seriesEvents.get(0).getStart());
    assertEquals("Second event time should be updated", newStart, seriesEvents.get(1).getStart());
    assertEquals("Third event time should remain unchanged", originalStart3,
            seriesEvents.get(2).getStart());

    // Verify event moved to new calendar date if necessary
    if (!newStart.toLocalDate().equals(originalStart2.toLocalDate())) {
      assertTrue("Event should be on new date",
              calendar.getCalendar().get(newStart.toLocalDate()).contains(seriesEvents.get(1)));
    }
  }

  // ========== EDIT EVENTS STARTING FROM SPECIFIC DATE ==========

  /**
   * Tests editing all events in a series starting from a specific date.
   */
  @Test
  public void testEditEventsStartingFromSpecificDate() {
    // Create a series
    List<String> repeatDays = Arrays.asList("M", "F");
    calendar.createSeriesTimes("Bi-weekly Review", testStart, testEnd, repeatDays, 4);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 8 events (2 days × 4 times)", 8, seriesEvents.size());

    // All events should initially have same subject
    for (Event event : seriesEvents) {
      assertEquals("All events should have original subject", "Bi-weekly Review",
              event.getSubject());
    }

    // Get the start time of the third Monday (should be seriesEvents.get(2) since Mondays come first)
    LocalDateTime thirdMondayStart = seriesEvents.get(2).getStart();

    // Edit events starting from the third Monday
    calendar.editEvents(PropertyType.SUBJECT, "Bi-weekly Review",
            thirdMondayStart, "Updated Review");

    // Verify first two Monday events unchanged
    assertEquals("First Monday should remain unchanged", "Bi-weekly Review",
            seriesEvents.get(0).getSubject());
    assertEquals("Second Monday should remain unchanged", "Bi-weekly Review",
            seriesEvents.get(1).getSubject());

    // Verify events from third Monday onward are changed
    assertEquals("Third Monday should be updated", "Updated Review",
            seriesEvents.get(2).getSubject());
    assertEquals("Fourth Monday should be updated", "Updated Review",
            seriesEvents.get(3).getSubject());

    // Verify Friday events that start at or after the specified time are also changed
    // Need to check which Friday events have start times >= thirdMondayStart
    for (int i = 4; i < 8; i++) { // Friday events are at positions 4-7
      Event fridayEvent = seriesEvents.get(i);
      if (!fridayEvent.getStart().isBefore(thirdMondayStart)) {
        assertEquals("Friday events at/after start time should be updated", "Updated Review",
                fridayEvent.getSubject());
      } else {
        assertEquals("Friday events before start time should remain unchanged", "Bi-weekly Review",
                fridayEvent.getSubject());
      }
    }
  }

  /**
   * Tests editing multiple properties of events starting from a specific date.
   */
  @Test
  public void testEditEventsStartingFromSpecificDateMultipleProperties() {
    // Create a series
    List<String> repeatDays = Arrays.asList("T", "R");
    calendar.createSeriesTimes("Team Meeting", testStart, testEnd, repeatDays, 3);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 6 events", 6, seriesEvents.size());

    // Get second Tuesday's start time (position 1)
    LocalDateTime secondTuesdayStart = seriesEvents.get(1).getStart();

    // Edit subject starting from second Tuesday
    calendar.editEvents(PropertyType.SUBJECT, "Team Meeting", secondTuesdayStart,
            "Advanced Meeting");

    // Edit description starting from second Tuesday
    calendar.editEvents(PropertyType.DESCRIPTION, "Advanced Meeting", secondTuesdayStart,
            "Advanced topics");

    // Edit location starting from second Tuesday
    calendar.editEvents(PropertyType.LOCATION, "Advanced Meeting", secondTuesdayStart, "PHYSICAL");

    // Verify first Tuesday unchanged
    assertEquals("First Tuesday subject unchanged", "Team Meeting", seriesEvents.get(0).getSubject());
    assertEquals("First Tuesday description unchanged", "", seriesEvents.get(0).getDesc());
    assertEquals("First Tuesday location unchanged", Location.ONLINE,
            seriesEvents.get(0).getLocation());

    // Verify events from second Tuesday onward changed
    for (int i = 1; i < seriesEvents.size(); i++) {
      Event event = seriesEvents.get(i);
      if (!event.getStart().isBefore(secondTuesdayStart)
              && event.getSubject().equals("Advanced Meeting")) {
        assertEquals("Event should have updated subject", "Advanced Meeting", event.getSubject());
        assertEquals("Event should have updated description", "Advanced topics", event.getDesc());
        assertEquals("Event should have updated location", Location.PHYSICAL, event.getLocation());
      }
    }
  }

  /**
   * Tests editing events with subject filtering in mixed-subject series.
   */
  @Test
  public void testEditEventsWithSubjectFilter() {
    // Create a series, then modify some events to have different subjects
    List<String> repeatDays = Arrays.asList("W");
    calendar.createSeriesTimes("Base Meeting", testStart, testEnd, repeatDays, 4);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 4 events", 4, seriesEvents.size());

    // Manually change subject of third event to create mixed subjects
    seriesEvents.get(2).setSubject("Different Meeting");

    // Edit events starting from second event, but only those with "Base Meeting" subject
    LocalDateTime secondEventStart = seriesEvents.get(1).getStart();
    calendar.editEvents(PropertyType.LOCATION, "Base Meeting", secondEventStart, "PHYSICAL");

    // Verify first event unchanged (before start time)
    assertEquals("First event location unchanged", Location.ONLINE,
            seriesEvents.get(0).getLocation());

    // Verify second event changed (matches subject and time criteria)
    assertEquals("Second event location changed", Location.PHYSICAL,
            seriesEvents.get(1).getLocation());

    // Verify third event unchanged (different subject)
    assertEquals("Third event location unchanged due to different subject",
            Location.ONLINE, seriesEvents.get(2).getLocation());

    // Verify fourth event changed (matches subject and time criteria)
    assertEquals("Fourth event location changed", Location.PHYSICAL,
            seriesEvents.get(3).getLocation());
  }

  // ========== EDIT ENTIRE SERIES ==========

  /**
   * Tests editing all events in an entire series simultaneously.
   */
  @Test
  public void testEditEntireSeries() {
    // Create a series
    List<String> repeatDays = Arrays.asList("M", "W", "F");
    calendar.createSeriesTimes("Daily Standup", testStart, testEnd, repeatDays, 2);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 6 events (3 days × 2 times)", 6, seriesEvents.size());

    // Verify all events have original subject
    for (Event event : seriesEvents) {
      assertEquals("All events should have original subject", "Daily Standup", event.getSubject());
    }

    // Edit entire series subject
    calendar.editSeries(PropertyType.SUBJECT, "Daily Standup", testStart, "Morning Standup");

    // Verify all events updated
    for (Event event : seriesEvents) {
      assertEquals("All events should have updated subject", "Morning Standup", event.getSubject());
    }

    // Verify series still exists with same key
    assertTrue("Series should still exist", calendar.getSeries().containsKey(testStart));
    assertEquals("Series should have same number of events", 6,
            calendar.getSeries().get(testStart).size());
  }

  @Test
  public void testEditEntireSeriesMultipleProperties() {
    // Create a series
    List<String> repeatDays = Arrays.asList("T", "F");
    calendar.createSeriesTimes("Weekly Review", testStart, testEnd, repeatDays, 3);

    List<Event> seriesEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 6 events", 6, seriesEvents.size());

    // Edit multiple properties of entire series
    calendar.editSeries(PropertyType.SUBJECT, "Weekly Review", testStart, "Comprehensive Review");
    calendar.editSeries(PropertyType.DESCRIPTION, "Comprehensive Review", testStart,
            "Detailed weekly analysis");
    calendar.editSeries(PropertyType.LOCATION, "Comprehensive Review", testStart, "PHYSICAL");
    calendar.editSeries(PropertyType.STATUS, "Comprehensive Review", testStart, "PRIVATE");

    // Verify all events have all updated properties
    for (Event event : seriesEvents) {
      assertEquals("Subject should be updated", "Comprehensive Review", event.getSubject());
      assertEquals("Description should be updated", "Detailed weekly analysis", event.getDesc());
      assertEquals("Location should be updated", Location.PHYSICAL, event.getLocation());
      assertEquals("Status should be updated", Status.PRIVATE, event.getStatus());
    }
  }

  @Test
  public void testEditEntireSeriesStartTime() {
    // Create a series
    List<String> repeatDays = Arrays.asList("W", "F");
    calendar.createSeriesTimes("Meeting Series", testStart, testEnd, repeatDays, 2);

    List<Event> originalEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 4 events initially", 4, originalEvents.size());

    // Store original start times for comparison
    LocalDateTime originalWed1 = originalEvents.get(0).getStart();
    LocalDateTime originalWed2 = originalEvents.get(1).getStart();
    LocalDateTime originalFri1 = originalEvents.get(2).getStart();
    LocalDateTime originalFri2 = originalEvents.get(3).getStart();

    // Edit start time of entire series (move 2 hours later)
    LocalDateTime newSeriesStart = testStart.plusHours(2);
    calendar.editSeries(PropertyType.START, "Meeting Series", testStart, newSeriesStart.toString());

    // Verify old series key removed and new series key created
    assertFalse("Old series key should be removed", calendar.getSeries().containsKey(testStart));
    assertTrue("New series key should exist", calendar.getSeries().containsKey(newSeriesStart));

    List<Event> updatedEvents = calendar.getSeries().get(newSeriesStart);
    assertEquals("Should still have 4 events", 4, updatedEvents.size());

    // Verify all start times shifted by 2 hours
    assertEquals("First Wed should be 2 hours later", originalWed1.plusHours(2),
            updatedEvents.get(0).getStart());
    assertEquals("Second Wed should be 2 hours later", originalWed2.plusHours(2),
            updatedEvents.get(1).getStart());
    assertEquals("First Fri should be 2 hours later", originalFri1.plusHours(2),
            updatedEvents.get(2).getStart());
    assertEquals("Second Fri should be 2 hours later", originalFri2.plusHours(2),
            updatedEvents.get(3).getStart());

    // Verify end times also shifted
    assertEquals("First Wed end should be 2 hours later", testEnd.plusHours(2),
            updatedEvents.get(0).getEnd());
    assertEquals("First Fri end should be 2 hours later", testEnd.plusHours(2),
            updatedEvents.get(2).getEnd());
  }

  @Test
  public void testEditEntireSeriesEndTime() {
    // Create a series with specific end time
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    List<String> repeatDays = List.of("M");

    calendar.createSeriesTimes("Monday Meeting", seriesStart, seriesEnd, repeatDays, 3);

    List<Event> seriesEvents = calendar.getSeries().get(seriesStart);
    assertEquals("Should have 3 events", 3, seriesEvents.size());

    // Verify original end times
    for (Event event : seriesEvents) {
      assertEquals("Original end time should be 11:00", 11, event.getEnd().getHour());
      assertEquals("Original end minutes should be 0", 0, event.getEnd().getMinute());
    }

    // Edit end time of entire series
    LocalDateTime newEndTime = LocalDateTime.of(2025, 6, 15, 11, 30);
    calendar.editSeries(PropertyType.END, "Monday Meeting", seriesStart, newEndTime.toString());

    // Verify all end times updated
    for (Event event : seriesEvents) {
      assertEquals("Updated end time should be 11:30", 11, event.getEnd().getHour());
      assertEquals("Updated end minutes should be 30", 30, event.getEnd().getMinute());
    }

    // Verify start times unchanged
    for (Event event : seriesEvents) {
      assertEquals("Start time should remain 10:00", 10, event.getStart().getHour());
      assertEquals("Start minutes should remain 0", 0, event.getStart().getMinute());
    }
  }

  // ========== SERIES EDITING EDGE CASES ==========

  @Test
  public void testEditNonExistentSeries() {
    // Create a series
    List<String> repeatDays = List.of("W");
    calendar.createSeriesTimes("Real Series", testStart, testEnd, repeatDays, 2);

    // Verify series exists
    assertTrue("Real series should exist", calendar.getSeries().containsKey(testStart));

    // Try to edit non-existent series
    LocalDateTime fakeStart = testStart.plusDays(1);
    calendar.editSeries(PropertyType.SUBJECT, "Fake Series", fakeStart, "Updated");

    // Verify original series unchanged
    List<Event> realEvents = calendar.getSeries().get(testStart);
    for (Event event : realEvents) {
      assertEquals("Real series events should remain unchanged", "Real Series", event.getSubject());
    }
  }

  @Test
  public void testEditSeriesBreaksContinuity() {
    // Create a series
    List<String> repeatDays = Arrays.asList("M", "W");
    calendar.createSeriesTimes("Original Series", testStart, testEnd, repeatDays, 3);

    List<Event> originalEvents = calendar.getSeries().get(testStart);
    assertEquals("Should have 6 events initially", 6, originalEvents.size());

    // Edit start time of entire series to break continuity
    LocalDateTime newStart = testStart.plusDays(3); // Move to Thursday
    calendar.editSeries(PropertyType.START, "Original Series", testStart, newStart.toString());

    // Verify old series removed, new series created
    assertFalse("Old series should be removed", calendar.getSeries().containsKey(testStart));
    assertTrue("New series should exist", calendar.getSeries().containsKey(newStart));

    List<Event> newEvents = calendar.getSeries().get(newStart);
    assertEquals("New series should have same number of events", 6, newEvents.size());

    // Verify all events moved to new day pattern
    for (Event event : newEvents) {
      // Events should now be on Thursday pattern + weekly intervals
      int dayOfWeek = event.getStart().getDayOfWeek().getValue();
      assertTrue("Events should be on expected days", dayOfWeek == 4 || dayOfWeek == 6);
    }
  }


  /**
   *
   */
  @Test
  public void testComplexSeriesEditingScenario() {
    // This test implements the complex example from the assignment requirements

    // 1. Create an event series called "First" on May 5 2025 from 10am-11am that repeats 6 times
    // on Mondays and Wednesdays
    LocalDateTime may5Start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime may5End = LocalDateTime.of(2025, 5, 5, 11, 0);
    List<String> mondayWednesday = Arrays.asList("M", "W");

    calendar.createSeriesTimes("First", may5Start, may5End, mondayWednesday, 6);

    List<Event> firstSeries = calendar.getSeries().get(may5Start);
    assertEquals("Should have 12 events (2 days × 6 times)", 12, firstSeries.size());

    // Verify all events have "First" subject initially
    for (Event event : firstSeries) {
      assertEquals("All events should have 'First' subject", "First", event.getSubject());
    }

    // 2. Edit the subject of the event starting on May 12 2025 to be "Second" and specify to
    // change all events in the series starting from this
    LocalDateTime may12Start = LocalDateTime.of(2025, 5, 12, 10, 0);
    calendar.editEvents(PropertyType.SUBJECT, "First", may12Start, "Second");

    // Count events with each subject
    int firstCount = 0, secondCount = 0;
    for (Event event : firstSeries) {
      if ("First".equals(event.getSubject())) { firstCount++ ;}
      else if ("Second".equals(event.getSubject())) { secondCount++; }
    }

    // Should have some "First" events (before May 12) and some "Second" events (from May 12 onward)
    assertTrue("Should have some 'First' events", firstCount > 0);
    assertTrue("Should have some 'Second' events", secondCount > 0);
    assertEquals("Total should still be 12", 12, firstCount + secondCount);

    // 3. Edit the subject of the event starting on May 5 2025 to be "Third" and specify for
    // all events in this series to change
    calendar.editSeries(PropertyType.SUBJECT, "First", may5Start, "Third");

    // Now all remaining "First" events should become "Third"
    int thirdCount = 0, secondCount2 = 0;
    for (Event event : firstSeries) {
      if ("Third".equals(event.getSubject())) { thirdCount++; }
      else if ("Second".equals(event.getSubject())) { secondCount2++; }
    }

    assertTrue("Should have 'Third' events", thirdCount > 0);
    assertTrue("Should still have 'Second' events", secondCount2 > 0);
    assertEquals("Total should still be 12", 12, thirdCount + secondCount2);
    assertEquals("Second count should remain same", secondCount, secondCount2);

    // 4. Edit the start time of the event starting on May 12 2025 to be 10:30am and specify to
    // change all events in the series starting from this
    LocalDateTime newStartTime = LocalDateTime.of(2025, 5, 12, 10, 30);
    calendar.editEvents(PropertyType.START, "Second", may12Start, newStartTime.toString());

    // This should split the series - some events remain in original series, others move to
    // new series
    assertTrue("Original series should still exist", calendar.getSeries().containsKey(may5Start));
    assertTrue("New series should be created", calendar.getSeries().containsKey(newStartTime));

    List<Event> originalRemaining = calendar.getSeries().get(may5Start);
    List<Event> newSeries = calendar.getSeries().get(newStartTime);

    assertTrue("Original series should have remaining events", originalRemaining.size() > 0);
    assertTrue("New series should have moved events", newSeries.size() > 0);

    // 5. Edit the subject of the event starting on May 5 2025 to be "Fourth" and specify for all
    // events in this series to change
    calendar.editSeries(PropertyType.SUBJECT, "Third", may5Start, "Fourth");

    // All events in the original series should now be "Fourth"
    for (Event event : originalRemaining) {
      assertEquals("Original series events should be 'Fourth'", "Fourth", event.getSubject());
    }

    // 6. Edit the subject of the event starting on May 12 2025 to be "Fifth" and specify for all
    // events in this series to change
    calendar.editSeries(PropertyType.SUBJECT, "Second", newStartTime, "Fifth");

    // All events in the new series should now be "Fifth"
    for (Event event : newSeries) {
      assertEquals("New series events should be 'Fifth'", "Fifth", event.getSubject());
    }

    // Final verification: we should have two separate series with different subjects and
    // start times
    assertNotEquals("Series should have different subjects",
            originalRemaining.get(0).getSubject(), newSeries.get(0).getSubject());
    assertNotEquals("Series should have different start times",
            originalRemaining.get(0).getStart().toLocalTime(),
            newSeries.get(0).getStart().toLocalTime());
  }
}