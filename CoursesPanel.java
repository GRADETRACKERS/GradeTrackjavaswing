package gradetrack.ui;

import gradetrack.exception.DuplicateTypeException;
import gradetrack.exception.InvalidInputException;
import gradetrack.logic.CourseManager;
import gradetrack.logic.CourseManager.SortOption;
import gradetrack.logic.GradeCalculator;
import gradetrack.logic.InputValidator;
import gradetrack.model.Assessment;
import gradetrack.model.Course;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Courses tab: choose a course, see its grade and category scores,
 * search/sort/add/update/delete its scores, add a new course or delete the course.
 */
class CoursesPanel extends JPanel implements Refreshable {

    private final MainFrame frame;

    private final DefaultComboBoxModel<Course> courseModel = new DefaultComboBoxModel<>();
    private final JComboBox<Course> courseBox = new JComboBox<>(courseModel);
    private boolean updating = false;   // stops combo-box events while we rebuild it

    private final JLabel creditsLabel = Theme.label("", Theme.BIG, Theme.PRIMARY);
    private final JLabel gradeLabel = Theme.label("", Theme.BIG, Theme.PRIMARY);
    private final JLabel gradedLabel = Theme.label("", Theme.BIG, Theme.PRIMARY);

    private final DefaultTableModel categoryModel = readOnlyModel("Category", "Weight", "Score");
    private final DefaultTableModel scoreModel = readOnlyModel("Name", "Type", "Score", "Out of", "Percent");
    private final JTable scoreTable = new JTable(scoreModel);
    private List<Assessment> shown = new java.util.ArrayList<>();   // scores currently in the table

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<SortOption> sortBox = new JComboBox<>(SortOption.values());

    private final JTextField nameField = new JTextField(12);
    private static final String OTHER_OPTION = "Other...";
    private final DefaultComboBoxModel<String> typeModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> typeBox = new JComboBox<>(typeModel);
    private boolean typeUpdating = false;   // stops type-box events while we rebuild it
    private String lastType = Course.DEFAULT_CATEGORIES[0];
    private final JTextField scoreField = new JTextField(6);
    private final JTextField totalField = new JTextField(6);

