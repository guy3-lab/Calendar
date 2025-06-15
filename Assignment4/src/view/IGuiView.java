package view;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import controller.parse.PropertyType;
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
   * Gets the calendar name for creating a new calendar.
   * @return the new calendar name
   */
  String getCalName();

  /**
   * gets the combobox for the timezone.
   * @return the combobox
   */
  JComboBox<String> getTimeZoneDropdown();

  /**
   * gets the date from the text field for when retrieving all events.
   * @return the date inputted
   */
  String getDateTextField();

  /**
   * gets the date from the text field for when creating a new event.
   * @return the date inputted
   */
  String getFromDateTextField();

  /**
   * gets the date from the text field for when creating a new event.
   * @return the date inputted
   */
  String getToDateTextField();

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

  JComboBox<PropertyType> getEditProperty();
  String getEditFromTextField();
  String getEditToTextField();
  String getEditValue();
  String getEditSubject();

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

  /**
   * Clears the date fields after creating an event.
   */
  void clearCalFieldsAfterCreation();

  /**
   * Clears the date fields after creating an event.
   */
  void clearDateFieldsAfterEditing();
}
