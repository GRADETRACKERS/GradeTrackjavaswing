package gradetrack.ui;

import gradetrack.logic.GradeCalculator;
import gradetrack.model.Course;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Overview tab: one card per course with its current grade and how much is graded. */
class OverviewPanel extends JPanel implements Refreshable {

    private final MainFrame frame;
    private final JPanel list = new JPanel();

    OverviewPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(Theme.BACKGROUND);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.BACKGROUND);
        list.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        list.removeAll();
        GradeCalculator calc = frame.getCalculator();

        if (frame.getManager().getCourses().isEmpty()) {
            list.add(Theme.label("No courses yet. Open the Courses tab and press \"+ New course\" to begin.",
                    Theme.BASE, Theme.MUTED));
        }

        for (Course c : frame.getManager().getCourses()) {
            list.add(buildCard(c, calc));
            list.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        list.revalidate();
        list.repaint();
    }

    private JPanel buildCard(Course c, GradeCalculator calc) {
        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(Theme.label(c.getName(), Theme.BOLD, Theme.PRIMARY), BorderLayout.NORTH);
        left.add(Theme.label(c.getCredits() + (c.getCredits() == 1 ? " credit" : " credits")
                + " · " + c.getAssessments().size() + " score(s)", Theme.BASE, Theme.MUTED), BorderLayout.SOUTH);

        JLabel grade = Theme.label(calc.currentGradeText(c), new java.awt.Font("SansSerif", java.awt.Font.BOLD, 26),
                Theme.PRIMARY);

        double graded = calc.percentGraded(c);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(graded));
        bar.setForeground(Theme.ACCENT);
        bar.setStringPainted(false);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        bottom.add(bar, BorderLayout.CENTER);
        bottom.add(Theme.label(Course.format(Math.round(graded * 10) / 10.0) + "% graded",
                Theme.BASE, Theme.MUTED), BorderLayout.EAST);

        JButton open = new JButton("Open");
        open.addActionListener(e -> frame.openCourse(c));
        JPanel openWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        openWrap.setOpaque(false);
        openWrap.add(open);

        JPanel right = new JPanel(new BorderLayout(10, 0));
        right.setOpaque(false);
        right.add(grade, BorderLayout.CENTER);
        right.add(openWrap, BorderLayout.EAST);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }
}
