package model.Calendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a day of a month that may contain events.
 */
public class Day {
  private final int day;
  private List<Event> events = new ArrayList<Event>();

  /**
   * constructs a new Day object in a month.
   * @param day the numbered day of the month
   */
  public Day(int day) {
    this.day = day;
  }

  /**
   * returns the numbered day of the month.
   * @return the numbered day of the month
   */
  public int getDay() {
    return day;
  }

  public List<Event> getEvents() {
    return events;
  }

  /**
   * adds a new event to this day.
   * @param event the event being added
   */
  public void addEvent(Event event) {
    events.add(event);
  }
}
