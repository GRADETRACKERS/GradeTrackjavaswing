package gradetrack.ui;

import gradetrack.exception.InvalidScoreException;
import gradetrack.logic.InputValidator;
import gradetrack.model.Course;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Target grade tab: pick a course and a target, see the average still needed. */
class TargetGradePanel extends JPanel implements Refreshable {

    private final MainFrame frame;
    private final DefaultComboBoxModel<Course> courseModel = new DefaultComboBoxModel<>();
    private final JComboBox<Course> courseBox = new JComboBox<>(courseModel);
    private final JTextField targetField = new JTextField("85", 6);
    private final JLabel currentLabel = Theme.label("", Theme.BASE, Theme.MUTED);
    private final JTextArea result = new JTextArea(4, 40);

    TargetGradePanel(MainFrame frame) {
        super(new BorderLayout(0, 12));
        this.frame = frame;
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBackground(Theme.CARD);
        form.setBorder(Theme.cardBorder());
        form.add(Theme.label("Course:", Theme.BOLD, Theme.PRIMARY));
        courseBox.setFont(Theme.BASE);
        courseBox.setPreferredSize(new Dimension(240, 32));
        form.add(courseBox);
        form.add(Theme.label("Target final grade (%):", Theme.BOLD, Theme.PRIMARY));
        targetField.setFont(Theme.BASE);
        form.add(targetField);
        JButton go = Theme.button("Compute", Theme.PRIMARY);
        go.addActionListener(e -> compute());
        form.add(go);
        targetField.addActionListener(e -> compute());

        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        result.setForeground(Theme.PRIMARY);
        result.setBackground(Theme.CARD);
        result.setBorder(Theme.cardBorder());
        result.setText("Choose a course and a target, then press Compute.");

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(currentLabel, BorderLayout.NORTH);
        JPanel resultWrap = new JPanel(new BorderLayout());
        resultWrap.setOpaque(false);
        resultWrap.add(result, BorderLayout.NORTH);   // keeps the box only as tall as its text
        center.add(resultWrap, BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        courseBox.addActionListener(e -> showCurrent());
    }

    @Override
    public void refresh() {
        Course selected = (Course) courseBox.getSelectedItem();
        courseModel.removeAllElements();
        for (Course c : frame.getManager().getCourses()) {
            courseModel.addElement(c);
        }
        if (selected != null && frame.getManager().getCourses().contains(selected)) {
            courseBox.setSelectedItem(selected);
        }
        showCurrent();
    }

    private void showCurrent() {
        Course c = (Course) courseBox.getSelectedItem();
        if (c == null) {
            currentLabel.setText("No course available. Add one in the Courses tab.");
        } else {
            currentLabel.setText("Current grade: " + frame.getCalculator().currentGradeText(c)
                    + "  ·  " + Course.format(Math.round(frame.getCalculator().percentGraded(c) * 10) / 10.0)
                    + "% of the course graded");
        }
    }

    private void compute() {
        Course c = (Course) courseBox.getSelectedItem();
        try {
            if (c == null) {
                throw new InvalidScoreException("Add a course first in the Courses tab.");
            }
            double target = InputValidator.parseTarget(targetField.getText());
            result.setText(frame.getPlanner().advise(c, target));
        } catch (InvalidScoreException e) {
            frame.showError(e.getMessage());
        }
    }
}
