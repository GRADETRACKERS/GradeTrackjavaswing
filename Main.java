package gradetrack;

import gradetrack.storage.StorageManager;
import gradetrack.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

/**
 * Entry point of GradeTrack.
 *
 * Run normally:            java -cp out gradetrack.Main
 * Use another data file:   java -cp out gradetrack.Main path/to/data.txt
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // the default look and feel is fine if this fails
        }

        final StorageManager storage = args.length > 0
                ? new StorageManager(new File(args[0]))
                : new StorageManager();

        SwingUtilities.invokeLater(() -> new MainFrame(storage).setVisible(true));
    }
}
