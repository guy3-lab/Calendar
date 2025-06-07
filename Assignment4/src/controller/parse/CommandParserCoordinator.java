package controller.parse;

import java.util.Arrays;
import java.util.List;

/**
 * Main parser that coordinates all specific parsers.
 */
public class CommandParserCoordinator {

  private final List<CommandParser> parsers;

  /**
   * Constructs the command parse coordinator.
   */
  public CommandParserCoordinator() {
    this.parsers = Arrays.asList(
            new CreateEventParser(),
            new EditEventParser(),
            new PrintEventsParser(),
            new ShowStatusParser()
    );
  }

  /**
   * Parse any command by delegating to appropriate parser.
   */
  public static ParseResult parseCommand(String input) {
    return new CommandParserCoordinator().parse(input);
  }

  private ParseResult parse(String input) {
    if (input == null || input.trim().isEmpty()) {
      return ParseResult.error("Input cannot be empty");
    }

    String trimmed = input.trim();

    if (trimmed.equalsIgnoreCase("exit")) {
      return ParseResult.exit();
    }

    // finds the appropriate parser for the command
    for (CommandParser parser : parsers) {
      if (parser.canHandle(trimmed)) {
        return parser.parse(trimmed); // calls the parsers parse method
      }
    }

    return ParseResult.error("Unknown command: " + trimmed);
  }
}