package view;

import java.util.Scanner;
import controller.CalendarController;

/**
 * Handles the interactive interface where a user can input commands one by one and get
 * immediate responses.
 */
public class InteractiveView implements IView {
  private final CalendarController controller;
  private final Scanner scanner;
  private static final String EXIT_COMMAND = "exit";
  private static final String PROMPT = "> ";

  public InteractiveView(CalendarController controller) {
    this.controller = controller;
    this.scanner = new Scanner(System.in);
  }

  @Override
  public void run() {
    System.out.println("Calendar Application - Interactive Mode");
    System.out.println("Type 'exit' to quit");
    System.out.println();

    while (true) {
      String command = getUserInput();

      if (command.isEmpty()) {
        continue;
      }

      if (command.equalsIgnoreCase(EXIT_COMMAND)) {
        System.out.println("Goodbye!");
        break;
      }

      processCommand(command);
      System.out.println();
    }

    scanner.close();
  }

  @Override
  public void run(String parameter) {
    // This is strictly for headless
    run();
  }

  @Override
  public void displayOutput(String output) {
    if (output != null && !output.isEmpty()) {
      System.out.println(output);
    }
  }

  @Override
  public void displayError(String error) {
    System.err.println("Error: " + error);
  }

  /**
   * Gets user input from the command line.
   */
  private String getUserInput() {
    System.out.print(PROMPT);
    return scanner.nextLine().trim();
  }

  /**
   * Processes a command through the controller.
   */
  private void processCommand(String command) {
    try {
      String result = controller.executeCommand(command);
      displayOutput(result);
    } catch (Exception e) {
      displayError(e.getMessage());
    }
  }
}
