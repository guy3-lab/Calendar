package controller;

public class ParseResult {
  private final boolean success;
  private final CommandType commandType;
  private final String errorMessage;

  // CREATE event fields
  private final String subject;
  private final java.time.LocalDateTime startTime;
  private final java.time.LocalDateTime endTime;
  private final RepeatInfo repeatInfo;

  // EDIT command fields
  private final PropertyType property;
  private final String eventSubject;
  private final java.time.LocalDateTime eventStart;
  private final java.time.LocalDateTime eventEnd;
  private final String newValue;

  // PRINT command fields
  private final java.time.LocalDate printStartDate;
  private final java.time.LocalDate printEndDate;

  // SHOW STATUS command fields
  private final java.time.LocalDateTime statusDateTime;

  private ParseResult(boolean success, CommandType commandType, String errorMessage,
                      String subject, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, RepeatInfo repeatInfo,
                      PropertyType property, String eventSubject, java.time.LocalDateTime eventStart,
                      java.time.LocalDateTime eventEnd, String newValue,
                      java.time.LocalDate printStartDate, java.time.LocalDate printEndDate,
                      java.time.LocalDateTime statusDateTime) {
    this.success = success;
    this.commandType = commandType;
    this.errorMessage = errorMessage;
    this.subject = subject;
    this.startTime = startTime;
    this.endTime = endTime;
    this.repeatInfo = repeatInfo;
    this.property = property;
    this.eventSubject = eventSubject;
    this.eventStart = eventStart;
    this.eventEnd = eventEnd;
    this.newValue = newValue;
    this.printStartDate = printStartDate;
    this.printEndDate = printEndDate;
    this.statusDateTime = statusDateTime;
  }

  public static ParseResult createEvent(String subject, java.time.LocalDateTime startTime,
                                        java.time.LocalDateTime endTime, RepeatInfo repeatInfo) {
    return new ParseResult(true, CommandType.CREATE_EVENT, null,
            subject, startTime, endTime, repeatInfo,
            null, null, null, null, null,
            null, null, null);
  }

  public static ParseResult editEvent(CommandType editType, PropertyType property,
                                      String eventSubject, java.time.LocalDateTime eventStart,
                                      java.time.LocalDateTime eventEnd, String newValue) {
    return new ParseResult(true, editType, null,
            null, null, null, null,
            property, eventSubject, eventStart, eventEnd, newValue,
            null, null, null);
  }

  public static ParseResult printEvents(java.time.LocalDate startDate, java.time.LocalDate endDate) {
    return new ParseResult(true, CommandType.PRINT_EVENTS, null,
            null, null, null, null,
            null, null, null, null, null,
            startDate, endDate, null);
  }

  public static ParseResult showStatus(java.time.LocalDateTime dateTime) {
    return new ParseResult(true, CommandType.SHOW_STATUS, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, dateTime);
  }

  public static ParseResult exit() {
    return new ParseResult(true, CommandType.EXIT, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null);
  }

  public static ParseResult error(String errorMessage) {
    return new ParseResult(false, null, errorMessage,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null);
  }

  // getters
  public boolean isSuccess() { return success; }
  public CommandType getCommandType() { return commandType; }
  public String getErrorMessage() { return errorMessage; }

  // CREATE event getters
  public String getSubject() { return subject; }
  public java.time.LocalDateTime getStartTime() { return startTime; }
  public java.time.LocalDateTime getEndTime() { return endTime; }
  public RepeatInfo getRepeatInfo() { return repeatInfo; }
  public boolean isAllDay() { return endTime == null; }
  public boolean isRepeating() { return repeatInfo != null; }

  // EDIT command getters
  public PropertyType getProperty() { return property; }
  public String getEventSubject() { return eventSubject; }
  public java.time.LocalDateTime getEventStart() { return eventStart; }
  public java.time.LocalDateTime getEventEnd() { return eventEnd; }
  public String getNewValue() { return newValue; }

  // PRINT command getters
  public java.time.LocalDate getPrintStartDate() { return printStartDate; }
  public java.time.LocalDate getPrintEndDate() { return printEndDate; }
  public boolean isPrintRange() { return printEndDate != null; }

  // SHOW STATUS command getters
  public java.time.LocalDateTime getStatusDateTime() { return statusDateTime; }
}
