package model.Enum;

public enum WeekDays {
  MONDAY(1, "M"), TUESDAY(2, "T"), WEDNESDAY(3, "W"), THURSDAY(4, "R"),
  FRIDAY(5, "F"), SATURDAY(6, "S"), SUNDAY(7, "U");

  private final int dayNum;
  private final String abbreviation;

  WeekDays(int n, String s) {
    this.dayNum = n;
    this.abbreviation = s;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public int getDayNum() {
    return dayNum;
  }

  public static int getDay(String abbreviation) {
    for (WeekDays day : WeekDays.values()) {
      if (day.getAbbreviation().equalsIgnoreCase(abbreviation)) {
        return day.getDayNum();
      }
    }
    return -1;
  }
}
