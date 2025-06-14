package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDateTime;

import model.calendar.Event;
import model.calendar.IEvent;
import model.enums.Location;
import model.enums.Status;

/**
 * Comprehensive test suite for Event class functionality.
 * Tests all event properties, creation methods, and validation.
 */
public class EventTest {

  private LocalDateTime testStart;
  private LocalDateTime testEnd;
  private LocalDateTime testAllDayStart;

  @Before
  public void setUp() {
    testStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    testEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    testAllDayStart = LocalDateTime.of(2025, 6, 15, 14, 30);
  }

  // ==================== EVENT PROPERTIES TESTS ====================

  @Test
  public void testRequiredPropertiesInBasicConstructor() {
    IEvent event = new Event("Test Meeting", testStart);

    assertNotNull("Subject should not be null", event.getSubject());
    assertEquals("Subject should match input", "Test Meeting", event.getSubject());
    assertNotNull("Start time should not be null", event.getStart());
    assertNotNull("End time should not be null even for all-day", event.getEnd());

    assertEquals("All-day event should start at 8:00 AM",
            LocalDateTime.of(2025, 6, 15, 8, 0), event.getStart());
    assertEquals("All-day event should end at 5:00 PM",
            LocalDateTime.of(2025, 6, 15, 17, 0), event.getEnd());
  }

  @Test
  public void testOptionalPropertiesDefaultValues() {
    IEvent event = new Event("Test Meeting", testStart);

    assertNotNull("Description should not be null", event.getDesc());
    assertEquals("Description should default to empty string", "", event.getDesc());

    assertNotNull("Location should not be null", event.getLocation());
    assertEquals("Location should default to ONLINE", Location.ONLINE, event.getLocation());

    assertNotNull("Status should not be null", event.getStatus());
    assertEquals("Status should default to PUBLIC", Status.PUBLIC, event.getStatus());
  }

