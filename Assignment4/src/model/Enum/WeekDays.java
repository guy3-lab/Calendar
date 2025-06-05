package model.Enum;

/**
 * The weekdays enum that are the days of the week with their specific abbreviation and
 * corresponding day number.
 */
public enum WeekDays {
  MONDAY(1, "M"), TUESDAY(2, "T"), WEDNESDAY(3, "W"), THURSDAY(4, "R"),
  FRIDAY(5, "F"), SATURDAY(6, "S"), SUNDAY(7, "U");

  private final int dayNum;
  private final String abbreviation;

  WeekDays(int n, String s) {
    this.dayNum = n;
    this.abbreviation = s;
  }

  /**
   * Gets the abbreviation value of the weekday.
   * @return the abbreviation value
   */
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Gets the corresponding day number.
   * @return the corresponding day number
   */
  public int getDayNum() {
    return dayNum;
  }

  /**
   * Static method that takes in an abbreviation and returns the corresponding day number
   * @param abbreviation the weekday abbreviation
   * @return the corresponding day number
   */
  public static int getDay(String abbreviation) {
    for (WeekDays day : WeekDays.values()) {
      if (day.getAbbreviation().equalsIgnoreCase(abbreviation)) {
        return day.getDayNum();
      }
    }
    return -1;
  }
}
