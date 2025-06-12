package controller.parse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Contains a mass of values extracted from the parsing that will be passed into the model methods.
 */
public class ParseResult {
  private final boolean success;
  private final CommandType commandType;
  private final String errorMessage;

  // CREATE event fields
  private final String subject;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final RepeatInfo repeatInfo;

  // EDIT command fields
  private final PropertyType property;
  private final String eventSubject;
  private final LocalDateTime eventStart;
  private final LocalDateTime eventEnd;
  private final String newValue;

  // PRINT command fields
  private final LocalDateTime printStartDate;
  private final LocalDateTime printEndDate;

  // SHOW STATUS command fields
  private final LocalDateTime statusDateTime;

  // CALENDAR command fields
  private final String calendarName;
  private final ZoneId timezone;
  private final String propertyName;
  private final String propertyValue;

  // COPY command fields
  private final String targetCalendarName;
  private final LocalDateTime targetDateTime;
  private final LocalDate sourceDate;
  private final LocalDate targetDate;
  private final LocalDate copyStartDate;
  private final LocalDate copyEndDate;

  private ParseResult(boolean success, CommandType commandType, String errorMessage,
                      String subject, LocalDateTime startTime,
                      LocalDateTime endTime, RepeatInfo repeatInfo,
                      PropertyType property, String eventSubject,
                      LocalDateTime eventStart,
                      LocalDateTime eventEnd, String newValue,
                      LocalDateTime printStartDate, LocalDateTime printEndDate,
                      LocalDateTime statusDateTime,
                      String calendarName, ZoneId timezone,
                      String propertyName, String propertyValue,
                      String targetCalendarName, LocalDateTime targetDateTime,
                      LocalDate sourceDate, LocalDate targetDate,
                      LocalDate copyStartDate, LocalDate copyEndDate) {
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
    this.calendarName = calendarName;
    this.timezone = timezone;
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
    this.sourceDate = sourceDate;
    this.targetDate = targetDate;
    this.copyStartDate = copyStartDate;
    this.copyEndDate = copyEndDate;
  }

