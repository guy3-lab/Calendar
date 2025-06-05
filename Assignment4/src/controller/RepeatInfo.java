package controller;

public class RepeatInfo {
  private final String repeatDays;
  private final Integer repeatTimes;
  private final java.time.LocalDate repeatUntil;

  public RepeatInfo(String repeatDays, Integer repeatTimes, java.time.LocalDate repeatUntil) {
    this.repeatDays = repeatDays;
    this.repeatTimes = repeatTimes;
    this.repeatUntil = repeatUntil;
  }

  public String getRepeatDays() { return repeatDays; }
  public Integer getRepeatTimes() { return repeatTimes; }
  public java.time.LocalDate getRepeatUntil() { return repeatUntil; }
  public boolean hasTimeLimit() { return repeatTimes != null; }
  public boolean hasDateLimit() { return repeatUntil != null; }
}
