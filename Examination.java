package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/** A major examination (midterm or final). */
public class Examination extends Assessment {

    public static final String TYPE = "Examination";

    public Examination(String name, Double score, double total) throws InvalidScoreException {
        super(name, score, total);
    }

    /** INHERITANCE + POLYMORPHISM: overrides the abstract method of Assessment. */
    @Override
    public String getType() {
        return TYPE;
    }

    /** Overrides describe() to mark examinations clearly in listings. */
    @Override
    public String describe() {
        return "[EXAM] " + super.describe();
    }
}
