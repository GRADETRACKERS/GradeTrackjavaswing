package gradetrack.logic;

import gradetrack.exception.InvalidScoreException;
import gradetrack.model.Course;

/**
 * Works out the average a student still needs on the remaining (ungraded)
 * work in order to reach a target final grade.
 */
public class TargetGradePlanner {

    private final GradeCalculator calculator;

    public TargetGradePlanner(GradeCalculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Average (percent) needed on the remaining work.
     * Returns a negative number if the target is already secured and a value
     * above 100 if it can no longer be reached.
     *
     * @throws InvalidScoreException if the target is not between 0 and 100
     */
    public double requiredAverage(Course course, double target) throws InvalidScoreException {
        validateTarget(target);
        double remaining = 100.0 - calculator.percentGraded(course);
        if (remaining < 0.0001) {
            throw new IllegalStateException("Nothing remains to be graded.");
        }
        return (target - calculator.earnedWeight(course)) / remaining * 100.0;
    }

    /** Builds the sentence shown to the user on the Target grade tab. */
    public String advise(Course course, double target) throws InvalidScoreException {
        validateTarget(target);
        double remaining = 100.0 - calculator.percentGraded(course);
        double earned = calculator.earnedWeight(course);

        if (remaining < 0.0001) {
            return String.format("Everything is graded. Your final grade is %.1f%%.", earned);
        }

        double needed = requiredAverage(course, target);
        String left = String.format("%.1f%%", remaining);
        if (needed <= 0) {
            return String.format("You have already secured %s. Even a 0%% on the remaining %s "
                    + "of the course keeps you at or above your target.",
                    Course.format(target) + "%", left);
        }
        if (needed > 100) {
            return String.format("A target of %s is no longer reachable. You would need an average "
                    + "of %.1f%% on the remaining %s of the course, which is above 100%%.",
                    Course.format(target) + "%", needed, left);
        }
        return String.format("You need an average of %.1f%% on the remaining %s of the course "
                + "to reach %s.", needed, left, Course.format(target) + "%");
    }

    private void validateTarget(double target) throws InvalidScoreException {
        if (Double.isNaN(target) || target < 0 || target > 100) {
            throw new InvalidScoreException("Target grade must be between 0 and 100.");
        }
    }
}
