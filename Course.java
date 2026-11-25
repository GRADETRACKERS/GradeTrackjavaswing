package gradetrack.model;

import gradetrack.exception.DuplicateTypeException;
import gradetrack.exception.InvalidInputException;
import gradetrack.exception.InvalidWeightException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A course: its name, credit hours, score types with their weights (always
 * totalling 100%) and the list of scores entered for it.
 *
 * Every course starts with four types (Quiz, Test, Examination, Laboratory).
 * The student can add more with "Other..." (see {@link #addCategory}).
 *
 * ENCAPSULATION: every field is private; outside code can only use the
 * public methods, which validate their input.
 */
public class Course {

    /** The types every new course starts with (static member shared by all courses). */
    public static final String[] DEFAULT_CATEGORIES = {
            Quiz.TYPE, Test.TYPE, Examination.TYPE, Laboratory.TYPE
    };

    public static final int MIN_CREDITS = 1;
    public static final int MAX_CREDITS = 12;
    public static final int MAX_TYPE_LENGTH = 20;

    private String name;
    private int credits;
    private final List<String> categories = new ArrayList<>();
    private final Map<String, Double> weights = new LinkedHashMap<>();
    private final List<Assessment> assessments = new ArrayList<>();

    /**
     * Full constructor.
     *
     * @param weights percent weight per type, must add up to 100. Names that are not
     *                one of the four default types are added to the course as custom types.
     */
    public Course(String name, int credits, Map<String, Double> weights) throws InvalidInputException {
        this.setName(name);
        this.setCredits(credits);
        for (String c : DEFAULT_CATEGORIES) {
            categories.add(c);
        }
        if (weights != null) {
            for (String key : weights.keySet()) {
                if (findCategory(key) == null) {
                    registerCategory(key);
                }
            }
        }
        this.setWeights(weights);
    }

    /** Convenience constructor that splits the weights equally (25% each). */
    public Course(String name, int credits) throws InvalidInputException {
        this(name, credits, equalWeights());
    }

    // ------------------------------------------------------------------
    // Getters / validated setters
    // ------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Course name cannot be empty.");
        }
        if (name.trim().length() > 40) {
            throw new InvalidInputException("Course name must be 40 characters or fewer.");
        }
        this.name = name.trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) throws InvalidInputException {
        if (credits < MIN_CREDITS || credits > MAX_CREDITS) {
            throw new InvalidInputException(
                    "Credit hours must be between " + MIN_CREDITS + " and " + MAX_CREDITS + ".");
        }
        this.credits = credits;
    }

    // ------------------------------------------------------------------
    // Types (categories) and weights
    // ------------------------------------------------------------------

    /** The type names of this course, in display order (read-only). */
    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /** Returns the stored spelling of a type (ignoring upper/lower case), or null if absent. */
    public String findCategory(String type) {
        if (type == null) {
            return null;
        }
        for (String c : categories) {
            if (c.equalsIgnoreCase(type.trim())) {
                return c;
            }
        }
        return null;
    }

    /** Returns a copy, so outside code cannot change the weights without validation. */
    public Map<String, Double> getWeights() {
        return new LinkedHashMap<>(weights);
    }

    public double getWeight(String category) {
        Double w = weights.get(findCategory(category));
        return w == null ? 0 : w;
    }

    /**
     * Adds a student-defined type (the "Other..." option) and gives it a weight.
     * The existing weights are scaled down in proportion so the total stays 100%.
     *
     * @return the name as stored (trimmed)
     * @throws DuplicateTypeException if the course already has this type
     */
    public String addCategory(String type, double weight) throws InvalidInputException {
        if (weight <= 0 || weight >= 100 || Double.isNaN(weight)) {
            throw new InvalidWeightException("The weight of a new type must be above 0 and below 100.");
        }
        String stored = registerCategory(type);   // validates name, rejects duplicates
        double factor = (100.0 - weight) / 100.0;
        for (String c : categories) {
            if (!c.equals(stored)) {
                weights.put(c, weights.get(c) * factor);
            }
        }
        weights.put(stored, weight);
        return stored;
    }

    /** Validates a type name and adds it to the list (weight 0 until set). */
    private String registerCategory(String type) throws InvalidInputException {
        if (type == null || type.trim().isEmpty()) {
            throw new InvalidInputException("Type name cannot be empty.");
        }
        String t = type.trim();
        if (t.length() > MAX_TYPE_LENGTH) {
            throw new InvalidInputException("Type name must be " + MAX_TYPE_LENGTH + " characters or fewer.");
        }
        if (t.indexOf(':') >= 0 || t.indexOf('\t') >= 0 || t.indexOf(';') >= 0) {
            throw new InvalidInputException("Type name cannot contain the characters : ; or tab.");
        }
        if (findCategory(t) != null) {
            throw new DuplicateTypeException(findCategory(t));
        }
        categories.add(t);
        weights.put(t, 0.0);
        return t;
    }

    /** Replaces all weights. Every type of this course must be given and the total must be 100. */
    public void setWeights(Map<String, Double> newWeights) throws InvalidWeightException {
        if (newWeights == null) {
            throw new InvalidWeightException("Type weights are required.");
        }
        double sum = 0;
        Map<String, Double> checked = new LinkedHashMap<>();
        for (String category : categories) {
            Double w = null;
            for (Map.Entry<String, Double> e : newWeights.entrySet()) {
                if (e.getKey().equalsIgnoreCase(category)) {
                    w = e.getValue();
                }
            }
            if (w == null) {
                w = 0.0;
            }
            if (w.isNaN() || w.isInfinite() || w < 0) {
                throw new InvalidWeightException("The " + category + " weight must be 0 or higher.");
            }
            checked.put(category, w);
            sum += w;
        }
        if (Math.abs(sum - 100.0) > 0.001) {
            throw new InvalidWeightException(
                    "Weights must add up to 100% (currently " + format(sum) + "%).");
        }
        this.weights.clear();
        this.weights.putAll(checked);
    }

    // ------------------------------------------------------------------
    // Scores
    // ------------------------------------------------------------------

    /** Read-only view of the scores in the order they were added. */
    public List<Assessment> getAssessments() {
        return Collections.unmodifiableList(assessments);
    }

    public void addAssessment(Assessment a) {
        assessments.add(a);
    }

    public boolean removeAssessment(Assessment a) {
        return assessments.remove(a);
    }

    /** All scores that belong to one type (polymorphism: uses getType()). */
    public List<Assessment> getAssessmentsOfType(String category) {
        List<Assessment> result = new ArrayList<>();
        for (Assessment a : assessments) {
            if (a.getType().equalsIgnoreCase(category)) {
                result.add(a);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Map<String, Double> equalWeights() {
        Map<String, Double> m = new LinkedHashMap<>();
        for (String c : DEFAULT_CATEGORIES) {
            m.put(c, 100.0 / DEFAULT_CATEGORIES.length);
        }
        return m;
    }

    /** Formats a number without useless trailing zeros (85.0 -> "85", 85.75 -> "85.75"). */
    public static String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        String s = String.format("%.2f", value);
        if (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @Override
    public String toString() {
        return name;   // lets a JComboBox show the course name directly
    }
}
