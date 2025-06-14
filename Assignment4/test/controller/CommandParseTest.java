package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import controller.parse.CommandParserCoordinator;
import controller.parse.CommandType;
import controller.parse.ParseResult;
import model.calendar.IEvent;
import model.enums.Location;
import model.enums.Status;

/**
 * Tests the command parser.
 */
public class CommandParseTest {

  private CalendarController controller;
  private LocalDate testDate;
  private LocalDateTime testDateTime;

  @Before
  public void setUp() {
    controller = new CalendarController();

    assertNotNull("Controller should be initialized", controller);
    assertNotNull("MultiCalendar should be initialized", controller.getMultiCalendar());

    controller.executeCommand("create calendar --name default --timezone America/New_York");
    controller.executeCommand("use calendar --name default");

    testDate = LocalDate.of(2025, 6, 15);
    testDateTime = LocalDateTime.of(testDate, java.time.LocalTime.of(10, 0));

    assertNotNull("Test date should be initialized", testDate);
    assertNotNull("Test datetime should be initialized", testDateTime);
    assertEquals("Test date should be June 15, 2025", LocalDate.of(2025, 6, 15), testDate);
    assertEquals("Test datetime should be 10:00 AM", 10, testDateTime.getHour());

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().
            getCurrent().getCalendar();
    assertNotNull("Calendar data should not be null", calendarData);
    assertTrue("Calendar should be empty initially", calendarData.isEmpty());
  }

  // ==================== CREATE COMMANDS TESTS ====================

