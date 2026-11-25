package gradetrack.storage;

import gradetrack.exception.InvalidInputException;
import gradetrack.logic.CourseManager;
import gradetrack.model.Assessment;
import gradetrack.model.Course;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Saves and loads all courses and scores in a plain-text file so the data is
 * kept between sessions and the program works fully offline.
 *
 * File format (one record per line, fields separated by a TAB):
 *   COURSE  name  credits  Type:weight  Type:weight ...   (one pair per type of the course)
 *   SCORE   type  name     score  total          (score is empty if not graded)
 * A SCORE line belongs to the COURSE line above it.
 */
public class StorageManager {

    private final File file;

    /** Default location: ~/.gradetrack/gradetrack_data.txt */
    public StorageManager() {
        this(new File(System.getProperty("user.home"), ".gradetrack" + File.separator + "gradetrack_data.txt"));
    }

    public StorageManager(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /** Writes every course to disk. Throws IOException so the caller can report it. */
    public void save(CourseManager manager) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create folder " + parent);
        }
        BufferedWriter out = null;
        try {
            out = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            for (Course c : manager.getCourses()) {
                out.write("COURSE\t" + clean(c.getName()) + "\t" + c.getCredits());
                for (String cat : c.getCategories()) {
                    out.write("\t" + cat + ":" + c.getWeight(cat));
                }
                out.newLine();
                for (Assessment a : c.getAssessments()) {
                    out.write("SCORE\t" + a.getType() + "\t" + clean(a.getName()) + "\t"
                            + (a.isGraded() ? String.valueOf(a.getScore()) : "") + "\t" + a.getTotal());
                    out.newLine();
                }
            }
        } finally {
            // runs whether or not writing succeeded, so the file is always released
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Loads saved data into the manager. A missing file is not an error
     * (first run). Lines that cannot be read are skipped and counted.
     *
     * @return number of lines that were skipped because they were damaged
     */
    public int load(CourseManager manager) throws IOException {
        manager.clear();
        if (!file.exists()) {
            return 0;
        }
        int skipped = 0;
        Course current = null;
        try (BufferedReader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] f = line.split("\t", -1);
                try {
                    if (f[0].equals("COURSE") && f.length >= 7) {
                        Map<String, Double> w = new LinkedHashMap<>();
                        if (f[3].contains(":")) {
                            // current format: Type:weight pairs
                            for (int i = 3; i < f.length; i++) {
                                int cut = f[i].lastIndexOf(':');
                                w.put(f[i].substring(0, cut), Double.parseDouble(f[i].substring(cut + 1)));
                            }
                        } else {
                            // older files: plain numbers for Quiz, Test, Examination, Laboratory (+ optional Other)
                            for (int i = 0; i < Course.DEFAULT_CATEGORIES.length; i++) {
                                w.put(Course.DEFAULT_CATEGORIES[i], Double.parseDouble(f[3 + i]));
                            }
                            if (f.length > 7 && Double.parseDouble(f[7]) > 0) {
                                w.put("Other", Double.parseDouble(f[7]));
                            }
                        }
                        current = new Course(f[1], Integer.parseInt(f[2]), w);
                        manager.addCourse(current);
                    } else if (f[0].equals("SCORE") && f.length >= 5 && current != null) {
                        Double score = f[3].isEmpty() ? null : Double.valueOf(f[3]);
                        manager.addScore(current, f[1], f[2], score, Double.parseDouble(f[4]));
                    } else {
                        skipped++;
                    }
                } catch (NumberFormatException | InvalidInputException e) {
                    skipped++;   // damaged line: skip it, keep loading the rest
                }
            }
        }
        return skipped;
    }

    /** Tabs and line breaks would break the file format, so replace them with spaces. */
    private static String clean(String s) {
        return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