  @Test
  public void testBuilderWithAllProperties() {
    IEvent event = new Event.EventBuilder("Important Meeting", testStart)
            .end(testEnd)
            .desc("Quarterly review meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    assertEquals("Subject should match", "Important Meeting", event.getSubject());
    assertEquals("Start time should match", testStart, event.getStart());
    assertEquals("End time should match", testEnd, event.getEnd());
    assertEquals("Description should match", "Quarterly review meeting", event.getDesc());
    assertEquals("Location should match", Location.PHYSICAL, event.getLocation());
    assertEquals("Status should match", Status.PRIVATE, event.getStatus());

  }

  @Test
  public void testBuilderWithPartialProperties() {
    IEvent event = new Event.EventBuilder("Partial Meeting", testStart)
            .desc("Only description set")
            .build();

    assertEquals("Subject should match", "Partial Meeting", event.getSubject());
    assertEquals("Start time should match", testStart, event.getStart());
    assertEquals("Description should match", "Only description set", event.getDesc());

    assertEquals("End should default to 5:00 PM same day",
            LocalDateTime.of(2025, 6, 15, 17, 0), event.getEnd());
    assertEquals("Location should default to ONLINE", Location.ONLINE, event.getLocation());
    assertEquals("Status should default to PUBLIC", Status.PUBLIC, event.getStatus());
  }

  @Test
  public void testBuilderWithEndTimeOnly() {
    IEvent event = new Event.EventBuilder("Meeting with End", testStart)
            .end(testEnd)
            .build();

    assertEquals("Start time should match", testStart, event.getStart());
    assertEquals("End time should match", testEnd, event.getEnd());
    assertEquals("Description should be empty", "", event.getDesc());
    assertEquals("Location should default to ONLINE", Location.ONLINE, event.getLocation());
    assertEquals("Status should default to PUBLIC", Status.PUBLIC, event.getStatus());
  }

  @Test
  public void testAllPropertySetters() {
    IEvent event = new Event("Original", testStart);

    event.setSubject("Updated Subject");
    assertEquals("Subject setter should work", "Updated Subject", event.getSubject());

    LocalDateTime newStart = testStart.plusHours(1);
    event.setStart(newStart);
    assertEquals("Start setter should work", newStart, event.getStart());

    LocalDateTime newEnd = testEnd.plusHours(2);
    event.setEnd(newEnd);
    assertEquals("End setter should work", newEnd, event.getEnd());

    event.setDesc("New description");
    assertEquals("Description setter should work", "New description", event.getDesc());

    event.setLocation(Location.PHYSICAL);
    assertEquals("Location setter should work", Location.PHYSICAL, event.getLocation());

    event.setStatus(Status.PRIVATE);
    assertEquals("Status setter should work", Status.PRIVATE, event.getStatus());
  }

  @Test
  public void testEventEquality() {
    IEvent event1 = new Event.EventBuilder("Meeting", testStart)
            .end(testEnd)
            .desc("Test description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    IEvent event2 = new Event.EventBuilder("Meeting", testStart)
            .end(testEnd)
            .desc("Test description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    IEvent event3 = new Event.EventBuilder("Different Meeting", testStart)
            .end(testEnd)
            .desc("Test description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    assertEquals("Identical events should be equal", event1, event2);
    assertNotEquals("Events with different subjects should not be equal", event1, event3);

    assertEquals("Equal events should have same hash code",
            event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testEventEqualityWithNulls() {
    IEvent event = new Event("Test", testStart);

    assertNotEquals("Event should not equal null", null, event);
    assertEquals("Event should equal itself", event, event);
  }

  // ==================== SINGLE EVENT CREATION TESTS ====================

  @Test
  public void testAllDayEventCreation() {
    IEvent event = new Event("All Day Conference", testAllDayStart);

    assertEquals("All-day should start at 8:00 AM", 8, event.getStart().getHour());
    assertEquals("All-day should start at 0 minutes", 0, event.getStart().getMinute());
    assertEquals("All-day should end at 5:00 PM", 17, event.getEnd().getHour());
    assertEquals("All-day should end at 0 minutes", 0, event.getEnd().getMinute());

    assertEquals("Should be on same date as input",
            testAllDayStart.toLocalDate(), event.getStart().toLocalDate());
    assertEquals("Start and end should be same date",
            event.getStart().toLocalDate(), event.getEnd().toLocalDate());
  }

  @Test
  public void testTimedEventCreation() {
    IEvent event = new Event.EventBuilder("Timed Meeting", testStart)
            .end(testEnd)
            .build();

    assertEquals("Start time should match exactly", testStart, event.getStart());
    assertEquals("End time should match exactly", testEnd, event.getEnd());
    assertEquals("Should preserve exact start hour", 10, event.getStart().getHour());
    assertEquals("Should preserve exact end hour", 11, event.getEnd().getHour());
  }

  @Test
  public void testMultiDayEventCreation() {
    LocalDateTime multiDayEnd = testStart.plusDays(2).plusHours(3);
    IEvent event = new Event.EventBuilder("Conference", testStart)
            .end(multiDayEnd)
            .build();

    assertEquals("Start should be preserved", testStart, event.getStart());
    assertEquals("End should be preserved", multiDayEnd, event.getEnd());
    assertNotEquals("Start and end dates should differ",
            event.getStart().toLocalDate(), event.getEnd().toLocalDate());

    long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(
            event.getStart().toLocalDate(), event.getEnd().toLocalDate());
    assertEquals("Should span 2 days", 2, daysDifference);
  }

  @Test
  public void testEventWithEmptySubject() {
    IEvent event = new Event("", testStart);

    assertEquals("Empty subject should be preserved", "", event.getSubject());
    assertNotNull("Event should still be created", event);
  }

  @Test
  public void testEventWithLongSubject() {
    String longSubject = "A".repeat(10000);
    IEvent event = new Event(longSubject, testStart);

    assertEquals("Long subject should be preserved", longSubject, event.getSubject());
    assertEquals("Subject length should be preserved", 10000, event.getSubject().length());
  }

  @Test
  public void testEventWithSpecialCharacters() {
    String specialSubject = "Meeting !@#$%^&*()_+-=[]{}|;':\",./<>?";
    IEvent event = new Event(specialSubject, testStart);

    assertEquals("Special characters should be preserved", specialSubject, event.getSubject());
  }

  @Test
  public void testShortDurationEvent() {
    LocalDateTime veryCloseEnd = testStart.plusMinutes(1);
    IEvent event = new Event.EventBuilder("Quick Meeting", testStart)
            .end(veryCloseEnd)
            .build();

    assertEquals("Short duration should be preserved", veryCloseEnd, event.getEnd());
    long duration = java.time.temporal.ChronoUnit.MINUTES.between(
            event.getStart(), event.getEnd());
    assertEquals("Duration should be 1 minute", 1, duration);
  }

  @Test
  public void testMidnightSpanningEvent() {
    LocalDateTime lateStart = LocalDateTime.of(2025, 6, 15, 23, 30);
    LocalDateTime earlyEnd = LocalDateTime.of(2025, 6, 16, 1, 30);

    IEvent event = new Event.EventBuilder("Midnight Event", lateStart)
            .end(earlyEnd)
            .build();

    assertEquals("Late start should be preserved", lateStart, event.getStart());
    assertEquals("Early end should be preserved", earlyEnd, event.getEnd());
    assertNotEquals("Should span midnight",
            event.getStart().toLocalDate(), event.getEnd().toLocalDate());
  }

  @Test
  public void testAllDayEventIgnoresInputTime() {
    LocalDateTime randomTime1 = LocalDateTime.of(2025, 6, 15, 14, 30);
    LocalDateTime randomTime2 = LocalDateTime.of(2025, 6, 15, 3, 45);
    LocalDateTime randomTime3 = LocalDateTime.of(2025, 6, 15, 22, 15);

    IEvent event1 = new Event("Event 1", randomTime1);
    IEvent event2 = new Event("Event 2", randomTime2);
    IEvent event3 = new Event("Event 3", randomTime3);

    assertEquals("All events should start at 8:00 AM",
            LocalDateTime.of(2025, 6, 15, 8, 0), event1.getStart());
    assertEquals("All events should start at 8:00 AM",
            LocalDateTime.of(2025, 6, 15, 8, 0), event2.getStart());
    assertEquals("All events should start at 8:00 AM",
            LocalDateTime.of(2025, 6, 15, 8, 0), event3.getStart());

    assertEquals("All events should end at 5:00 PM",
            LocalDateTime.of(2025, 6, 15, 17, 0), event1.getEnd());
    assertEquals("All events should end at 5:00 PM",
            LocalDateTime.of(2025, 6, 15, 17, 0), event2.getEnd());
    assertEquals("All events should end at 5:00 PM",
            LocalDateTime.of(2025, 6, 15, 17, 0), event3.getEnd());
  }

  // ==================== ADDITIONAL PROPERTY VALIDATION TESTS ====================

  @Test
  public void testBuilderNullHandling() {
    IEvent event = new Event.EventBuilder("Test", testStart)
            .end(null)
            .desc(null)
            .location(null)
            .status(null)
            .build();

    assertEquals("Null end should default to 5:00 PM",
            LocalDateTime.of(2025, 6, 15, 17, 0), event.getEnd());
    assertEquals("Null description should default to empty", "", event.getDesc());
    assertEquals("Null location should default to ONLINE", Location.ONLINE, event.getLocation());
    assertEquals("Null status should default to PUBLIC", Status.PUBLIC, event.getStatus());
  }

  @Test
  public void testBuilderChaining() {
    Event.EventBuilder builder = new Event.EventBuilder("Chainable", testStart);

    assertSame("end() should return builder", builder, builder.end(testEnd));
    assertSame("desc() should return builder", builder, builder.desc("test"));
    assertSame("location() should return builder", builder, builder.location(Location.PHYSICAL));
    assertSame("status() should return builder", builder, builder.status(Status.PRIVATE));

    IEvent event = builder.build();
    assertNotNull("Build should return event", event);
  }


  @Test
  public void testEventImmutabilityAfterCreation() {
    IEvent original = new Event.EventBuilder("Original", testStart)
            .end(testEnd)
            .desc("Original desc")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    String originalSubject = original.getSubject();
    LocalDateTime originalStart = original.getStart();
    LocalDateTime originalEnd = original.getEnd();
    String originalDesc = original.getDesc();
    Location originalLocation = original.getLocation();
    Status originalStatus = original.getStatus();

    original.setSubject("Modified");
    original.setStart(testStart.plusHours(1));
    original.setEnd(testEnd.plusHours(1));
    original.setDesc("Modified desc");
    original.setLocation(Location.ONLINE);
    original.setStatus(Status.PUBLIC);

    assertNotEquals("Subject should be modifiable", originalSubject, original.getSubject());
    assertNotEquals("Start should be modifiable", originalStart, original.getStart());
    assertNotEquals("End should be modifiable", originalEnd, original.getEnd());
    assertNotEquals("Description should be modifiable", originalDesc, original.getDesc());
    assertNotEquals("Location should be modifiable", originalLocation, original.getLocation());
    assertNotEquals("Status should be modifiable", originalStatus, original.getStatus());
  }
}
