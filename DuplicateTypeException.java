package gradetrack.exception;

/** Thrown when a course already has a score type with the same name. */
public class DuplicateTypeException extends InvalidInputException {

    public DuplicateTypeException(String typeName) {
        super("You already have the type \"" + typeName + "\" in this course.");
    }
}
