package view;


import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import controller.GUICalendarController;
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
  private JButton createButton;
  private JButton chooseDate;

  private JTextField dayTextField;
  private JTextField monthTextField;
  private JTextField yearTextField;
  private JTextField hourTextField;
  private JTextField minuteTextField;

  private JTextField nameTextField;
  private JTextField fromDayTextField;
  private JTextField fromMonthTextField;
  private JTextField fromYearTextField;
  private JTextField fromHourTextField;
  private JTextField fromMinuteTextField;

  private JTextField toDayTextField;
  private JTextField toMonthTextField;
  private JTextField toYearTextField;
  private JTextField toHourTextField;
  private JTextField toMinuteTextField;

  private String events;

  /**
   * Constructor of the GUI view, sets up the application view.
   */
  public GUIView(GUICalendarController controller) {
    frame = new JFrame("Calendar App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 500);
    frame.setLayout(new BorderLayout());

    //top panel
    JPanel currentPanel = new JPanel();
    calendarLabel = new JLabel();
    calendarsDropdown = new JComboBox<String>();
    calendarsDropdown.setActionCommand("calendarSelected");
    currentPanel.add(calendarLabel);
    currentPanel.add(calendarsDropdown);

    JPanel statusPanel = new JPanel();
    statusLabel = new JLabel();
    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    statusPanel.add(statusLabel);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(currentPanel);
    topPanel.add(statusPanel);

    frame.add(topPanel, BorderLayout.NORTH);


    //center panel (outputs)
    calendarPanel = new JPanel();
    frame.add(calendarPanel, BorderLayout.CENTER);


    //bottom panel
    //search panel on top of the bottom panel
    JPanel searchPanel = new JPanel();
    chooseDate = new JButton("Get Events");
    chooseDate.setActionCommand("chooseDate");
    JLabel dayLabel = new JLabel("Day");
    JLabel monthLabel = new JLabel("Month");
    JLabel yearLabel = new JLabel("Year");
    JLabel hourLabel = new JLabel("Hour");
    JLabel minuteLabel = new JLabel("Minute");
    dayTextField = new JTextField(2);
    monthTextField = new JTextField(2);
    yearTextField = new JTextField(4);
    hourTextField = new JTextField(2);
    minuteTextField = new JTextField(2);

    searchPanel.add(yearLabel);
    searchPanel.add(yearTextField);
    searchPanel.add(monthLabel);
    searchPanel.add(monthTextField);
    searchPanel.add(dayLabel);
    searchPanel.add(dayTextField);
    searchPanel.add(hourLabel);
    searchPanel.add(hourTextField);
    searchPanel.add(minuteLabel);
    searchPanel.add(minuteTextField);
    searchPanel.add(chooseDate);

    //create panel below search panel
    JPanel createPanel = new JPanel();
    createButton = new JButton("Create Event");
    createButton.setActionCommand("createEvent");
    nameTextField = new JTextField(10);
    JLabel eventLabel = new JLabel("Event Name");
    JLabel fromLabel = new JLabel("From: ");
    JLabel fromDayLabel = new JLabel("Day");
    JLabel fromMonthLabel = new JLabel("Month");
    JLabel fromYearLabel = new JLabel("Year");
    JLabel fromHourLabel = new JLabel("Hour");
    JLabel fromMinuteLabel = new JLabel("Minute");
    fromDayTextField = new JTextField(2);
    fromMonthTextField = new JTextField(2);
    fromYearTextField = new JTextField(4);
    fromHourTextField = new JTextField(2);
    fromMinuteTextField = new JTextField(2);

    JLabel toLabel = new JLabel("To: ");
    JLabel toDayLabel = new JLabel("Day");
    JLabel toMonthLabel = new JLabel("Month");
    JLabel toYearLabel = new JLabel("Year");
    JLabel toHourLabel = new JLabel("Hour");
    JLabel toMinuteLabel = new JLabel("Minute");
    toDayTextField = new JTextField(2);
    toMonthTextField = new JTextField(2);
    toYearTextField = new JTextField(4);
    toHourTextField = new JTextField(2);
    toMinuteTextField = new JTextField(2);

    createPanel.add(eventLabel);
    createPanel.add(nameTextField);
    createPanel.add(fromLabel);
    createPanel.add(fromYearLabel);
    createPanel.add(fromMonthLabel);
    createPanel.add(fromDayLabel);
    createPanel.add(fromHourLabel);
    createPanel.add(fromMinuteLabel);
    createPanel.add(toLabel);
    createPanel.add(toYearLabel);
    createPanel.add(toMonthLabel);
    createPanel.add(toDayLabel);
    createPanel.add(toHourLabel);
    createPanel.add(toMinuteLabel);
    createPanel.add(createButton);

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    bottomPanel.add(searchPanel);
    bottomPanel.add(createPanel);
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
    calendarPanel.setLayout(new GridLayout(0, 7));
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
  }

  @Override
  public void setCalendarsDropdown(List<ISpecificCalendar> calendars) {
    this.calendarsDropdown.removeAllItems();
    for (ISpecificCalendar calendar : calendars) {
      this.calendarsDropdown.addItem(calendar.getName());
    }
  }

  @Override
  public String getYearTextField() {
    return this.yearTextField.getText();
  }

  @Override
  public String getMonthTextField() {
    return this.monthTextField.getText();
  }

  @Override
  public String getDayTextField() {
    return this.dayTextField.getText();
  }

  @Override
  public String getHourTextField() {
    return this.hourTextField.getText();
  }

  @Override
  public String getMinuteTextField() {
    return this.minuteTextField.getText();
  }

  @Override
  public String getFromYearTextField() {
    return this.yearTextField.getText();
  }

  @Override
  public String getFromMonthTextField() {
    return this.monthTextField.getText();
  }

  @Override
  public String getEventName() {
    return this.nameTextField.getText();
  }

  @Override
  public String getFromDayTextField() {
    return this.dayTextField.getText();
  }

  @Override
  public String getFromHourTextField() {
    return this.hourTextField.getText();
  }

  @Override
  public String getFromMinuteTextField() {
    return this.minuteTextField.getText();
  }

  @Override
  public String getToYearTextField() {
    return this.yearTextField.getText();
  }

  @Override
  public String getToMonthTextField() {
    return this.monthTextField.getText();
  }

  @Override
  public String getToDayTextField() {
    return this.dayTextField.getText();
  }

  @Override
  public String getToHourTextField() {
    return this.hourTextField.getText();
  }

  @Override
  public String getToMinuteTextField() {
    return this.minuteTextField.getText();
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
  public void clearDateFieldsAfterRetrieving() {
    yearTextField.setText("");
    monthTextField.setText("");
    dayTextField.setText("");
    hourTextField.setText("");
    minuteTextField.setText("");
  }

  @Override
  public void clearDateFieldsAfterCreation() {
    fromYearTextField.setText("");
    fromMonthTextField.setText("");
    fromDayTextField.setText("");
    fromHourTextField.setText("");
    fromMinuteTextField.setText("");

    toYearTextField.setText("");
    toMonthTextField.setText("");
    toDayTextField.setText("");
    toHourTextField.setText("");
    toMinuteTextField.setText("");
  }
}