  @Test
  public void testCreateEventWithStartAndEndTime() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().
            getCurrent().getCalendar().isEmpty());

    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with create event", command.startsWith("create event"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should indicate success", result.contains("Created event"));
    assertTrue("Result should contain event name", result.contains("Meeting"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().
            getCurrent().getCalendar();
    assertFalse("Calendar should not be empty after creation", calendarData.isEmpty());
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    List<IEvent> dayEvents = calendarData.get(testDate);
    assertNotNull("Day events should not be null", dayEvents);
    assertEquals("Should have exactly one event", 1, dayEvents.size());

    IEvent createdEvent = dayEvents.get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should match", "Meeting", createdEvent.getSubject());
    assertEquals("Event start time should match", testDateTime, createdEvent.getStart());
    assertEquals("Event end time should match", testDateTime.plusHours(1), createdEvent.getEnd());
    assertEquals("Default location should be ONLINE", Location.ONLINE, createdEvent.getLocation());
    assertEquals("Default status should be PUBLIC", Status.PUBLIC, createdEvent.getStatus());
  }

  @Test
  public void testCreateEventWithQuotedSubject() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "create event \"Team Meeting & Discussion\" " +
            "from 2025-06-15T14:00 to 2025-06-15T15:30";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain quoted subject", command.contains("\""));

    int firstQuote = command.indexOf("\"");
    int lastQuote = command.lastIndexOf("\"");
    assertTrue("Should have opening quote", firstQuote >= 0);
    assertTrue("Should have closing quote", lastQuote > firstQuote);

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created event"));
    assertTrue("Result should contain full subject", result.contains("Team Meeting & Discussion"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    IEvent createdEvent = calendarData.get(testDate).get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should include special characters", "Team Meeting & Discussion",
            createdEvent.getSubject());
    assertEquals("Event start should be 2:00 PM", 14, createdEvent.getStart().getHour());
    assertEquals("Event end should be 3:30 PM", 15, createdEvent.getEnd().getHour());
    assertEquals("Event end minutes should be 30", 30, createdEvent.getEnd().getMinute());
  }

  @Test
  public void testCreateAllDayEvent() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "create event Holiday on 2025-06-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' keyword", command.contains(" on "));
    assertFalse("Command should not contain 'to' keyword", command.contains(" to "));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created all-day event"));
    assertTrue("Result should contain event name", result.contains("Holiday"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));

    IEvent createdEvent = calendarData.get(testDate).get(0);
    assertNotNull("Created event should not be null", createdEvent);
    assertEquals("Event subject should match", "Holiday", createdEvent.getSubject());
    assertEquals("All-day event should start at 8:00 AM", 8, createdEvent.getStart().getHour());
    assertEquals("All-day event should end at 5:00 PM", 17, createdEvent.getEnd().getHour());
    assertEquals("All-day event should be on same date", testDate, createdEvent.getStart().
            toLocalDate());
    assertEquals("All-day event end should be on same date", testDate, createdEvent.getEnd().
            toLocalDate());
  }

  @Test
  public void testCreateRecurringEventWithTimes() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());
    assertTrue("Series should be empty initially", controller.getMultiCalendar().getCurrent().
            getSeries().isEmpty());

    String command = "create event \"Weekly Standup\" from 2025-06-16T09:00 to 2025-06-16T09:30 " +
            "repeats MWF for 6 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain repeats keyword", command.contains(" repeats "));
    assertTrue("Command should contain 'for' keyword", command.contains(" for "));
    assertTrue("Command should contain 'times' keyword", command.contains(" times"));

    assertTrue("Command should contain weekday abbreviations", command.contains("MWF"));
    assertTrue("Command should specify number of repetitions", command.contains("6 times"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Weekly Standup"));
    assertTrue("Result should contain occurrence count", result.contains("6 occurrences"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertFalse("Calendar should not be empty after creation", calendarData.isEmpty());

    Map<LocalDateTime, List<IEvent>> seriesData = controller.getMultiCalendar().getCurrent().
            getSeries();
    assertFalse("Series should not be empty after creation", seriesData.isEmpty());
    assertEquals("Should have one series", 1, seriesData.size());

    LocalDate monday = LocalDate.of(2025, 6, 16);
    LocalDate wednesday = LocalDate.of(2025, 6, 18);
    LocalDate friday = LocalDate.of(2025, 6, 20);

    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));
    assertTrue("Calendar should contain Friday", calendarData.containsKey(friday));

    IEvent mondayEvent = calendarData.get(monday).get(0);
    assertNotNull("Monday event should exist", mondayEvent);
    assertEquals("Monday event should have correct subject", "Weekly Standup",
            mondayEvent.getSubject());
    assertEquals("Monday event should start at 9:00", 9, mondayEvent.getStart().getHour());
    assertEquals("Monday event should end at 9:30", 9, mondayEvent.getEnd().getHour());
    assertEquals("Monday event should end at 30 minutes", 30, mondayEvent.getEnd().getMinute());
  }

  @Test
  public void testCreateRecurringEventWithUntilDate() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());
    assertTrue("Series should be empty initially", controller.getMultiCalendar().getCurrent().
            getSeries().isEmpty());

    String command = "create event \"Daily Workout\" from 2025-06-16T07:00 to 2025-06-16T08:00 " +
            "repeats MTWRF until 2025-06-27";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain 'repeats' keyword", command.contains(" repeats "));
    assertTrue("Command should contain 'until' keyword", command.contains(" until "));

    assertTrue("Command should contain valid until date", command.contains("2025-06-27"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Daily Workout"));
    assertTrue("Result should contain until date", result.contains("until 2025-06-27"));

    Map<LocalDateTime, List<IEvent>> seriesData = controller.getMultiCalendar().getCurrent().
            getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    LocalDate startDate = LocalDate.of(2025, 6, 16);
    LocalDate endDate = LocalDate.of(2025, 6, 27);

    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate tuesday1 = LocalDate.of(2025, 6, 17);
    LocalDate friday2 = LocalDate.of(2025, 6, 27);

    assertTrue("Should have event on first Monday", calendarData.containsKey(monday1));
    assertTrue("Should have event on first Tuesday", calendarData.containsKey(tuesday1));
    assertTrue("Should have event on final Friday", calendarData.containsKey(friday2));

    IEvent workoutEvent = calendarData.get(monday1).get(0);
    assertNotNull("Workout event should exist", workoutEvent);
    assertEquals("Event subject should match", "Daily Workout", workoutEvent.getSubject());
    assertEquals("Event should start at 7:00 AM", 7, workoutEvent.getStart().getHour());
    assertEquals("Event should end at 8:00 AM", 8, workoutEvent.getEnd().getHour());
  }

  @Test
  public void testCreateRecurringAllDayEventWithTimes() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "create event \"Weekend Fun\" on 2025-06-14 repeats SU for 4 times";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' for all-day", command.contains(" on "));
    assertTrue("Command should specify weekend days", command.contains("SU"));
    assertTrue("Command should specify occurrence count", command.contains("4 times"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Weekend Fun"));
    assertTrue("Result should contain occurrence count", result.contains("4 occurrences"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    LocalDate saturday1 = LocalDate.of(2025, 6, 14);
    LocalDate sunday1 = LocalDate.of(2025, 6, 15);

    assertTrue("Should have event on first Saturday", calendarData.containsKey(saturday1));
    assertTrue("Should have event on first Sunday", calendarData.containsKey(sunday1));

    IEvent saturdayEvent = calendarData.get(saturday1).get(0);
    assertNotNull("Saturday event should exist", saturdayEvent);
    assertEquals("Event subject should match", "Weekend Fun", saturdayEvent.getSubject());
    assertEquals("All-day event should start at 8:00 AM", 8, saturdayEvent.getStart().getHour());
    assertEquals("All-day event should end at 5:00 PM", 17, saturdayEvent.getEnd().getHour());
  }

  @Test
  public void testCreateRecurringAllDayEventWithUntilDate() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "create event \"Monthly Review\" on 2025-06-15 repeats U until 2025-08-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should use 'on' for all-day", command.contains(" on "));
    assertTrue("Command should specify Sunday", command.contains("U"));
    assertTrue("Command should have until date", command.contains("until 2025-08-15"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Created recurring event series"));
    assertTrue("Result should contain event name", result.contains("Monthly Review"));
    assertTrue("Result should contain until date", result.contains("until 2025-08-15"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertFalse("Calendar should not be empty", calendarData.isEmpty());

    LocalDate sunday1 = LocalDate.of(2025, 6, 15);
    LocalDate sunday2 = LocalDate.of(2025, 6, 22);
    LocalDate sunday3 = LocalDate.of(2025, 6, 29);

    assertTrue("Should have event on first Sunday", calendarData.containsKey(sunday1));
    assertTrue("Should have event on second Sunday", calendarData.containsKey(sunday2));
    assertTrue("Should have event on third Sunday", calendarData.containsKey(sunday3));

    IEvent reviewEvent = calendarData.get(sunday1).get(0);
    assertNotNull("Review event should exist", reviewEvent);
    assertEquals("Event subject should match", "Monthly Review", reviewEvent.getSubject());
    assertEquals("All-day event should start at 8:00 AM", 8, reviewEvent.getStart().getHour());
    assertEquals("All-day event should end at 5:00 PM", 17, reviewEvent.getEnd().getHour());
  }

  // ==================== EDIT COMMANDS TESTS ====================

  @Test
  public void testEditSingleEventSubject() {
    controller.executeCommand("create event Old Meeting from 2025-06-15T10:00 to 2025-06-15T11:00");

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event", 1, calendarData.get(testDate).size());

    String command = "edit event subject Old Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 " +
            "with New Meeting";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit event", command.startsWith("edit event"));
    assertTrue("Command should specify subject property", command.contains("subject"));
    assertTrue("Command should contain 'with' keyword", command.contains(" with "));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));
    assertTrue("Result should reference old event name", result.contains("Old Meeting"));

    List<IEvent> events = calendarData.get(testDate);
    assertEquals("Should still have one event", 1, events.size());

    IEvent modifiedEvent = events.get(0);
    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should be updated", "New Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should remain unchanged", testDateTime, modifiedEvent.getStart());
    assertEquals("End time should remain unchanged", testDateTime.plusHours(1),
            modifiedEvent.getEnd());
    assertEquals("Location should remain unchanged", Location.ONLINE, modifiedEvent.getLocation());
    assertEquals("Status should remain unchanged", Status.PUBLIC, modifiedEvent.getStatus());
  }

  @Test
  public void testEditSingleEventStartTime() {
    LocalDateTime originalStart = testDateTime;
    LocalDateTime originalEnd = testDateTime.plusHours(2);
    controller.executeCommand("create event Meeting from 2025-06-15T10:00 to 2025-06-15T12:00");

    String command = "edit event start Meeting from 2025-06-15T10:00 to 2025-06-15T12:00 with " +
            "2025-06-15T14:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify start property", command.contains("start"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();

    assertTrue("Calendar should still contain test date", calendarData.containsKey(testDate));
    List<IEvent> events = calendarData.get(testDate);
    assertEquals("Should have one event", 1, events.size());

    IEvent modifiedEvent = events.get(0);
    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should be updated", LocalDateTime.of(2025, 6, 15, 14, 0),
            modifiedEvent.getStart());

    LocalDateTime expectedEnd = LocalDateTime.of(2025, 6, 15, 16, 0);
    assertEquals("End time should be adjusted", expectedEnd, modifiedEvent.getEnd());
  }

  @Test
  public void testEditSingleEventEndTime() {
    controller.executeCommand("create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00");

    String command = "edit event end Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 " +
            "with 2025-06-15T12:30";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify end property", command.contains("end"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    List<IEvent> events = calendarData.get(testDate);
    IEvent modifiedEvent = events.get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Start time should remain unchanged", testDateTime, modifiedEvent.getStart());
    assertEquals("End time should be updated", LocalDateTime.of(2025, 6, 15, 12, 30),
            modifiedEvent.getEnd());
  }

  @Test
  public void testEditSingleEventLocation() {
    controller.executeCommand("create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00");

    String command = "edit event location Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 " +
            "with PHYSICAL";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify location property", command.contains("location"));
    assertTrue("Command should specify new location", command.contains("PHYSICAL"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    IEvent modifiedEvent = calendarData.get(testDate).get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Location should be updated", Location.PHYSICAL, modifiedEvent.getLocation());
    assertEquals("Other properties should remain unchanged", Status.PUBLIC,
            modifiedEvent.getStatus());
  }

  @Test
  public void testEditSingleEventStatus() {
    controller.executeCommand("create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00");

    String command = "edit event status Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 with " +
            "PRIVATE";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should specify status property", command.contains("status"));
    assertTrue("Command should specify new status", command.contains("PRIVATE"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated event"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    IEvent modifiedEvent = calendarData.get(testDate).get(0);

    assertNotNull("Modified event should exist", modifiedEvent);
    assertEquals("Subject should remain unchanged", "Meeting", modifiedEvent.getSubject());
    assertEquals("Status should be updated", Status.PRIVATE, modifiedEvent.getStatus());
    assertEquals("Other properties should remain unchanged", Location.ONLINE,
            modifiedEvent.getLocation());
  }

  @Test
  public void testEditEventsInSeriesFromSpecificDate() {
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 9, 0);
    controller.executeCommand("create event \"Weekly Meeting\" from 2025-06-16T09:00 to " +
            "2025-06-16T10:00 " +
            "repeats MWF for 4 times");

    Map<LocalDateTime, List<IEvent>> seriesData = controller.getMultiCalendar().getCurrent().
            getSeries();
    assertFalse("Series should not be empty", seriesData.isEmpty());

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate wednesday1 = LocalDate.of(2025, 6, 18);
    LocalDate friday1 = LocalDate.of(2025, 6, 20);

    assertTrue("Calendar should contain first Monday", calendarData.containsKey(monday1));
    assertTrue("Calendar should contain first Wednesday", calendarData.containsKey(wednesday1));
    assertTrue("Calendar should contain first Friday", calendarData.containsKey(friday1));

    String command = "edit events subject Weekly Meeting from 2025-06-18T09:00 with " +
            "Updated Meeting";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit events", command.startsWith("edit events"));
    assertTrue("Command should specify Wednesday start", command.contains("2025-06-18T09:00"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated events starting from"));

    IEvent mondayEvent = calendarData.get(monday1).get(0);
    IEvent wednesdayEvent = calendarData.get(wednesday1).get(0);
    IEvent fridayEvent = calendarData.get(friday1).get(0);

    assertNotNull("Monday event should exist", mondayEvent);
    assertNotNull("Wednesday event should exist", wednesdayEvent);
    assertNotNull("Friday event should exist", fridayEvent);

    assertEquals("Monday event should keep original subject", "Weekly Meeting",
            mondayEvent.getSubject());

    assertEquals("Wednesday event should be updated", "Updated Meeting",
            wednesdayEvent.getSubject());
    assertEquals("Friday event should be updated", "Updated Meeting", fridayEvent.getSubject());
  }

  @Test
  public void testEditEntireEventSeries() {
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 14, 0);
    controller.executeCommand("create event \"Team Standup\" from 2025-06-16T14:00 to " +
            "2025-06-16T14:30 " +
            "repeats MTWRF for 3 times");

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    LocalDate monday = LocalDate.of(2025, 6, 16);
    LocalDate tuesday = LocalDate.of(2025, 6, 17);
    LocalDate wednesday = LocalDate.of(2025, 6, 18);

    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday));
    assertTrue("Calendar should contain Tuesday", calendarData.containsKey(tuesday));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday));

    String command = "edit series subject Team Standup from 2025-06-16T14:00 with Daily Standup";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with edit series", command.startsWith("edit series"));
    assertTrue("Command should reference Tuesday", command.contains("2025-06-16T14:00"));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated entire series"));

    IEvent mondayEvent = calendarData.get(monday).get(0);
    IEvent tuesdayEvent = calendarData.get(tuesday).get(0);
    IEvent wednesdayEvent = calendarData.get(wednesday).get(0);

    assertNotNull("Monday event should exist", mondayEvent);
    assertNotNull("Tuesday event should exist", tuesdayEvent);
    assertNotNull("Wednesday event should exist", wednesdayEvent);

    assertEquals("Monday event should be updated", "Daily Standup", mondayEvent.getSubject());
    assertEquals("Tuesday event should be updated", "Daily Standup", tuesdayEvent.getSubject());
    assertEquals("Wednesday event should be updated", "Daily Standup", wednesdayEvent.getSubject());

    assertEquals("All events should maintain original time", 14, mondayEvent.getStart().getHour());
    assertEquals("All events should maintain original duration", 30,
            java.time.Duration.between(mondayEvent.getStart(), mondayEvent.getEnd()).toMinutes());
  }

  @Test
  public void testEditEventSeriesStartTimeBreaksSeries() {
    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 16, 10, 0);
    controller.executeCommand("create event \"Morning Sync\" from 2025-06-16T10:00 to " +
            "2025-06-16T11:00 " +
            "repeats MW for 3 times");

    Map<LocalDateTime, List<IEvent>> seriesData = controller.getMultiCalendar().
            getCurrent().getSeries();
    assertTrue("Should have series with original start time", seriesData.containsKey(seriesStart));

    String command = "edit events start Morning Sync from 2025-06-18T10:00 with 2025-06-18T11:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Result should indicate success", result.contains("Updated events starting from"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().
            getCurrent().getCalendar();
    LocalDate monday1 = LocalDate.of(2025, 6, 16);
    LocalDate wednesday1 = LocalDate.of(2025, 6, 18);

    assertTrue("Calendar should contain Monday", calendarData.containsKey(monday1));
    assertTrue("Calendar should contain Wednesday", calendarData.containsKey(wednesday1));

    IEvent mondayEvent = calendarData.get(monday1).get(0);
    IEvent wednesdayEvent = calendarData.get(wednesday1).get(0);

    assertEquals("Monday event should keep original time", 10, mondayEvent.getStart().getHour());

    assertEquals("Wednesday event should have new time", 11, wednesdayEvent.getStart().getHour());

    LocalDateTime newSeriesStart = LocalDateTime.of(2025, 6, 18, 11, 0);
    assertTrue("Should have new series with updated start time",
            seriesData.containsKey(newSeriesStart));
  }

  // ==================== QUERY COMMANDS TESTS ====================

  @Test
  public void testPrintEventsOnSpecificDate() {
    controller.executeCommand("create event \"Morning Meeting\" from 2025-06-15T09:00 to " +
            "2025-06-15T10:00");
    controller.executeCommand("create event Lunch from 2025-06-15T12:00 to 2025-06-15T13:00");

    String command = "print events on 2025-06-15";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with print events", command.startsWith("print events"));
    assertTrue("Command should use 'on' keyword", command.contains(" on "));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should contain date header", result.contains("Events on 2025-06-15"));
    assertTrue("Result should contain Morning Meeting", result.contains("Morning Meeting"));
    assertTrue("Result should contain Lunch", result.contains("Lunch"));

    assertTrue("Result should contain time information", result.contains("09:00"));
    assertTrue("Result should contain time information", result.contains("12:00"));
  }

  @Test
  public void testPrintEventsOnEmptyDate() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "print events on 2025-06-15";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertTrue("Should return no events message",
            result.contains("No events scheduled on this day"));
  }

  @Test
  public void testPrintEventsInDateRange() {
    LocalDate day1 = testDate;
    LocalDate day2 = testDate.plusDays(1);
    LocalDate day3 = testDate.plusDays(2);

    controller.executeCommand("create event \"Day 1 Event\" " +
            "from 2025-06-15T10:00 to 2025-06-15T11:00");
    controller.executeCommand("create event \"Day 2 Event\" " +
            "from 2025-06-16T14:00 to 2025-06-16T15:00");
    controller.executeCommand("create event \"Day 3 Event\" " +
            "from 2025-06-17T16:00 to 2025-06-17T17:00");

    String command = "print events from 2025-06-15T08:00 to 2025-06-17T18:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should contain 'from' keyword", command.contains(" from "));
    assertTrue("Command should contain 'to' keyword", command.contains(" to "));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should contain date range header", result.contains("Events from 2025-06-15 "
            + "to 2025-06-17"));
    assertTrue("Result should contain Day 1 Event", result.contains("Day 1 Event"));
    assertTrue("Result should contain Day 2 Event", result.contains("Day 2 Event"));
    assertTrue("Result should contain Day 3 Event", result.contains("Day 3 Event"));
  }

  @Test
  public void testPrintEventsRangeWithTimeFiltering() {
    controller.executeCommand("create event \"Early Event\" from 2025-06-15T07:00 to " +
            "2025-06-15T08:00");
    controller.executeCommand("create event \"Late Event\" from 2025-06-15T15:00 to " +
            "2025-06-15T16:00");

    String command = "print events from 2025-06-15T10:00 to 2025-06-15T18:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not contain Early Event", result.contains("Early Event"));
    assertTrue("Result should contain Late Event", result.contains("Late Event"));
  }

  @Test
  public void testShowStatusBusyWithEvent() {
    controller.executeCommand("create event \"Important Meeting\" from 2025-06-15T10:00 to " +
            "2025-06-15T11:00");

    String command = "show status on 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with show status", command.startsWith("show status"));
    assertTrue("Command should use 'on' keyword", command.contains(" on "));

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertEquals("Status should be busy", "busy", result);
  }

  @Test
  public void testShowStatusAvailableNoEvents() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "show status on 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);

    String result = controller.executeCommand(command);

    assertNotNull("Result should not be null", result);
    assertEquals("Status should be available", "available", result);
  }

  @Test
  public void testShowStatusDuringEventPeriod() {
    controller.executeCommand("create event \"Long Meeting\" from 2025-06-15T10:00 to " +
            "2025-06-15T12:00");

    String command1 = "show status on 2025-06-15T10:00";
    String result1 = controller.executeCommand(command1);
    assertEquals("Should be busy at event start", "busy", result1);

    String command2 = "show status on 2025-06-15T11:00";
    String result2 = controller.executeCommand(command2);
    assertEquals("Should be busy during event", "busy", result2);

    String command3 = "show status on 2025-06-15T12:00";
    String result3 = controller.executeCommand(command3);
    assertEquals("Should be available at event end", "available", result3);

    String command4 = "show status on 2025-06-15T13:00";
    String result4 = controller.executeCommand(command4);
    assertEquals("Should be available after event", "available", result4);
  }

  // ==================== COMMAND VALIDATION TESTS ====================

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
      assertTrue("Exception should indicate unknown command",
              e.getMessage().contains("Unknown command"));
    }
  }

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

    String whitespaceCommand = "   ";
    try {
      controller.executeCommand(whitespaceCommand);
      fail("Should throw exception for whitespace-only command");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
    }
  }

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

  @Test
  public void testDuplicateEventCreation() {
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00";
    String result1 = controller.executeCommand(command);
    assertNotNull("First result should not be null", result1);
    assertTrue("First creation should succeed", result1.contains("Created event"));

    Map<LocalDate, List<IEvent>> calendarData = controller.getMultiCalendar().getCurrent().
            getCalendar();
    assertTrue("Calendar should contain test date", calendarData.containsKey(testDate));
    assertEquals("Should have one event", 1, calendarData.get(testDate).size());

    try {
      controller.executeCommand(command);
      fail("Should throw exception for duplicate event");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertTrue("Exception should indicate duplicate or already exists",
              e.getMessage().toLowerCase().contains("already exists") ||
                      e.getMessage().toLowerCase().contains("duplicate"));
    }

    assertEquals("Should still have only one event", 1, calendarData.get(testDate).size());
  }

  @Test
  public void testEditNonExistentEvent() {
    assertTrue("Calendar should be empty initially", controller.getMultiCalendar().getCurrent().
            getCalendar().isEmpty());

    String command = "edit event subject NonExistent from 2025-06-15T10:00 to " +
            "2025-06-15T11:00 with NewName";
    assertNotNull("Command should not be null", command);
    assertTrue("Command should reference non-existent event", command.contains("NonExistent"));

    try {
      controller.executeCommand(command);
      fail("Should throw exception for non-existent event");
    } catch (IllegalArgumentException e) {
      assertNotNull("Exception should not be null", e);
      assertNotNull("Exception message should not be null", e.getMessage());
    }
  }

  @Test
  public void testEditCommandInvalidProperty() {
    controller.executeCommand("create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00");

    String command = "edit event invalidproperty Meeting from 2025-06-15T10:00 to " +
            "2025-06-15T11:00 with value";
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

  @Test
  public void testInvalidRepeatPattern() {
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 " +
            "repeats XYZ for 3 times";
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

  @Test
  public void testNegativeRepeatTimes() {
    String command = "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00 " +
            "repeats M for -5 times";
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

  @Test
  public void testEndTimeBeforeStartTime() {
    String command = "create event Meeting from 2025-06-15T11:00 to 2025-06-15T10:00";
    assertNotNull("Command should not be null", command);

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


  @Test
  public void testExitCommand() {
    String exitCommand = "exit";
    assertNotNull("Exit command should not be null", exitCommand);
    assertEquals("Exit command should be exact", "exit", exitCommand);

    String result = controller.executeCommand(exitCommand);

    assertNull("Exit command should return null", result);
  }

  @Test
  public void testExitCommandCaseInsensitive() {
    String[] exitCommands = {"EXIT", "Exit", "eXiT"};

    for (String exitCommand : exitCommands) {
      assertNotNull("Exit command should not be null", exitCommand);

      String result = controller.executeCommand(exitCommand);

      assertNull("Exit command should return null regardless of case", result);
    }
  }

  @Test
  public void testCommandParsingCoordinatorNullInput() {
    ParseResult result = CommandParserCoordinator.parseCommand(null);

    assertNotNull("Parse result should not be null", result);
    assertFalse("Parse result should indicate failure", result.isSuccess());
    assertNotNull("Error message should not be null", result.getErrorMessage());
    assertTrue("Error message should indicate empty input",
            result.getErrorMessage().contains("empty"));
  }

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

  @Test
  public void testSuccessfulCommandParsing() {
    String validCommand = "create event Test from 2025-06-15T10:00 to 2025-06-15T11:00";
    ParseResult result = CommandParserCoordinator.parseCommand(validCommand);

    assertNotNull("Parse result should not be null", result);
    assertTrue("Parse result should indicate success", result.isSuccess());
    assertEquals("Command type should be CREATE_EVENT", CommandType.CREATE_EVENT,
            result.getCommandType());
    assertNull("Error message should be null for successful parse", result.getErrorMessage());

    assertEquals("Subject should be parsed correctly", "Test", result.getSubject());
    assertNotNull("Start time should be parsed", result.getStartTime());
    assertNotNull("End time should be parsed", result.getEndTime());
    assertEquals("Start time should match", LocalDateTime.of(2025, 6, 15, 10, 0),
            result.getStartTime());
    assertEquals("End time should match", LocalDateTime.of(2025, 6, 15, 11, 0),
            result.getEndTime());
    assertFalse("Should not be all-day", result.isAllDay());
    assertFalse("Should not be repeating", result.isRepeating());
  }
}