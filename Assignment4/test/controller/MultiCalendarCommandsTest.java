package controller;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import model.calendar.Event;
import model.multicalendar.MultiCalendar;
import model.multicalendar.IMultiCalendar;
import static org.junit.Assert.*;

/**
 * Test suite for multi-calendar support commands as specified in section 5 of the assignment.
 */
public class MultiCalendarCommandsTest {

  private CalendarController controller;
  private IMultiCalendar multiCalendar;

  @Before
  public void setUp() {
    multiCalendar = new MultiCalendar();
    controller = new CalendarController(multiCalendar, new controller.format.OutputFormatter());
  }

  // ========== CREATE CALENDAR COMMAND TESTS ==========

  /**
   * Tests successful creation of a calendar with unique name and valid timezone.
   */
  @Test
  public void testCreateCalendarValidNameAndTimezone() {
    String command = "create calendar --name Work --timezone America/New_York";
    String result = controller.executeCommand(command);

    assertNotNull(result);
    assertTrue(result.contains("Created calendar"));
    assertEquals(1, multiCalendar.getCalendars().size());
    assertEquals("Work", multiCalendar.getCalendars().get(0).getName());
    assertEquals(ZoneId.of("America/New_York"), multiCalendar.getCalendars().get(0).getTimeZone());
  }

  /**
   * Tests creation of multiple calendars with different timezones.
   */
  @Test
  public void testCreateMultipleCalendarsWithDifferentTimezones() {
    controller.executeCommand("create calendar --name Personal --timezone America/Los_Angeles");
    controller.executeCommand("create calendar --name Work --timezone Europe/London");
    controller.executeCommand("create calendar --name Travel --timezone Asia/Tokyo");

    assertEquals(3, multiCalendar.getCalendars().size());
    assertEquals(ZoneId.of("America/Los_Angeles"), multiCalendar.getCalendars().get(0).getTimeZone());
    assertEquals(ZoneId.of("Europe/London"), multiCalendar.getCalendars().get(1).getTimeZone());
    assertEquals(ZoneId.of("Asia/Tokyo"), multiCalendar.getCalendars().get(2).getTimeZone());
  }