  /**
   * Creates the new event.
   * @param subject the subject of the event
   * @param startTime the start time of the event
   * @param endTime the end time of the event
   * @param repeatInfo the repeat days
   * @return a new event
   */
  public static ParseResult createEvent(String subject, LocalDateTime startTime,
                                        LocalDateTime endTime, RepeatInfo repeatInfo) {
    return new ParseResult(true, CommandType.CREATE_EVENT, null,
            subject, startTime, endTime, repeatInfo,
            null, null, null, null, null,
            null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
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
                                      String eventSubject, LocalDateTime eventStart,
                                      LocalDateTime eventEnd, String newValue) {
    return new ParseResult(true, editType, null,
            null, null, null, null,
            property, eventSubject, eventStart, eventEnd, newValue,
            null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Parses the print event command.
   * @param startDate the start date of the event
   * @param endDate the end date of the event
   * @return the event to be printed
   */
  public static ParseResult printEventsInterval(LocalDateTime startDate,
                                                LocalDateTime endDate) {
    return new ParseResult(true, CommandType.PRINT_EVENTS, null,
            null, null, null, null,
            null, null, null, null, null,
            startDate, endDate, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Parses the print event command.
   * @param startDate the start date of the event
   * @return the event to be printed
   */
  public static ParseResult printEventsDay(LocalDate startDate) {
    return new ParseResult(true, CommandType.PRINT_EVENTS, null,
            null, null, null, null,
            null, null, null, null, null,
            startDate.atStartOfDay(), null, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Shows the status of the specified date.
   * @param dateTime the day to check
   * @return the status of the date
   */
  public static ParseResult showStatus(LocalDateTime dateTime) {
    return new ParseResult(true, CommandType.SHOW_STATUS, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, dateTime,
            null, null, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Creates a new calendar.
   * @param calendarName the name of the calendar
   * @param timezone the timezone of the calendar
   * @return create calendar command result
   */
  public static ParseResult createCalendar(String calendarName, ZoneId timezone) {
    return new ParseResult(true, CommandType.CREATE_CALENDAR, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            calendarName, timezone, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Edits an existing calendar.
   * @param calendarName the name of the calendar to edit
   * @param propertyName the property to change
   * @param propertyValue the new value
   * @return edit calendar command result
   */
  public static ParseResult editCalendar(String calendarName, String propertyName,
                                         String propertyValue) {
    return new ParseResult(true, CommandType.EDIT_CALENDAR, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            calendarName, null, propertyName, propertyValue,
            null, null, null, null,
            null, null);
  }

  /**
   * Sets the active calendar.
   * @param calendarName the name of the calendar to use
   * @return use calendar command result
   */
  public static ParseResult useCalendar(String calendarName) {
    return new ParseResult(true, CommandType.USE_CALENDAR, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            calendarName, null, null, null,
            null, null, null, null,
            null, null);
  }

  /**
   * Copies a single event.
   * @param eventName the event name
   * @param sourceDateTime the source event date/time
   * @param targetCalendarName the target calendar
   * @param targetDateTime the target date/time
   * @return copy event command result
   */
  public static ParseResult copySingleEvent(String eventName, LocalDateTime sourceDateTime,
                                            String targetCalendarName,
                                            LocalDateTime targetDateTime) {
    return new ParseResult(true, CommandType.COPY_SINGLE_EVENT, null,
            eventName, sourceDateTime, null, null,
            null, null, null, null, null,
            null, null, null,
            null, null, null, null,
            targetCalendarName, targetDateTime, null, null,
            null, null);
  }

  /**
   * Copies all events on a day.
   * @param sourceDate the source date
   * @param targetCalendarName the target calendar
   * @param targetDate the target date
   * @return copy events command result
   */
  public static ParseResult copyEventsOnDay(LocalDate sourceDate, String targetCalendarName,
                                            LocalDate targetDate) {
    return new ParseResult(true, CommandType.COPY_EVENTS_ON_DAY, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            null, null, null, null,
            targetCalendarName, null, sourceDate, targetDate,
            null, null);
  }

  /**
   * Copies events between dates.
   * @param startDate the start date
   * @param endDate the end date
   * @param targetCalendarName the target calendar
   * @param targetDate the target start date
   * @return copy events between command result
   */
  public static ParseResult copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                              String targetCalendarName, LocalDate targetDate) {
    return new ParseResult(true, CommandType.COPY_EVENTS_BETWEEN, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            null, null, null, null,
            targetCalendarName, null, null, targetDate, startDate, endDate);
  }

  /**
   * Exits the application.
   * @return exit command
   */
  public static ParseResult exit() {
    return new ParseResult(true, CommandType.EXIT, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
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
            null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null);
  }

  // Existing getters...

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
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * gets the end time of the event to be made.
   * @return the localDateTime of the end time
   */
  public LocalDateTime getEndTime() {
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
  public LocalDateTime getEventStart() {
    return eventStart;
  }

  /**
   * the event's end time.
   * @return the localDateTime of the end time
   */
  public LocalDateTime getEventEnd() {
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
  public LocalDateTime getPrintStartDate() {
    return printStartDate;
  }

  /**
   * The end date of the event that we want to print.
   * @return the end time as a localDate
   */
  public LocalDateTime getPrintEndDate() {
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
  public LocalDateTime getStatusDateTime() {
    return statusDateTime;
  }

  // CALENDAR command getters

  /**
   * Gets the calendar name.
   * @return the calendar name
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Gets the timezone.
   * @return the timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Gets the property name for edit calendar.
   * @return the property name
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Gets the property value for edit calendar.
   * @return the property value
   */
  public String getPropertyValue() {
    return propertyValue;
  }

  // COPY command getters

  /**
   * Gets the target calendar name.
   * @return the target calendar name
   */
  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  /**
   * Gets the target date/time for copy event.
   * @return the target date/time
   */
  public LocalDateTime getTargetDateTime() {
    return targetDateTime;
  }

  /**
   * Gets the source date for copy events.
   * @return the source date
   */
  public LocalDate getSourceDate() {
    return sourceDate;
  }

  /**
   * Gets the target date for copy events.
   * @return the target date
   */
  public LocalDate getTargetDate() {
    return targetDate;
  }

  /**
   * Gets the start date for copy between.
   * @return the start date
   */
  public LocalDate getCopyStartDate() {
    return copyStartDate;
  }

  /**
   * Gets the end date for copy between.
   * @return the end date
   */
  public LocalDate getCopyEndDate() {
    return copyEndDate;
  }
}