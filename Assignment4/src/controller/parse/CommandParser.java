package controller.parse;

/**
 * Represents a parser that can process user input commands.
 * This interface defines the structure for all command parsers in the system.
 * Each parser should be able to:
 * Check if it can handle a specific input.
 * Parse the input and return the appropriate result.
 * Implementing classes should focus on specific types of commands and decide
 * if they can process the given input.
 */
public interface CommandParser {
  /**
   * Parses through the input.
   * @param input the input
   * @return the parsed result
   */
  ParseResult parse(String input);

  /**
   * Checks if the input can be handled.
   * @param input the input
   * @return true or false
   */
  boolean canHandle(String input);
}
