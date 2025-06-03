package model.Calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Enum.Months;
import model.Enum.WeekDays;

/**
 * represents a Calendar object that contains a year, and a list of months and its days.
 */
public class Calendar implements ICalendar{
  private final int year;
  private final Map<Months, List<Day>> calendar;

  /**
   * Creates a calendar object that takes in a year as an argument to account for leap years.
   * @param year used to account for leap years
   */
  public Calendar(int year) {
    this.year = year;
    this.calendar = new HashMap<Months, List<Day>>();

    for (Months month : Months.values()) {
      int daysInMonth = month.getDays(year);

      List<Day> days = new ArrayList<>();
      for (int i = 0; i < daysInMonth; i++) {
        days.add(new Day(i + 1));
      }
      calendar.put(month, days);
    }
  }

  @Override
  public void createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime) {
    Event event;
    if (endTime == null) {
      event = new Event(subject, startTime);
      addEventHelper(event, startTime);

    } else {
      event = new Event.EventBuilder(subject, startTime).end(endTime).build();
      while (!startTime.toLocalDate().isAfter(endTime.toLocalDate())) {
        addEventHelper(event, startTime);

        startTime = startTime.plusDays(1);
      }
    }
  }

  /**
   * Adds the event to a day specified.
   * @param event the event being added
   * @param startTime the time to add the event
   */
  private void addEventHelper(Event event, LocalDateTime startTime) {
    Months m = Months.values()[startTime.getMonthValue() - 1];
    List<Day> days = calendar.get(m);

    for (Day d : days) {
      if (d.getDay() == startTime.getDayOfMonth()) {
        if (d.getEvents().contains(event)) {
          throw new IllegalArgumentException("Event already exists");
        }
        d.addEvent(event);
      }
    }
  }

  @Override
  public void createSeries(String subject, LocalDateTime startTime, LocalDateTime endTime,
                           List<String> repeatDays, int times) {
    LocalDateTime[][] weekdayRanges = new LocalDateTime[repeatDays.size()][2];

    for (int i = 0; i < repeatDays.size(); i++) {
      int dayNum = WeekDays.getDay(repeatDays.get(i));
      int currentDay = startTime.getDayOfWeek().getValue();

      int difference = (dayNum - currentDay + 7) % 7;
      weekdayRanges[i][0] = startTime.plusDays(difference);
      weekdayRanges[i][1] = endTime.plusDays(difference);
    }

    for (LocalDateTime[] day : weekdayRanges) {
      for (int t = 0; t < times; t++) {
        createEvent(subject, day[0], day[1]);
        day[0] = day[0].plusDays(7);
        day[1] = day[1].plusDays(7);
      }
    }
  }

  @Override
  public Map<Months, List<Day>> getCalendar() {
    return calendar;
  }
}
