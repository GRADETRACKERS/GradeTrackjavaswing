package gradetrack.exception;

/** Thrown when category weights are negative or do not add up to 100%. */
public class InvalidWeightException extends InvalidInputException {

    public InvalidWeightException(String message) {
        super(message);
    }
}