  /**
   * Tests that creating a calendar with a duplicate name throws an exception.
   */
  @Test
  public void testCreateCalendarDuplicateName() {
    controller.executeCommand("create calendar --name Work --timezone America/New_York");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("create calendar --name Work --timezone Europe/Paris");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("already exists"));
  }

  /**
   * Tests that creating a calendar with invalid timezone format throws an exception.
   */
  @Test
  public void testCreateCalendarInvalidTimezone() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("create calendar --name Test --timezone Invalid/Timezone");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("timezone"));
  }

  /**
   * Tests creation of calendars with various valid IANA timezone formats.
   */
  @Test
  public void testCreateCalendarVariousValidTimezones() {
    String[] validTimezones = {
            "America/New_York",
            "Europe/Paris",
            "Asia/Kolkata",
            "Australia/Sydney",
            "Africa/Cairo",
            "Pacific/Auckland",
            "America/Argentina/Buenos_Aires"
    };

    for (int i = 0; i < validTimezones.length; i++) {
      String command = String.format("create calendar --name Cal%d --timezone %s", i, validTimezones[i]);
      controller.executeCommand(command);
    }

    assertEquals(validTimezones.length, multiCalendar.getCalendars().size());
  }

  // ========== EDIT CALENDAR COMMAND TESTS ==========

  /**
   * Tests successful modification of calendar name property.
   */
  @Test
  public void testEditCalendarName() {
    controller.executeCommand("create calendar --name OldName --timezone America/New_York");
    controller.executeCommand("edit calendar --name OldName --property name NewName");

    assertEquals("NewName", multiCalendar.getCalendars().get(0).getName());
    assertEquals(1, multiCalendar.getCalendars().size());
  }

  /**
   * Tests successful modification of calendar timezone property.
   */
  @Test
  public void testEditCalendarTimezone() {
    controller.executeCommand("create calendar --name Work --timezone America/New_York");
    controller.executeCommand("edit calendar --name Work --property timezone Europe/London");

    assertEquals(ZoneId.of("Europe/London"), multiCalendar.getCalendars().get(0).getTimeZone());
  }

  /**
   * Tests that editing a non-existent calendar throws an exception.
   */
  @Test
  public void testEditNonExistentCalendar() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("edit calendar --name NonExistent --property name NewName");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("not found"));
  }

  /**
   * Tests that editing with invalid property name throws an exception.
   */
  @Test
  public void testEditCalendarInvalidProperty() {
    controller.executeCommand("create calendar --name Test --timezone America/New_York");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("edit calendar --name Test --property color blue");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("property"));
  }

  /**
   * Tests that changing calendar name to an existing name throws an exception.
   */
  @Test
  public void testEditCalendarNameToDuplicate() {
    controller.executeCommand("create calendar --name Cal1 --timezone America/New_York");
    controller.executeCommand("create calendar --name Cal2 --timezone America/New_York");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("edit calendar --name Cal2 --property name Cal1");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("already exists"));
  }

  // ========== USE CALENDAR COMMAND TESTS ==========

  /**
   * Tests that use calendar command properly sets the active calendar context.
   */
  @Test
  public void testUseCalendarSetsContext() {
    controller.executeCommand("create calendar --name Personal --timezone America/New_York");
    controller.executeCommand("create calendar --name Work --timezone Europe/London");
    controller.executeCommand("use calendar --name Work");

    assertEquals("Work", multiCalendar.getCurrent().getName());
  }

  /**
   * Tests that using a non-existent calendar throws an exception.
   */
  @Test
  public void testUseNonExistentCalendar() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("use calendar --name NonExistent");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("not found"));
  }

  /**
   * Tests that creating events without a calendar in use throws an exception.
   */
  @Test
  public void testCreateEventWithoutCalendarInUse() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("no calendar"));
  }

  /**
   * Tests switching context between multiple calendars.
   */
  @Test
  public void testSwitchBetweenCalendars() {
    controller.executeCommand("create calendar --name Cal1 --timezone America/New_York");
    controller.executeCommand("create calendar --name Cal2 --timezone Europe/Paris");

    controller.executeCommand("use calendar --name Cal1");
    assertEquals("Cal1", multiCalendar.getCurrent().getName());

    controller.executeCommand("use calendar --name Cal2");
    assertEquals("Cal2", multiCalendar.getCurrent().getName());

    controller.executeCommand("use calendar --name Cal1");
    assertEquals("Cal1", multiCalendar.getCurrent().getName());
  }

  // ========== COPY EVENT COMMAND TESTS ==========

  /**
   * Tests copying a single event to another calendar in the same timezone.
   */
  @Test
  public void testCopyEventSameTimezone() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");
    controller.executeCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");

    controller.executeCommand("copy event Meeting on 2025-01-15T10:00 --target Target to 2025-01-20T14:00");

    controller.executeCommand("use calendar --name Target");
    String result = controller.executeCommand("print events on 2025-01-20");
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("14:00"));
    assertTrue(result.contains("15:00"));
  }

  /**
   * Tests copying an event from EST to PST with proper timezone conversion.
   */
  @Test
  public void testCopyEventAcrossTimezones() {
    controller.executeCommand("create calendar --name EastCoast --timezone America/New_York");
    controller.executeCommand("create calendar --name WestCoast --timezone America/Los_Angeles");
    controller.executeCommand("use calendar --name EastCoast");
    controller.executeCommand("create event Conference from 2025-01-15T14:00 to 2025-01-15T16:00");

    controller.executeCommand("copy event Conference on 2025-01-15T14:00 --target WestCoast to 2025-01-15T14:00");

    controller.executeCommand("use calendar --name WestCoast");
    String result = controller.executeCommand("print events on 2025-01-15");
    assertTrue(result.contains("Conference"));
    assertTrue(result.contains("14:00"));
    assertTrue(result.contains("16:00"));
  }

  /**
   * Tests that copying a non-existent event throws an exception.
   */
  @Test
  public void testCopyNonExistentEvent() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("copy event NonExistent on 2025-01-15T10:00 --target Target to 2025-01-20T10:00");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("not found"));
  }

  /**
   * Tests that all event properties are preserved when copying.
   */
  @Test
  public void testCopyEventPreservesAllProperties() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.executeCommand("edit event description Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with Important client meeting");
    controller.executeCommand("edit event location Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with PHYSICAL");
    controller.executeCommand("edit event status Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with PRIVATE");

    controller.executeCommand("copy event Meeting on 2025-01-15T10:00 --target Target to 2025-01-20T14:00");

    controller.executeCommand("use calendar --name Target");
    Event copiedEvent = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Meeting", copiedEvent.getSubject());
    assertEquals("Important client meeting", copiedEvent.getDesc());
    assertEquals("PHYSICAL", copiedEvent.getLocation().toString());
    assertEquals("PRIVATE", copiedEvent.getStatus().toString());
  }

  // ========== COPY EVENTS (SINGLE DAY) COMMAND TESTS ==========

  /**
   * Tests copying all events from a specific day to another calendar.
   */
  @Test
  public void testCopyEventsOnDay() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event Morning from 2025-01-15T09:00 to 2025-01-15T10:00");
    controller.executeCommand("create event Lunch from 2025-01-15T12:00 to 2025-01-15T13:00");
    controller.executeCommand("create event Afternoon from 2025-01-15T15:00 to 2025-01-15T16:30");

    controller.executeCommand("copy events on 2025-01-15 --target Target to 2025-01-20");

    controller.executeCommand("use calendar --name Target");
    List<Event> copiedEvents = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 1, 20));
    assertEquals(3, copiedEvents.size());
  }

  /**
   * Tests copying events across timezones with proper time adjustments.
   */
  @Test
  public void testCopyEventsWithTimezoneConversion() {
    controller.executeCommand("create calendar --name EST --timezone America/New_York");
    controller.executeCommand("create calendar --name PST --timezone America/Los_Angeles");
    controller.executeCommand("use calendar --name EST");

    controller.executeCommand("create event Meeting from 2025-01-15T14:00 to 2025-01-15T15:00");

    controller.executeCommand("copy events on 2025-01-15 --target PST to 2025-01-15");

    controller.executeCommand("use calendar --name PST");
    Event copiedEvent = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0), copiedEvent.getStart());
    assertEquals(LocalDateTime.of(2025, 1, 15, 12, 0), copiedEvent.getEnd());
  }

  /**
   * Tests copying events from a day with no events.
   */
  @Test
  public void testCopyEventsFromEmptyDay() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("copy events on 2025-01-15 --target Target to 2025-01-20");

    controller.executeCommand("use calendar --name Target");
    assertFalse(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 1, 20)));
  }

  // ========== COPY EVENTS BETWEEN DATES COMMAND TESTS ==========

  /**
   * Tests copying all events within a date range to another calendar.
   */
  @Test
  public void testCopyEventsBetweenDates() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event Day1 from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.executeCommand("create event Day2 from 2025-01-16T14:00 to 2025-01-16T15:00");
    controller.executeCommand("create event Day3 from 2025-01-17T09:00 to 2025-01-17T10:00");

    controller.executeCommand("copy events between 2025-01-15 and 2025-01-17 --target Target to 2025-01-20");

    controller.executeCommand("use calendar --name Target");
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 1, 20)));
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 1, 21)));
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 1, 22)));
  }

  /**
   * Tests copying only the occurrences of a recurring event that fall within the specified range.
   */
  @Test
  public void testCopyRecurringEventPartialOverlap() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event Weekly from 2025-01-13T10:00 to 2025-01-13T11:00 repeats M for 4 times");

    controller.executeCommand("copy events between 2025-01-10 and 2025-01-23 --target Target to 2025-02-01");

    controller.executeCommand("use calendar --name Target");
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 2, 1)));
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 2, 8)));
    assertFalse(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 1, 25)));
    assertFalse(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 2, 15)));
  }

  /**
   * Tests that copied recurring events maintain their series relationship in the target calendar.
   */
  @Test
  public void testCopyEventsMaintainsSeriesStatus() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event TeamMeeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats W for 3 times");

    controller.executeCommand("copy events between 2025-01-15 and 2025-01-29 " +
            "--target Target to 2025-02-01");

    controller.executeCommand("use calendar --name Target");
    controller.executeCommand("edit series subject TeamMeeting from 2025-02-01T10:00 with UpdatedMeeting");

    List<Event> feb1 = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 2, 1));
    List<Event> feb8 = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 2, 8));
    List<Event> feb15 = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 2, 15));

    assertEquals("UpdatedMeeting", feb1.get(0).getSubject());
    assertEquals("UpdatedMeeting", feb8.get(0).getSubject());
    assertEquals("UpdatedMeeting", feb15.get(0).getSubject());
  }

  /**
   * Tests that the end date in copy range is inclusive.
   */
  @Test
  public void testCopyEventsBetweenInclusiveEndDate() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event StartDay from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.executeCommand("create event EndDay from 2025-01-17T15:00 to 2025-01-17T16:00");

    controller.executeCommand("copy events between 2025-01-15 and 2025-01-17 --target Target to 2025-02-01");

    controller.executeCommand("use calendar --name Target");
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 2, 1)));
    assertTrue(multiCalendar.getCurrent().getCalendar().containsKey(LocalDate.of(2025, 2, 3)));
  }

  // ========== EDGE CASES AND ERROR HANDLING ==========

  /**
   * Tests that copying to a non-existent calendar throws an exception.
   */
  @Test
  public void testCopyToNonExistentCalendar() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("use calendar --name Source");
    controller.executeCommand("create event Test from 2025-01-15T10:00 to 2025-01-15T11:00");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("copy event Test on 2025-01-15T10:00 --target NonExistent to 2025-01-20T10:00");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("not found"));
  }

  /**
   * Tests that copy commands fail when no calendar is in use.
   */
  @Test
  public void testCopyWithoutCalendarInUse() {
    controller.executeCommand("create calendar --name Target --timezone America/New_York");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      controller.executeCommand("copy event Test on 2025-01-15T10:00 --target Target to 2025-01-20T10:00");
    });
    assertTrue(exception.getMessage().toLowerCase().contains("no calendar"));
  }

  /**
   * Tests copying events across multiple timezones with DST considerations.
   */
  @Test
  public void testComplexTimezoneConversion() {
    controller.executeCommand("create calendar --name Tokyo --timezone Asia/Tokyo");
    controller.executeCommand("create calendar --name London --timezone Europe/London");
    controller.executeCommand("use calendar --name Tokyo");

    controller.executeCommand("create event International from 2025-01-15T10:00 to 2025-01-15T11:00");

    controller.executeCommand("copy event International on 2025-01-15T10:00 --target London to 2025-01-15T09:00");

    controller.executeCommand("use calendar --name London");
    Event copiedEvent = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals(LocalDateTime.of(2025, 1, 15, 9, 0), copiedEvent.getStart());
  }

  /**
   * Tests creating and using calendars with spaces in their names.
   */
  @Test
  public void testCalendarNameWithSpaces() {
    controller.executeCommand("create calendar --name \"My Personal Calendar\" --timezone America/New_York");
    controller.executeCommand("use calendar --name \"My Personal Calendar\"");

    assertEquals("My Personal Calendar", multiCalendar.getCurrent().getName());
  }

  /**
   * Tests that all-day events remain all-day when copied.
   */
  @Test
  public void testCopyAllDayEvent() {
    controller.executeCommand("create calendar --name Source --timezone America/New_York");
    controller.executeCommand("create calendar --name Target --timezone Europe/London");
    controller.executeCommand("use calendar --name Source");

    controller.executeCommand("create event Holiday on 2025-01-15");

    controller.executeCommand("copy event Holiday on 2025-01-15T08:00 --target Target to 2025-01-20T08:00");

    controller.executeCommand("use calendar --name Target");
    Event copiedEvent = multiCalendar.getCurrent().getCalendar().get(LocalDate.of(2025, 1, 20)).get(0);
    assertTrue(copiedEvent.isAllDay());
  }
}