package gradetrack.ui;

import gradetrack.logic.CourseManager;
import gradetrack.logic.GradeCalculator;
import gradetrack.logic.TargetGradePlanner;
import gradetrack.model.Course;
import gradetrack.storage.StorageManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main window. It owns the shared objects (manager, calculators, storage)
 * and the four tabs: Overview, Courses, Target grade and GPA.
 * Whenever data changes, {@link #dataChanged()} saves it and redraws every tab.
 */
public class MainFrame extends JFrame {

    private final CourseManager manager = new CourseManager();
    private final GradeCalculator calculator = new GradeCalculator();
    private final TargetGradePlanner planner = new TargetGradePlanner(calculator);
    private final StorageManager storage;

    private final List<Refreshable> tabs = new ArrayList<>();
    private final JLabel statusLabel = Theme.label("", Theme.BASE, Theme.MUTED);
    private final JTabbedPane tabPane = new JTabbedPane();

    public MainFrame(StorageManager storage) {
        super("GradeTrack - Student Grade and GPA Tracking System");
        this.storage = storage;

        loadSavedData();
        buildInterface();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 620));
        setSize(960, 700);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Accessors used by the tabs
    // ------------------------------------------------------------------

    CourseManager getManager() {
        return manager;
    }

    GradeCalculator getCalculator() {
        return calculator;
    }

    TargetGradePlanner getPlanner() {
        return planner;
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    private void loadSavedData() {
        try {
            int skipped = storage.load(manager);
            if (skipped > 0) {
                JOptionPane.showMessageDialog(null,
                        skipped + " damaged line(s) in the saved file were skipped.",
                        "GradeTrack", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not read the saved data:\n" + e.getMessage(),
                    "GradeTrack", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("GradeTrack", Theme.TITLE, java.awt.Color.WHITE), BorderLayout.NORTH);
        titles.add(Theme.label("Track your grades, GPA and target in one place",
                Theme.BASE, new java.awt.Color(0xD6, 0xE4, 0xF0)), BorderLayout.SOUTH);
        header.add(titles, BorderLayout.WEST);
        header.add(Theme.label("Online · offline ready", Theme.BOLD, new java.awt.Color(0xB8, 0xEB, 0xD6)),
                BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Tabs
        OverviewPanel overview = new OverviewPanel(this);
        CoursesPanel courses = new CoursesPanel(this);
        TargetGradePanel target = new TargetGradePanel(this);
        GpaPanel gpa = new GpaPanel(this);
        tabs.add(overview);
        tabs.add(courses);
        tabs.add(target);
        tabs.add(gpa);

        tabPane.setFont(Theme.BOLD);
        tabPane.addTab("Overview", overview);
        tabPane.addTab("Courses", courses);
        tabPane.addTab("Target grade", target);
        tabPane.addTab("GPA", gpa);
        root.add(tabPane, BorderLayout.CENTER);

        // Status bar
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(Theme.CARD);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        status.add(statusLabel, BorderLayout.WEST);
        root.add(status, BorderLayout.SOUTH);

        setContentPane(root);
        refreshAll();
        setStatus("Data file: " + storage.getFile().getAbsolutePath());
    }

    // ------------------------------------------------------------------
    // Shared actions
    // ------------------------------------------------------------------

    /** Save to disk, then redraw every tab. */
    void dataChanged() {
        try {
            storage.save(manager);
            setStatus("Saved.");
        } catch (IOException e) {
            showError("Could not save your data: " + e.getMessage());
        }
        refreshAll();
    }

    void refreshAll() {
        for (Refreshable tab : tabs) {
            tab.refresh();
        }
    }

    /** Lets another tab jump to the Courses tab with a given course selected. */
    void openCourse(Course course) {
        tabPane.setSelectedIndex(1);
        for (Refreshable tab : tabs) {
            if (tab instanceof CoursesPanel) {
                ((CoursesPanel) tab).select(course);
            }
        }
    }

    void setStatus(String text) {
        statusLabel.setText(text);
    }

    void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "GradeTrack", JOptionPane.INFORMATION_MESSAGE);
    }
}
