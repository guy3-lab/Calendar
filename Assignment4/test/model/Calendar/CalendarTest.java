package model.Calendar;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDateTime;
import java.util.List;
import model.Enum.Months;
import static org.junit.Assert.assertEquals;

public class CalendarTest {
  Calendar calendar;
  Calendar calendarLeap;

  @Before
  public void setUp() throws Exception {
    calendar = new Calendar(2025);
    calendarLeap = new Calendar(2024);
  }

  @Test
  public void ExceptionTest() {
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));

    try {
      calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
              LocalDateTime.parse("2025-10-05T15:00"));
    } catch (Exception e) {
      assertEquals("Event already exists", e.getMessage());
    }

    try {
      calendarLeap.createEvent("test", LocalDateTime.parse("2024-02-30T10:00"),
              LocalDateTime.parse("2024-02-30T15:00"));
    } catch (Exception e) {
      assertEquals("Text '2024-02-30T10:00' could not be parsed: Invalid date 'FEBRUARY 30'",
              e.getMessage());
    }
  }

  @Test
  public void createEventTest() {
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T10:00"),
            LocalDateTime.parse("2025-10-05T15:00"));
    calendar.createEvent("test", LocalDateTime.parse("2025-10-05T15:00"),
            LocalDateTime.parse("2025-10-05T17:00"));

    Months m = Months.values()[9];
    List<Day> days = calendar.getCalendar().get(m);
    Event event = new Event.EventBuilder("test", LocalDateTime.parse("2025-10-05T10:00")).
            end(LocalDateTime.parse("2025-10-05T15:00")).build();
    assertEquals(true ,days.get(4).getEvents().contains(event));
    assertEquals(2, days.get(4).getEvents().size());

    calendar.createEvent("test", LocalDateTime.parse("2025-10-31T10:00"),
            LocalDateTime.parse("2025-10-31T15:00"));

    Event event2 = new Event.EventBuilder("test", LocalDateTime.parse("2025-10-31T10:00")).
            end(LocalDateTime.parse("2025-10-31T15:00")).build();
    assertEquals(true ,days.get(30).getEvents().contains(event2));


    Months m2 = Months.values()[1];
    List<Day> days2 = calendar.getCalendar().get(m2);
    calendar.createEvent("test", LocalDateTime.parse("2025-02-27T05:00"), null);
    Event eventNoEndTime = new Event("test",
            LocalDateTime.parse("2025-02-27T05:00"));
    assertEquals(true ,days2.get(26).getEvents().contains(eventNoEndTime));
  }
}