package model.multicalendar;


import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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
  }

  @Test
  public void editCalendarTest() {
    List<SpecificCalendar> calendars = mc.getCalendars();
    assertEquals("c1", calendars.get(0).getName());
    mc.editCalendar("c1", "name", "newC1");
    assertEquals("newC1", calendars.get(0).getName());

    assertEquals(ZoneId.of("America/Los_Angeles"), calendars.get(0).getTimeZone());
    mc.editCalendar("newC1", "timezone", "Europe/Paris");
    assertEquals(ZoneId.of("Europe/Paris"), calendars.get(0).getTimeZone());
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
    List<SpecificCalendar> calendars = mc.getCalendars();
    SpecificCalendar c1 = calendars.get(0);
    SpecificCalendar c2 = calendars.get(1);

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
  }

  @Test
  public void copyEventsTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<SpecificCalendar> calendars = mc.getCalendars();
    SpecificCalendar c1 = calendars.get(0);
    SpecificCalendar c2 = calendars.get(1);

    //all full day events
    c1.createEvent("event1", LocalDateTime.parse("2000-10-10T10:00"), null);
    c1.createEvent("event2", LocalDateTime.parse("2000-10-10T11:00"), null);
    c1.createEvent("event3", LocalDateTime.parse("2000-10-10T12:00"), null);
    c1.createEvent("event4", LocalDateTime.parse("2000-10-10T15:00"), null);

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

    assertEquals(4, c2.getCalendar().get(LocalDate.parse("2000-10-12")).size());
  }

  @Test
  public void copyEventsIntervalTest() {
    mc.addCalendar("c2", ZoneId.of("Europe/Paris"));
    List<SpecificCalendar> calendars = mc.getCalendars();
    SpecificCalendar c1 = calendars.get(0);
    SpecificCalendar c2 = calendars.get(1);

    c1.createEvent("event1", LocalDateTime.parse("2000-10-10T10:00"), null);
    c1.createEvent("event2", LocalDateTime.parse("2000-10-11T11:00"), null);
    c1.createEvent("event3", LocalDateTime.parse("2000-10-12T12:00"), null);
    c1.createEvent("event4", LocalDateTime.parse("2000-10-13T15:00"), null);

    //checking if all events made properly
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-10")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-11")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-12")));
    assertTrue(c1.getCalendar().containsKey(LocalDate.parse("2000-10-13")));

    mc.useCalendar("c1");
    assertEquals("c1", mc.getCurrent().getName());

    mc.copyEventsInterval(LocalDate.parse("2000-10-11"), LocalDate.parse("2000-10-12"),
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
    assertEquals(LocalDateTime.parse("2000-10-14T02:00"),
            c2.getCalendar().get(LocalDate.parse("2000-10-13")).get(1).getEnd());
  }
}