package gradetrack.exception;

/** Thrown when a course with the same name already exists. */
public class DuplicateCourseException extends InvalidInputException {

    public DuplicateCourseException(String courseName) {
        super("A course named \"" + courseName + "\" already exists.");
    }
}