    CoursesPanel(MainFrame frame) {
        super(new BorderLayout(0, 10));
        this.frame = frame;
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(buildTop(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(buildSummary());
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildScoresSection());
        add(body, BorderLayout.CENTER);
        add(buildAddForm(), BorderLayout.SOUTH);

        courseBox.addActionListener(e -> {
            if (!updating) {
                refreshDetails();
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshScores(); }
            public void removeUpdate(DocumentEvent e) { refreshScores(); }
            public void changedUpdate(DocumentEvent e) { refreshScores(); }
        });
        sortBox.addActionListener(e -> refreshScores());
        typeBox.addActionListener(e -> {
            if (typeUpdating) {
                return;
            }
            if (OTHER_OPTION.equals(typeBox.getSelectedItem())) {
                addCustomType();
            } else if (typeBox.getSelectedItem() != null) {
                lastType = (String) typeBox.getSelectedItem();
            }
        });
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    private JPanel buildTop() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(Theme.label("Course:", Theme.BOLD, Theme.PRIMARY));
        courseBox.setFont(Theme.BASE);
        courseBox.setPreferredSize(new Dimension(260, 32));
        top.add(courseBox);

        JButton newCourse = Theme.button("+ New course", Theme.ACCENT);
        newCourse.addActionListener(e -> createCourse());
        top.add(newCourse);

        JButton weights = Theme.button("Edit weights", Theme.PRIMARY);
        weights.addActionListener(e -> editWeights());
        top.add(weights);

        JButton delete = Theme.button("Delete course", Theme.DANGER);
        delete.addActionListener(e -> deleteCourse());
        top.add(delete);
        return top;
    }

    private JPanel buildSummary() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 3, 8, 0));
        stats.setBackground(Theme.CARD);
        stats.setBorder(Theme.cardBorder());
        stats.add(stat("Credits", creditsLabel));
        stats.add(stat("Current grade", gradeLabel));
        stats.add(stat("Graded", gradedLabel));

        JTable cat = new JTable(categoryModel);
        styleTable(cat);
        JScrollPane catScroll = new JScrollPane(cat);
        catScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        row.add(stats);
        row.add(catScroll);
        row.setPreferredSize(new Dimension(100, 180));   // header + 5 category rows
        row.setMinimumSize(new Dimension(100, 180));     // stop BoxLayout from squeezing it
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        return row;
    }

    private JPanel stat(String caption, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        value.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 26));
        p.add(Theme.label(caption, Theme.BASE, Theme.MUTED), BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildScoresSection() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tools.setOpaque(false);
        tools.add(Theme.label("Scores", Theme.BOLD, Theme.PRIMARY));
        tools.add(Box.createHorizontalStrut(10));
        tools.add(new JLabel("Search (name or type):"));
        tools.add(searchField);
        tools.add(new JLabel("Sort by:"));
        tools.add(sortBox);
        section.add(tools, BorderLayout.NORTH);

        styleTable(scoreTable);
        JScrollPane scroll = new JScrollPane(scoreTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton update = new JButton("Update selected score");
        update.addActionListener(e -> updateSelectedScore());
        JButton remove = new JButton("Delete selected score");
        remove.addActionListener(e -> deleteSelectedScore());
        actions.add(update);
        actions.add(remove);
        section.add(actions, BorderLayout.SOUTH);
        return section;
    }

    private JPanel buildAddForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setBackground(Theme.CARD);
        form.setBorder(Theme.cardBorder());
        form.add(Theme.label("Add a score:", Theme.BOLD, Theme.PRIMARY));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Type"));
        form.add(typeBox);
        form.add(new JLabel("Score"));
        scoreField.setToolTipText("Leave blank if the work is not graded yet");
        form.add(scoreField);
        form.add(new JLabel("Out of"));
        form.add(totalField);
        JButton add = Theme.button("Add score", Theme.PRIMARY);
        add.addActionListener(e -> addScore());
        form.add(add);
        return form;
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable t) {
        t.setFont(Theme.BASE);
        t.setRowHeight(26);
        t.getTableHeader().setFont(Theme.BOLD);
        t.setFillsViewportHeight(true);
    }

    // ------------------------------------------------------------------
    // Refreshing
    // ------------------------------------------------------------------

    @Override
    public void refresh() {
        Course selected = (Course) courseBox.getSelectedItem();
        updating = true;
        courseModel.removeAllElements();
        for (Course c : frame.getManager().getCourses()) {
            courseModel.addElement(c);
        }
        if (selected != null && frame.getManager().getCourses().contains(selected)) {
            courseBox.setSelectedItem(selected);
        } else if (courseModel.getSize() > 0) {
            courseBox.setSelectedIndex(0);
        }
        updating = false;
        refreshDetails();
    }

    void select(Course course) {
        courseBox.setSelectedItem(course);
    }

    private void refreshDetails() {
        Course c = (Course) courseBox.getSelectedItem();
        GradeCalculator calc = frame.getCalculator();
        categoryModel.setRowCount(0);

        if (c == null) {
            creditsLabel.setText("–");
            gradeLabel.setText("–");
            gradedLabel.setText("–");
        } else {
            creditsLabel.setText(String.valueOf(c.getCredits()));
            gradeLabel.setText(calc.currentGradeText(c));
            gradedLabel.setText(Course.format(Math.round(calc.percentGraded(c) * 10) / 10.0) + "%");
            for (String cat : c.getCategories()) {
                double g = calc.categoryGrade(c, cat);
                categoryModel.addRow(new Object[]{cat, Course.format(c.getWeight(cat)) + "%",
                        g < 0 ? "–" : String.format("%.1f%%", g)});
            }
        }
        rebuildTypes(c);
        refreshScores();
    }

    /** Fills the Type box with this course's types plus the "Other..." option. */
    private void rebuildTypes(Course c) {
        typeUpdating = true;
        typeModel.removeAllElements();
        java.util.List<String> types = c == null
                ? java.util.Arrays.asList(Course.DEFAULT_CATEGORIES) : c.getCategories();
        for (String t : types) {
            typeModel.addElement(t);
        }
        typeModel.addElement(OTHER_OPTION);
        typeBox.setSelectedItem(types.contains(lastType) ? lastType : types.get(0));
        typeUpdating = false;
    }

    /**
     * The "Other..." option: the user types a new type name. If the course already has
     * that type, a message says so; otherwise the type is added with a weight.
     */
    private void addCustomType() {
        Course c = (Course) courseBox.getSelectedItem();
        if (c == null) {
            frame.showError("Create or select a course first.");
            rebuildTypes(null);
            return;
        }
        String entered = JOptionPane.showInputDialog(this, "Type a new score type (for example Project or Assignment):",
                "Other type", JOptionPane.PLAIN_MESSAGE);
        if (entered == null) {                 // cancelled
            rebuildTypes(c);
            return;
        }
        try {
            String existing = c.findCategory(entered);
            if (existing != null) {
                lastType = existing;           // jump to the type they already have
                throw new DuplicateTypeException(existing);
            }
            if (entered.trim().isEmpty()) {
                throw new InvalidInputException("Type name cannot be empty.");
            }
            String w = JOptionPane.showInputDialog(this,
                    "How much of the course grade is \"" + entered.trim() + "\" worth (%)?\n"
                            + "The other types will be reduced in proportion so the total stays 100%.",
                    "Weight of new type", JOptionPane.PLAIN_MESSAGE);
            if (w == null) {
                rebuildTypes(c);
                return;
            }
            double weight = InputValidator.parseWeight(w, entered.trim());
            lastType = c.addCategory(entered, weight);
            frame.dataChanged();
            frame.setStatus("Added type \"" + lastType + "\". You can fine-tune the weights with Edit weights.");
        } catch (InvalidInputException e) {
            frame.showError(e.getMessage());
            rebuildTypes(c);
        }
    }

    /** Lets the user change every weight of the selected course (must total 100%). */
    private void editWeights() {
        Course c = (Course) courseBox.getSelectedItem();
        if (c == null) {
            frame.showError("There is no course selected.");
            return;
        }
        Map<String, JTextField> fields = new LinkedHashMap<>();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        for (String cat : c.getCategories()) {
            JTextField f = new JTextField(Course.format(c.getWeight(cat)), 5);
            fields.put(cat, f);
            form.add(new JLabel(cat + " weight (%)"));
            form.add(f);
        }
        form.add(new JLabel(""));
        form.add(Theme.label("Weights must add up to 100%", Theme.BASE, Theme.MUTED));

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "Edit weights - " + c.getName(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                Map<String, Double> weights = new LinkedHashMap<>();
                for (Map.Entry<String, JTextField> en : fields.entrySet()) {
                    weights.put(en.getKey(), InputValidator.parseWeight(en.getValue().getText(), en.getKey()));
                }
                c.setWeights(weights);
                frame.dataChanged();
                return;
            } catch (InvalidInputException e) {
                frame.showError(e.getMessage());
            }
        }
    }

    private void refreshScores() {
        Course c = (Course) courseBox.getSelectedItem();
        scoreModel.setRowCount(0);
        shown = new java.util.ArrayList<>();
        if (c == null) {
            return;
        }
        shown = frame.getManager().view(c, searchField.getText(), (SortOption) sortBox.getSelectedItem());
        for (Assessment a : shown) {
            scoreModel.addRow(new Object[]{
                    a.getName(), a.getType(),
                    a.isGraded() ? Course.format(a.getScore()) : "not graded",
                    Course.format(a.getTotal()),
                    a.isGraded() ? String.format("%.1f%%", a.getPercent()) : "–"});
        }
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void addScore() {
        Course c = (Course) courseBox.getSelectedItem();
        try {
            if (c == null) {
                throw new InvalidInputException("Create or select a course first.");
            }
            String name = InputValidator.requireName(nameField.getText(), "Score name");
            Double score = InputValidator.parseScoreOrBlank(scoreField.getText());
            double total = InputValidator.parseTotal(totalField.getText());
            frame.getManager().addScore(c, (String) typeBox.getSelectedItem(), name, score, total);

            nameField.setText("");
            scoreField.setText("");
            totalField.setText("");
            nameField.requestFocusInWindow();
            frame.dataChanged();
        } catch (InvalidInputException e) {
            frame.showError(e.getMessage());
        }
    }

    private Assessment selectedScore() {
        int row = scoreTable.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            frame.showError("Select a score in the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void updateSelectedScore() {
        Assessment a = selectedScore();
        if (a == null) {
            return;
        }
        String current = a.isGraded() ? Course.format(a.getScore()) : "";
        Object input = JOptionPane.showInputDialog(this,
                "New score for \"" + a.getName() + "\" (out of " + Course.format(a.getTotal())
                        + ").\nLeave blank to mark it as not graded.",
                "Update score", JOptionPane.PLAIN_MESSAGE, null, null, current);
        if (input == null) {
            return;   // cancelled
        }
        try {
            a.setScore(InputValidator.parseScoreOrBlank((String) input));
            frame.dataChanged();
        } catch (InvalidInputException e) {
            frame.showError(e.getMessage());
        }
    }

    private void deleteSelectedScore() {
        Assessment a = selectedScore();
        Course c = (Course) courseBox.getSelectedItem();
        if (a == null || c == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + a.getName() + "\"?",
                "Delete score", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            frame.getManager().deleteScore(c, a);
            frame.dataChanged();
        }
    }

    private void deleteCourse() {
        Course c = (Course) courseBox.getSelectedItem();
        if (c == null) {
            frame.showError("There is no course to delete.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + c.getName() + "\" and all of its scores?\nThis cannot be undone.",
                "Delete course", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            frame.getManager().deleteCourse(c);
            frame.dataChanged();
        }
    }

    /** Shows the "New course" form again after an error so the user keeps what they typed. */
    private void createCourse() {
        JTextField name = new JTextField(18);
        JTextField credits = new JTextField("3", 4);
        Map<String, JTextField> weightFields = new LinkedHashMap<>();
        String[] defaults = {"20", "20", "40", "20"};

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        form.add(new JLabel("Course name"));
        form.add(name);
        form.add(new JLabel("Credit hours"));
        form.add(credits);
        for (int i = 0; i < Course.DEFAULT_CATEGORIES.length; i++) {
            JTextField f = new JTextField(defaults[i], 4);
            weightFields.put(Course.DEFAULT_CATEGORIES[i], f);
            form.add(new JLabel(Course.DEFAULT_CATEGORIES[i] + " weight (%)"));
            form.add(f);
        }
        form.add(new JLabel(""));
        form.add(Theme.label("Weights must add up to 100%", Theme.BASE, Theme.MUTED));

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "New course",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                String courseName = InputValidator.requireName(name.getText(), "Course name");
                int creditHours = InputValidator.parseCredits(credits.getText());
                Map<String, Double> weights = new LinkedHashMap<>();
                for (Map.Entry<String, JTextField> en : weightFields.entrySet()) {
                    weights.put(en.getKey(), InputValidator.parseWeight(en.getValue().getText(), en.getKey()));
                }
                Course course = new Course(courseName, creditHours, weights);
                frame.getManager().addCourse(course);
                courseBox.setSelectedItem(course);
                frame.dataChanged();
                courseBox.setSelectedItem(course);
                return;
            } catch (InvalidInputException e) {
                frame.showError(e.getMessage());   // loop shows the form again
            }
        }
    }
}
