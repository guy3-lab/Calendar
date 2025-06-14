package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;


import javax.swing.*;

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
          JComboBox<?> source = (JComboBox<?>) e.getSource();
          String selected = (String) source.getSelectedItem();
          multiCalendar.useCalendar(selected);
          view.getCalendarLabel().setText(selected);
          break;
        case "chooseDate":
          chooseDateHelper();
          break;
        case "createEvent":
          createEventHelper();
        default: //nothing happens because no action is done
      }
    } catch (Exception ex) {
      view.setStatus(ex.getMessage());
    }
  }

  //prints out the first 10 events starting at the inputted starting time
  private void chooseDateHelper() {
    String year = view.getYearTextField();
    String month = view.getMonthTextField();
    String day = view.getDayTextField();

    int hourInt = Integer.parseInt(view.getHourTextField());
    String hour = String.format("%02d", hourInt);

    int minInt = Integer.parseInt(view.getMinuteTextField());
    String minute = String.format("%02d", minInt);

    LocalDateTime start = LocalDateTime.parse(year + "-" + month + "-" + day + "T"
            + hour + ":" + minute);
    view.setEvents(multiCalendar.getCurrent().printEventsInterval(start, null));
    view.clearDateFieldsAfterRetrieving();
    view.updateCalendar();
    view.setStatus("Events starting at " + start);
  }

  //creates an event from the user inputs
  private void createEventHelper() {
    String eventName = view.getEventName();
    String fromYear = view.getFromYearTextField();
    String toYear = view.getToYearTextField();

    String fromMonth = view.getFromMonthTextField();
    String toMonth = view.getToMonthTextField();

    String fromDay = view.getFromDayTextField();
    String toDay = view.getToDayTextField();

    int fromHourInt = Integer.parseInt(view.getFromHourTextField());
    String fromHour = String.format("%02d", fromHourInt);
    int toHourInt = Integer.parseInt(view.getToHourTextField());
    String toHour = String.format("%02d", toHourInt);

    int fromMinInt = Integer.parseInt(view.getFromMinuteTextField());
    String fromMin = String.format("%02d", fromMinInt);
    int toMinInt = Integer.parseInt(view.getToMinuteTextField());
    String toMin = String.format("%02d", toMinInt);

    LocalDateTime start = LocalDateTime.parse(fromYear + "-" + fromMonth + "-" + fromDay + "T"
            + fromHour + ":" + fromMin);
    LocalDateTime end = LocalDateTime.parse(toYear + "-" + toMonth + "-" + toDay + "T"
            + toHour + ":" + toMin);
    multiCalendar.getCurrent().createEvent(eventName, start, end);
    view.clearDateFieldsAfterCreation();
    view.setStatus(eventName + " created");
  }
}

