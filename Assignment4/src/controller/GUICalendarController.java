package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;


import javax.swing.*;

import controller.parse.PropertyType;
import model.multicalendar.IMultiCalendar;
import view.IGuiView;


/**
 * Represents the controller for the application with a GUI
 */
public class GUICalendarController implements IController, ActionListener {
  private final IMultiCalendar multiCalendar;
  private final IGuiView view;

  /**
   * Constructs the calendar controller that now has a GUI as a view.
   *
   * @param multiCalendar the multicalendar being passed in
   * @param view          the view being passed in
   */
  public GUICalendarController(IMultiCalendar multiCalendar, IGuiView view) {
    this.multiCalendar = multiCalendar;
    this.view = view;
  }

  @Override
  public String processCommand(String command) {
    return "";
  }

  @Override
  public void go() {
    multiCalendar.addCalendar("Default", ZoneId.systemDefault());
    multiCalendar.useCalendar("Default");
    this.view.setCalendarsDropdown(multiCalendar.getCalendars());
    this.view.setActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    try {
      switch (command) {
        case "calendarSelected":
          selectCalendar(e);
          break;
        case "chooseDate":
          chooseDateHelper();
          break;
        case "createEvent":
          createEventHelper();
          break;
        case "createCalendar":
          createNewCalendar();
          break;
        case "editEvent":
          editEventHelper();
          break;
        default: //nothing happens because no action is done
      }
    } catch (Exception ex) {
      view.setStatus(ex.getMessage());
    }
  }

  //sets the selected calendar to the current
  private void selectCalendar(ActionEvent e) {
    JComboBox<?> source = (JComboBox<?>) e.getSource();
    String selected = (String) source.getSelectedItem();
    multiCalendar.useCalendar(selected);
    view.setEvents("");
    view.updateCalendar();
    view.getCalendarLabel().setText(selected);
  }

  //prints out the first 10 events starting at the inputted starting time
  private void chooseDateHelper() {
    String date = view.getDateTextField();

    LocalDateTime start = LocalDateTime.parse(date);
    String events = multiCalendar.getCurrent().printEventsInterval(start, null);
    if (events.isEmpty()) {
      throw new IllegalArgumentException("No events found starting at " + date);
    }
    view.setEvents(events);
    view.clearDateFieldsAfterRetrieving();
    view.updateCalendar();
    view.setStatus("Events starting at " + start);
  }

  //creates an event from the user inputs
  private void createEventHelper() {
    String eventName = view.getEventName();
    String fromDate = view.getFromDateTextField();
    String toDate = view.getToDateTextField();

    LocalDateTime start = LocalDateTime.parse(fromDate);
    LocalDateTime end;
    if (toDate.isEmpty()) {
      end = null;
    } else {
      end = LocalDateTime.parse(toDate);
    }

    multiCalendar.getCurrent().createEvent(eventName, start, end);
    view.updateCalendar();
    view.clearDateFieldsAfterCreation();
    view.setStatus(eventName + " created");
  }

  //creates a new calendar
  private void createNewCalendar() {
    String calName = view.getCalName();
    String zone = view.getTimeZoneDropdown().getSelectedItem().toString();
    ZoneId zoneId = ZoneId.of(zone);
    multiCalendar.addCalendar(calName, zoneId);
    view.clearCalFieldsAfterCreation();
    view.setCalendarsDropdown(multiCalendar.getCalendars());
    view.setStatus(calName + " created");
  }

  //edits an event
  private void editEventHelper() {
    PropertyType property = (PropertyType) view.getEditProperty().getSelectedItem();
    String subject = view.getEditSubject();
    LocalDateTime start = LocalDateTime.parse(view.getEditFromTextField());
    LocalDateTime end = LocalDateTime.parse(view.getEditToTextField());
    String value = view.getEditValue();
    if (property == PropertyType.START || property == PropertyType.END) {
      try {
        LocalDateTime.parse(value);
      } catch (Exception ex) {
        throw new IllegalArgumentException("Invalid date value: " + value);
      }
    }
    multiCalendar.getCurrent().editEvent(property, subject, start, end, value);
    view.clearDateFieldsAfterEditing();
    view.updateCalendar();
    view.setStatus("Event edited");
  }
}

