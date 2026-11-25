package gradetrack.logic;

import gradetrack.exception.InvalidInputException;
import gradetrack.exception.InvalidScoreException;
import gradetrack.exception.InvalidWeightException;

/**
 * Turns the text typed by the user into numbers and rejects bad values.
 * Every method throws a custom exception with a clear message that the
 * interface shows directly to the user.
 */
public final class InputValidator {

    private InputValidator() {
    }

    /** Course or score name: required text. */
    public static String requireName(String text, String label) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(label + " cannot be empty.");
        }
        return text.trim();
    }

    /** Credit hours: a whole number. */
    public static int parseCredits(String text) throws InvalidInputException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidInputException("Credit hours must be a whole number (for example 3).");
        }
    }

    /** A weight in percent. An empty box counts as 0. */
    public static double parseWeight(String text, String category) throws InvalidWeightException {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            double w = Double.parseDouble(text.trim());
            if (w < 0 || Double.isNaN(w) || Double.isInfinite(w)) {
                throw new InvalidWeightException("The " + category + " weight must be 0 or higher.");
            }
            return w;
        } catch (NumberFormatException e) {
            throw new InvalidWeightException("The " + category + " weight must be a number.");
        }
    }

    /**
     * A score. Blank means "not graded yet" and returns null.
     */
    public static Double parseScoreOrBlank(String text) throws InvalidScoreException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidScoreException("Score must be a number, or left blank if not graded yet.");
        }
    }

    /** The "Out of" value: required and greater than 0. */
    public static double parseTotal(String text) throws InvalidScoreException {
        try {
            double t = Double.parseDouble(text.trim());
            if (t <= 0 || Double.isNaN(t) || Double.isInfinite(t)) {
                throw new InvalidScoreException("'Out of' must be greater than 0.");
            }
            return t;
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidScoreException("'Out of' must be a number greater than 0.");
        }
    }

    /** Target grade: a number from 0 to 100. */
    public static double parseTarget(String text) throws InvalidScoreException {
        try {
            double t = Double.parseDouble(text.trim().replace("%", ""));
            if (t < 0 || t > 100 || Double.isNaN(t)) {
                throw new InvalidScoreException("Target grade must be between 0 and 100.");
            }
            return t;
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidScoreException("Target grade must be a number between 0 and 100.");
        }
    }
}
