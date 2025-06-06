package controller.parse;

/**
 * The base interface for all types of command parsers.
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
