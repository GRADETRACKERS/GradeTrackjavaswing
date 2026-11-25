package gradetrack.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/** Shared colors, fonts and small helpers so every tab looks the same. */
final class Theme {

    static final Color PRIMARY = new Color(0x1F, 0x4E, 0x79);
    static final Color ACCENT = new Color(0x2E, 0x86, 0x6B);
    static final Color DANGER = new Color(0xB0, 0x3A, 0x2E);
    static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xF9);
    static final Color CARD = Color.WHITE;
    static final Color BORDER = new Color(0xD5, 0xDB, 0xE3);
    static final Color MUTED = new Color(0x5F, 0x6B, 0x7A);

    static final Font BASE = new Font("SansSerif", Font.PLAIN, 14);
    static final Font BOLD = new Font("SansSerif", Font.BOLD, 14);
    static final Font TITLE = new Font("SansSerif", Font.BOLD, 20);
    static final Font BIG = new Font("SansSerif", Font.BOLD, 36);

    private Theme() {
    }

    static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    static JButton button(String text, Color background) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setBackground(background);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
}
