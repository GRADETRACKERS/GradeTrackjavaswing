package gradetrack.exception;

/** Thrown when a score or "out of" value is not a valid number or is out of range. */
public class InvalidScoreException extends InvalidInputException {

    public InvalidScoreException(String message) {
        super(message);
    }
}
