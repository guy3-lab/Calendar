package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import controller.CalendarController;

/**
 * Handles the headless mode user interface where commands are read from a file
 * and executed line-by-line.
 */
public class HeadlessView implements IView {
  private final CalendarController controller;
  private static final String EXIT_COMMAND = "exit";
  private static final String COMMENT_PREFIX = "#";

  public HeadlessView(CalendarController controller) {
    this.controller = controller;
  }

  @Override
  public void run() {
    displayError("Headless mode requires a command file");
  }

  @Override
  public void run(String filename) {
    System.out.println("Calendar Application - Headless Mode");
    System.out.println("Reading commands from: " + filename);
    System.out.println();

    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      processCommandFile(reader);
    } catch (IOException e) {
      displayError("Could not read command file '" + filename + "': " + e.getMessage());
      // instead of System.exit(1), which terminates every test hereafter
      // throw an exception that tests can catch
      throw new RuntimeException("Failed to read command file: " + e.getMessage());
    }
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
   * Processes all commands from the file.
   */
  private void processCommandFile(BufferedReader reader) throws IOException {
    String command;
    int lineNumber = 0;
    boolean exitFound = false;

    while ((command = reader.readLine()) != null) {
      lineNumber++;
      command = command.trim();

      if (command.isEmpty() || command.startsWith(COMMENT_PREFIX)) {
        continue;
      }

      System.out.println("Currently executing: " + command);

      if (command.equalsIgnoreCase(EXIT_COMMAND)) {
        exitFound = true;
        displayOutput("Exit command found. Terminating.");
        break;
      }

      processCommand(command, lineNumber);
      System.out.println();
    }

    if (!exitFound) {
      displayError("Command file must end with 'exit' command");
      // instead of System.exit(1), which terminates every test hereafter
      // throw an exception that tests can catch
      throw new RuntimeException("Command file must end with 'exit' command");
    }
  }

  /**
   * Processes a single command through the controller.
   */
  private void processCommand(String command, int lineNumber) {
    try {
      String result = controller.executeCommand(command);
      displayOutput(result);
    } catch (Exception e) {
      displayError("Line " + lineNumber + ": " + e.getMessage());
    }
  }
}