package gradetrack.model;

import gradetrack.exception.InvalidScoreException;

/**
 * A score whose type is one the student typed in ("Other..."): Project, Assignment,
 * Recitation and so on. Unlike Quiz or Test, its type name is stored in the object.
 */
public class Other extends Assessment {

    private final String type;

    public Other(String type, String name, Double score, double total) throws InvalidScoreException {
        super(name, score, total);
        this.type = type;
    }

    /** INHERITANCE + POLYMORPHISM: overrides the abstract method of Assessment. */
    @Override
    public String getType() {
        return type;
    }
}
