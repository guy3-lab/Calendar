package model.Enum;

/**
 * Represents the fixed months in a year.
 */
public enum Months {
  JAN(31), FEB(28), MAR(31), APR(30), MAY(31), JUN(30),
  JUL(31), AUG(31), SEP(30), OCT(31), NOV(30), DEC(31);

  private final int days;

  /**
   * Constructs the months with their specified amount of days and numerical value of the month.
   * @param days the amount of days in the month
   */
  Months(int days) {
    this.days = days;
  }

  /**
   * Gets the days of the month.
   * @param y the year to check for leap years
   * @return the days of the month
   */
  public int getDays(int y) {
    if (this.equals(FEB) && (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) {
      return 29;
    }
    return days;
  }
}
