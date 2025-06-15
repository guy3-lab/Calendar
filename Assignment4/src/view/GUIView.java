package view;


import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TimeZone;

import javax.swing.*;

import controller.GUICalendarController;
import controller.parse.PropertyType;
import model.calendar.ISpecificCalendar;

/**
 * Represents the GUI of the calendar program.
 */
public class GUIView extends JFrame implements IGuiView {
  private JFrame frame;
  private JPanel calendarPanel;
  private JLabel statusLabel;
  private JLabel calendarLabel;

  private JComboBox<String> calendarsDropdown;
  private JComboBox<String> timeZonesDropdown;
  private JComboBox<PropertyType> editPropertyDropdown;

  private JButton createButton;
  private JButton editButton;
  private JButton newCalendarButton;
  private JButton chooseDate;

  private JTextField calNameTextField;
  private JTextField dateTextField;
  private JTextField nameTextField;
  private JTextField fromDateTextField;
  private JTextField toDateTextField;

  private JTextField editSubjectTextField;
  private JTextField editValueTextField;
  private JTextField fromEditTextField;
  private JTextField toEditTextField;


  private String events;

  /**
   * Constructor of the GUI view, sets up the application view.
   */
  public GUIView(GUICalendarController controller) {
    frame = new JFrame("Calendar App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 1000);
    frame.setLayout(new BorderLayout());

    //top panel
    JPanel currentPanel = new JPanel();
    calendarLabel = new JLabel();
    calendarsDropdown = new JComboBox<String>();
    calendarsDropdown.setActionCommand("calendarSelected");
    currentPanel.add(calendarLabel);
    currentPanel.add(calendarsDropdown);

    JPanel newCalendarPanel = new JPanel();
    JLabel createNewCalendar = new JLabel("Create new calendar: ");
    calNameTextField = new JTextField();
    String[] timeZones = TimeZone.getAvailableIDs();
    timeZonesDropdown = new JComboBox<>(timeZones);
    newCalendarButton = new JButton("Create");
    newCalendarButton.setActionCommand("createCalendar");
    newCalendarPanel.add(createNewCalendar);
    newCalendarPanel.add(calNameTextField);
    newCalendarPanel.add(timeZonesDropdown);
    newCalendarPanel.add(newCalendarButton);

    JPanel statusPanel = new JPanel();
    statusLabel = new JLabel();
    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    statusPanel.add(statusLabel);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(currentPanel);
    topPanel.add(newCalendarPanel);
    topPanel.add(statusPanel);

    frame.add(topPanel, BorderLayout.NORTH);


    //center panel (outputs)
    calendarPanel = new JPanel();
    frame.add(calendarPanel, BorderLayout.CENTER);


    //bottom panel
    //search panel on top of the bottom panel
    JPanel searchPanel = new JPanel();
    chooseDate = new JButton("Get Events");
    JLabel chooseDateLabel = new JLabel("Choose a date (YYYY-MM-DDThh:mm): ");
    chooseDate.setActionCommand("chooseDate");
    dateTextField = new JTextField(16);

    searchPanel.add(chooseDateLabel);
    searchPanel.add(dateTextField);
    searchPanel.add(chooseDate);

    //create panel below search panel
    JPanel createPanel = new JPanel();
    createButton = new JButton("Create Event");
    createButton.setActionCommand("createEvent");
    nameTextField = new JTextField(10);
    JLabel eventLabel = new JLabel("Event Name: ");
    JLabel fromLabel = new JLabel("From (YYYY-MM-DDThh:mm): ");
    fromDateTextField = new JTextField(16);

    JLabel toLabel = new JLabel("To (YYYY-MM-DDThh:mm): ");
    toDateTextField = new JTextField(16);

    createPanel.add(eventLabel);
    createPanel.add(nameTextField);
    createPanel.add(fromLabel);
    createPanel.add(fromDateTextField);
    createPanel.add(toLabel);
    createPanel.add(toDateTextField);
    createPanel.add(createButton);

    //edit panel
    JPanel editPanel = new JPanel();
    editButton = new JButton("Edit");
    editButton.setActionCommand("editEvent");
    JLabel property = new JLabel("Property: ");
    JLabel subject = new JLabel("Subject: ");
    JLabel from = new JLabel("From (YYYY-MM-DDThh:mm): ");
    JLabel to = new JLabel("To (YYYY-MM-DDThh:mm): ");
    JLabel value = new JLabel("Value: ");
    editPropertyDropdown = new JComboBox<PropertyType>(PropertyType.values());
    editSubjectTextField = new JTextField(16);
    fromEditTextField = new JTextField(16);
    toEditTextField = new JTextField(16);
    editValueTextField = new JTextField(16);

    editPanel.add(property);
    editPanel.add(editPropertyDropdown);
    editPanel.add(subject);
    editPanel.add(editSubjectTextField);
    editPanel.add(from);
    editPanel.add(fromEditTextField);
    editPanel.add(to);
    editPanel.add(toEditTextField);
    editPanel.add(value);
    editPanel.add(editValueTextField);

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    bottomPanel.add(searchPanel);
    bottomPanel.add(createPanel);
    bottomPanel.add(editPanel);
    frame.add(bottomPanel, BorderLayout.SOUTH);

    updateCalendar();
  }

  @Override
  public void run() {
    frame.setVisible(true);
  }

  @Override
  public void run(String parameter) {
    run();
  }

  @Override
  public void displayOutput(String output) {

  }

  @Override
  public void displayError(String error) {

  }

  @Override
  public void updateCalendar() {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(12, 7));
    calendarPanel.setBackground(Color.WHITE);

    JTextArea eventsArea = new JTextArea(events);  // 'events' is the string you set from controller
    eventsArea.setEditable(false);
    eventsArea.setLineWrap(true);
    eventsArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(eventsArea);
    calendarPanel.add(scrollPane, BorderLayout.CENTER);

    frame.revalidate();
    frame.repaint();
  }

