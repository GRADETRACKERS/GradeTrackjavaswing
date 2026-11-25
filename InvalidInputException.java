package gradetrack.exception;

/**
 * Base class of all GradeTrack validation errors.
 * Extending Exception makes it a checked exception, so callers must
 * either catch it (try-catch) or declare it (throws).
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}
