package controller;

/**
 * The base interface for all types of command parsers.
 */
public interface CommandParser {
  ParseResult parse(String input);
  boolean canHandle(String input);
}
