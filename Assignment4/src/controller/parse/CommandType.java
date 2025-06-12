package controller.parse;

/**
 * The fixed command types that exist for this application.
 */
public enum CommandType {
  CREATE_EVENT, EDIT_EVENT, EDIT_EVENTS, EDIT_SERIES,
  PRINT_EVENTS, SHOW_STATUS, EXIT,
  CREATE_CALENDAR, EDIT_CALENDAR, USE_CALENDAR,
  COPY_SINGLE_EVENT, COPY_EVENTS_ON_DAY, COPY_EVENTS_BETWEEN
}