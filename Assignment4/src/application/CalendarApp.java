package application;

import controller.CalendarController;
import controller.GUICalendarController;
import model.multicalendar.IMultiCalendar;
import model.multicalendar.MultiCalendar;
import view.GUIView;
import view.HeadlessView;
import view.InteractiveView;

/**
 * Main application class that handles command-line arguments and delegates
 * to appropriate view classes following strict MVC pattern.
 */
public class CalendarApp {
  private static final String INTERACTIVE_MODE = "interactive";
  private static final String HEADLESS_MODE = "headless";
  private static final String GUI_MODE = "gui";

  /**
   * The main function.
   * @param args user inputs
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      // Default to GUI mode when no arguments provided
      launchGUI();
      return;
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be '--mode'");
      printUsage();
      return;
    }

    if (args.length < 2) {
      System.err.println("Error: Mode not specified");
      printUsage();
      return;
    }

    String mode = args[1].toLowerCase();
    CalendarController controller = new CalendarController();

    switch (mode) {
      case INTERACTIVE_MODE:
        new InteractiveView(controller).run();
        break;
      case HEADLESS_MODE:
        if (args.length < 3) {
          System.err.println("Error: Headless mode requires a command file");
          printUsage();
          return;
        }
        new HeadlessView(controller).run(args[2]);
        break;
      case GUI_MODE:
        launchGUI();
        break;
      default:
        System.err.println("Error: Invalid mode '" + args[1] + "'. Must be 'interactive', " +
                "'headless', or 'gui'");
        printUsage();
    }
  }

  /**
   * Launches the GUI version of the calendar application.
   */
  private static void launchGUI() {
    // Add thread checker
    System.out.println("Main thread: " + Thread.currentThread().getName());

    javax.swing.SwingUtilities.invokeLater(() -> {
      System.out.println("EDT thread: " + Thread.currentThread().getName());
      try {
        System.out.println("Creating MultiCalendar...");
        IMultiCalendar multiCalendar = new MultiCalendar();

        System.out.println("Creating GUIView...");
        // Check if constructor completes
        long start = System.currentTimeMillis();
        GUIView view = new GUIView();
        long end = System.currentTimeMillis();
        System.out.println("GUIView created in " + (end - start) + "ms");

        System.out.println("Creating GUICalendarController...");
        GUICalendarController controller = new GUICalendarController(multiCalendar, view);

        System.out.println("Calling controller.go()...");
        controller.go();

        System.out.println("GUI initialization complete");

        // Check if EDT is still responsive
        javax.swing.Timer testTimer = new javax.swing.Timer(1000, e -> {
          System.out.println("EDT is responsive at: " + System.currentTimeMillis());
        });
        testTimer.setRepeats(true);
        testTimer.start();

      } catch (Exception e) {
        System.err.println("Error during GUI initialization:");
        e.printStackTrace();
      }
    });
  }

  /**
   * Prints usage information for the application.
   */
  private static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  java CalendarApp                          - Run in GUI mode (default)");
    System.out.println("  java CalendarApp --mode gui               - Run in GUI mode");
    System.out.println("  java CalendarApp --mode interactive       - Run in interactive mode");
    System.out.println("  java CalendarApp --mode headless <file>   - Run commands from file");
    System.out.println();
    System.out.println("Modes:");
    System.out.println("  gui          - Run with graphical user interface");
    System.out.println("  interactive  - Run in interactive mode with command prompt");
    System.out.println("  headless     - Run commands from file (must end with 'exit')");
  }
}