  @Override
  public void setActionListener(ActionListener listener) {
    calendarsDropdown.addActionListener(listener);
    createButton.addActionListener(listener);
    chooseDate.addActionListener(listener);
    newCalendarButton.addActionListener(listener);
    editButton.addActionListener(listener);
  }

  @Override
  public void setCalendarsDropdown(List<ISpecificCalendar> calendars) {
    this.calendarsDropdown.removeAllItems();
    for (ISpecificCalendar calendar : calendars) {
      this.calendarsDropdown.addItem(calendar.getName());
    }
  }

  @Override
  public String getCalName() {
    return this.calNameTextField.getText();
  }

  @Override
  public JComboBox<String> getTimeZoneDropdown() {
    return this.timeZonesDropdown;
  }

  @Override
  public String getDateTextField() {
    return this.dateTextField.getText();
  }

  @Override
  public String getFromDateTextField() {
    return this.fromDateTextField.getText();
  }

  @Override
  public String getToDateTextField() {
    return this.toDateTextField.getText();
  }

  @Override
  public String getEventName() {
    return this.nameTextField.getText();
  }

  @Override
  public JLabel getCalendarLabel() {
    return this.calendarLabel;
  }

  @Override
  public void setEvents(String events) {
    this.events = events;
  }

  @Override
  public void setStatus(String status) {
    this.statusLabel.setText(status);
  }

  @Override
  public JComboBox<PropertyType> getEditProperty() {
    return this.editPropertyDropdown;
  }
  @Override
  public String getEditFromTextField() {
    return this.fromEditTextField.getText();
  }

  @Override
  public String getEditToTextField() {
    return this.toEditTextField.getText();
  }

  @Override
  public String getEditValue() {
    return this.editValueTextField.getText();
  }

  @Override
  public String getEditSubject() {
    return this.editSubjectTextField.getText();
  }


  @Override
  public void clearDateFieldsAfterRetrieving() {
    dateTextField.setText("");
  }

  @Override
  public void clearDateFieldsAfterCreation() {
    fromDateTextField.setText("");
    toDateTextField.setText("");
  }

  @Override
  public void clearCalFieldsAfterCreation() {
    calNameTextField.setText("");
  }

  @Override
  public void clearDateFieldsAfterEditing() {
    editSubjectTextField.setText("");
    fromEditTextField.setText("");
    toEditTextField.setText("");
    editValueTextField.setText("");
  }
}
