package view;

/**
 * View interface used in displaying input and results of the calendar application.
 */
public interface IView {
  /**
   * Runs the view with default behavior.
   */
  void run();

  /**
   * Runs the view with a parameter (a command file).
   */
  void run(String parameter);

  /**
   * Displays output to the user.
   */
  void displayOutput(String output);

  /**
   * Displays error messages to the user.
   */
  void displayError(String error);
}