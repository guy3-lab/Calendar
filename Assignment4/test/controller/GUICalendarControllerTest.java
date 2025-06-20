package controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.swing.*;

import controller.parse.PropertyType;
import model.calendar.Event;
import model.calendar.IEvent;
import model.calendar.ISpecificCalendar;
import model.multicalendar.IMultiCalendar;
import view.IGuiView;

public class GUICalendarControllerTest {
  private GUICalendarController controller;
  private MockMultiCalendar mockModel;
  private MockGuiView mockView;

  @Before
  public void setUp() {
    mockModel = new MockMultiCalendar();
    mockView = new MockGuiView();
    controller = new GUICalendarController(mockModel, mockView);
  }

  /**
   * Tests that go() initializes the application with a default calendar.
   */
  @Test
  public void testGoInitializesDefaultCalendar() {
    controller.go();

    assertEquals(1, mockModel.calendars.size());
    assertEquals("Default", mockModel.calendars.get(0).getName());
    assertEquals("Default", mockModel.currentCalendarName);
    assertTrue(mockView.actionListenerSet);
    assertEquals("Current Calendar: Default", mockView.calendarLabelText);
    assertTrue(mockView.viewRan);
  }

  /**
   * Tests calendar selection changes the current calendar.
   */
  @Test
  public void testCalendarSelection() throws Exception {
    controller.go();
    mockModel.addCalendar("Work", ZoneId.systemDefault());

    JComboBox<String> combo = new JComboBox<>();
    combo.addItem("Work");
    combo.setSelectedItem("Work");
    ActionEvent event = new ActionEvent(combo, ActionEvent.ACTION_PERFORMED, "calendarSelected");

    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Work", mockModel.currentCalendarName);
    assertEquals("Current Calendar: Work", mockView.calendarLabelText);
    assertEquals("Switched to calendar: Work", mockView.statusText);
  }

  /**
   * Tests viewing events without specifying a date shows events from today.
   */
  @Test
  public void testViewScheduleWithoutDate() throws Exception {
    controller.go();
    mockView.dateText = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "chooseDate");
    controller.actionPerformed(event);
    waitForEDT();

