package view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.CalendarController;

/**
 * Test class that tests the command line interface in order to ensure that everything
 * works correctly.
 */
public class CLITest {

  private ByteArrayOutputStream outputStream;
  private ByteArrayOutputStream errorStream;
  private PrintStream originalOut;
  private PrintStream originalErr;
  private File tempDirectory;
  private String tempDirPath;

  @Before
  public void setUp() {
    originalOut = System.out;
    originalErr = System.err;
    outputStream = new ByteArrayOutputStream();
    errorStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    System.setErr(new PrintStream(errorStream));

    try {
      Path tempPath = Files.createTempDirectory("calendar_test");
      tempDirectory = tempPath.toFile();
      tempDirPath = tempDirectory.getAbsolutePath();
    } catch (IOException e) {
      fail("Failed to create temporary directory: " + e.getMessage());
    }
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);

    if (tempDirectory != null && tempDirectory.exists()) {
      File[] files = tempDirectory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.exists()) {
            file.delete();
          }
        }
      }
      tempDirectory.delete();
    }
  }

  /**
   * Tests interactive view initialization and basic component setup.
   */
  @Test
  public void testInteractiveViewInitialization() {
    CalendarController controller = new CalendarController();
    assertNotNull("Controller should be initialized", controller);

    InteractiveView view = new InteractiveView(controller);
    assertNotNull("Interactive view should be initialized", view);

    assertTrue("Interactive view should implement IView", view instanceof IView);
  }

  /**
   * Tests interactive view output display functionality with various message types.
   */
  @Test
  public void testInteractiveViewOutputDisplay() {
    CalendarController controller = new CalendarController();
    InteractiveView view = new InteractiveView(controller);

    String testMessage = "Test output message";
    view.displayOutput(testMessage);

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);
    assertTrue("Output should contain test message", output.contains(testMessage));

    outputStream.reset();

    view.displayOutput(null);
    output = outputStream.toString();
    assertEquals("No output should be produced for null message", "", output);

    outputStream.reset();
    view.displayOutput("");
    output = outputStream.toString();
    assertEquals("No output should be produced for empty message", "", output);
  }

  /**
   * Tests interactive view error display functionality with proper error stream usage.
   */
  @Test
  public void testInteractiveViewErrorDisplay() {
    CalendarController controller = new CalendarController();
    InteractiveView view = new InteractiveView(controller);

    String errorMessage = "Test error message";
    view.displayError(errorMessage);

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Error output should contain error message", errorOutput
            .contains(errorMessage));
    assertTrue("Error output should contain 'Error:' prefix",
            errorOutput.contains("Error:"));

    String standardOutput = outputStream.toString();
    assertEquals("Standard output should be empty for error messages", "",
            standardOutput);
  }

  /**
   * Tests headless view initialization and component setup with controller dependency.
   */
  @Test
  public void testHeadlessViewInitialization() {
    CalendarController controller = new CalendarController();
    assertNotNull("Controller should be initialized", controller);

    HeadlessView view = new HeadlessView(controller);
    assertNotNull("Headless view should be initialized", view);

    assertTrue("Headless view should implement IView", view instanceof IView);
  }

  /**
   * Tests headless view output display functionality with proper formatting.
   */
  @Test
  public void testHeadlessViewOutputDisplay() {
    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    String testMessage = "Command executed successfully";
    view.displayOutput(testMessage);

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);
    assertTrue("Output should contain test message", output.contains(testMessage));

    outputStream.reset();

    view.displayOutput(null);
    output = outputStream.toString();
    assertEquals("No output should be produced for null message", "", output);

    outputStream.reset();
    view.displayOutput("");
    output = outputStream.toString();
    assertEquals("No output should be produced for empty message", "", output);
  }

  /**
   * Tests headless view error display functionality with proper error stream handling.
   */
  @Test
  public void testHeadlessViewErrorDisplay() {
    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    String errorMessage = "Command processing failed";
    view.displayError(errorMessage);

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Error output should contain error message", errorOutput
            .contains(errorMessage));
    assertTrue("Error output should contain 'Error:' prefix", errorOutput
            .contains("Error:"));

    String standardOutput = outputStream.toString();
    assertEquals("Standard output should be empty for error messages", "",
            standardOutput);
  }

  /**
   * Tests successful headless mode execution with valid command file containing multiple commands.
   */
  @Test
  public void testHeadlessExecutionSuccess() {
    String commands =
            "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event Morning Meeting from 2025-06-15T09:00 to 2025-06-15T10:00\n" +
                    "create event Lunch from 2025-06-15T12:00 to 2025-06-15T13:00\n" +
                    "print events on 2025-06-15\n" +
                    "show status on 2025-06-15T09:30\n" +
                    "show status on 2025-06-15T11:00\n" +
                    "exit\n";

    File commandFile = createTestCommandFile("success_test.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);
    assertTrue("Output should contain headless mode indicator", output
            .contains("Headless Mode"));
    assertTrue("Output should contain file path", output.contains(commandFile
            .getAbsolutePath()));
    assertTrue("Output should contain command execution traces",
            output.contains("Currently executing:"));
    assertTrue("Output should contain exit confirmation", output
            .contains("Exit command found"));

    String errorOutput = errorStream.toString();
    assertEquals("No errors should be produced for valid execution", "",
            errorOutput);
  }

  /**
   * Tests headless mode execution with command file containing comments and empty lines.
   */
  @Test
  public void testHeadlessExecutionWithCommentsAndEmptyLines() {
    String commands =
            "# This is a comment\n" +
                    "\n" +
                    "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event Test Event from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
                    "\n" +
                    "# Another comment\n" +
                    "print events on 2025-06-15\n" +
                    "\n" +
                    "exit\n";

    File commandFile = createTestCommandFile("comments_test.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);
    assertTrue("Output should contain event creation", output.contains("Created event"));
    assertFalse("Output should not contain comment lines",
            output.contains("Currently executing: #"));

    String[] lines = output.split("\n");
    for (String line : lines) {
      if (line.contains("Currently executing:")) {
        assertFalse("Should not execute comment lines", line.contains("#"));
        assertFalse("Should not execute empty lines", line.trim().isEmpty());
      }
    }
  }

  /**
   * Tests headless mode file processing with sequential command execution and proper line tracking.
   */
  @Test
  public void testHeadlessFileProcessingWithLineNumbers() {
    String commands =
            "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event Valid Event from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
                    "invalid command here\n" +
                    "print events on 2025-06-15\n" +
                    "another invalid command\n" +
                    "show status on 2025-06-15T10:30\n" +
                    "exit\n";

    File commandFile = createTestCommandFile("line_numbers_test.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    String errorOutput = errorStream.toString();

    assertNotNull("Output should not be null", output);
    assertNotNull("Error output should not be null", errorOutput);

    assertTrue("Should contain valid event creation", output.contains("Created event"));
    assertTrue("Should contain event listing", output.contains("Valid Event"));

    assertTrue("Error output should contain line number references",
            errorOutput.contains("Line 4:") || errorOutput.contains("Line 6:"));

    assertTrue("Should contain exit confirmation despite errors",
            output.contains("Exit command found"));
  }

  /**
   * Tests headless mode behavior when command file is missing or inaccessible.
   */
  @Test
  public void testHeadlessModeFileNotFound() {
    String nonExistentFile = tempDirPath + "/nonexistent_file.txt";
    assertFalse("File should not exist", new File(nonExistentFile).exists());

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    try {
      view.run(nonExistentFile);
      fail("Should throw exception for missing file");
    } catch (RuntimeException e) {
      assertTrue("Exception should mention file reading",
              e.getMessage().contains("Failed to read command file"));
    }

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Should contain file not found error",
            errorOutput.contains("Could not read command file"));

    String output = outputStream.toString();
    assertFalse("Should not contain success messages",
            output.contains("Exit command found"));
  }

  /**
   * Tests headless mode behavior when command file is missing the required exit command.
   */
  @Test
  public void testHeadlessModeNoExitCommand() {
    String commands =
            "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
                    "print events on 2025-06-15\n";

    File commandFile = createTestCommandFile("no_exit.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    try {
      view.run(commandFile.getAbsolutePath());
      fail("Should throw exception for missing exit command");
    } catch (RuntimeException e) {
      assertEquals("Command file must end with 'exit' command", e.getMessage());
    }

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Should contain exit command error",
            errorOutput.contains("Command file must end with 'exit' command"));
  }

  /**
   * Tests headless mode behavior with empty command file.
   */
  @Test
  public void testHeadlessModeEmptyFile() {
    File emptyFile = createTestCommandFile("empty.txt", "");
    assertNotNull("Empty file should be created", emptyFile);
    assertTrue("Empty file should exist", emptyFile.exists());
    assertEquals("File should be empty", 0, emptyFile.length());

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    try {
      view.run(emptyFile.getAbsolutePath());
      fail("Should throw exception for missing exit command");
    } catch (RuntimeException e) {
      assertEquals("Command file must end with 'exit' command", e.getMessage());
    }

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Should contain exit command error for empty file",
            errorOutput.contains("Command file must end with 'exit' command"));

    String output = outputStream.toString();
    assertTrue("Should contain headless mode startup message",
            output.contains("Headless Mode"));
  }

  /**
   * Tests headless mode behavior with command file containing only comments and whitespace.
   */
  @Test
  public void testHeadlessModeOnlyCommentsAndWhitespace() {
    String commands =
            "# This is a comment\n" +
                    "\n" +
                    "# Another comment\n" +
                    "\t\n" +
                    "# Final comment\n";

    File commentFile = createTestCommandFile("only_comments.txt", commands);
    assertNotNull("Comment file should be created", commentFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    try {
      view.run(commentFile.getAbsolutePath());
      fail("Should throw exception for missing exit command");
    } catch (RuntimeException e) {
      assertEquals("Command file must end with 'exit' command", e.getMessage());
    }

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Should contain exit command error",
            errorOutput.contains("Command file must end with 'exit' command"));

    String output = outputStream.toString();
    assertFalse("Should not contain command execution traces",
            output.contains("Currently executing:"));
  }

  /**
   * Tests error handling during command execution in headless mode with recovery continuation.
   */
  @Test
  public void testHeadlessErrorHandlingAndRecovery() {
    String commands =
            "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event Good Event from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
                    "invalid command that should fail\n" +
                    "create event from invalid-date to 2025-06-15T11:00\n" +
                    "print events on 2025-06-15\n" +
                    "show status on invalid-datetime\n" +
                    "show status on 2025-06-15T10:30\n" +
                    "exit\n";

    File commandFile = createTestCommandFile("error_recovery_test.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);
    assertTrue("Should contain successful event creation",
            output.contains("Created event"));
    assertTrue("Should complete with exit", output.contains("Exit command found"));

    String errorOutput = errorStream.toString();
    assertNotNull("Error output should not be null", errorOutput);
    assertTrue("Should contain line number references",
            errorOutput.contains("Line 4:") || errorOutput.contains("Line 5:") ||
                    errorOutput.contains("Line 7:"));

    String[] outputLines = output.split("\n");
    int executionLines = 0;
    for (String line : outputLines) {
      if (line.contains("Currently executing:")) {
        executionLines++;
      }
    }
    assertTrue("Should attempt to execute multiple commands despite errors",
            executionLines >= 5);
  }

  /**
   * Tests complete end-to-end workflow of headless mode with complex command sequences.
   */
  @Test
  public void testCompleteHeadlessWorkflow() {
    String commands =
            "create calendar --name default --timezone America/New_York\n" +
                    "use calendar --name default\n" +
                    "create event \"Morning Standup\" " +
                    "from 2025-06-16T09:00 to 2025-06-16T09:30\n" +
                    "create event Lunch from 2025-06-16T12:00 to 2025-06-16T13:00\n" +
                    "create event \"Team Meeting\" from 2025-06-16T14:00 to 2025-06-16T15:00\n" +
                    "create event \"Daily Workout\" from 2025-06-16T07:00 to 2025-06-16T08:00 " +
                    "repeats MTWRF for 3 times\n" +
                    "create event \"Weekend Fun\" on 2025-06-14 repeats SU for 2 times\n" +
                    "print events on 2025-06-16\n" +
                    "print events from 2025-06-14T00:00 to 2025-06-20T23:59\n" +
                    "show status on 2025-06-16T09:15\n" +
                    "show status on 2025-06-16T10:00\n" +
                    "show status on 2025-06-16T12:30\n" +
                    "edit event subject Lunch from 2025-06-16T12:00 to " +
                    "2025-06-16T13:00 with \"Business Lunch\"\n" +
                    "edit event location \"Team Meeting\" from 2025-06-16T14:00 to " +
                    "2025-06-16T15:00 with PHYSICAL\n" +
                    "print events on 2025-06-16\n" +
                    "exit\n";

    File workflowFile = createTestCommandFile("complete_workflow.txt", commands);
    assertNotNull("Workflow file should be created", workflowFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(workflowFile.getAbsolutePath());

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);

    assertTrue("Should show headless mode startup", output.contains("Headless Mode"));
    assertTrue("Should show file being read", output.contains(workflowFile.getName()));
    assertTrue("Should create Morning Standup", output.contains("Morning Standup"));
    assertTrue("Should create recurring workout", output.contains("Daily Workout"));
    assertTrue("Should create weekend events", output.contains("Weekend Fun"));
    assertTrue("Should list events for specific day", output.contains("Team Meeting"));
    assertTrue("Should show busy/available status",
            output.contains("busy") || output.contains("available"));
    assertTrue("Should show edit confirmations", output.contains("Updated event"));
    assertTrue("Should show Business Lunch after edit", output.contains("Business Lunch"));
    assertTrue("Should exit properly", output.contains("Exit command found"));
  }

  /**
   * Tests argument validation for interactive mode with various cases.
   */
  @Test
  public void testInteractiveModeArgumentValidation() {
    String[] validArgs = {"--mode", "interactive"};
    assertTrue("Valid interactive arguments should be accepted",
            validateInteractiveArgs(validArgs));

    String[] caseArgs = {"--mode", "INTERACTIVE"};
    assertTrue("Case insensitive arguments should be accepted",
            validateInteractiveArgs(caseArgs));

    String[] invalidArgs = {"--mode", "invalid"};
    assertFalse("Invalid mode should be rejected",
            validateInteractiveArgs(invalidArgs));

    String[] incompleteArgs = {"--mode"};
    assertFalse("Incomplete arguments should be rejected",
            validateInteractiveArgs(incompleteArgs));
  }

  /**
   * Tests argument validation for headless mode with file parameter validation.
   */
  @Test
  public void testHeadlessModeArgumentValidation() {
    File commandFile = createTestCommandFile("test_commands.txt",
            "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
                    "print events on 2025-06-15\n" +
                    "exit\n");

    String[] args = {"--mode", "headless", commandFile.getAbsolutePath()};
    assertTrue("Valid headless arguments should be accepted",
            validateHeadlessArgs(args));

    String[] caseCombinations = {"--mode", "HEADLESS", commandFile.getAbsolutePath()};
    assertTrue("Case variations should be accepted",
            validateHeadlessArgs(caseCombinations));

    String[] missingFile = {"--mode", "headless"};
    assertFalse("Missing file should be rejected",
            validateHeadlessArgs(missingFile));

    String[] nonExistentFile = {"--mode", "headless", "/nonexistent/file.txt"};
    assertFalse("Non-existent file should be rejected",
            validateHeadlessArgs(nonExistentFile));
  }

  /**
   * Tests rapid command execution with reasonable load for performance validation.
   */
  @Test
  public void testRapidCommandExecution() {
    StringBuilder commandBuilder = new StringBuilder();

    commandBuilder.append("create calendar --name default --timezone America/New_York\n");
    commandBuilder.append("use calendar --name default\n");

    for (int i = 1; i <= 5; i++) {
      int startHour = (i % 12) + 1;
      int endHour = (startHour % 12) + 1;
      commandBuilder.append(String.format(
              "create event \"Event %d\" from 2025-06-15T%02d:00 to 2025-06-15T%02d:00\n",
              i, startHour, endHour
      ));
    }

    commandBuilder.append("print events on 2025-06-15\n");
    commandBuilder.append("show status on 2025-06-15T01:15\n");
    commandBuilder.append("exit\n");

    File stressFile = createTestCommandFile("stress_test.txt", commandBuilder.toString());
    assertNotNull("Stress test file should be created", stressFile);

    long startTime = System.currentTimeMillis();

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(stressFile.getAbsolutePath());

    long endTime = System.currentTimeMillis();
    long executionTime = endTime - startTime;

    String output = outputStream.toString();
    assertTrue("Should complete stress test", output.contains("Exit command found"));
    assertTrue("Execution should complete in reasonable time",
            executionTime < 3000);
    assertTrue("Should attempt to create events",
            output.contains("Currently executing: " +
            "create event"));
  }

  private File createTestCommandFile(String filename, String content) {
    try {
      File file = new File(tempDirectory, filename);
      FileWriter writer = new FileWriter(file);
      writer.write(content);
      writer.close();
      return file;
    } catch (IOException e) {
      fail("Failed to create test command file: " + e.getMessage());
      return null;
    }
  }

  private boolean validateInteractiveArgs(String[] args) {
    if (args == null || args.length < 2) {
      return false;
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      return false;
    }

    return args[1].equalsIgnoreCase("interactive");
  }

  private boolean validateHeadlessArgs(String[] args) {
    if (args == null || args.length < 3) {
      return false;
    }

    if (!args[0].equalsIgnoreCase("--mode")) {
      return false;
    }

    if (!args[1].equalsIgnoreCase("headless")) {
      return false;
    }

    return new File(args[2]).exists();
  }

  /**
   * Tests headless mode with the provided valid commands test file.
   */
  @Test
  public void testHeadlessWithValidCommandsFile() {
    String commands = "# Valid Commands Test File\n" +
            "\n" +
            "create calendar --name default --timezone America/New_York\n" +
            "use calendar --name default\n" +
            "\n" +
            "# Create single events\n" +
            "create event \"Morning Meeting\" from 2025-06-15T09:00 to 2025-06-15T10:00\n" +
            "create event Lunch from 2025-06-15T12:00 to 2025-06-15T13:00\n" +
            "create event \"Team Standup\" from 2025-06-16T09:30 to 2025-06-16T10:00\n" +
            "\n" +
            "# Create all-day events\n" +
            "create event Holiday on 2025-06-20\n" +
            "create event \"Company Retreat\" on 2025-06-25\n" +
            "\n" +
            "# Create recurring events with time limits\n" +
            "create event \"Daily Workout\" from 2025-06-16T07:00 " +
            "to 2025-06-16T08:00 repeats MTWRF for 5 times\n" +
            "create event \"Weekly Review\" from 2025-06-16T15:00 " +
            "to 2025-06-16T16:00 repeats F for 3 times\n" +
            "\n" +
            "# Create recurring events with date limits\n" +
            "create event \"Weekend Fun\" on 2025-06-14 repeats SU for 4 times\n" +
            "create event \"Morning Jog\" from 2025-06-16T06:00 " +
            "to 2025-06-16T07:00 repeats MW until 2025-06-30\n" +
            "\n" +
            "# Query events\n" +
            "print events on 2025-06-15\n" +
            "print events on 2025-06-16\n" +
            "print events from 2025-06-14T00:00 to 2025-06-20T23:59\n" +
            "\n" +
            "# Check availability status\n" +
            "show status on 2025-06-15T09:30\n" +
            "show status on 2025-06-15T11:00\n" +
            "show status on 2025-06-16T07:30\n" +
            "show status on 2025-06-20T12:00\n" +
            "\n" +
            "# Edit single events\n" +
            "edit event subject Lunch from 2025-06-15T12:00 " +
            "to 2025-06-15T13:00 with \"Business Lunch\"\n" +
            "edit event location \"Morning Meeting\" " +
            "from 2025-06-15T09:00 to 2025-06-15T10:00 with PHYSICAL\n" +
            "edit event status \"Team Standup\" from 2025-06-16T09:30 " +
            "to 2025-06-16T10:00 with PRIVATE\n" +
            "\n" +
            "# Edit event series\n" +
            "edit series subject \"Daily Workout\" from 2025-06-16T07:00 " +
            "with \"Morning Exercise\"\n" +
            "edit events location \"Weekly Review\" from 2025-06-16T15:00 with PHYSICAL\n" +
            "\n" +
            "# Final queries to verify changes\n" +
            "print events on 2025-06-15\n" +
            "print events on 2025-06-16\n" +
            "show status on 2025-06-15T12:30\n" +
            "\n" +
            "exit\n";

    File commandFile = createTestCommandFile("test_commands.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    assertNotNull("Output should not be null", output);

    assertTrue("Should create Morning Meeting",
            output.contains("Created event: \"Morning Meeting\""));
    assertTrue("Should create Lunch event",
            output.contains("Created event: \"Lunch\""));
    assertTrue("Should create all-day Holiday",
            output.contains("Created all-day event: \"Holiday\""));
    assertTrue("Should create recurring Daily Workout",
            output.contains("Created recurring event series: \"Daily Workout\""));
    assertTrue("Should create recurring Weekend Fun",
            output.contains("Created recurring event series: \"Weekend Fun\""));

    assertTrue("Should print events or show no events message",
            output.contains("Morning Meeting") ||
                    output.contains("Business Lunch") || output.contains("No events on this day"));
    assertTrue("Should show busy status",
            output.contains("busy"));
    assertTrue("Should show status results", output.contains("busy")
            || output.contains("available"));

    assertTrue("Should update to Business Lunch",
            output.contains("Updated event: \"Lunch\""));
    assertTrue("Should update location",

            output.contains("Updated event: \"Morning Meeting\""));
    assertTrue("Should update series",
            output.contains("Updated entire series: \"Daily Workout\""));

    assertTrue("Should show Business Lunch in final query",
            output.contains("Business Lunch"));
    assertTrue("Should exit properly", output.contains("Exit command found"));

    String errorOutput = errorStream.toString();
    assertEquals("Should have no errors for valid commands", "", errorOutput);
  }

  /**
   * Tests headless mode with the provided invalid commands test file that includes error recovery.
   */
  @Test
  public void testHeadlessWithInvalidCommandsFile() {
    String commands = "# Invalid Commands Test File\n" +
            "\n" +
            "create calendar --name default --timezone America/New_York\n" +
            "use calendar --name default\n" +
            "\n" +
            "# Valid command\n" +
            "create event \"Good Event\" from 2025-06-15T10:00 to 2025-06-15T11:00\n" +
            "\n" +
            "# Invalid command - completely wrong syntax\n" +
            "this is not a valid command at all\n" +
            "\n" +
            "# Valid command\n" +
            "create event \"Another Good Event\" from 2025-06-15T14:00 to 2025-06-15T15:00\n" +
            "\n" +
            "# Invalid command - missing required fields\n" +
            "create event\n" +
            "\n" +
            "# Invalid command - invalid date format\n" +
            "create event \"Bad Date Event\" from invalid-date-format to 2025-06-15T12:00\n" +
            "\n" +
            "# Valid command\n" +
            "print events on 2025-06-15\n" +
            "\n" +
            "# Invalid command - invalid time format (25 hours)\n" +
            "create event \"Bad Time Event\" from 2025-06-15T25:00 to 2025-06-15T26:00\n" +
            "\n" +
            "# Invalid command - end time before start time\n" +
            "create event \"Backwards Event\" from 2025-06-15T15:00 to 2025-06-15T14:00\n" +
            "\n" +
            "# Valid command\n" +
            "show status on 2025-06-15T10:30\n" +
            "\n" +
            "# Invalid command - invalid repeat pattern\n" +
            "create event \"Bad Repeat\" from 2025-06-15T09:00 " +
            "to 2025-06-15T10:00 repeats XYZ for 3 times\n" +
            "\n" +
            "# Invalid command - negative repeat times\n" +
            "create event \"Negative Repeat\" from 2025-06-15T16:00 " +
            "to 2025-06-15T17:00 repeats M for -5 times\n" +
            "\n" +
            "# Valid command\n" +
            "create event \"Final Good Event\" from 2025-06-15T18:00 to 2025-06-15T19:00\n" +
            "\n" +
            "# Invalid command - invalid property for editing\n" +
            "edit event invalidproperty \"Good Event\" from 2025-06-15T10:00 " +
            "to 2025-06-15T11:00 with value\n" +
            "\n" +
            "# Invalid command - editing non-existent event\n" +
            "edit event subject \"Non-Existent Event\" from 2025-06-15T20:00 " +
            "to 2025-06-15T21:00 with \"New Name\"\n" +
            "\n" +
            "# Valid command\n" +
            "print events on 2025-06-15\n" +
            "\n" +
            "# Invalid command - invalid status query format\n" +
            "show status on invalid-datetime-format\n" +
            "\n" +
            "# Valid command - should still work despite previous errors\n" +
            "show status on 2025-06-15T18:30\n" +
            "\n" +
            "exit\n";

    File commandFile = createTestCommandFile("test_commands_invalid.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    view.run(commandFile.getAbsolutePath());

    String output = outputStream.toString();
    String errorOutput = errorStream.toString();

    assertNotNull("Output should not be null", output);
    assertNotNull("Error output should not be null", errorOutput);

    assertTrue("Should create Good Event",
            output.contains("Created event: \"Good Event\""));
    assertTrue("Should create Another Good Event",
            output.contains("Created event: \"Another Good Event\""));
    assertTrue("Should create Final Good Event",
            output.contains("Created event: \"Final Good Event\""));
    assertTrue("Should show events when printing", output.contains("Good Event"));
    assertTrue("Should show busy status", output.contains("busy"));
    assertTrue("Should complete with exit", output.contains("Exit command found"));

    assertTrue("Should report unknown command error",
            errorOutput.contains("Unknown command"));
    assertTrue("Should report create command error",
            errorOutput.contains("Create command error"));
    assertTrue("Should report invalid datetime error",
            errorOutput.contains("Invalid datetime"));
    assertTrue("Should report end time before start error",
            errorOutput.contains("End time must be after start"));
    assertTrue("Should report invalid repeat days error",
            errorOutput.contains("Invalid repeat days"));
    assertTrue("Should report negative repeat times error",
            errorOutput.contains("Repeat times must be positive"));
    assertTrue("Should report edit command error",
            errorOutput.contains("Unknown property"));
    assertTrue("Should report show status error",
            errorOutput.contains("Invalid datetime"));

    assertTrue("Error messages should include line numbers",
            errorOutput.contains("Line ") && (errorOutput.contains(":") ||
                    errorOutput.contains("error")));

    assertTrue("Should recover and execute final print command",
            output.contains("Currently executing: print events on 2025-06-15"));
    assertTrue("Should recover and execute final status check",
            output.contains("Currently executing: show status on 2025-06-15T18:30"));
  }

  /**
   * Tests headless mode with a command file missing the exit command.
   */
  @Test
  public void testHeadlessWithNoExitCommandFile() {
    String commands = "# No Exit Command Test File\n" +
            "\n" +
            "create calendar --name default --timezone America/New_York\n" +
            "use calendar --name default\n" +
            "\n" +
            "# Create some events\n" +
            "create event \"Meeting 1\" from 2025-06-15T09:00 to 2025-06-15T10:00\n" +
            "create event \"Meeting 2\" from 2025-06-15T11:00 to 2025-06-15T12:00\n" +
            "create event \"All Day Event\" on 2025-06-15\n" +
            "\n" +
            "# Create a recurring event\n" +
            "create event \"Daily Standup\" from 2025-06-16T09:00 " +
            "to 2025-06-16T09:30 repeats MTWRF for 5 times\n" +
            "\n" +
            "# Query the events\n" +
            "print events on 2025-06-15\n" +
            "print events on 2025-06-16\n" +
            "\n" +
            "# Check status\n" +
            "show status on 2025-06-15T09:30\n" +
            "show status on 2025-06-15T13:00\n" +
            "\n" +
            "# Edit an event\n" +
            "edit event subject \"Meeting 1\" from 2025-06-15T09:00 " +
            "to 2025-06-15T10:00 with \"Important Meeting\"\n" +
            "\n" +
            "# Final query\n" +
            "print events on 2025-06-15\n";

    File commandFile = createTestCommandFile("test_commands_noExit.txt", commands);
    assertNotNull("Command file should be created", commandFile);

    CalendarController controller = new CalendarController();
    HeadlessView view = new HeadlessView(controller);

    try {
      view.run(commandFile.getAbsolutePath());
      fail("Should throw exception for missing exit command");
    } catch (RuntimeException e) {
      assertEquals("Command file must end with 'exit' command", e.getMessage());
    }

    String output = outputStream.toString();
    String errorOutput = errorStream.toString();

    assertNotNull("Output should not be null", output);
    assertNotNull("Error output should not be null", errorOutput);

    assertTrue("Should create Meeting 1",
            output.contains("Created event: \"Meeting 1\""));
    assertTrue("Should create Meeting 2",
            output.contains("Created event: \"Meeting 2\""));
    assertTrue("Should create All Day Event",
            output.contains("Created all-day event: \"All Day Event\""));
    assertTrue("Should create recurring Daily Standup",
            output.contains("Created recurring event series: \"Daily Standup\""));
    assertTrue("Should execute print command and show events",
            output.contains("Meeting 1") || output.contains("Meeting 2") ||
                    output.contains("Important Meeting"));
    assertTrue("Should show status results", output.contains("busy") ||
            output.contains("available"));
    assertTrue("Should update event", output.contains("Updated event:"));

    assertTrue("Should report missing exit command",
            errorOutput.contains("Command file must end with 'exit' command"));

    assertFalse("Should not have exit confirmation",
            output.contains("Exit command found"));
  }
}