package controller.parse;

/**
 * Class that contains the repeat info that the user had inputted.
 */
public class RepeatInfo {
  private final String repeatDays;
  private final Integer repeatTimes;
  private final java.time.LocalDate repeatUntil;

  /**
   * Constructs the repeat info that is provided.
   * @param repeatDays the days to repeat on
   * @param repeatTimes the amount of times to be repeated
   * @param repeatUntil the date to repeat until
   */
  public RepeatInfo(String repeatDays, Integer repeatTimes, java.time.LocalDate repeatUntil) {
    this.repeatDays = repeatDays;
    this.repeatTimes = repeatTimes;
    this.repeatUntil = repeatUntil;
  }

  /**
   * gets the days that are being repeated on.
   * @return String of the days
   */
  public String getRepeatDays() {
    return repeatDays;
  }

  /**
   * Gets the times an event is to be repeated.
   * @return integer of the times
   */
  public Integer getRepeatTimes() {
    return repeatTimes;
  }

  /**
   * gets the date to repeat until.
   * @return the localDate of the date
   */
  public java.time.LocalDate getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Checks if there is a limit to the times.
   * @return true or false
   */
  public boolean hasTimeLimit() {
    return repeatTimes != null;
  }

  /**
   * checks if there is a date to repeat until.
   * @return true or false
   */
  public boolean hasDateLimit() {
    return repeatUntil != null;
  }
}
