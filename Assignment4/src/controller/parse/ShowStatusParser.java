package controller.parse;

import java.time.LocalDateTime;

/**
 * Class that handles the commands that asks to show status.
 */
public class ShowStatusParser implements CommandParser {
  @Override
  public boolean canHandle(String input) {
    return input.toLowerCase().startsWith("show status ");
  }

  @Override
  public ParseResult parse(String input) {
    try {
      LocalDateTime dateTime = ParsingTools.extractDateTime(input, " on ", null);
      return ParseResult.showStatus(dateTime);
    } catch (Exception e) {
      return ParseResult.error("Show status error: " + e.getMessage());
    }
  }
}
