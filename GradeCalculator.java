package gradetrack.logic;

import gradetrack.model.Assessment;
import gradetrack.model.Course;

/**
 * Computes a course's current grade and how much of it is already graded.
 *
 * How it works: each category has a weight (e.g. Quiz 20%). Inside a category,
 * every item counts by its points, so a 100-point item matters more than a
 * 10-point item of the same category.
 *
 *   category score = points earned / points graded          (graded items only)
 *   current grade  = weighted average of the category scores that have graded work
 *
 * Work that is not yet graded therefore never lowers (or raises) the current grade.
 * For planning, the same data gives:
 *
 *   earned weight  = weight x (points earned / ALL points in the category)
 *   graded weight  = weight x (points graded / ALL points in the category)
 *
 * which tell how much of the course is finished (see percentGraded) and how many
 * course points are already banked (see earnedWeight, used by TargetGradePlanner).
 */
public class GradeCalculator {

    // ---- static utilities, with METHOD OVERLOADING ---------------------

    /** Percent of a raw score. */
    public static double toPercent(double score, double total) {
        return total <= 0 ? 0 : (score / total) * 100.0;
    }

    /** Overload: percent of a graded assessment (-1 if it is not graded yet). */
    public static double toPercent(Assessment a) {
        return a.isGraded() ? toPercent(a.getScore(), a.getTotal()) : -1;
    }

    // ---- instance methods ---------------------------------------------

    /** Weighted points earned so far, out of 100. */
    public double earnedWeight(Course course) {
        double earned = 0;
        for (String category : course.getCategories()) {
            double total = totalPoints(course, category);
            if (total > 0) {
                earned += course.getWeight(category) * (gradedEarned(course, category) / total);
            }
        }
        return earned;
    }

    /** Percentage (0-100) of the whole course that has been graded. */
    public double percentGraded(Course course) {
        double graded = 0;
        for (String category : course.getCategories()) {
            double total = totalPoints(course, category);
            if (total > 0) {
                graded += course.getWeight(category) * (gradedPoints(course, category) / total);
            }
        }
        return graded;
    }

    /** True when at least one score has been entered for the course. */
    public boolean hasGradedWork(Course course) {
        return percentGraded(course) > 0.0000001;
    }

    /**
     * Current grade in percent based only on graded work,
     * or -1 when nothing is graded yet.
     */
    public double currentGrade(Course course) {
        double sum = 0;
        double weightUsed = 0;
        for (String category : course.getCategories()) {
            double avg = categoryGrade(course, category);   // -1 if nothing graded in it
            if (avg >= 0) {
                sum += course.getWeight(category) * avg;
                weightUsed += course.getWeight(category);
            }
        }
        return weightUsed <= 0.0000001 ? -1 : sum / weightUsed;
    }

    /** Percent score of one category from its graded items, or -1 if none is graded. */
    public double categoryGrade(Course course, String category) {
        double graded = gradedPoints(course, category);
        return graded <= 0 ? -1 : toPercent(gradedEarned(course, category), graded);
    }

    /** Same as {@link #currentGrade(Course)} but formatted for display ("–" if none). */
    public String currentGradeText(Course course) {
        double g = currentGrade(course);
        return g < 0 ? "–" : String.format("%.1f%%", g);
    }

    // ---- private helpers ----------------------------------------------

    private double totalPoints(Course course, String category) {
        double sum = 0;
        for (Assessment a : course.getAssessmentsOfType(category)) {
            sum += a.getTotal();
        }
        return sum;
    }

    private double gradedPoints(Course course, String category) {
        double sum = 0;
        for (Assessment a : course.getAssessmentsOfType(category)) {
            if (a.isGraded()) {
                sum += a.getTotal();
            }
        }
        return sum;
    }

    private double gradedEarned(Course course, String category) {
        double sum = 0;
        for (Assessment a : course.getAssessmentsOfType(category)) {
            if (a.isGraded()) {
                sum += a.getScore();
            }
        }
        return sum;
    }
}
