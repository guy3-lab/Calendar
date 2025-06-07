package controller.parse;

/**
 * Contains a mass of values extracted from the parsing that will be passed into the model methods.
 */
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
  private final java.time.LocalDateTime printStartDate;
  private final java.time.LocalDateTime printEndDate;

  // SHOW STATUS command fields
  private final java.time.LocalDateTime statusDateTime;

  private ParseResult(boolean success, CommandType commandType, String errorMessage,
                      String subject, java.time.LocalDateTime startTime,
                      java.time.LocalDateTime endTime, RepeatInfo repeatInfo,
                      PropertyType property, String eventSubject,
                      java.time.LocalDateTime eventStart,
                      java.time.LocalDateTime eventEnd, String newValue,
                      java.time.LocalDateTime printStartDate, java.time.LocalDateTime printEndDate,
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

  /**
   * Creates the new event.
   * @param subject the subject of the event
   * @param startTime the start time of the event
   * @param endTime the end time of the event
   * @param repeatInfo the repeat days
   * @return a new event
   */
  public static ParseResult createEvent(String subject, java.time.LocalDateTime startTime,
                                        java.time.LocalDateTime endTime, RepeatInfo repeatInfo) {
    return new ParseResult(true, CommandType.CREATE_EVENT, null,
            subject, startTime, endTime, repeatInfo,
            null, null, null, null, null,
            null, null, null);
  }

  /**
   * Edits the event depending on the constraints.
   * @param editType the type of edit being made
   * @param property the property that is being edited
   * @param eventSubject the event subject
   * @param eventStart the event start time
   * @param eventEnd the event end time
   * @param newValue the new value to be changed into
   * @return an edited event
   */
  public static ParseResult editEvent(CommandType editType, PropertyType property,
                                      String eventSubject, java.time.LocalDateTime eventStart,
                                      java.time.LocalDateTime eventEnd, String newValue) {
    return new ParseResult(true, editType, null,
            null, null, null, null,
            property, eventSubject, eventStart, eventEnd, newValue,
            null, null, null);
  }

  /**
   * Parses the print event command.
   * @param startDate the start date of the event
   * @param endDate the end date of the event
   * @return the event to be printed
   */
  public static ParseResult printEventsInterval(java.time.LocalDateTime startDate,
                                        java.time.LocalDateTime endDate) {
    return new ParseResult(true, CommandType.PRINT_EVENTS, null,
            null, null, null, null,
            null, null, null, null, null,
            startDate, endDate, null);
  }

  /**
   * Parses the print event command.
   * @param startDate the start date of the event
   * @return the event to be printed
   */
  public static ParseResult printEventsDay(java.time.LocalDate startDate) {
    return new ParseResult(true, CommandType.PRINT_EVENTS, null,
            null, null, null, null,
            null, null, null, null, null,
            startDate.atStartOfDay(), null, null);
  }

  /**
   * Shows the status of the specified date.
   * @param dateTime the day to check
   * @return the status of the date
   */
  public static ParseResult showStatus(java.time.LocalDateTime dateTime) {
    return new ParseResult(true, CommandType.SHOW_STATUS, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, dateTime);
  }

  /**
   * Exits the application.
   * @return exit command
   */
  public static ParseResult exit() {
    return new ParseResult(true, CommandType.EXIT, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null);
  }

  /**
   * presents an error.
   * @param errorMessage the error message
   * @return the error parsed
   */
  public static ParseResult error(String errorMessage) {
    return new ParseResult(false, null, errorMessage,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null);
  }

  // getters

  /**
   * returns whether or not it's a success.
   * @return true or false
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * returns the command type being passed.
   * @return the command type
   */
  public CommandType getCommandType() {
    return commandType;
  }

  /**
   * gets the error message.
   * @return the error message as a string
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  // CREATE event getters

  /**
   * returns the subject to make the event.
   * @return a string of the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start time of the event to be made.
   * @return the localDateTime of the start time
   */
  public java.time.LocalDateTime getStartTime() {
    return startTime;
  }


  /**
   * gets the end time of the event to be made.
   * @return the localDateTime of the end time
   */
  public java.time.LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * gets the repeat info of the event.
   * @return the repeat info
   */
  public RepeatInfo getRepeatInfo() {
    return repeatInfo;
  }

  /**
   * Returns whether or not the end time exists or not. If it does, it's a full day.
   * @return true or false
   */
  public boolean isAllDay() {
    return endTime == null;
  }

  /**
   * Returns whether or not the event repeats.
   * @return true or false
   */
  public boolean isRepeating() {
    return repeatInfo != null;
  }

  // EDIT command getters

  /**
   * gets the property type that is to be changed.
   * @return the property type
   */
  public PropertyType getProperty() {
    return property;
  }

  /**
   * The event's subject.
   * @return string of the subject
   */
  public String getEventSubject() {
    return eventSubject;
  }

  /**
   * the event's start time.
   * @return the localDateTime of the event
   */
  public java.time.LocalDateTime getEventStart() {
    return eventStart;
  }

  /**
   * the event's end time.
   * @return the localDateTime of the end time
   */
  public java.time.LocalDateTime getEventEnd() {
    return eventEnd;
  }

  /**
   * the new value to be changed into.
   * @return the string of the new value
   */
  public String getNewValue() {
    return newValue;
  }

  // PRINT command getters

  /**
   * The start time of the event that we want to print.
   * @return the start time as a localDate
   */
  public java.time.LocalDateTime getPrintStartDate() {
    return printStartDate;
  }

  /**
   * The end date of the event that we want to print.
   * @return the end time as a localDate
   */
  public java.time.LocalDateTime getPrintEndDate() {
    return printEndDate;
  }

  /**
   * Returns whether or not the end time is valid.
   * @return true or false
   */
  public boolean isPrintRange() {
    return printEndDate != null;
  }

  // SHOW STATUS command getters

  /**
   * Returns the date time of the day we want to check the status of.
   * @return the localDateTime of the day
   */
  public java.time.LocalDateTime getStatusDateTime() {
    return statusDateTime;
  }
}
