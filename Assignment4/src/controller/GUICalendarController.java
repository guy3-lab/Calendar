package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import controller.parse.PropertyType;
import model.calendar.IEvent;
import model.multicalendar.IMultiCalendar;
import view.IGuiView;

/**
 * Controller for the GUI version of the calendar application.
 */
public class GUICalendarController implements IController, ActionListener {
  private final IMultiCalendar multiCalendar;
  private final IGuiView view;
  private static final DateTimeFormatter DISPLAY_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    // Create default calendar if it doesn't exist
    try {
      multiCalendar.addCalendar("Default", ZoneId.systemDefault());
    } catch (IllegalArgumentException e) {
      // Calendar already exists, that's fine
    }

    multiCalendar.useCalendar("Default");
    this.view.setCalendarsDropdown(multiCalendar.getCalendars());
    this.view.setActionListener(this);
    view.getCalendarLabel().setText("Current Calendar: Default");

    // Initialize with welcome message
    view.setEvents("Welcome to Calendar Application\n\nUse 'View Events' to see your schedule.");
    view.updateCalendar();

    // Show the view
    view.run();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    // Run the action in a separate thread to prevent EDT freeze
    SwingUtilities.invokeLater(() -> {
      try {
        switch (command) {
          case "calendarSelected":
            selectCalendar(e);
            break;
          case "chooseDate":
            viewSchedule();
            break;
          case "createEvent":
            createEvent();
            break;
          case "createCalendar":
            createNewCalendar();
            break;
          case "editEvent":
            editEvent();
            break;
          default:
            // Unknown command
        }
      } catch (Exception ex) {
        ex.printStackTrace(); // Debug
        view.setStatus("Error: " + formatErrorMessage(ex));
      }
    });
  }

  private void selectCalendar(ActionEvent e) {
    try {
      JComboBox<?> source = (JComboBox<?>) e.getSource();
      String selected = (String) source.getSelectedItem();
      if (selected != null) {
        multiCalendar.useCalendar(selected);
        view.getCalendarLabel().setText("Current Calendar: " + selected);
        view.setStatus("Switched to calendar: " + selected);
        // Clear the current view
        view.setEvents("");
        view.updateCalendar();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      view.setStatus("Error switching calendar: " + ex.getMessage());
    }
  }

  private void viewSchedule() {
    try {
      String date = view.getDateTextField();
      if (date.isEmpty()) {
        // If no date specified, show from today
        LocalDateTime today = LocalDateTime.now();
        showEventsFrom(today);
      } else {
        LocalDateTime start = LocalDateTime.parse(date);
        showEventsFrom(start);
      }
    } catch (DateTimeParseException ex) {
      view.setStatus("Invalid date format. Use: YYYY-MM-DDThh:mm (e.g., 2025-06-19T10:00)");
    } catch (Exception ex) {
      ex.printStackTrace();
      view.setStatus("Error: " + formatErrorMessage(ex));
    }
  }

  private void showEventsFrom(LocalDateTime start) {
    try {
      StringBuilder eventDisplay = new StringBuilder();
      eventDisplay.append("Events from ").append(start.format(DISPLAY_FORMAT)).append(":\n\n");

      Map<LocalDate, List<IEvent>> calendar = multiCalendar.getCurrent().getCalendar();

      int eventCount = 0;

      // Get all dates in the calendar and process them in chronological order
      List<LocalDate> allDates = new ArrayList<>(calendar.keySet());
      allDates.sort(LocalDate::compareTo); // Sort dates chronologically

      for (LocalDate date : allDates) {
        // Only process dates that are on or after the start date
        if (!date.isBefore(start.toLocalDate())) {
          List<IEvent> dayEvents = calendar.get(date);

          for (IEvent event : dayEvents) {
            // Only include events that start at or after the specified time
            if (!event.getStart().isBefore(start)) {
              eventCount++;
              eventDisplay.append(String.format("%d. %s\n", eventCount, event.getSubject()));
              eventDisplay.append(String.format("   Start: %s\n",
                      event.getStart().format(DISPLAY_FORMAT)));
              eventDisplay.append(String.format("   End: %s\n",
                      event.getEnd().format(DISPLAY_FORMAT)));
              if (event.getLocation() != null) {
                eventDisplay.append(String.format("   Location: %s\n", event.getLocation()));
              }
              eventDisplay.append("\n");
            }
          }
        }
      }

      if (eventCount == 0) {
        eventDisplay.append("No events found from the specified date onwards.");
      }

      view.setEvents(eventDisplay.toString());
      view.updateCalendar();
      view.setStatus("Showing " + eventCount + " event(s) from " + start.format(DISPLAY_FORMAT));

    } catch (Exception ex) {
      ex.printStackTrace();
      view.setEvents("Error loading events. Please try again.");
      view.updateCalendar();
      view.setStatus("Error displaying events: " + ex.getMessage());
    }
  }

  private void createEvent() {
    try {
      String eventName = view.getEventName();
      if (eventName.isEmpty()) {
        view.setStatus("Event name cannot be empty");
        return;
      }

      String fromDate = view.getFromDateTextField();
      if (fromDate.isEmpty()) {
        view.setStatus("Start date/time cannot be empty");
        return;
      }

      String toDate = view.getToDateTextField();

      System.out.println("Creating event: " + eventName); // Debug
      System.out.println("From: " + fromDate); // Debug
      System.out.println("To: " + toDate); // Debug

      LocalDateTime start = LocalDateTime.parse(fromDate);
      LocalDateTime end = toDate.isEmpty() ? null : LocalDateTime.parse(toDate);

      if (end != null && end.isBefore(start)) {
        view.setStatus("End time must be after start time");
        return;
      }

      // Create the event
      multiCalendar.getCurrent().createEvent(eventName, start, end);

      System.out.println("Event created successfully"); // Debug

      view.clearDateFieldsAfterCreation();
      view.setStatus("Event '" + eventName + "' created successfully");

      // Don't try to refresh the view - it might cause freezing
      view.setEvents("Event '" + eventName + "' was created.\n" +
              "Use 'View Events' to see your updated schedule.");
      view.updateCalendar();

    } catch (DateTimeParseException ex) {
      view.setStatus("Invalid date format. Use: YYYY-MM-DDThh:mm (e.g., 2025-06-19T14:00)");
    } catch (Exception ex) {
      ex.printStackTrace();
      view.setStatus("Error: " + formatErrorMessage(ex));
    }
  }

  private void createNewCalendar() {
    try {
      String calName = view.getCalName();
      if (calName.isEmpty()) {
        view.setStatus("Calendar name cannot be empty");
        return;
      }

      String zone = view.getTimeZoneDropdown().getSelectedItem().toString();
      ZoneId zoneId = ZoneId.of(zone);

      multiCalendar.addCalendar(calName, zoneId);
      view.clearCalFieldsAfterCreation();
      view.setCalendarsDropdown(multiCalendar.getCalendars());
      view.setStatus("Calendar '" + calName + "' created successfully");
    } catch (Exception ex) {
      ex.printStackTrace();
      view.setStatus("Error: " + formatErrorMessage(ex));
    }
  }

  private void editEvent() {
    try {
      PropertyType property = (PropertyType) view.getEditProperty().getSelectedItem();
      String subject = view.getEditSubject();

      if (subject.isEmpty()) {
        view.setStatus("Event subject cannot be empty");
        return;
      }

      String startStr = view.getEditFromTextField();
      String endStr = view.getEditToTextField();

      if (startStr.isEmpty() || endStr.isEmpty()) {
        view.setStatus("Event start and end times cannot be empty");
        return;
      }

      LocalDateTime start = LocalDateTime.parse(startStr);
      LocalDateTime end = LocalDateTime.parse(endStr);
      String value = view.getEditValue();

      if (value.isEmpty() && property != PropertyType.DESCRIPTION) {
        view.setStatus("New value cannot be empty");
        return;
      }

      multiCalendar.getCurrent().editEvent(property, subject, start, end, value);
      view.clearDateFieldsAfterEditing();
      view.setStatus("Event edited successfully");

      // Don't refresh - might cause freezing
      view.setEvents("Event edited successfully.\n" +
              "Use 'View Events' to see your updated schedule.");
      view.updateCalendar();

    } catch (DateTimeParseException ex) {
      view.setStatus("Invalid date format. Use: YYYY-MM-DDThh:mm");
    } catch (Exception ex) {
      ex.printStackTrace();
      view.setStatus("Error: " + formatErrorMessage(ex));
    }
  }

  private String formatErrorMessage(Exception ex) {
    String message = ex.getMessage();

    if (message == null) {
      return "An unexpected error occurred";
    }

    // Make common errors more user-friendly
    if (message.contains("already exists")) {
      return "That name already exists. Please choose a different name.";
    } else if (message.contains("not found")) {
      return "The requested item was not found.";
    } else if (message.contains("Invalid datetime")) {
      return "Invalid date/time format. Use: YYYY-MM-DDThh:mm";
    } else if (message.contains("End time must be after")) {
      return "The end time must be after the start time.";
    }

    return message;
  }
}