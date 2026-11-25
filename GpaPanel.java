package gradetrack.ui;

import gradetrack.logic.GPACalculator;
import gradetrack.logic.GradeCalculator;
import gradetrack.model.Course;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** GPA tab: GPA weighted by credit hours (1.00 is the highest grade) and a per-course list. */
class GpaPanel extends JPanel implements Refreshable {

    private final MainFrame frame;
    private final JLabel gpaLabel = Theme.label("–", Theme.BIG, Theme.PRIMARY);
    private final JLabel noteLabel = Theme.label("", Theme.BASE, Theme.MUTED);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Course", "Credits", "Percent", "Grade point"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    GpaPanel(MainFrame frame) {
        super(new BorderLayout(0, 12));
        this.frame = frame;
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.CARD);
        head.setBorder(Theme.cardBorder());
        head.add(Theme.label("GPA weighted by credits", Theme.BASE, Theme.MUTED), BorderLayout.NORTH);
        head.add(gpaLabel, BorderLayout.CENTER);
        head.add(noteLabel, BorderLayout.SOUTH);
        add(head, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(Theme.BASE);
        table.setRowHeight(28);
        table.getTableHeader().setFont(Theme.BOLD);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        GradeCalculator calc = frame.getCalculator();
        java.util.List<Course> courses = frame.getManager().getCourses();

        double gpa = GPACalculator.computeGPA(courses, calc);
        gpaLabel.setText(gpa < 0 ? "–" : String.format("%.2f", gpa));
        noteLabel.setText("1.00 is the highest grade. Courses with no graded work are not counted."
                + (gpa < 0 ? "" : "  Credits counted: " + GPACalculator.countedCredits(courses, calc)));

        model.setRowCount(0);
        for (Course c : courses) {
            double g = calc.currentGrade(c);
            model.addRow(new Object[]{
                    c.getName(), c.getCredits(),
                    g < 0 ? "–" : String.format("%.1f%%", g),
                    g < 0 ? "–" : String.format("%.2f", GPACalculator.toGradePoint(g))});
        }
    }
}
