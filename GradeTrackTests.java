package gradetrack;

import gradetrack.exception.DuplicateCourseException;
import gradetrack.exception.InvalidInputException;
import gradetrack.exception.InvalidScoreException;
import gradetrack.exception.InvalidWeightException;
import gradetrack.logic.CourseManager;
import gradetrack.logic.CourseManager.SortOption;
import gradetrack.logic.GPACalculator;
import gradetrack.logic.GradeCalculator;
import gradetrack.logic.InputValidator;
import gradetrack.logic.TargetGradePlanner;
import gradetrack.model.Assessment;
import gradetrack.model.Course;
import gradetrack.storage.StorageManager;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Plain-Java checks (no libraries). Run: java -cp out:testout gradetrack.GradeTrackTests */
public class GradeTrackTests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String label, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + label);
        } else {
            failed++;
            System.out.println("FAIL  " + label);
        }
    }

    private static Map<String, Double> w(double q, double t, double e, double l) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("Quiz", q);
        m.put("Test", t);
        m.put("Examination", e);
        m.put("Laboratory", l);
        return m;
    }

    public static void main(String[] args) throws Exception {
        GradeCalculator calc = new GradeCalculator();
        TargetGradePlanner planner = new TargetGradePlanner(calc);
        CourseManager mgr = new CourseManager();

        // ---- Documentation example data --------------------------------
        Course physics = new Course("Physics 101", 4, w(20, 20, 40, 20));
        mgr.addCourse(physics);
        mgr.addScore(physics, "Quiz", "Quiz 1", 78.0, 100);
        mgr.addScore(physics, "Test", "Test 1", 76.5, 100);
        mgr.addScore(physics, "Examination", "Midterm", 78.0, 100);
        mgr.addScore(physics, "Laboratory", "Lab 1", 78.0, 100);

        Course calculus = new Course("Calculus II", 3, w(20, 30, 30, 20));
        mgr.addCourse(calculus);
        mgr.addScore(calculus, "Quiz", "Quiz 1", 88.0, 100);
        mgr.addScore(calculus, "Test", "Test 1", 84.3, 100);

        Course intro = new Course("Intro to Programming", 3, w(20, 20, 40, 20));
        mgr.addCourse(intro);
        mgr.addScore(intro, "Quiz", "Quiz 1", 88.0, 100);
        mgr.addScore(intro, "Test", "Test 1", 88.0, 100);
        mgr.addScore(intro, "Examination", "Final", 88.0, 100);
        mgr.addScore(intro, "Laboratory", "Lab 1", 88.0, 100);

        Course biology = new Course("Biology", 3);
        mgr.addCourse(biology);

        // ---- Calculations ---------------------------------------------
        check("Physics current grade = 77.7%", calc.currentGradeText(physics).equals("77.7%"));
        check("Calculus II current grade = 85.8%", calc.currentGradeText(calculus).equals("85.8%"));
        check("Calculus II is 50% graded", Math.abs(calc.percentGraded(calculus) - 50) < 1e-9);
        check("Intro current grade = 88.0%", calc.currentGradeText(intro).equals("88.0%"));
        check("Biology shows dash (no graded work)", calc.currentGradeText(biology).equals("–"));
        check("GPA = 2.23 (documentation example)",
                GPACalculator.computeGPA(mgr.getCourses(), calc) == 2.23);
        check("Biology excluded from GPA credits", GPACalculator.countedCredits(mgr.getCourses(), calc) == 10);

        check("Target on fully graded course: documented message",
                planner.advise(physics, 85).equals("Everything is graded. Your final grade is 77.7%."));

        // Calculus: earned = 20*.88 + 30*.843 = 42.89 ; remaining 50
        double needed = planner.requiredAverage(calculus, 85);
        check("Calculus target 85 needs " + String.format("%.2f", needed) + "% on the rest (= (85-42.89)/50)",
                Math.abs(needed - ((85 - 42.89) / 50 * 100)) < 1e-6);
        check("Unreachable target is explained",
                planner.advise(calculus, 99).contains("no longer reachable"));
        check("Already-secured target is explained",
                planner.advise(calculus, 30).contains("already secured"));
        check("Target on empty course needs the target itself",
                Math.abs(planner.requiredAverage(biology, 90) - 90) < 1e-9);

        // ---- Scores: ungraded, search, sort ---------------------------
        mgr.addScore(calculus, "Quiz", "Quiz 2", null, 100);
        Course c = calculus;
        check("Ungraded Quiz 2 does not change current grade (still 85.8%)",
                calc.currentGradeText(c).equals("85.8%"));
        check("Ungraded Quiz 2 lowers percent graded (50% -> 40%) because the quiz category is half done",
                Math.abs(calc.percentGraded(c) - 40) < 1e-9);
        check("Target math still adds up after Quiz 2: needed = (85-34.09)/60",
                Math.abs(planner.requiredAverage(c, 85) - ((85 - 34.09) / 60 * 100)) < 1e-6);
        List<Assessment> found = mgr.search(c, "Quiz");
        check("Search \"Quiz\" lists Quiz 1 and Quiz 2",
                found.size() == 2 && found.get(0).getName().equals("Quiz 1") && found.get(1).getName().equals("Quiz 2"));
        check("Search by name is case-insensitive", mgr.search(c, "test 1").size() == 1);
        check("Search with no match is empty", mgr.search(c, "zzz").isEmpty());
        check("Default order = order added",
                mgr.view(c, "", SortOption.ORDER_ADDED).get(0).getName().equals("Quiz 1")
                        && mgr.view(c, "", SortOption.ORDER_ADDED).get(2).getName().equals("Quiz 2"));
        check("Sort score high-to-low puts ungraded last",
                mgr.view(c, "", SortOption.SCORE_HIGH).get(2).getName().equals("Quiz 2")
                        && mgr.view(c, "", SortOption.SCORE_HIGH).get(0).getName().equals("Quiz 1"));
        check("Sort score low-to-high puts ungraded last",
                mgr.view(c, "", SortOption.SCORE_LOW).get(0).getName().equals("Test 1")
                        && mgr.view(c, "", SortOption.SCORE_LOW).get(2).getName().equals("Quiz 2"));
        check("Sorting never changes the stored order",
                c.getAssessments().get(0).getName().equals("Quiz 1"));

        // ---- Polymorphism ---------------------------------------------
        check("Types come from subclasses",
                c.getAssessments().get(0).getType().equals("Quiz")
                        && c.getAssessments().get(1).getType().equals("Test"));
        check("Examination overrides describe()",
                mgr.view(intro, "Final", SortOption.NAME).get(0).describe().startsWith("[EXAM]"));

        // ---- Exceptions / validation ----------------------------------
        check("Score above total rejected", throwsScore(() -> mgr.addScore(physics, "Quiz", "X", 120.0, 100)));
        check("Negative score rejected", throwsScore(() -> mgr.addScore(physics, "Quiz", "X", -1.0, 100)));
        check("Zero total rejected", throwsScore(() -> mgr.addScore(physics, "Quiz", "X", 1.0, 0)));
        check("Empty score name rejected", throwsScore(() -> mgr.addScore(physics, "Quiz", "  ", 1.0, 10)));
        check("Letters in score rejected", throwsScore(() -> InputValidator.parseScoreOrBlank("abc")));
        check("Blank score is allowed (null)", InputValidator.parseScoreOrBlank("  ") == null);
        check("Bad target rejected", throwsScore(() -> InputValidator.parseTarget("150")));
        check("Weights not totalling 100 rejected", throwsWeight(() -> new Course("Bad", 3, w(10, 10, 10, 10))));
        check("Negative weight rejected", throwsWeight(() -> new Course("Bad", 3, w(-10, 40, 40, 30))));
        check("Non-numeric weight rejected", throwsWeight(() -> InputValidator.parseWeight("x", "Quiz")));
        check("Credits out of range rejected", throwsInput(() -> new Course("Bad", 0)));
        check("Non-numeric credits rejected", throwsInput(() -> InputValidator.parseCredits("three")));
        check("Empty course name rejected", throwsInput(() -> new Course("", 3)));
        boolean dup = false;
        try {
            mgr.addCourse(new Course("physics 101", 3));
        } catch (DuplicateCourseException e) {
            dup = true;
        }
        check("Duplicate course name rejected (case-insensitive)", dup);

        // ---- Storage round trip ---------------------------------------
        File tmp = File.createTempFile("gradetrack", ".txt");
        tmp.deleteOnExit();
        StorageManager store = new StorageManager(tmp);
        store.save(mgr);
        CourseManager loaded = new CourseManager();
        int skipped = store.load(loaded);
        check("Reload skips nothing", skipped == 0);
        check("Reload keeps 4 courses", loaded.getCourses().size() == 4);
        check("Reload keeps same GPA", GPACalculator.computeGPA(loaded.getCourses(), calc) == 2.23);
        Course lc = loaded.findCourse("calculus ii");
        check("Reload keeps ungraded score as ungraded",
                lc != null && lc.getAssessments().size() == 3 && !lc.getAssessments().get(2).isGraded());

        File missing = new File(tmp.getParentFile(), "does_not_exist_" + System.nanoTime() + ".txt");
        check("Missing file is a normal first run", new StorageManager(missing).load(new CourseManager()) == 0);

        // damaged line is skipped, the rest still loads
        java.nio.file.Files.write(tmp.toPath(),
                "COURSE\tA\t3\t25\t25\t25\t25\nSCORE\tQuiz\tQ\tabc\t10\ngarbage\nSCORE\tQuiz\tOK\t5\t10\n"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CourseManager damaged = new CourseManager();
        int sk = store.load(damaged);
        check("Damaged lines skipped (2) and good data kept",
                sk == 2 && damaged.getCourses().size() == 1 && damaged.getCourses().get(0).getAssessments().size() == 1);

        // ---- "Other..." custom types ------------------------------------
        Course se = new Course("Software Eng", 3, w(20, 20, 40, 20));
        mgr.addCourse(se);
        String added = se.addCategory("  Project ", 20);
        check("Custom type is added trimmed", added.equals("Project") && se.getCategories().size() == 5);
        check("Existing weights are scaled so the total stays 100",
                Math.abs(se.getWeight("Quiz") - 16) < 1e-9 && Math.abs(se.getWeight("Examination") - 32) < 1e-9
                        && Math.abs(se.getWeights().values().stream().mapToDouble(Double::doubleValue).sum() - 100) < 1e-9);
        boolean dupCustom = false;
        String dupMsg = "";
        try {
            se.addCategory("project", 10);
        } catch (gradetrack.exception.DuplicateTypeException e) {
            dupCustom = true;
            dupMsg = e.getMessage();
        }
        check("Typing a custom type that exists says you already have it (" + dupMsg + ")",
                dupCustom && dupMsg.contains("already have the type \"Project\""));
        check("Typing a built-in type name (Quiz) also says you already have it",
                throwsInput(() -> se.addCategory("QUIZ", 10)));
        check("Empty custom type rejected", throwsInput(() -> se.addCategory("   ", 10)));
        check("Custom type weight must be between 0 and 100", throwsWeight(() -> se.addCategory("Recitation", 100)));
        check("Failed add leaves the type list unchanged", se.getCategories().size() == 5);
        mgr.addScore(se, "project", "Final Project", 90.0, 100);
        check("Score of a custom type is an Other with that type name",
                se.getAssessments().get(0).getType().equals("Project"));
        check("Custom type score counts in the grade", calc.currentGradeText(se).equals("90.0%"));
        check("Custom type appears in search by type", mgr.search(se, "proj").size() == 1);
        check("Score for a type the course does not have is rejected",
                throwsScore(() -> mgr.addScore(se, "Recitation", "R1", 5.0, 10)));
        Map<String, Double> reweigh = new LinkedHashMap<>(se.getWeights());
        reweigh.put("Project", 25.0);
        check("Edit weights must total 100", throwsWeight(() -> se.setWeights(reweigh)));
        reweigh.put("Quiz", 11.0);
        se.setWeights(reweigh);
        check("Edit weights accepted when they total 100", se.getWeight("Project") == 25);

        store.save(mgr);
        CourseManager again = new CourseManager();
        check("Custom types survive save and load", store.load(again) == 0
                && again.findCourse("Software Eng").getCategories().contains("Project")
                && again.findCourse("Software Eng").getWeight("Project") == 25
                && again.findCourse("Software Eng").getAssessments().get(0).getType().equals("Project"));

        java.nio.file.Files.write(tmp.toPath(),
                "COURSE\tOld\t3\t20.0\t20.0\t40.0\t20.0\nSCORE\tQuiz\tQ\t5\t10\nCOURSE\tOld5\t3\t10.0\t10.0\t40.0\t20.0\t20.0\n"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CourseManager old = new CourseManager();
        check("Old save files (4 or 5 plain weights) still load",
                store.load(old) == 0 && old.getCourses().size() == 2
                        && old.getCourses().get(0).getCategories().size() == 4
                        && old.getCourses().get(1).getWeight("Other") == 20);

        // ---- Delete ---------------------------------------------------
        mgr.deleteCourse(biology);
        check("Deleted course is gone from list and lookup",
                mgr.getCourses().size() == 4 && mgr.findCourse("Biology") == null);

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private interface Action {
        void run() throws Exception;
    }

    private static boolean throwsScore(Action a) {
        try { a.run(); } catch (InvalidScoreException e) { return true; } catch (Exception e) { return false; }
        return false;
    }

    private static boolean throwsWeight(Action a) {
        try { a.run(); } catch (InvalidWeightException e) { return true; } catch (Exception e) { return false; }
        return false;
    }

    private static boolean throwsInput(Action a) {
        try { a.run(); } catch (InvalidInputException e) { return true; } catch (Exception e) { return false; }
        return false;
    }
}