    assertTrue(mockView.eventsText.contains("Events from"));
    assertTrue(mockView.statusText.contains("Showing"));
  }

  /**
   * Tests viewing events with a valid date.
   */
  @Test
  public void testViewScheduleWithValidDate() throws Exception {
    controller.go();
    mockView.dateText = "2025-06-19T10:00";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "chooseDate");
    controller.actionPerformed(event);
    waitForEDT();

    assertTrue(mockView.eventsText.contains("Events from 2025-06-19 10:00"));
  }

  /**
   * Tests viewing events with invalid date format shows error.
   */
  @Test
  public void testViewScheduleWithInvalidDate() throws Exception {
    controller.go();
    mockView.dateText = "invalid-date";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "chooseDate");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Invalid date format. Use: YYYY-MM-DDThh:mm (e.g., 2025-06-19T10:00)", mockView.statusText);
  }

  /**
   * Tests creating an event with valid inputs.
   */
  @Test
  public void testCreateEventValid() throws Exception {
    controller.go();
    mockView.eventName = "Team Meeting";
    mockView.fromDate = "2025-06-19T10:00";
    mockView.toDate = "2025-06-19T11:00";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals(1, mockModel.eventsCreated.size());
    assertEquals("Team Meeting", mockModel.eventsCreated.get(0).subject);
    assertTrue(mockView.dateFieldsCleared);
    assertEquals("Event 'Team Meeting' created successfully", mockView.statusText);
  }

  /**
   * Tests creating an all-day event by leaving end time empty.
   */
  @Test
  public void testCreateAllDayEvent() throws Exception {
    controller.go();
    mockView.eventName = "Conference";
    mockView.fromDate = "2025-06-19T08:00";
    mockView.toDate = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals(1, mockModel.eventsCreated.size());
    assertNull(mockModel.eventsCreated.get(0).endTime);
  }

  /**
   * Tests event creation fails with empty event name.
   */
  @Test
  public void testCreateEventEmptyName() throws Exception {
    controller.go();
    mockView.eventName = "";
    mockView.fromDate = "2025-06-19T10:00";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Event name cannot be empty", mockView.statusText);
    assertEquals(0, mockModel.eventsCreated.size());
  }

  /**
   * Tests event creation fails with empty start date.
   */
  @Test
  public void testCreateEventEmptyStartDate() throws Exception {
    controller.go();
    mockView.eventName = "Meeting";
    mockView.fromDate = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Start date/time cannot be empty", mockView.statusText);
    assertEquals(0, mockModel.eventsCreated.size());
  }

  /**
   * Tests event creation fails when end time is before start time.
   */
  @Test
  public void testCreateEventEndBeforeStart() throws Exception {
    controller.go();
    mockView.eventName = "Meeting";
    mockView.fromDate = "2025-06-19T11:00";
    mockView.toDate = "2025-06-19T10:00";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("End time must be after start time", mockView.statusText);
  }

  /**
   * Tests creating a new calendar with valid inputs.
   */
  @Test
  public void testCreateCalendarValid() throws Exception {
    controller.go();
    mockView.calName = "Personal";
    mockView.selectedTimeZone = "America/New_York";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createCalendar");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals(2, mockModel.calendars.size());
    assertTrue(mockView.calFieldsCleared);
    assertEquals("Calendar 'Personal' created successfully", mockView.statusText);
  }

  /**
   * Tests calendar creation fails with empty name.
   */
  @Test
  public void testCreateCalendarEmptyName() throws Exception {
    controller.go();
    mockView.calName = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "createCalendar");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Calendar name cannot be empty", mockView.statusText);
    assertEquals(1, mockModel.calendars.size());
  }

  /**
   * Tests editing an event with valid inputs.
   */
  @Test
  public void testEditEventValid() throws Exception {
    controller.go();
    // Create event through the current calendar
    mockModel.getCurrent().createEvent("Meeting", LocalDateTime.parse("2025-06-19T10:00"), LocalDateTime.parse("2025-06-19T11:00"));

    mockView.editProperty = PropertyType.SUBJECT;
    mockView.editSubject = "Meeting";
    mockView.editFrom = "2025-06-19T10:00";
    mockView.editTo = "2025-06-19T11:00";
    mockView.editValue = "Updated Meeting";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "editEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals(1, mockModel.eventsEdited.size());
    assertTrue(mockView.editFieldsCleared);
    assertEquals("Event edited successfully", mockView.statusText);
  }

  /**
   * Tests edit event fails with empty subject.
   */
  @Test
  public void testEditEventEmptySubject() throws Exception {
    controller.go();
    mockView.editProperty = PropertyType.SUBJECT;
    mockView.editSubject = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "editEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Event subject cannot be empty", mockView.statusText);
    assertEquals(0, mockModel.eventsEdited.size());
  }

  /**
   * Tests edit event fails with empty start/end times.
   */
  @Test
  public void testEditEventEmptyTimes() throws Exception {
    controller.go();
    mockView.editProperty = PropertyType.SUBJECT;
    mockView.editSubject = "Meeting";
    mockView.editFrom = "";
    mockView.editTo = "";

    ActionEvent event = new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, "editEvent");
    controller.actionPerformed(event);
    waitForEDT();

    assertEquals("Event start and end times cannot be empty", mockView.statusText);
  }

  /**
   * Tests processCommand returns empty string as expected.
   */
  @Test
  public void testProcessCommand() {
    assertEquals("", controller.processCommand("any command"));
  }

  private void waitForEDT() throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(() -> {});
  }

  class MockMultiCalendar implements IMultiCalendar {
    List<ISpecificCalendar> calendars = new ArrayList<>();
    String currentCalendarName;
    List<EventCreated> eventsCreated = new ArrayList<>();
    List<EventEdited> eventsEdited = new ArrayList<>();

    class EventCreated {
      String subject;
      LocalDateTime startTime;
      LocalDateTime endTime;

      EventCreated(String s, LocalDateTime st, LocalDateTime et) {
        this.subject = s;
        this.startTime = st;
        this.endTime = et;
      }
    }

    class EventEdited {
      PropertyType property;
      String subject;
      LocalDateTime start;
      LocalDateTime end;
      String value;

      EventEdited(PropertyType p, String s, LocalDateTime st, LocalDateTime et, String v) {
        this.property = p;
        this.subject = s;
        this.start = st;
        this.end = et;
        this.value = v;
      }
    }

    @Override
    public void addCalendar(String name, ZoneId timezone) {
      for (ISpecificCalendar cal : calendars) {
        if (cal.getName().equals(name)) {
          throw new IllegalArgumentException("Calendar already exists");
        }
      }
      calendars.add(new MockSpecificCalendar(name, timezone, this));
    }

    @Override
    public void editCalendar(String name, String property, String value) {}

    @Override
    public void useCalendar(String name) {
      for (ISpecificCalendar cal : calendars) {
        if (cal.getName().equals(name)) {
          currentCalendarName = name;
          return;
        }
      }
      throw new IllegalArgumentException("Calendar not found");
    }

    @Override
    public void copyEvent(String eventName, LocalDateTime date, String calendarName, LocalDateTime targetDate) {}

    @Override
    public void copyEvents(LocalDate date, String calendarName, LocalDate targetDate) {}

    @Override
    public void copyEventsInterval(LocalDate startDate, LocalDate endDate, String calendarName, LocalDate targetDate) {}

    @Override
    public List<ISpecificCalendar> getCalendars() {
      return calendars;
    }

    @Override
    public ISpecificCalendar getCurrent() {
      for (ISpecificCalendar cal : calendars) {
        if (cal.getName().equals(currentCalendarName)) {
          return cal;
        }
      }
      return null;
    }

    public Event createEvent(String subject, LocalDateTime start, LocalDateTime end) {
      eventsCreated.add(new EventCreated(subject, start, end));
      Event event = new Event(subject, start);
      if (end != null) {
        event = new Event.EventBuilder(subject, start).end(end).build();
      }
      return event;
    }
  }

  class MockSpecificCalendar implements ISpecificCalendar {
    private String name;
    private ZoneId timeZone;
    private Map<LocalDate, List<IEvent>> calendar = new HashMap<>();
    private MockMultiCalendar parentModel;

    MockSpecificCalendar(String name, ZoneId timeZone, MockMultiCalendar parent) {
      this.name = name;
      this.timeZone = timeZone;
      this.parentModel = parent;
    }

    @Override
    public String getName() { return name; }

    @Override
    public ZoneId getTimeZone() { return timeZone; }

    @Override
    public void setName(String name) { this.name = name; }

    @Override
    public void setTimeZone(ZoneId timeZone) { this.timeZone = timeZone; }

    @Override
    public Map<LocalDate, List<IEvent>> getCalendar() { return calendar; }

    @Override
    public Map<LocalDateTime, List<IEvent>> getSeries() { return new HashMap<>(); }

    @Override
    public Map<LocalDateTime, LocalDateTime> getOldToNewSeries() { return new HashMap<>(); }

    @Override
    public Event createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime) {
      Event event = parentModel.createEvent(subject, startTime, endTime);
      // Add the event to this calendar's data structure
      LocalDate date = startTime.toLocalDate();
      if (!calendar.containsKey(date)) {
        calendar.put(date, new ArrayList<>());
      }
      calendar.get(date).add(event);
      return event;
    }

    @Override
    public void editEvent(PropertyType property, String subject, LocalDateTime startTime, LocalDateTime endTime, String value) {
      parentModel.eventsEdited.add(parentModel.new EventEdited(property, subject, startTime, endTime, value));
    }

    @Override
    public void createSeriesTimes(String subject, LocalDateTime startTime, LocalDateTime endTime, List<String> repeatDays, int times) {}

    @Override
    public void createSeriesUntil(String subject, LocalDateTime startTime, LocalDateTime endTime, List<String> repeatDays, LocalDate until) {}

    @Override
    public void editEvents(PropertyType property, String subject, LocalDateTime startTime, String value) {}

    @Override
    public void editSeries(PropertyType property, String subject, LocalDateTime startTime, String value) {}

    @Override
    public String printEvents(LocalDate day) { return ""; }

    @Override
    public String printEventsInterval(LocalDateTime start, LocalDateTime end) { return ""; }

    @Override
    public String showStatus(LocalDateTime day) { return "available"; }

    @Override
    public void fullCreate(String subject, LocalDateTime startDate, LocalDateTime endDate, String desc, model.enums.Location location, model.enums.Status status) {}
  }

  class MockGuiView implements IGuiView {
    boolean actionListenerSet = false;
    boolean viewRan = false;
    volatile String calendarLabelText = "";
    volatile String statusText = "";
    volatile String eventsText = "";
    boolean dateFieldsCleared = false;
    boolean calFieldsCleared = false;
    boolean editFieldsCleared = false;

    String dateText = "";
    String eventName = "";
    String fromDate = "";
    String toDate = "";
    String calName = "";
    String selectedTimeZone = "UTC";
    PropertyType editProperty = PropertyType.SUBJECT;
    String editSubject = "";
    String editFrom = "";
    String editTo = "";
    String editValue = "";

    final JLabel calendarLabel;
    final JComboBox<String> timeZoneDropdown;
    final MockGuiView self = this;

    MockGuiView() {
      calendarLabel = new JLabel() {
        @Override
        public void setText(String text) {
          super.setText(text);
          self.calendarLabelText = text;
        }

        @Override
        public String getText() {
          return self.calendarLabelText;
        }
      };
      timeZoneDropdown = new JComboBox<>();
    }

    @Override
    public void run() { viewRan = true; }

    @Override
    public void run(String parameter) { viewRan = true; }

    @Override
    public void displayOutput(String output) {}

    @Override
    public void displayError(String error) {}

    @Override
    public void updateCalendar() {}

    @Override
    public void setActionListener(java.awt.event.ActionListener listener) { actionListenerSet = true; }

    @Override
    public void setCalendarsDropdown(List<ISpecificCalendar> calendars) {}

    @Override
    public String getCalName() { return calName; }

    @Override
    public JComboBox<String> getTimeZoneDropdown() {
      timeZoneDropdown.removeAllItems();
      timeZoneDropdown.addItem(selectedTimeZone);
      timeZoneDropdown.setSelectedItem(selectedTimeZone);
      return timeZoneDropdown;
    }

    @Override
    public String getDateTextField() { return dateText; }

    @Override
    public String getFromDateTextField() { return fromDate; }

    @Override
    public String getToDateTextField() { return toDate; }

    @Override
    public String getEventName() { return eventName; }

    @Override
    public JLabel getCalendarLabel() { return calendarLabel; }

    @Override
    public void setEvents(String events) { eventsText = events; }

    @Override
    public void setStatus(String status) { statusText = status; }

    @Override
    public JComboBox<PropertyType> getEditProperty() {
      JComboBox<PropertyType> combo = new JComboBox<>();
      combo.setSelectedItem(editProperty);
      return combo;
    }

    @Override
    public String getEditFromTextField() { return editFrom; }

    @Override
    public String getEditToTextField() { return editTo; }

    @Override
    public String getEditValue() { return editValue; }

    @Override
    public String getEditSubject() { return editSubject; }

    @Override
    public void clearDateFieldsAfterRetrieving() {}

    @Override
    public void clearDateFieldsAfterCreation() { dateFieldsCleared = true; }

    @Override
    public void clearCalFieldsAfterCreation() { calFieldsCleared = true; }

    @Override
    public void clearDateFieldsAfterEditing() { editFieldsCleared = true; }
  }
}