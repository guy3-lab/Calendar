package view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TimeZone;
import java.time.ZoneId;

import javax.swing.*;

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

  private String events = "";

  /**
   * Constructor of the GUI view, sets up the application view.
   */
  public GUIView() {
    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 700);
    frame.setLayout(new BorderLayout());

    // Create main panels
    frame.add(createTopPanel(), BorderLayout.NORTH);
    frame.add(createCenterPanel(), BorderLayout.CENTER);
    frame.add(createBottomPanel(), BorderLayout.SOUTH);

    // Center the window
    frame.setLocationRelativeTo(null);
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Calendar selection panel
    JPanel currentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calendarLabel = new JLabel("Current Calendar: ");
    calendarLabel.setFont(new Font("Arial", Font.BOLD, 14));
    calendarsDropdown = new JComboBox<String>();
    calendarsDropdown.setActionCommand("calendarSelected");
    calendarsDropdown.setPreferredSize(new Dimension(150, 25));
    currentPanel.add(calendarLabel);
    currentPanel.add(calendarsDropdown);

    // New calendar panel
    JPanel newCalendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    newCalendarPanel.add(new JLabel("New Calendar Name:"));
    calNameTextField = new JTextField(15);
    newCalendarPanel.add(calNameTextField);

    newCalendarPanel.add(new JLabel("Timezone:"));
    String[] timeZones = TimeZone.getAvailableIDs();
    timeZonesDropdown = new JComboBox<>(timeZones);
    timeZonesDropdown.setSelectedItem(ZoneId.systemDefault().getId());
    timeZonesDropdown.setPreferredSize(new Dimension(200, 25));
    newCalendarPanel.add(timeZonesDropdown);

    newCalendarButton = new JButton("Create Calendar");
    newCalendarButton.setActionCommand("createCalendar");
    newCalendarPanel.add(newCalendarButton);

    // Status panel
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusLabel = new JLabel(" ");
    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    statusLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    statusLabel.setPreferredSize(new Dimension(0, 25));
    statusPanel.add(statusLabel, BorderLayout.CENTER);

    topPanel.add(currentPanel);
    topPanel.add(newCalendarPanel);
    topPanel.add(Box.createVerticalStrut(5));
    topPanel.add(statusPanel);

    return topPanel;
  }

  private JPanel createCenterPanel() {
    calendarPanel = new JPanel(new BorderLayout());
    calendarPanel.setBorder(BorderFactory.createTitledBorder("Schedule View (Shows up to 10 events)"));

    // Create scrollable text area for events
    JTextArea eventsArea = new JTextArea();
    eventsArea.setEditable(false);
    eventsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    eventsArea.setText("No events to display");

    JScrollPane scrollPane = new JScrollPane(eventsArea);
    scrollPane.setPreferredSize(new Dimension(0, 200)); // Limit height
    calendarPanel.add(scrollPane, BorderLayout.CENTER);

    return calendarPanel;
  }

  private JPanel createBottomPanel() {
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    // View schedule panel
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchPanel.setBorder(BorderFactory.createTitledBorder("View Schedule"));
    searchPanel.add(new JLabel("From Date (YYYY-MM-DDThh:mm):"));
    dateTextField = new JTextField(16);
    searchPanel.add(dateTextField);
    chooseDate = new JButton("View Events");
    chooseDate.setActionCommand("chooseDate");
    searchPanel.add(chooseDate);

    // Create event panel
    JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    createPanel.setBorder(BorderFactory.createTitledBorder("Create Event"));
    createPanel.add(new JLabel("Event Name:"));
    nameTextField = new JTextField(15);
    createPanel.add(nameTextField);
    createPanel.add(new JLabel("From:"));
    fromDateTextField = new JTextField(16);
    createPanel.add(fromDateTextField);
    createPanel.add(new JLabel("To (empty for all-day):"));
    toDateTextField = new JTextField(16);
    createPanel.add(toDateTextField);
    createButton = new JButton("Create Event");
    createButton.setActionCommand("createEvent");
    createPanel.add(createButton);

    // Edit event panel
    JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    editPanel.setBorder(BorderFactory.createTitledBorder("Edit Event"));
    editPanel.add(new JLabel("Property:"));
    editPropertyDropdown = new JComboBox<PropertyType>(PropertyType.values());
    editPropertyDropdown.setPreferredSize(new Dimension(100, 25));
    editPanel.add(editPropertyDropdown);
    editPanel.add(new JLabel("Subject:"));
    editSubjectTextField = new JTextField(10);
    editPanel.add(editSubjectTextField);
    editPanel.add(new JLabel("From:"));
    fromEditTextField = new JTextField(16);
    editPanel.add(fromEditTextField);
    editPanel.add(new JLabel("To:"));
    toEditTextField = new JTextField(16);
    editPanel.add(toEditTextField);
    editPanel.add(new JLabel("New Value:"));
    editValueTextField = new JTextField(15);
    editPanel.add(editValueTextField);
    editButton = new JButton("Edit Event");
    editButton.setActionCommand("editEvent");
    editPanel.add(editButton);

    bottomPanel.add(searchPanel);
    bottomPanel.add(Box.createVerticalStrut(5));
    bottomPanel.add(createPanel);
    bottomPanel.add(Box.createVerticalStrut(5));
    bottomPanel.add(editPanel);

    return bottomPanel;
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
    // Not used in GUI mode
  }

  @Override
  public void displayError(String error) {
    JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void updateCalendar() {
    // Find the text area in the scroll pane and update it
    Component[] components = calendarPanel.getComponents();
    for (Component comp : components) {
      if (comp instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) comp;
        JViewport viewport = scrollPane.getViewport();
        Component view = viewport.getView();
        if (view instanceof JTextArea) {
          JTextArea textArea = (JTextArea) view;
          textArea.setText(events != null && !events.isEmpty() ? events : "No events to display");
          textArea.setCaretPosition(0);
        }
      }
    }
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
    return this.calNameTextField.getText().trim();
  }

  @Override
  public JComboBox<String> getTimeZoneDropdown() {
    return this.timeZonesDropdown;
  }

  @Override
  public String getDateTextField() {
    return this.dateTextField.getText().trim();
  }

  @Override
  public String getFromDateTextField() {
    return this.fromDateTextField.getText().trim();
  }

  @Override
  public String getToDateTextField() {
    return this.toDateTextField.getText().trim();
  }

  @Override
  public String getEventName() {
    return this.nameTextField.getText().trim();
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
    return this.fromEditTextField.getText().trim();
  }

  @Override
  public String getEditToTextField() {
    return this.toEditTextField.getText().trim();
  }

  @Override
  public String getEditValue() {
    return this.editValueTextField.getText().trim();
  }

  @Override
  public String getEditSubject() {
    return this.editSubjectTextField.getText().trim();
  }

  @Override
  public void clearDateFieldsAfterRetrieving() {
    // Keep the date for convenience
  }

  @Override
  public void clearDateFieldsAfterCreation() {
    nameTextField.setText("");
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