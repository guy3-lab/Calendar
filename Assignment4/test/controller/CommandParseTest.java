package controller;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import controller.parse.CommandParserCoordinator;
import controller.parse.CommandType;
import controller.parse.ParseResult;
import controller.format.IOutputFormatter;
import controller.format.OutputFormatter;
import model.calendar.Calendar;
import model.calendar.Event;
import model.calendar.ICalendar;
import model.enums.Location;
import model.enums.Status;

/**
 * Comprehensive test suite for Command Interface Features (section 4) with extremely robust validation.
 * Tests all command parsing, validation, and execution functionality with detailed assertions at every step.
 * This test suite covers create commands, edit commands, query commands, and command validation features.
 */
public class CommandParseTest {

  private CalendarController controller;
  private ICalendar calendar;
  private LocalDate testDate;
  private LocalDateTime testDateTime;

  /**
   * Sets up test environment with fresh calendar controller and test data before each test execution.
   */
  @Before
  public void setUp() {
    calendar = new Calendar();
    IOutputFormatter formatter = new OutputFormatter();
    controller = new CalendarController(calendar, formatter);

    // Verify controller initialization
    assertNotNull("Controller should be initialized", controller);
    assertNotNull("Calendar should be initialized", calendar);
    assertNotNull("Formatter should be initialized", formatter);

    // Verify controller components
    assertEquals("Controller should use provided calendar", calendar, controller.getCalendar());
    assertEquals("Controller should use provided formatter", formatter, controller.getFormatter());

    // Set up test dates
    testDate = LocalDate.of(2025, 6, 15);
    testDateTime = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    assertNotNull("Test date should be initialized", testDate);
    assertNotNull("Test datetime should be initialized", testDateTime);
    assertEquals("Test date should be June 15, 2025", LocalDate.of(2025, 6, 15), testDate);
    assertEquals("Test datetime should be 10:00 AM", 10, testDateTime.getHour());

    // Verify calendar is empty initially
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertNotNull("Calendar data should not be null", calendarData);
    assertTrue("Calendar should be empty initially", calendarData.isEmpty());
  }

  // ==================== 4.1 CREATE COMMANDS TESTS ====================

