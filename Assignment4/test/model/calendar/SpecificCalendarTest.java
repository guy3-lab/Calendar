package model.calendar;


import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;

import model.enums.Location;
import model.enums.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the new SpecificCalendar class and its methods.
 */
public class SpecificCalendarTest {
  SpecificCalendar cal;

  @Before
  public void setUp() {
    cal = new SpecificCalendar("c1", ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void fullCreateTest() {
    cal.fullCreate("event1", LocalDateTime.parse("2000-10-10T10:00"),
            LocalDateTime.parse("2000-10-10T14:00"),
            "Description example", Location.ONLINE, Status.PUBLIC);
    List<IEvent> calendar = cal.getCalendar().get(LocalDate.parse("2000-10-10"));
    assertTrue(cal.getCalendar().containsKey(LocalDate.parse("2000-10-10")));

    assertEquals("event1", calendar.get(0).getSubject());
    assertEquals(LocalDateTime.parse("2000-10-10T10:00"), calendar.get(0).getStart());
    assertEquals(LocalDateTime.parse("2000-10-10T14:00"), calendar.get(0).getEnd());
    assertEquals("Description example", calendar.get(0).getDesc());
    assertEquals(Location.ONLINE, calendar.get(0).getLocation());
    assertEquals(Status.PUBLIC, calendar.get(0).getStatus());
  }

  @Test
  public void getNameTest() {
    assertEquals("c1", cal.getName());
  }

  @Test
  public void getTimezoneTest() {
    assertEquals(ZoneId.of("America/Los_Angeles"), cal.getTimeZone());
  }

  @Test
  public void getOldToNewTest() {
    assertEquals(new HashMap<>(), cal.getOldToNewSeries());
  }

  @Test
  public void setNameTest() {
    cal.setName("c2");
    assertEquals("c2", cal.getName());
  }

  @Test
  public void setTimezoneTest() {
    cal.setTimeZone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), cal.getTimeZone());
  }
}