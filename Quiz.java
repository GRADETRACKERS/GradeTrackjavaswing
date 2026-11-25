package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/** A short quiz. Usually the smallest and most frequent graded item. */
public class Quiz extends Assessment {

    public static final String TYPE = "Quiz";

    public Quiz(String name, Double score, double total) throws InvalidScoreException {
        super(name, score, total);
    }

    /** INHERITANCE + POLYMORPHISM: overrides the abstract method of Assessment. */
    @Override
    public String getType() {
        return TYPE;
    }

}
