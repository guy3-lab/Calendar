package view;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import model.calendar.ISpecificCalendar;

public interface IGuiView extends IView {

  void updateCalendar();
  /**
   * Sets the action listener to the controller that is passed in.
   * @param listener the listener
   */
  void setActionListener(ActionListener listener);

  /**
   * Sets/updates the calendars dropdown anytime there's a new calendar.
   */
  void setCalendarsDropdown(List<ISpecificCalendar> calendars);

  /**
   * Gets the year that was typed into this text field.
   * @return the string that the user inputted
   */
  String getYearTextField();

  /**
   * Gets the month that was typed into this text field.
   * @return the string that the user inputted
   */
  String getMonthTextField();

  /**
   * Gets the day that was typed into this text field.
   * @return the string that the user inputted
   */
  String getDayTextField();

  /**
   * Gets the hour that was typed into this text field.
   * @return the string that the user inputted
   */
  String getHourTextField();

  /**
   * Gets the minute that was typed into this text field.
   * @return the string that the user inputted
   */
  String getMinuteTextField();

  /**
   * Gets the year that was typed into this text field.
   * @return the string that the user inputted
   */
  String getFromYearTextField();

  /**
   * Gets the month that was typed into this text field.
   * @return the string that the user inputted
   */
  String getFromMonthTextField();

  /**
   * Gets the day that was typed into this text field.
   * @return the string that the user inputted
   */
  String getFromDayTextField();

  /**
   * Gets the hour that was typed into this text field.
   * @return the string that the user inputted
   */
  String getFromHourTextField();

  /**
   * Gets the minute that was typed into this text field.
   * @return the string that the user inputted
   */
  String getFromMinuteTextField();

  /**
   * Gets the year that was typed into this text field.
   * @return the string that the user inputted
   */
  String getToYearTextField();

  /**
   * Gets the month that was typed into this text field.
   * @return the string that the user inputted
   */
  String getToMonthTextField();

  /**
   * Gets the day that was typed into this text field.
   * @return the string that the user inputted
   */
  String getToDayTextField();

  /**
   * Gets the hour that was typed into this text field.
   * @return the string that the user inputted
   */
  String getToHourTextField();

  /**
   * Gets the minute that was typed into this text field.
   * @return the string that the user inputted
   */
  String getToMinuteTextField();

  /**
   * Gets the event name that was typed into this text field.
   * @return the string that the user inputted
   */
  String getEventName();

  /**
   * Gets the calendar name that's in the label.
   * @return the label of the calendar.
   */
  JLabel getCalendarLabel();

  /**
   * Sets the event text area to the inputted events.
   * @param events the events of the day that was chosen
   */
  void setEvents(String events);

  /**
   * Sets the status text to give a new status message.
   * @param status the new message.
   */
  void setStatus(String status);

  /**
   * Clears the date fields after retrieving all events from given date.
   */
  void clearDateFieldsAfterRetrieving();

  /**
   * Clears the date fields after creating an event.
   */
  void clearDateFieldsAfterCreation();
}
