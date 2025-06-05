package application;

import controller.Execution.CalendarController;
import view.HeadlessView;
import view.InteractiveView;

/**
 * Main application class that handles command-line arguments and delegates
 * to appropriate view classes following strict MVC pattern.
 */
public class CalendarApp {
  private static final String INTERACTIVE_MODE = "interactive";
  private static final String HEADLESS_MODE = "headless";

  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
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
      default:
        System.err.println("Error: Invalid mode '" + args[1] + "'. Must be 'interactive' or 'headless'");
        printUsage();
    }
  }

  /**
   * Prints usage information for the application.
   */
  private static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  java CalendarApp --mode interactive");
    System.out.println("  java CalendarApp --mode headless <command_file>");
    System.out.println();
    System.out.println("Modes:");
    System.out.println("  interactive  - Run in interactive mode with command prompt");
    System.out.println("  headless     - Run commands from file (must end with 'exit')");
  }
}