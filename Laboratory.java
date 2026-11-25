package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/** A laboratory activity or laboratory exercise. */
public class Laboratory extends Assessment {

    public static final String TYPE = "Laboratory";

    public Laboratory(String name, Double score, double total) throws InvalidScoreException {
        super(name, score, total);
    }

    /** INHERITANCE + POLYMORPHISM: overrides the abstract method of Assessment. */
    @Override
    public String getType() {
        return TYPE;
    }

}
