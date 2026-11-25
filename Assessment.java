package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/**
 * Abstract parent of every graded item (Quiz, Test, Examination, Laboratory).
 *
 * ABSTRACTION: the class cannot be instantiated; it only describes what every
 * assessment has (name, score, total points) and forces each child type to
 * state its own category through {@link #getType()}.
 *
 * ENCAPSULATION: all fields are private and reached only through getters/setters
 * that validate their input.
 */
public abstract class Assessment {

    private String name;
    private Double score;      // null means "not yet graded"
    private double total;      // the "Out of" value

    /**
     * @param name  label such as "Quiz 1"
     * @param score points earned, or null when the work is not graded yet
     * @param total maximum points ("Out of"), must be greater than 0
     */
    protected Assessment(String name, Double score, double total) throws InvalidScoreException {
        this.setName(name);
        this.setTotal(total);
        this.setScore(score);
    }

    /** Category name this assessment belongs to (each subclass overrides this). */
    public abstract String getType();

    // ---- getters and validated setters --------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidScoreException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidScoreException("Score name cannot be empty.");
        }
        this.name = name.trim();
    }

    public Double getScore() {
        return score;
    }

    /** Pass null to mark the work as not yet graded. */
    public void setScore(Double score) throws InvalidScoreException {
        if (score != null) {
            if (score.isNaN() || score.isInfinite()) {
                throw new InvalidScoreException("Score must be a valid number.");
            }
            if (score < 0) {
                throw new InvalidScoreException("Score cannot be negative.");
            }
            if (score > this.total) {
                throw new InvalidScoreException(
                        "Score (" + Course.format(score) + ") cannot be higher than 'Out of' ("
                        + Course.format(this.total) + ").");
            }
        }
        this.score = score;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) throws InvalidScoreException {
        if (Double.isNaN(total) || Double.isInfinite(total) || total <= 0) {
            throw new InvalidScoreException("'Out of' must be greater than 0.");
        }
        this.total = total;
    }

    public boolean isGraded() {
        return score != null;
    }

    /** Percent score of this item, or -1 when it is not graded. */
    public double getPercent() {
        return isGraded() ? (score / total) * 100.0 : -1;
    }

    /**
     * Text shown in the scores table. Subclasses may override it
     * (polymorphism) to add their own wording.
     */
    public String describe() {
        String shown = isGraded() ? Course.format(score) + " / " + Course.format(total)
                                  : "not graded / " + Course.format(total);
        return name + " (" + getType() + "): " + shown;
    }

    @Override
    public String toString() {
        return describe();
    }
}
