package gradetrack.logic;

import gradetrack.model.Course;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Computes the GPA weighted by credit hours.
 * Uses the 1.00 (highest) to 5.00 (failed) scale used in the Philippines.
 * All members are static, so no object of this class is needed.
 */
public final class GPACalculator {

    private GPACalculator() {
        // utility class: not meant to be instantiated
    }

    /** Percent -> grade point (1.00 highest, 3.00 lowest passing, 5.00 failed). */
    public static double toGradePoint(double percent) {
        if (percent >= 97) return 1.00;
        if (percent >= 94) return 1.25;
        if (percent >= 91) return 1.50;
        if (percent >= 88) return 1.75;
        if (percent >= 85) return 2.00;
        if (percent >= 82) return 2.25;
        if (percent >= 79) return 2.50;
        if (percent >= 76) return 2.75;
        if (percent >= 75) return 3.00;
        return 5.00;
    }

    /**
     * GPA of all courses that already have graded work, weighted by credits.
     * Courses with no graded work are skipped.
     *
     * @return the GPA rounded to 2 decimals, or -1 if no course has graded work
     */
    public static double computeGPA(List<Course> courses, GradeCalculator calculator) {
        double points = 0;
        int credits = 0;
        for (Course c : courses) {
            double grade = calculator.currentGrade(c);
            if (grade >= 0) {
                points += toGradePoint(grade) * c.getCredits();
                credits += c.getCredits();
            }
        }
        if (credits == 0) {
            return -1;
        }
        // BigDecimal avoids binary rounding surprises such as 2.225 -> 2.22
        return BigDecimal.valueOf(points / credits).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Total credits of the courses counted in the GPA. */
    public static int countedCredits(List<Course> courses, GradeCalculator calculator) {
        int credits = 0;
        for (Course c : courses) {
            if (calculator.hasGradedWork(c)) {
                credits += c.getCredits();
            }
        }
        return credits;
    }
}