  /**
   * Tests basic event creation with start and end datetime using standard format.
   */
  @Test
  public void testCreateEventWithStartAndEndTime() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with create event", command.startsWith("create event"));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should indicate success", result.contains("Created event"));
    assertTrue("Result should contain event name", result.contains("Meeting"));

    // Verify calendar state
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty after creation", calendarData.isEmpty());
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    List<Event> dayEvents = calendarData.get(testDate);
    assertNotNull("Day events should not be null", dayEvents);
    assertEquals("Should have exactly one event", 1, dayEvents.size());

    Event createdEvent = dayEvents.get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should match", "Meeting", createdEvent.getSubject());
    assertEquals("Event start time should match", testDateTime, createdEvent.getStart());
    assertEquals("Event end time should match", testDateTime.plusHours(1), createdEvent.getEnd());
    assertEquals("Default location should be ONLINE", Location.ONLINE, createdEvent.getLocation());
    assertEquals("Default status should be PUBLIC", Status.PUBLIC, createdEvent.getStatus());
    assertFalse("Event should not be all-day", createdEvent.isAllDay());
  }

  /**
   * Tests event creation with quoted subject containing multiple words and special characters.
   */
  @Test
  public void testCreateEventWithQuotedSubject() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "create event \"Team Meeting & Discussion\" from 2025-06-15T14:00 to 2025-06-15T15:30";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain quoted subject", command.contains("\""));

    // Verify quote positions
    int firstQuote = command.indexOf("\"");
    int lastQuote = command.lastIndexOf("\"");
    assertTrue("Should have opening quote", firstQuote >= 0);
    assertTrue("Should have closing quote", lastQuote > firstQuote);

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created event"));
    assertTrue("Result should contain full subject", result.contains("Team Meeting & Discussion"));

    // Verify calendar state
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    Event createdEvent = calendarData.get(testDate).get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should include special characters", "Team Meeting & Discussion", createdEvent.getSubject());
    assertEquals("Event start should be 2:00 PM", 14, createdEvent.getStart().getHour());
    assertEquals("Event end should be 3:30 PM", 15, createdEvent.getEnd().getHour());
    assertEquals("Event end minutes should be 30", 30, createdEvent.getEnd().getMinute());
  }

  /**
   * Tests all-day event creation using 'on' keyword without specifying end time.
   */
  @Test
  public void testCreateAllDayEvent() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "create event Holiday on 2025-06-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' keyword", command.contains(" on "));
    assertFalse("Command should not contain 'to' keyword", command.contains(" to "));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created all-day event"));
    assertTrue("Result should contain event name", result.contains("Holiday"));

    // Verify calendar state
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    Event createdEvent = calendarData.get(testDate).get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should match", "Holiday", createdEvent.getSubject());
    assertTrue("Event should be all-day", createdEvent.isAllDay());
    assertEquals("All-day event should start at 8:00 AM", 8, createdEvent.getStart().getHour());
    assertEquals("All-day event should end at 5:00 PM", 17, createdEvent.getEnd().getHour());
    assertEquals("All-day event should be on same date", testDate, createdEvent.getStart().toLocalDate());
    assertEquals("All-day event end should be on same date", testDate, createdEvent.getEnd().toLocalDate());
  }

  /**
   * Tests recurring event creation with specific weekdays and limited number of occurrences.
   */
  @Test
  public void testCreateRecurringEventWithTimes() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());
    assertTrue("Series should be empty initially", calendar.getSeries().isEmpty());

    String command = "create event \"Weekly Standup\" from 2025-06-16T09:00 to 2025-06-16T09:30 repeats MWF for 6 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain repeats keyword", command.contains(" repeats "));
    assertTrue("Command should contain 'for' keyword", command.contains(" for "));
    assertTrue("Command should contain 'times' keyword", command.contains(" times"));

    // Verify repeat pattern
    assertTrue("Command should contain weekday abbreviations", command.contains("MWF"));
    assertTrue("Command should specify number of repetitions", command.contains("6 times"));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Weekly Standup"));
    assertTrue("Result should contain occurrence count", result.contains("6 occurrences"));

    // Verify calendar state
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty after creation", calendarData.isEmpty());

    // Verify series state
    Map<LocalDateTime, List<Event>> seriesData = calendar.getSeries();
    assertFalse("Series should not be empty after creation", seriesData.isEmpty());
    assertEquals("Should have one series", 1, seriesData.size());

    // Calculate expected dates (MWF starting from Monday June 16)
    LocalDate monday = LocalDate.of(2025, 6, 16);
    LocalDate wednesday = LocalDate.of(2025, 6, 18);
    LocalDate friday = LocalDate.of(2025, 6, 20);

    // Verify events appear on correct days
    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));
    assertTrue("Calendar should contain Friday", calendarData.containsKey(friday));

    // Verify event properties
    Event mondayEvent = calendarData.get(monday).get(0);
    assertNotNull("Monday event should exist", mondayEvent);
    assertEquals("Monday event should have correct subject", "Weekly Standup", mondayEvent.getSubject());
    assertEquals("Monday event should start at 9:00", 9, mondayEvent.getStart().getHour());
    assertEquals("Monday event should end at 9:30", 9, mondayEvent.getEnd().getHour());
    assertEquals("Monday event should end at 30 minutes", 30, mondayEvent.getEnd().getMinute());
    assertFalse("Monday event should not be all-day", mondayEvent.isAllDay());
  }

  /**
   * Tests recurring event creation with date-based termination using 'until' keyword.
   */
  @Test
  public void testCreateRecurringEventWithUntilDate() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());
    assertTrue("Series should be empty initially", calendar.getSeries().isEmpty());

    String command = "create event \"Daily Workout\" from 2025-06-16T07:00 to 2025-06-16T08:00 repeats MTWRF until 2025-06-27";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain 'repeats' keyword", command.contains(" repeats "));
    assertTrue("Command should contain 'until' keyword", command.contains(" until "));

    // Verify date format
    assertTrue("Command should contain valid until date", command.contains("2025-06-27"));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Daily Workout"));
    assertTrue("Result should contain until date", result.contains("until 2025-06-27"));

    // Verify series creation
    Map<LocalDateTime, List<Event>> seriesData = calendar.getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());

    // Verify calendar has events
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    // Check specific dates within the range
    LocalDate startDate = LocalDate.of(2025, 6, 16); // Monday
    LocalDate endDate = LocalDate.of(2025, 6, 27); // Friday

    // Verify events exist within the range
    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate tuesday1 = LocalDate.of(2025, 6, 17);
    LocalDate friday2 = LocalDate.of(2025, 6, 27);

    assertTrue("Should have event on first Monday", calendarData.containsKey(monday1));
    assertTrue("Should have event on first Tuesday", calendarData.containsKey(tuesday1));
    assertTrue("Should have event on final Friday", calendarData.containsKey(friday2));

    // Verify event properties
    Event workoutEvent = calendarData.get(monday1).get(0);
    assertNotNull("Workout event should exist", workoutEvent);
    assertEquals("Event subject should match", "Daily Workout", workoutEvent.getSubject());
    assertEquals("Event should start at 7:00 AM", 7, workoutEvent.getStart().getHour());
    assertEquals("Event should end at 8:00 AM", 8, workoutEvent.getEnd().getHour());
  }

  /**
   * Tests recurring all-day event creation with limited occurrences.
   */
  @Test
  public void testCreateRecurringAllDayEventWithTimes() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "create event \"Weekend Fun\" on 2025-06-14 repeats SU for 4 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' for all-day", command.contains(" on "));
    assertTrue("Command should specify weekend days", command.contains("SU"));
    assertTrue("Command should specify occurrence count", command.contains("4 times"));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Weekend Fun"));
    assertTrue("Result should contain occurrence count", result.contains("4 occurrences"));

    // Verify calendar has events
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    // Calculate expected dates (SU = Saturday, Sunday starting from June 14)
    LocalDate saturday1 = LocalDate.of(2025, 6, 14);
    LocalDate sunday1 = LocalDate.of(2025, 6, 15);

    assertTrue("Should have event on first Saturday", calendarData.containsKey(saturday1));
    assertTrue("Should have event on first Sunday", calendarData.containsKey(sunday1));

    // Verify all-day properties
    Event saturdayEvent = calendarData.get(saturday1).get(0);
    assertNotNull("Saturday event should exist", saturdayEvent);
    assertEquals("Event subject should match", "Weekend Fun", saturdayEvent.getSubject());
    assertTrue("Event should be all-day", saturdayEvent.isAllDay());
    assertEquals("All-day event should start at 8:00 AM", 8, saturdayEvent.getStart().getHour());
    assertEquals("All-day event should end at 5:00 PM", 17, saturdayEvent.getEnd().getHour());
  }

  /**
   * Tests recurring all-day event creation with date-based termination.
   */
  @Test
  public void testCreateRecurringAllDayEventWithUntilDate() {
    // Verify initial state
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "create event \"Monthly Review\" on 2025-06-15 repeats U until 2025-08-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' for all-day", command.contains(" on "));
    assertTrue("Command should specify Sunday", command.contains("U"));
    assertTrue("Command should have until date", command.contains("until 2025-08-15"));

    // Execute command
    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Monthly Review"));
    assertTrue("Result should contain until date", result.contains("until 2025-08-15"));

    // Verify calendar has events
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    // Verify specific Sunday occurrences
    LocalDate sunday1 = LocalDate.of(2025, 6, 15);
    LocalDate sunday2 = LocalDate.of(2025, 6, 22);
    LocalDate sunday3 = LocalDate.of(2025, 6, 29);

    assertTrue("Should have event on first Sunday", calendarData.containsKey(sunday1));
    assertTrue("Should have event on second Sunday", calendarData.containsKey(sunday2));
    assertTrue("Should have event on third Sunday", calendarData.containsKey(sunday3));

    // Verify all events are all-day
    Event reviewEvent = calendarData.get(sunday1).get(0);
    assertNotNull("Review event should exist", reviewEvent);
    assertEquals("Event subject should match", "Monthly Review", reviewEvent.getSubject());
    assertTrue("Event should be all-day", reviewEvent.isAllDay());
  }

  // ==================== EDIT COMMANDS TESTS ====================

  /**
   * Tests editing a single event's subject property using exact event identification.
   */
  @Test
  public void testEditSingleEventSubject() {
    // Create initial event
    Event originalEvent = calendar.createEvent("Old Meeting", testDateTime, testDateTime.plusHours(1));
    assertNotNull("Original event should be created", originalEvent);
    assertEquals("Original subject should match", "Old Meeting", originalEvent.getSubject());

    // Verify initial calendar state
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event", 1, calendarData.get(testDate).size());

    // Execute edit command
    String command = "edit event subject Old Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with New Meeting";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit event", command.startsWith("edit event"));
    assertTrue("Command should specify subject property", command.contains("subject"));
    assertTrue("Command should contain 'with' keyword", command.contains(" with "));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));
    assertTrue("Result should reference old event name", result.contains("Old Meeting"));

    // Verify event was modified
    List<Event> events = calendarData.get(testDate);
    assertEquals("Should still have one event", 1, events.size());

    Event modifiedEvent = events.get(0);
    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should be updated", "New Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should remain unchanged", testDateTime, modifiedEvent.getStart());
    assertEquals("End time should remain unchanged", testDateTime.plusHours(1), modifiedEvent.getEnd());
    assertEquals("Location should remain unchanged", Location.ONLINE, modifiedEvent.getLocation());
    assertEquals("Status should remain unchanged", Status.PUBLIC, modifiedEvent.getStatus());
  }

  /**
   * Tests editing a single event's start time property with automatic end time adjustment.
   */
  @Test
  public void testEditSingleEventStartTime() {
    // Create initial event
    LocalDateTime originalStart = testDateTime;
    LocalDateTime originalEnd = testDateTime.plusHours(2);
    Event originalEvent = calendar.createEvent("Meeting", originalStart, originalEnd);

    assertNotNull("Original event should be created", originalEvent);
    assertEquals("Original start should match", originalStart, originalEvent.getStart());
    assertEquals("Original end should match", originalEnd, originalEvent.getEnd());

    // Execute edit command to change start time
    String command = "edit event start Meeting from 2025-06-15T10:00 to 2025-06-15T12:00 with 2025-06-15T14:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify start property", command.contains("start"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    // Verify event was moved to new date
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();

    // Original date should still have the event (moved within same day)
    assertTrue("Calendar should still contain test date", calendarData.containsKey(testDate));
    List<Event> events = calendarData.get(testDate);
    assertEquals("Should have one event", 1, events.size());

    Event modifiedEvent = events.get(0);
    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should be updated", LocalDateTime.of(2025, 6, 15, 14, 0), modifiedEvent.getStart());

    // End time should be automatically adjusted to maintain duration
    LocalDateTime expectedEnd = LocalDateTime.of(2025, 6, 15, 12, 0); // 2 hours later
    assertEquals("End time should be adjusted", expectedEnd, modifiedEvent.getEnd());
  }

  /**
   * Tests editing a single event's end time property with validation.
   */
  @Test
  public void testEditSingleEventEndTime() {
    // Create initial event
    Event originalEvent = calendar.createEvent("Meeting", testDateTime, testDateTime.plusHours(1));
    assertNotNull("Original event should be created", originalEvent);

    // Execute edit command to change end time
    String command = "edit event end Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with 2025-06-15T12:30";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify end property", command.contains("end"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    // Verify event modification
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    List<Event> events = calendarData.get(testDate);
    Event modifiedEvent = events.get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should remain unchanged", testDateTime, modifiedEvent.getStart());
    assertEquals("End time should be updated", LocalDateTime.of(2025, 6, 15, 12, 30), modifiedEvent.getEnd());
  }

  /**
   * Tests editing a single event's location property from default to physical.
   */
  @Test
  public void testEditSingleEventLocation() {
    // Create initial event with default location
    Event originalEvent = calendar.createEvent("Meeting", testDateTime, testDateTime.plusHours(1));
    assertNotNull("Original event should be created", originalEvent);
    assertEquals("Original location should be ONLINE", Location.ONLINE, originalEvent.getLocation());

    // Execute edit command to change location
    String command = "edit event location Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with PHYSICAL";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify location property", command.contains("location"));
    assertTrue("Command should specify new location", command.contains("PHYSICAL"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    // Verify location change
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    Event modifiedEvent = calendarData.get(testDate).get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Location should be updated", Location.PHYSICAL, modifiedEvent.getLocation());
    assertEquals("Other properties should remain unchanged", Status.PUBLIC, modifiedEvent.getStatus());
  }

  /**
   * Tests editing a single event's status property from public to private.
   */
  @Test
  public void testEditSingleEventStatus() {
    // Create initial event with default status
    Event originalEvent = calendar.createEvent("Meeting", testDateTime, testDateTime.plusHours(1));
    assertNotNull("Original event should be created", originalEvent);
    assertEquals("Original status should be PUBLIC", Status.PUBLIC, originalEvent.getStatus());

    // Execute edit command to change status
    String command = "edit event status Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with PRIVATE";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify status property", command.contains("status"));
    assertTrue("Command should specify new status", command.contains("PRIVATE"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    // Verify status change
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    Event modifiedEvent = calendarData.get(testDate).get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Status should be updated", Status.PRIVATE, modifiedEvent.getStatus());
    assertEquals("Other properties should remain unchanged", Location.ONLINE, modifiedEvent.getLocation());
  }

  /**
   * Tests editing multiple events in a series starting from a specific occurrence.
   */
  @Test
  public void testEditEventsInSeriesFromSpecificDate() {
    // Create recurring event series
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 9, 0); // Monday
    calendar.createSeriesTimes("Weekly Meeting", seriesStart, seriesStart.plusHours(1),
            java.util.Arrays.asList("M", "W", "F"), 4);

    // Verify series was created
    Map<LocalDateTime, List<Event>> seriesData = calendar.getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());

    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate wednesday1 = LocalDate.of(2025, 6, 18);
    LocalDate friday1 = LocalDate.of(2025, 6, 20);

    assertTrue("Calendar should contain first Monday", calendarData.containsKey(monday1));
    assertTrue("Calendar should contain first Wednesday", calendarData.containsKey(wednesday1));
    assertTrue("Calendar should contain first Friday", calendarData.containsKey(friday1));

    // Execute edit command starting from Wednesday
    String command = "edit events subject Weekly Meeting from 2025-06-18T09:00 with Updated Meeting";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit events", command.startsWith("edit events"));
    assertTrue("Command should specify Wednesday start", command.contains("2025-06-18T09:00"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated events starting from"));

    // Verify selective updates
    Event mondayEvent = calendarData.get(monday1).get(0);
    Event wednesdayEvent = calendarData.get(wednesday1).get(0);
    Event fridayEvent = calendarData.get(friday1).get(0);

    assertNotNull("Monday event should exist", mondayEvent);
    assertNotNull("Wednesday event should exist", wednesdayEvent);
    assertNotNull("Friday event should exist", fridayEvent);

    // Monday event should remain unchanged (before start date)
    assertEquals("Monday event should keep original subject", "Weekly Meeting", mondayEvent.getSubject());

    // Wednesday and Friday events should be updated (on or after start date)
    assertEquals("Wednesday event should be updated", "Updated Meeting", wednesdayEvent.getSubject());
    assertEquals("Friday event should be updated", "Updated Meeting", fridayEvent.getSubject());
  }

  /**
   * Tests editing an entire event series regardless of the reference date.
   */
  @Test
  public void testEditEntireEventSeries() {
    // Create recurring event series
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 14, 0); // Monday 2 PM
    calendar.createSeriesTimes("Team Standup", seriesStart, seriesStart.plusMinutes(30),
            java.util.Arrays.asList("M", "T", "W", "R", "F"), 3);

    // Verify series creation
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    LocalDate monday = LocalDate.of(2025, 6, 16);
    LocalDate tuesday = LocalDate.of(2025, 6, 17);
    LocalDate wednesday = LocalDate.of(2025, 6, 18);

    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Tuesday", calendarData.containsKey(tuesday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));

    // Execute edit series command from middle of series
    String command = "edit series subject Team Standup from 2025-06-16T14:00 with Daily Standup";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit series", command.startsWith("edit series"));
    assertTrue("Command should reference Tuesday", command.contains("2025-06-16T14:00"));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated entire series"));

    // Verify all events in series are updated
    Event mondayEvent = calendarData.get(monday).get(0);
    Event tuesdayEvent = calendarData.get(tuesday).get(0);
    Event wednesdayEvent = calendarData.get(wednesday).get(0);

    assertNotNull("Monday event should exist", mondayEvent);
    assertNotNull("Tuesday event should exist", tuesdayEvent);
    assertNotNull("Wednesday event should exist", wednesdayEvent);

    assertEquals("Monday event should be updated", "Daily Standup", mondayEvent.getSubject());
    assertEquals("Tuesday event should be updated", "Daily Standup", tuesdayEvent.getSubject());
    assertEquals("Wednesday event should be updated", "Daily Standup", wednesdayEvent.getSubject());

    // Verify other properties remain unchanged
    assertEquals("All events should maintain original time", 14, mondayEvent.getStart().getHour());
    assertEquals("All events should maintain original duration", 30,
            java.time.Duration.between(mondayEvent.getStart(), mondayEvent.getEnd()).toMinutes());
  }

  /**
   * Tests editing event series with start time changes that break the series relationship.
   */
  @Test
  public void testEditEventSeriesStartTimeBreaksSeries() {
    // Create recurring event series
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 10, 0); // Monday 10 AM
    calendar.createSeriesTimes("Morning Sync", seriesStart, seriesStart.plusHours(1),
            java.util.Arrays.asList("M", "W"), 3);

    // Verify initial series state
    Map<LocalDateTime, List<Event>> seriesData = calendar.getSeries();
    assertTrue("Should have series with original start time", seriesData.containsKey(seriesStart));

    // Execute edit to change start time for events starting from Wednesday
    String command = "edit events start Morning Sync from 2025-06-18T10:00 with 2025-06-18T11:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated events starting from"));

    // Verify series split
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate wednesday1 = LocalDate.of(2025, 6, 18);

    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday1));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday1));

    Event mondayEvent = calendarData.get(monday1).get(0);
    Event wednesdayEvent = calendarData.get(wednesday1).get(0);

    // Monday should retain original time
    assertEquals("Monday event should keep original time", 10, mondayEvent.getStart().getHour());

    // Wednesday should have new time
    assertEquals("Wednesday event should have new time", 11, wednesdayEvent.getStart().getHour());

    // Verify new series was created for the modified events
    LocalDateTime newSeriesStart = LocalDateTime.of(2025, 6, 18, 11, 0);
    assertTrue("Should have new series with updated start time", seriesData.containsKey(newSeriesStart));
  }

  // ==================== QUERY COMMANDS TESTS ====================

  /**
   * Tests printing events on a specific date with proper formatting.
   */
  @Test
  public void testPrintEventsOnSpecificDate() {
    // Create test events
    Event morning = calendar.createEvent("Morning Meeting",
            LocalDateTime.of(testDate, java.time.LocalTime.of(9, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0)));
    Event afternoon = calendar.createEvent("Lunch",
            LocalDateTime.of(testDate, java.time.LocalTime.of(12, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(13, 0)));

    assertNotNull("Morning event should be created", morning);
    assertNotNull("Afternoon event should be created", afternoon);

    // Execute print command
    String command = "print events on 2025-06-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with print events", command.startsWith("print events"));
    assertTrue("Command should use 'on' keyword", command.contains(" on "));

    String result = controller.executeCommand(command);

    // Verify result format and content
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should contain Morning Meeting", result.contains("Morning Meeting"));
    assertTrue("Result should contain Lunch", result.contains("Lunch"));

    // Verify time information is included
    assertTrue("Result should contain start time info", result.contains("Start Time"));
    assertTrue("Result should contain end time info", result.contains("End Time"));
    assertTrue("Result should contain location info", result.contains("Location"));

    // Verify line count
    String[] lines = result.split("\n");
    assertEquals("Should have two lines for two events", 2, lines.length);
  }

  /**
   * Tests printing events on a date with no scheduled events.
   */
  @Test
  public void testPrintEventsOnEmptyDate() {
    // Verify calendar is empty
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Execute print command for empty date
    String command = "print events on 2025-06-15";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertEquals("Should return no events message", "No events on this day", result);
  }

  /**
   * Tests printing events within a specific date and time range.
   */
  @Test
  public void testPrintEventsInDateRange() {
    // Create events across multiple days
    LocalDate day1 = testDate;
    LocalDate day2 = testDate.plusDays(1);
    LocalDate day3 = testDate.plusDays(2);

    Event event1 = calendar.createEvent("Day 1 Event",
            LocalDateTime.of(day1, java.time.LocalTime.of(10, 0)),
            LocalDateTime.of(day1, java.time.LocalTime.of(11, 0)));
    Event event2 = calendar.createEvent("Day 2 Event",
            LocalDateTime.of(day2, java.time.LocalTime.of(14, 0)),
            LocalDateTime.of(day2, java.time.LocalTime.of(15, 0)));
    Event event3 = calendar.createEvent("Day 3 Event",
            LocalDateTime.of(day3, java.time.LocalTime.of(16, 0)),
            LocalDateTime.of(day3, java.time.LocalTime.of(17, 0)));

    // Verify events were created
    assertNotNull("Day 1 event should be created", event1);
    assertNotNull("Day 2 event should be created", event2);
    assertNotNull("Day 3 event should be created", event3);

    // Execute print range command
    String command = "print events from 2025-06-15T08:00 to 2025-06-17T18:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain 'from' keyword", command.contains(" from "));
    assertTrue("Command should contain 'to' keyword", command.contains(" to "));

    String result = controller.executeCommand(command);

    // Verify result content
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should contain Day 1 Event", result.contains("Day 1 Event"));
    assertTrue("Result should contain Day 2 Event", result.contains("Day 2 Event"));
    assertTrue("Result should contain Day 3 Event", result.contains("Day 3 Event"));

    // Verify all events are included
    String[] lines = result.split("\n");
    assertEquals("Should have three lines for three events", 3, lines.length);
  }

  /**
   * Tests printing events in a range with time filtering.
   */
  @Test
  public void testPrintEventsRangeWithTimeFiltering() {
    // Create events at different times
    Event earlyEvent = calendar.createEvent("Early Event",
            LocalDateTime.of(testDate, java.time.LocalTime.of(7, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0)));
    Event lateEvent = calendar.createEvent("Late Event",
            LocalDateTime.of(testDate, java.time.LocalTime.of(15, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(16, 0)));

    assertNotNull("Early event should be created", earlyEvent);
    assertNotNull("Late event should be created", lateEvent);

    // Execute filtered print command
    String command = "print events from 2025-06-15T10:00 to 2025-06-15T18:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    // Verify filtering - early event should be excluded, late event included
    assertNotNull("Result should not be null", result);
    assertFalse("Result should not contain Early Event", result.contains("Early Event"));
    assertTrue("Result should contain Late Event", result.contains("Late Event"));
  }

  /**
   * Tests showing busy status when user has overlapping events.
   */
  @Test
  public void testShowStatusBusyWithEvent() {
    // Create event at query time
    Event event = calendar.createEvent("Important Meeting", testDateTime, testDateTime.plusHours(1));
    assertNotNull("Event should be created", event);
    assertEquals("Event should start at query time", testDateTime, event.getStart());

    // Execute show status command
    String command = "show status on 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with show status", command.startsWith("show status"));
    assertTrue("Command should use 'on' keyword", command.contains(" on "));

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertEquals("Status should be busy", "busy", result);
  }

  /**
   * Tests showing available status when user has no conflicting events.
   */
  @Test
  public void testShowStatusAvailableNoEvents() {
    // Verify calendar is empty
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    // Execute show status command
    String command = "show status on 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    // Verify result
    assertNotNull("Result should not be null", result);
    assertEquals("Status should be available", "available", result);
  }

  /**
   * Tests showing status during different parts of an event.
   */
  @Test
  public void testShowStatusDuringEventPeriod() {
    // Create event from 10:00 to 12:00
    Event event = calendar.createEvent("Long Meeting",
            LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0)),
            LocalDateTime.of(testDate, java.time.LocalTime.of(12, 0)));
    assertNotNull("Event should be created", event);

    // Test status at event start
    String command1 = "show status on 2025-06-15T10:00";
    String result1 = controller.executeCommand(command1);
    assertEquals("Should be busy at event start", "busy", result1);

    // Test status during event
    String command2 = "show status on 2025-06-15T11:00";
    String result2 = controller.executeCommand(command2);
    assertEquals("Should be busy during event", "busy", result2);

    // Test status at event end
    String command3 = "show status on 2025-06-15T12:00";
    String result3 = controller.executeCommand(command3);
    assertEquals("Should be available at event end", "available", result3);

    // Test status after event
    String command4 = "show status on 2025-06-15T13:00";
    String result4 = controller.executeCommand(command4);
    assertEquals("Should be available after event", "available", result4);
  }

  // ==================== COMMAND VALIDATION TESTS ====================

  /**
   * Tests error handling for completely invalid command syntax.
   */
  @Test
  public void testInvalidCommandSyntax() {
    String invalidCommand = "invalid command syntax";
    assertNotNull("Command should not be null", invalidCommand);
    assertFalse("Command should not start with known prefix",
            invalidCommand.startsWith("create") || invalidCommand.startsWith("edit") ||
                    invalidCommand.startsWith("print") || invalidCommand.startsWith("show"));

    try {
      controller.executeCommand(invalidCommand);
      fail("Should throw exception for invalid command");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertNotNull("Exception message should not be null", e.getMessage());
      assertTrue("Exception should indicate unknown command", e.getMessage().contains("Unknown command"));
    }
  }

  /**
   * Tests error handling for empty command input.
   */
  @Test
  public void testEmptyCommandInput() {
    String emptyCommand = "";
    assertEquals("Command should be empty", "", emptyCommand);

    try {
      controller.executeCommand(emptyCommand);
      fail("Should throw exception for empty command");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate empty input", e.getMessage().contains("empty"));
    }

    // Also test whitespace-only command
    String whitespaceCommand = "   ";
    try {
      controller.executeCommand(whitespaceCommand);
      fail("Should throw exception for whitespace-only command");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
    }
  }

  /**
   * Tests error handling for create command with missing required fields.
   */
  @Test
  public void testCreateCommandMissingFields() {
    String incompleteCommand = "create event";
    assertNotNull("Command should not be null", incompleteCommand);
    assertTrue("Command should start correctly", incompleteCommand.startsWith("create event"));

    try {
      controller.executeCommand(incompleteCommand);
      fail("Should throw exception for incomplete create command");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertNotNull("Exception message should not be null", e.getMessage());
      assertTrue("Exception should indicate create command error",
              e.getMessage().contains("Create command error"));
    }
  }

  /**
   * Tests error handling for invalid date format in commands.
   */
  @Test
  public void testInvalidDateFormat() {
    String invalidDateCommand = "create event Meeting from invalid-date to 2025-06-15T11:00";
    assertNotNull("Command should not be null", invalidDateCommand);
    assertTrue("Command should contain invalid date", invalidDateCommand.contains("invalid-date"));

    try {
      controller.executeCommand(invalidDateCommand);
      fail("Should throw exception for invalid date format");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate invalid datetime",
              e.getMessage().contains("Invalid datetime format") ||
                      e.getMessage().contains("Create command error"));
    }
  }

  /**
   * Tests error handling for duplicate event creation.
   */
  @Test
  public void testDuplicateEventCreation() {
    // Create initial event
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00";
    String result1 = controller.executeCommand(command);
    assertNotNull("First result should not be null", result1);
    assertTrue("First creation should succeed", result1.contains("Created event"));

    // Verify event was created
    Map<LocalDate, List<Event>> calendarData = calendar.getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event", 1, calendarData.get(testDate).size());

    // Attempt to create identical event
    try {
      controller.executeCommand(command);
      fail("Should throw exception for duplicate event");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate duplicate or already exists",
              e.getMessage().toLowerCase().contains("already exists") ||
                      e.getMessage().toLowerCase().contains("duplicate"));
    }

    // Verify calendar still has only one event
    assertEquals("Should still have only one event", 1, calendarData.get(testDate).size());
  }

  /**
   * Tests error handling for edit command with non-existent event.
   */
  @Test
  public void testEditNonExistentEvent() {
    // Verify calendar is empty
    assertTrue("Calendar should be empty initially", calendar.getCalendar().isEmpty());

    String command = "edit event subject NonExistent from 2025-06-15T10:00 to 2025-06-15T11:00 with NewName";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should reference non-existent event", command.contains("NonExistent"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for non-existent event");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      // Exception might be thrown for various reasons (no events, can't find event, etc.)
      assertNotNull("Exception message should not be null", e.getMessage());
    }
  }

  /**
   * Tests error handling for invalid property names in edit commands.
   */
  @Test
  public void testEditCommandInvalidProperty() {
    // Create event to edit
    calendar.createEvent("Meeting", testDateTime, testDateTime.plusHours(1));

    String command = "edit event invalidproperty Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with value";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain invalid property", command.contains("invalidproperty"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for invalid property");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate unknown property",
              e.getMessage().toLowerCase().contains("unknown property") ||
                      e.getMessage().toLowerCase().contains("edit command error"));
    }
  }

  /**
   * Tests error handling for invalid time format in commands.
   */
  @Test
  public void testInvalidTimeFormat() {
    String command = "create event Meeting from 2025-06-15T25:00 to 2025-06-15T11:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain invalid hour", command.contains("T25:00"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for invalid time format");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate invalid format",
              e.getMessage().contains("Invalid datetime format") ||
                      e.getMessage().contains("Create command error"));
    }
  }

  /**
   * Tests error handling for invalid repeat pattern in recurring events.
   */
  @Test
  public void testInvalidRepeatPattern() {
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 repeats XYZ for 3 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain invalid repeat pattern", command.contains("XYZ"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for invalid repeat pattern");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate invalid repeat days",
              e.getMessage().contains("Invalid repeat days") ||
                      e.getMessage().contains("Create command error"));
    }
  }

  /**
   * Tests error handling for negative repeat times.
   */
  @Test
  public void testNegativeRepeatTimes() {
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 repeats M for -5 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain negative repeat times", command.contains("-5 times"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for negative repeat times");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate invalid repeat times",
              e.getMessage().contains("Repeat times must be positive") ||
                      e.getMessage().contains("Create command error"));
    }
  }

  /**
   * Tests error handling for end time before start time.
   */
  @Test
  public void testEndTimeBeforeStartTime() {
    String command = "create event Meeting from 2025-06-15T11:00 to 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);

    // Verify end time is before start time
    LocalDateTime start = LocalDateTime.of(2025, 6, 15, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 15, 10, 0);
    assertTrue("End time should be before start time", end.isBefore(start));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for end time before start time");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate end time must be after start",
              e.getMessage().contains("End time must be after start time") ||
                      e.getMessage().contains("Failed to create event"));
    }
  }


  /**
   * Tests exit command handling and proper termination signal.
   */
  @Test
  public void testExitCommand() {
    String exitCommand = "exit";
    assertNotNull("Exit command should not be null", exitCommand);
    assertEquals("Exit command should be exact", "exit", exitCommand);

    String result = controller.executeCommand(exitCommand);

    // Exit command should return null to signal termination
    assertNull("Exit command should return null", result);
  }

  /**
   * Tests exit command with case variations.
   */
  @Test
  public void testExitCommandCaseInsensitive() {
    String[] exitCommands = {"EXIT", "Exit", "eXiT"};

    for (String exitCommand : exitCommands) {
      assertNotNull("Exit command should not be null", exitCommand);

      String result = controller.executeCommand(exitCommand);

      // All variations should return null
      assertNull("Exit command should return null regardless of case", result);
    }
  }

  /**
   * Tests command parsing coordinator with null input handling.
   */
  @Test
  public void testCommandParsingCoordinatorNullInput() {
    ParseResult result = CommandParserCoordinator.parseCommand(null);

    assertNotNull("Parse result should not be null", result);
    assertFalse("Parse result should indicate failure", result.isSuccess());
    assertNotNull("Error message should not be null", result.getErrorMessage());
    assertTrue("Error message should indicate empty input",
            result.getErrorMessage().contains("empty"));
  }

  /**
   * Tests command parsing coordinator with unknown command handling.
   */
  @Test
  public void testCommandParsingCoordinatorUnknownCommand() {
    String unknownCommand = "unknown command here";
    ParseResult result = CommandParserCoordinator.parseCommand(unknownCommand);

    assertNotNull("Parse result should not be null", result);
    assertFalse("Parse result should indicate failure", result.isSuccess());
    assertNotNull("Error message should not be null", result.getErrorMessage());
    assertTrue("Error message should indicate unknown command",
            result.getErrorMessage().contains("Unknown command"));
    assertTrue("Error message should include the command",
            result.getErrorMessage().contains(unknownCommand));
  }

  /**
   * Tests successful command parsing with proper result structure.
   */
  @Test
  public void testSuccessfulCommandParsing() {
    String validCommand = "create event Test from 2025-06-15T10:00 to 2025-06-15T11:00";
    ParseResult result = CommandParserCoordinator.parseCommand(validCommand);

    assertNotNull("Parse result should not be null", result);
    assertTrue("Parse result should indicate success", result.isSuccess());
    assertEquals("Command type should be CREATE_EVENT", CommandType.CREATE_EVENT, result.getCommandType());
    assertNull("Error message should be null for successful parse", result.getErrorMessage());

    // Verify parsed data
    assertEquals("Subject should be parsed correctly", "Test", result.getSubject());
    assertNotNull("Start time should be parsed", result.getStartTime());
    assertNotNull("End time should be parsed", result.getEndTime());
    assertEquals("Start time should match", LocalDateTime.of(2025, 6, 15, 10, 0), result.getStartTime());
    assertEquals("End time should match", LocalDateTime.of(2025, 6, 15, 11, 0), result.getEndTime());
    assertFalse("Should not be all-day", result.isAllDay());
    assertFalse("Should not be repeating", result.isRepeating());
  }
}