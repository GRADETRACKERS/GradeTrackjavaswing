package gradetrack.logic;

import gradetrack.exception.DuplicateCourseException;
import gradetrack.exception.InvalidInputException;
import gradetrack.exception.InvalidScoreException;
import gradetrack.model.Assessment;
import gradetrack.model.Course;
import gradetrack.model.Examination;
import gradetrack.model.Laboratory;
import gradetrack.model.Other;
import gradetrack.model.Quiz;
import gradetrack.model.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps every course and handles adding/deleting courses and
 * adding, updating, deleting, searching and sorting scores.
 *
 * Collections used:
 *  - ArrayList keeps the courses in the order they were added.
 *  - HashMap gives fast lookup of a course by (lower-case) name.
 */
public class CourseManager {

    /** The sort choices offered on the Courses tab. */
    public enum SortOption {
        ORDER_ADDED("Order added"),
        NAME("Name (A-Z)"),
        TYPE("Type"),
        SCORE_HIGH("Score (high to low)"),
        SCORE_LOW("Score (low to high)");

        private final String label;

        SortOption(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final List<Course> courses = new ArrayList<>();
    private final Map<String, Course> byName = new HashMap<>();

    // ------------------------------------------------------------------
    // Courses
    // ------------------------------------------------------------------

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public Course findCourse(String name) {
        return name == null ? null : byName.get(name.trim().toLowerCase(Locale.ROOT));
    }

    /** @throws DuplicateCourseException if a course with the same name exists */
    public void addCourse(Course course) throws DuplicateCourseException {
        String key = course.getName().toLowerCase(Locale.ROOT);
        if (byName.containsKey(key)) {
            throw new DuplicateCourseException(course.getName());
        }
        courses.add(course);
        byName.put(key, course);
    }

    public boolean deleteCourse(Course course) {
        byName.remove(course.getName().toLowerCase(Locale.ROOT));
        return courses.remove(course);
    }

    public void clear() {
        courses.clear();
        byName.clear();
    }

    // ------------------------------------------------------------------
    // Scores
    // ------------------------------------------------------------------

    /**
     * Factory: builds the right Assessment subclass from the type name.
     * The caller only sees "Assessment" (polymorphism).
     */
    public static Assessment createAssessment(String type, String name, Double score, double total)
            throws InvalidScoreException {
        if (Quiz.TYPE.equalsIgnoreCase(type)) {
            return new Quiz(name, score, total);
        } else if (Test.TYPE.equalsIgnoreCase(type)) {
            return new Test(name, score, total);
        } else if (Examination.TYPE.equalsIgnoreCase(type)) {
            return new Examination(name, score, total);
        } else if (Laboratory.TYPE.equalsIgnoreCase(type)) {
            return new Laboratory(name, score, total);
        }
        // any other name is a student-defined type ("Other...")
        return new Other(type, name, score, total);
    }

    /** Creates a score and adds it to the course. A null score means not graded yet. */
    public Assessment addScore(Course course, String type, String name, Double score, double total)
            throws InvalidInputException {
        if (course == null) {
            throw new InvalidInputException("Select or create a course first.");
        }
        String storedType = course.findCategory(type);
        if (storedType == null) {
            throw new InvalidScoreException("This course has no type \"" + type
                    + "\". Choose \"Other...\" to add it first.");
        }
        Assessment a = createAssessment(storedType, name, score, total);
        course.addAssessment(a);
        return a;
    }

    public boolean deleteScore(Course course, Assessment a) {
        return course.removeAssessment(a);
    }

    /**
     * Searches by name or type (case-insensitive, partial match).
     * An empty search returns every score.
     */
    public List<Assessment> search(Course course, String text) {
        List<Assessment> result = new ArrayList<>();
        String q = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        for (Assessment a : course.getAssessments()) {
            if (q.isEmpty()
                    || a.getName().toLowerCase(Locale.ROOT).contains(q)
                    || a.getType().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(a);
            }
        }
        return result;
    }

    /** Returns a sorted copy; the course's own order (order added) is never changed. */
    public List<Assessment> sort(List<Assessment> list, SortOption option) {
        List<Assessment> sorted = new ArrayList<>(list);
        Comparator<Assessment> byNameCmp = Comparator.comparing(
                a -> a.getName().toLowerCase(Locale.ROOT));
        switch (option) {
            case NAME:
                sorted.sort(byNameCmp);
                break;
            case TYPE:
                sorted.sort(Comparator.comparing(Assessment::getType).thenComparing(byNameCmp));
                break;
            case SCORE_HIGH:
                // ungraded work (percent = -1) naturally ends up last
                sorted.sort(Comparator.comparingDouble(Assessment::getPercent).reversed());
                break;
            case SCORE_LOW:
                // ungraded work is placed last here as well
                sorted.sort(Comparator.comparingDouble(
                        (Assessment a) -> a.isGraded() ? a.getPercent() : Double.MAX_VALUE));
                break;
            case ORDER_ADDED:
            default:
                break;   // keep original order
        }
        return sorted;
    }

    /** Search, then sort: what the Scores table shows. */
    public List<Assessment> view(Course course, String searchText, SortOption option) {
        return sort(search(course, searchText), option);
    }
}
