package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/** A periodic written test. */
public class Test extends Assessment {

    public static final String TYPE = "Test";

    public Test(String name, Double score, double total) throws InvalidScoreException {
        super(name, score, total);
    }

    /** INHERITANCE + POLYMORPHISM: overrides the abstract method of Assessment. */
    @Override
    public String getType() {
        return TYPE;
    }

}
