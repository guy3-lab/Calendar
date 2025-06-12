package model.multicalendar;


import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import model.calendar.ISpecificCalendar;
import model.calendar.SpecificCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiCalendarTest {
  MultiCalendar mc;

  @Before
  public void setUp() {
    mc = new MultiCalendar();
    mc.addCalendar("c1", ZoneId.of("America/Los_Angeles"));

  }

  @Test
  public void addCalendarTest() {
    assertEquals(1, mc.getCalendars().size());
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    assertEquals(2, mc.getCalendars().size());

    //tries adding a calendar that already exists
    try {
      mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar with name c2 already exists.", e.getMessage());
    }
  }

  @Test
  public void editCalendarTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<ISpecificCalendar> calendars = mc.getCalendars();
    assertEquals("c1", calendars.get(0).getName());
    mc.editCalendar("c1", "name", "newC1");
    assertEquals("newC1", calendars.get(0).getName());

    assertEquals(ZoneId.of("America/Los_Angeles"), calendars.get(0).getTimeZone());
    mc.editCalendar("newC1", "timezone", "Europe/Paris");
    assertEquals(ZoneId.of("Europe/Paris"), calendars.get(0).getTimeZone());

    //tries editing a calendar that doesn't exist
    try {
      mc.editCalendar("doesn't exist", "timezone", "Europe/Paris");
    } catch (IllegalArgumentException e) {
     assertEquals("Calendar doesn't exist not found.", e.getMessage());
    }

    //tries editing calendar name to existing calendar
    try {
      mc.editCalendar("newC1", "name", "c2");
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar with name c2 already exists.", e.getMessage());
    }

    //invalid property
    try {
      mc.editCalendar("newC1", "test", "c2");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid property: test", e.getMessage());
    }
  }

  @Test
  public void useCalendarTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    mc.useCalendar("c1");
    assertEquals("c1", mc.getCurrent().getName());

    mc.useCalendar("c2");
    assertEquals("c2", mc.getCurrent().getName());
  }

  @Test
  public void copyEventTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<ISpecificCalendar> calendars = mc.getCalendars();
    ISpecificCalendar c1 = calendars.get(0);
    ISpecificCalendar c2 = calendars.get(1);

    //full day event
    c1.createEvent("event1", LocalDateTime.parse("2000-10-10T10:00"), null);
    //checking if c1 calendar got event added to the day
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-10")));

    mc.useCalendar("c1");
    assertEquals("c1", mc.getCurrent().getName());
    mc.copyEvent("event1", LocalDateTime.parse("2000-10-10T08:00"), "c2",
            LocalDateTime.parse("2000-10-10T10:00"));

    assertTrue(c2.getCalendar().containsKey(LocalDate.parse("2000-10-10")));
    //checks if the name got copied over
    assertEquals("event1", c2.getCalendar().get(LocalDate.parse("2000-10-10")).get(0).getSubject());
    //checks if the time is correct
    assertEquals(LocalDateTime.parse("2000-10-10T10:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-10")).get(0).getStart());
    assertEquals(LocalDateTime.parse("2000-10-10T19:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-10")).get(0).getEnd());

    //tries copying the event again even though the event already exists
    try {
      mc.copyEvent("event1", LocalDateTime.parse("2000-10-10T08:00"), "c2",
              LocalDateTime.parse("2000-10-10T10:00"));
    } catch (IllegalArgumentException e) {
      assertEquals("Event already exists", e.getMessage());
    }

    //tries copying the event to a nonexistent calendar
    try {
      mc.copyEvent("event1", LocalDateTime.parse("2000-10-10T08:00"), "c3",
              LocalDateTime.parse("2000-10-10T10:00"));
    } catch (IllegalArgumentException e) {
      assertEquals("No target calendar found", e.getMessage());
    }

    //tries copying an event that doesn't exist
    try {
      mc.copyEvent("test", LocalDateTime.parse("2000-10-10T08:00"), "c2",
              LocalDateTime.parse("2000-10-10T10:00"));
    } catch (IllegalArgumentException e) {
      assertEquals("No event found", e.getMessage());
    }

    //Two events with the same name and start time, but different end times
    c1.createEvent("sameEvent", LocalDateTime.parse("2000-10-10T09:00"),
            LocalDateTime.parse("2000-10-10T10:00"));
    c1.createEvent("sameEvent", LocalDateTime.parse("2000-10-10T09:00"),
            LocalDateTime.parse("2000-10-10T11:00"));

    //tries copying an event, but can not specify which one
    try {
      mc.copyEvent("sameEvent", LocalDateTime.parse("2000-10-10T09:00"), "c2",
              LocalDateTime.parse("2000-10-10T10:00"));
    } catch (IllegalArgumentException e) {
      assertEquals("Multiple events found", e.getMessage());
    }
  }

  @Test
  public void copyEventsTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<ISpecificCalendar> calendars = mc.getCalendars();
    ISpecificCalendar c1 = calendars.get(0);
    ISpecificCalendar c2 = calendars.get(1);

    //all full day events
    c1.createEvent("event1", LocalDateTime.parse("2000-10-10T10:00"), null);
    c1.createEvent("event2", LocalDateTime.parse("2000-10-10T11:00"), null);
    c1.createEvent("event3", LocalDateTime.parse("2000-10-10T12:00"), null);

    //creates a valid series that can be converted
    List<String> days = Arrays.asList("T", "W");
    c1.createSeriesTimes("event4", LocalDateTime.parse("2000-10-10T08:00"),
            LocalDateTime.parse("2000-10-10T09:00"), days, 2);

    //checking if all events made properly
    assertEquals(4 ,c1.getCalendar().get(LocalDate.parse("2000-10-10")).size());
    mc.useCalendar("c1");
    assertEquals("c1", mc.getCurrent().getName());

    mc.copyEvents(LocalDate.parse("2000-10-10"), "c2", LocalDate.parse("2000-10-12"));

    //checks the correct times that are copied over to a different time zone
    assertTrue(c2.getCalendar().containsKey(LocalDate.parse("2000-10-12")));
    assertEquals(LocalDateTime.parse("2000-10-12T17:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-12")).get(0).getStart());
    assertEquals(LocalDateTime.parse("2000-10-13T02:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-12")).get(0).getEnd());

    //checks if the event that's in a series gets added
    assertTrue(c2.getOldToNewSeries().containsKey(LocalDateTime.parse("2000-10-10T08:00")));
    assertTrue(c2.getSeries().containsKey(LocalDateTime.parse("2000-10-12T17:00")));

    assertEquals(4, c2.getCalendar().get(LocalDate.parse("2000-10-12")).size());
  }

  @Test
  public void copyEventsIntervalTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<ISpecificCalendar> calendars = mc.getCalendars();
    ISpecificCalendar c1 = calendars.get(0);
    ISpecificCalendar c2 = calendars.get(1);

    c1.createEvent("event1", LocalDateTime.parse("2000-10-10T10:00"), null);
    c1.createEvent("event2", LocalDateTime.parse("2000-10-11T11:00"), null);

    //creates a valid series that can be converted
    List<String> days = Arrays.asList("R", "F");
    c1.createSeriesTimes("event3", LocalDateTime.parse("2000-10-12T08:00"),
            LocalDateTime.parse("2000-10-12T09:00"), days, 2);

    //checking if all events made properly
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-10")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-11")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-12")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-13")));

    mc.useCalendar("c1");
    assertEquals("c1", mc.getCurrent().getName());

    mc.copyEventsInterval(LocalDate.parse("2000-10-11"), LocalDate.parse("2000-10-13"),
            "c2", LocalDate.parse("2000-10-12"));
    mc.useCalendar("c2");
    assertEquals("c2", mc.getCurrent().getName());
    assertTrue(c2.getCalendar().containsKey(LocalDate.parse("2000-10-12")));
    assertEquals(LocalDateTime.parse("2000-10-12T17:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-12")).get(0).getStart());
    assertEquals(LocalDateTime.parse("2000-10-13T02:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-12")).get(0).getEnd());

    assertTrue(c2.getCalendar().containsKey(LocalDate.parse("2000-10-13")));
    assertEquals(LocalDateTime.parse("2000-10-13T17:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-13")).get(1).getStart());
    assertEquals(LocalDateTime.parse("2000-10-13T18:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-13")).get(1).getEnd());

    assertTrue(c2.getCalendar().containsKey(LocalDate.parse("2000-10-14")));
    assertEquals(LocalDateTime.parse("2000-10-14T17:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-14")).get(0).getStart());
    assertEquals(LocalDateTime.parse("2000-10-14T18:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-14")).get(0).getEnd());

    //checking if c2 calendar succesfully created series
    assertTrue(c2.getOldToNewSeries().containsKey(LocalDateTime.parse("2000-10-12T08:00")));
    assertEquals(2, c2.getSeries().get(LocalDateTime.parse("2000-10-13T17:00")).size());
  }
}